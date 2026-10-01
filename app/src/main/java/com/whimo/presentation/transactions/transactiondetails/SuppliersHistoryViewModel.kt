/*
 * Copyright (c) 2025 EFI (https://efi.int/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whimo.presentation.transactions.transactiondetails

import android.content.Context
import android.net.Uri
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.geodata.GeoDataInteractor
import com.whimo.domain.transactions.TransactionsInteractor
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import java.io.InputStream

class SuppliersHistoryViewModel(
    private val interactor: TransactionsInteractor,
    private val geoDataInteractor: GeoDataInteractor,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel<SuppliersHistoryContract.Binding>() {

    private var isInitial: Boolean = true
    private var transactionModel: TransactionModel? = null
    private val filter = TransactionsFilter()

    private var state: TransactionsState = TransactionsState.Empty
    private var transactions: List<TransactionModel> = emptyList()

    private var downloadedGeoJson: String? = null
    private var downloadedCSV: ResponseBody? = null
    private var downloadedBundle: ResponseBody? = null
    private var fileName = ""

    override fun createBinding(): SuppliersHistoryContract.Binding {
        return SuppliersHistoryContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is SuppliersHistoryContract.Event.OnCreate -> onCreate(event.transactionModel, event.isInitial)
            is SuppliersHistoryContract.Event.Refresh -> onRefresh()
            is SuppliersHistoryContract.Event.NextPage -> onNextPage()
            is SuppliersHistoryContract.Event.OnSupplierClick -> onSupplierClick(event.transaction)
            is SuppliersHistoryContract.Event.DownloadGeoJson -> downloadGeoJson()
            is SuppliersHistoryContract.Event.DownloadCSV -> downloadCSV()
            is SuppliersHistoryContract.Event.DownloadBundle -> downloadBundle()
            is SuppliersHistoryContract.Event.SaveToFile -> saveToFile2(event.context, event.uri)
            is SuppliersHistoryContract.Event.RequestLocations -> requestLocations()
            is SuppliersHistoryContract.Event.OnBackToInitialClick -> onBackToInitialClick()
        }
    }

    override fun copyBinding(binding: SuppliersHistoryContract.Binding): SuppliersHistoryContract.Binding {
        return binding.copy()
    }

    init {
        launch {
            interactor.stateFlow.collect { state ->
                this@SuppliersHistoryViewModel.state = state

                if (state is TransactionsState.Success) {
                    transactions = state.transactions
                }

                if (state is TransactionsState.End) {
                    transactions = state.transactions
                }

                delay(100)
                updateBinding { b ->
                    b.state = state
                    b.transactions = transactions
                }
            }
        }
    }

    private fun onCreate(transactionModel: TransactionModel, isInitial: Boolean) {
        this.isInitial = isInitial
        this.transactionModel = transactionModel

        filter.status = transactionModel.status
        filter.user = transactionModel.seller
        filter.commodityGroup = transactionModel.commodity.group
        filter.harvestSeason = transactionModel.harvestSeason
        filter.dateEnd = transactionModel.updatedDate ?: transactionModel.createdDate

        updateView()
        onRefresh()
    }

    private fun updateView() {
        updateBinding { b ->
            b.title = if (isInitial) {
                resourceProvider.getString(R.string.my_suppliers_history)
            } else {
                resourceProvider.getString(R.string.traders_history, filter.user?.username ?: "")
            }
            b.state = state

            b.transactions = transactions

            b.showInitialActionButtons = isInitial
            b.showActionButtons = !isInitial

            b.dialogDescription = if (isInitial) {
                resourceProvider.getString(R.string.download_farms_locations_description)
            } else {
                resourceProvider.getString(
                    R.string.downloading_locations_data_for_trader,
                    filter.user?.username ?: "",
                    transactionModel?.volume ?: 0,
                    transactionModel?.commodity?.unit ?: ""
                )
            }
        }
    }

    private fun onRefresh() {
        if (filter.status == TransactionStatus.Accepted && filter.user != null && filter.commodityGroup != null) {
            launch {
                interactor.refresh(filter, false)
            }
        }
    }

    private fun onNextPage() {
        if (filter.status == TransactionStatus.Accepted && filter.user != null && filter.commodityGroup != null) {
            launch {
                interactor.loadNextPage(filter)
            }
        }
    }

    private fun onSupplierClick(transaction: TransactionModel) {
        setEffect(SuppliersHistoryContract.Effect.NavigateSupplier(transaction))
    }

    private fun requestLocations() {
        if (transactionModel != null) {
            setEffect(SuppliersHistoryContract.Effect.ToggleRequestLoader(true))
            launch {
                geoDataInteractor.requestTransactionGeoData(transactionId = transactionModel!!.id)
                    .onSuccess {
                        setEffect(
                            SuppliersHistoryContract.Effect.ToggleRequestLoader(false),
                            SuppliersHistoryContract.Effect.ShowMessage(resourceProvider.getString(R.string.locations_requested))
                        )
                    }
                    .onError {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(
                            SuppliersHistoryContract.Effect.ToggleRequestLoader(false),
                            SuppliersHistoryContract.Effect.ShowMessage(errorMessage)
                        )
                    }
            }
        }
    }

    private fun downloadGeoJson() {
        if (transactionModel != null) {
            setEffect(SuppliersHistoryContract.Effect.ToggleDataLoader(true))
            launch {
                geoDataInteractor.downloadTransactionGeoJson(transactionId = transactionModel!!.id)
                    .onSuccess {
                        if (it != null) {
                            downloadedGeoJson = it.data
                            fileName = "Whimo-${transactionModel!!.id}.geojson"

                            setEffect(
                                SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                                SuppliersHistoryContract.Effect.TextDataDownloaded(fileName)
                            )
                        }
                    }
                    .onError {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(
                            SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                            SuppliersHistoryContract.Effect.ShowMessage(errorMessage)
                        )
                    }
            }
        }
    }

    private fun downloadCSV() {
        if (transactionModel != null) {
            setEffect(SuppliersHistoryContract.Effect.ToggleDataLoader(true))
            launch {
                geoDataInteractor.downloadTransactionCSV(transactionId = transactionModel!!.id)
                    .onSuccess {
                        if (it != null) {
                            downloadedCSV = it
                            fileName = "Whimo-${transactionModel!!.id}.csv"

                            setEffect(
                                SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                                SuppliersHistoryContract.Effect.CSVDataDownloaded(fileName)
                            )
                        }
                    }
                    .onError {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(
                            SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                            SuppliersHistoryContract.Effect.ShowMessage(errorMessage)
                        )
                    }
            }
        }
    }

    private fun downloadBundle() {
        if (transactionModel != null) {
            setEffect(SuppliersHistoryContract.Effect.ToggleDataLoader(true))
            launch {
                geoDataInteractor.downloadTransactionBundle(transactionId = transactionModel!!.id)
                    .onSuccess {
                        if (it != null) {
                            downloadedBundle = it
                            fileName = "Whimo-${transactionModel!!.id}.zip"

                            setEffect(
                                SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                                SuppliersHistoryContract.Effect.ZipDataDownloaded(fileName)
                            )
                        }
                    }
                    .onError {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(
                            SuppliersHistoryContract.Effect.ToggleDataLoader(false),
                            SuppliersHistoryContract.Effect.ShowMessage(errorMessage)
                        )
                    }
            }
        }
    }

    private fun saveToFile2(context: Context, uri: Uri) {
        setEffect(SuppliersHistoryContract.Effect.ToggleDataLoader(true))

        launch {
            var inputStream: InputStream? = null

            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    if (downloadedGeoJson != null) {
                        outputStream.write(downloadedGeoJson!!.toByteArray())
                        downloadedGeoJson = null

                    } else {
                        inputStream = downloadedCSV?.byteStream() ?: downloadedBundle?.byteStream()

                        if (inputStream != null) {
                            val buffer = ByteArray(1024)
                            var read: Int

                            while (inputStream!!.read(buffer).also { read = it } != -1) {
                                outputStream.write(buffer, 0, read)
                            }
                            outputStream.flush()
                        }

                        downloadedCSV = null
                        downloadedBundle = null
                    }
                }
                setEffect(SuppliersHistoryContract.Effect.ShowMessage(resourceProvider.getString(R.string.file_saved, fileName)))
            } catch (e: Exception) {
                e.printStackTrace()
                setEffect(SuppliersHistoryContract.Effect.ShowMessage(resourceProvider.getString(R.string.file_save_failed)))
            } finally {
                inputStream?.close()
                setEffect(SuppliersHistoryContract.Effect.ToggleDataLoader(false))
            }
        }
    }

    private fun onBackToInitialClick() {
        setEffect(SuppliersHistoryContract.Effect.NavigateInitialSupplier)
    }
}
