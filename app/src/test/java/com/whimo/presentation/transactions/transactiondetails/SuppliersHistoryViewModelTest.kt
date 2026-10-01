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

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.R
import com.whimo.data.base.common.BaseResult
import com.whimo.data.geodata.model.request.UpdateGeoDataRequest
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.geodata.GeoDataInteractor
import com.whimo.domain.geodata.models.DownloadGeoDataModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.domain.transactions.TransactionsInteractor
import com.whimo.domain.transactions.models.BaseModel
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.TransactionType
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState
import com.whimo.domain.transactions.models.UserModel
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class SuppliersHistoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionsInteractor: TestTransactionsInteractor
    private lateinit var viewModel: SuppliersHistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionsInteractor = TestTransactionsInteractor()
        val resourceProvider = TestResourceProvider()
        viewModel = SuppliersHistoryViewModel(
            interactor = transactionsInteractor,
            geoDataInteractor = TestGeoDataInteractor(),
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreate refreshes supplier history with updatedDate as dateEnd`() = runTest {
        val updatedDate = LocalDateTime.of(2025, 2, 3, 12, 30, 45)

        viewModel.handleEvents(
            SuppliersHistoryContract.Event.OnCreate(
                transactionModel = transaction(updatedDate = updatedDate),
            )
        )
        advanceUntilIdle()

        assertEquals(updatedDate, transactionsInteractor.refreshedDateEnd)
    }

    @Test
    fun `onCreate refreshes supplier history with createdDate when updatedDate is missing`() = runTest {
        val createdDate = LocalDateTime.of(2025, 2, 3, 10, 0, 0)

        viewModel.handleEvents(
            SuppliersHistoryContract.Event.OnCreate(
                transactionModel = transaction(createdDate = createdDate, updatedDate = null),
            )
        )
        advanceUntilIdle()

        assertEquals(createdDate, transactionsInteractor.refreshedDateEnd)
    }

    @Test
    fun `onCreate refreshes supplier history with transaction harvest season`() = runTest {
        viewModel.handleEvents(
            SuppliersHistoryContract.Event.OnCreate(
                transactionModel = transaction(harvestSeason = harvestSeason),
            )
        )
        advanceUntilIdle()

        assertEquals(harvestSeason.id, transactionsInteractor.refreshedHarvestSeasonId)
    }

    @Test
    fun `next page keeps supplier history harvest season`() = runTest {
        viewModel.handleEvents(
            SuppliersHistoryContract.Event.OnCreate(
                transactionModel = transaction(harvestSeason = harvestSeason),
            )
        )
        advanceUntilIdle()

        viewModel.handleEvents(SuppliersHistoryContract.Event.NextPage)
        advanceUntilIdle()

        assertEquals(harvestSeason.id, transactionsInteractor.nextPageHarvestSeasonId)
    }

    private fun transaction(
        createdDate: LocalDateTime = LocalDateTime.of(2025, 2, 3, 10, 0, 0),
        updatedDate: LocalDateTime? = LocalDateTime.of(2025, 2, 3, 12, 30, 45),
        harvestSeason: HarvestSeasonModel? = null,
    ) = TransactionModel(
        id = "transaction-id",
        createdDate = createdDate,
        updatedDate = updatedDate,
        expiresDate = null,
        type = TransactionType.Downstream,
        status = TransactionStatus.Accepted,
        action = TransactionAction.Buying,
        locationProvider = LocationProvider.GPS,
        location = null,
        commodity = CommodityModel(
            id = "commodity-id",
            code = "COCOA",
            name = "Cocoa",
            unit = "kg",
            hasRecipe = false,
            group = CommodityGroupModel(
                id = "group-id",
                name = "Cocoa group",
                commodities = null,
            ),
            balance = null,
        ),
        volume = 10f,
        traceability = TraceabilityStatus.Full,
        seller = UserModel(id = "seller-id", username = "seller"),
        buyer = UserModel(id = "buyer-id", username = "buyer"),
        isBuyingFromFarmer = false,
        isAutomatic = false,
        createdById = "creator-id",
        harvestSeason = harvestSeason,
    )

    private class TestTransactionsInteractor : TransactionsInteractor {
        override val stateFlow: SharedFlow<TransactionsState> = MutableSharedFlow()
        var refreshedDateEnd: LocalDateTime? = null
        var refreshedHarvestSeasonId: String? = null
        var nextPageHarvestSeasonId: String? = null

        override suspend fun refresh(filter: TransactionsFilter, useCache: Boolean) {
            refreshedDateEnd = filter.dateEnd
            refreshedHarvestSeasonId = filter.harvestSeason?.id
        }

        override suspend fun loadNextPage(filter: TransactionsFilter) {
            nextPageHarvestSeasonId = filter.harvestSeason?.id
        }
    }

    private class TestGeoDataInteractor : GeoDataInteractor {
        override suspend fun updateTransactionGeoData(
            transactionId: String,
            request: UpdateGeoDataRequest
        ): BaseResult<Boolean> {
            throw NotImplementedError()
        }

        override suspend fun requestTransactionGeoData(transactionId: String): BaseResult<BaseModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionGeoData(transactionId: String): BaseResult<DownloadGeoDataModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionGeoJson(transactionId: String): BaseResult<DownloadGeoDataModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionCSV(transactionId: String): BaseResult<ResponseBody> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionBundle(transactionId: String): BaseResult<ResponseBody> {
            throw NotImplementedError()
        }
    }

    private class TestResourceProvider : ResourceProvider {
        override fun resources(): Resources {
            throw NotImplementedError()
        }

        override fun getString(res: Int, vararg args: Any): String {
            return when (res) {
                R.string.my_suppliers_history -> "My suppliers history"
                R.string.traders_history -> "Trader history"
                R.string.download_farms_locations_description -> "Download farms"
                R.string.downloading_locations_data_for_trader -> "Download trader"
                else -> "string-$res"
            }
        }

        override fun getColor(color: Int): Int {
            throw NotImplementedError()
        }

        override fun getDrawable(icon: Int): Drawable? {
            throw NotImplementedError()
        }

        override fun cacheDir(): File {
            throw NotImplementedError()
        }
    }

    companion object {
        private val harvestSeason = HarvestSeasonModel(
            id = "harvest-season-id",
            name = "Harvest season 2025/26",
            startDate = LocalDate.of(2025, 9, 1),
            endDate = LocalDate.of(2026, 9, 1),
            status = HarvestSeasonStatus.Active,
        )
    }
}
