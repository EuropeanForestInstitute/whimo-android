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
package com.whimo.presentation.createtransaction.commodity

import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractor
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.domain.harvestseasons.models.getYearRangeText
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.extensions.toQuantityText
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider

class CommodityVolumeViewModel(
    private val commodityInteractor: CommodityInteractor,
    private val harvestSeasonsInteractor: HarvestSeasonsInteractor,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel<CommodityVolumeContract.Binding>() {

    private var transaction: CreateTransactionModel? = null
    private val filter = CommodityBalanceFilter()
    private var balance: Float? = null
    private var volume: Float? = null
    private var selectedHarvestSeason: HarvestSeasonModel? = null
    private var harvestSeasons: List<HarvestSeasonModel> = emptyList()
    private var loadedCommodityId: String? = null
    private var isHarvestSeasonsLoading = false
    private var isBalanceLoading = false

    override fun createBinding(): CommodityVolumeContract.Binding {
        return CommodityVolumeContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is CommodityVolumeContract.Event.OnCreate -> onCreate(event.transaction)
            is CommodityVolumeContract.Event.OnVolumeChanged -> onVolumeChanged(event.volume)
            is CommodityVolumeContract.Event.OnHarvestSeasonSelected -> onHarvestSeasonSelected(event.harvestSeason)
            is CommodityVolumeContract.Event.OnConfirm -> onConfirm()
        }
    }

    override fun copyBinding(binding: CommodityVolumeContract.Binding): CommodityVolumeContract.Binding {
        return binding.copy()
    }

    private fun onCreate(transaction: CreateTransactionModel) {
        this.transaction = transaction
        volume = transaction.volume

        filter.groupId = transaction.commodity?.group?.id
        filter.commodityId = transaction.commodity?.id

        val commodityId = transaction.commodity?.id
        if (commodityId == null) {
            selectedHarvestSeason = null
            harvestSeasons = emptyList()
            loadedCommodityId = null
            filter.harvestSeason = null
            balance = null
            isHarvestSeasonsLoading = false
            isBalanceLoading = false
            updateView()
            return
        }

        val shouldLoadSeasons = loadedCommodityId != commodityId || harvestSeasons.isEmpty()
        if (loadedCommodityId != commodityId) {
            harvestSeasons = emptyList()
            isBalanceLoading = false
        }

        selectedHarvestSeason = if (loadedCommodityId == commodityId) {
            selectDefaultSeason(transaction.harvestSeason, harvestSeasons)
        } else {
            transaction.harvestSeason
        }
        filter.harvestSeason = selectedHarvestSeason
        balance = transaction.commodity.balance

        if (shouldLoadSeasons) {
            isHarvestSeasonsLoading = true
        }

        updateView()

        if (shouldLoadSeasons) {
            loadedCommodityId = commodityId
            loadHarvestSeasons(commodityId)
        } else {
            loadSelectedSeasonBalance()
        }
    }

    private fun updateView() {
        updateBinding { b ->
            b.query = filter.query
            b.volume = volume?.toString()
            b.selectedHarvestSeason = selectedHarvestSeason
            b.harvestSeasons = harvestSeasons
            b.harvestSeasonSelectorVisible = isHarvestSeasonSelectorVisible()
            b.inputEnabled = transaction?.commodity != null

            val balanceText = getBalanceText()
            val insufficientBalance = isInsufficientBalance()

            if (transaction?.action == TransactionAction.Selling && balanceText != null) {
                if (insufficientBalance || balance == null || balance == 0f) {
                    b.supportingText = ""
                    b.warningText = resourceProvider.getString(R.string.your_balance, balanceText)
                } else {
                    b.supportingText = resourceProvider.getString(R.string.your_balance, balanceText)
                    b.warningText = ""
                }
            } else {
                b.supportingText = ""
                b.warningText = ""
            }

            b.insufficientBalance = insufficientBalance
            b.insufficientBalanceMessage = when {
                !insufficientBalance -> ""
                isAutomaticTransactionAllowed() -> resourceProvider.getString(
                    R.string.automatic_transaction_balance_message,
                    getMissingVolumeText()
                )
                else -> resourceProvider.getString(
                    R.string.insufficient_season_balance_message,
                    selectedHarvestSeason?.getYearRangeText() ?: ""
                )
            }

            b.buttonEnabled = isConfirmEnabled()
        }
    }

    private fun loadHarvestSeasons(commodityId: String) {
        launch {
            val seasonFilter = HarvestSeasonFilter(
                commodityIds = listOf(commodityId),
                status = HarvestSeasonStatus.Active.takeIf { transaction?.isProducerTransaction == true },
                allowUnlinkedFallback = false,
            )
            val cachedSeasons = harvestSeasonsInteractor.getHarvestSeasonsFromDB(seasonFilter)

            if (cachedSeasons.isNotEmpty()) {
                applyLoadedHarvestSeasons(
                    commodityId = commodityId,
                    seasons = cachedSeasons,
                )
            }

            harvestSeasonsInteractor.getHarvestSeasons(seasonFilter)
                .onSuccess { seasons ->
                    if (!isCurrentCommodityRequest(commodityId)) return@onSuccess

                    applyLoadedHarvestSeasons(
                        commodityId = commodityId,
                        seasons = seasons.orEmpty(),
                    )
                }
                .onError {
                    if (!isCurrentCommodityRequest(commodityId)) return@onError

                    isHarvestSeasonsLoading = false
                    selectedHarvestSeason = selectDefaultSeason(selectedHarvestSeason, harvestSeasons)
                    filter.harvestSeason = selectedHarvestSeason
                    val errorMessage = errorHandler.parseError(it)
                    setEffect(CommodityVolumeContract.Effect.ShowMessage(errorMessage))
                    updateView()
                }
        }
    }

    private fun applyLoadedHarvestSeasons(
        commodityId: String,
        seasons: List<HarvestSeasonModel>,
    ) {
        if (!isCurrentCommodityRequest(commodityId)) return

        isHarvestSeasonsLoading = false
        harvestSeasons = seasons
        selectedHarvestSeason = selectDefaultSeason(selectedHarvestSeason, harvestSeasons)
        filter.harvestSeason = selectedHarvestSeason
        updateView()
        loadSelectedSeasonBalance()
    }

    private fun loadSelectedSeasonBalance() {
        val commodity = transaction?.commodity ?: return
        val requestCommodityId = commodity.id
        val requestSeason = selectedHarvestSeason

        if (transaction?.action != TransactionAction.Selling || requestSeason == null) {
            balance = commodity.balance ?: 0f
            isBalanceLoading = false
            updateView()
            return
        }

        filter.harvestSeason = requestSeason
        val requestFilter = filter.copy(harvestSeason = requestSeason)

        isBalanceLoading = true
        updateView()

        launch {
            val cachedBalances = commodityInteractor.getBalancesFromDB(requestFilter)
            val cachedBalance = cachedBalances
                .firstOrNull { it.harvestSeason?.id == requestSeason.id }
                ?.volume

            if (cachedBalance != null && isCurrentBalanceRequest(requestCommodityId, requestSeason.id)) {
                balance = cachedBalance
                isBalanceLoading = false
                updateTransactionCommodityBalance()
                updateView()
            }

            commodityInteractor.getBalances(requestFilter)
                .onSuccess { balances ->
                    if (!isCurrentBalanceRequest(requestCommodityId, requestSeason.id)) return@onSuccess

                    isBalanceLoading = false
                    balance = balances
                        .orEmpty()
                        .firstOrNull { it.harvestSeason?.id == requestSeason.id }
                        ?.volume ?: 0f
                    updateTransactionCommodityBalance()
                    updateView()
                }
                .onError {
                    if (!isCurrentBalanceRequest(requestCommodityId, requestSeason.id)) return@onError

                    isBalanceLoading = false
                    val errorMessage = errorHandler.parseError(it)
                    setEffect(CommodityVolumeContract.Effect.ShowMessage(errorMessage))
                    updateView()
                }
        }
    }

    private fun selectDefaultSeason(
        currentSeason: HarvestSeasonModel?,
        seasons: List<HarvestSeasonModel>,
    ): HarvestSeasonModel? {
        val currentSeasonInList = currentSeason?.let { current ->
            seasons.find { it.id == current.id }
        }

        if (currentSeasonInList != null) {
            return currentSeasonInList
        }

        if (transaction?.isProducerTransaction == true) {
            return seasons.firstOrNull { it.status == HarvestSeasonStatus.Active }
        }

        return seasons.firstOrNull { it.status == HarvestSeasonStatus.Active } ?: seasons.firstOrNull()
    }

    private fun isCurrentCommodityRequest(commodityId: String): Boolean {
        return loadedCommodityId == commodityId && transaction?.commodity?.id == commodityId
    }

    private fun isCurrentBalanceRequest(commodityId: String, harvestSeasonId: String): Boolean {
        return transaction?.commodity?.id == commodityId &&
                selectedHarvestSeason?.id == harvestSeasonId
    }

    private fun onHarvestSeasonSelected(harvestSeason: HarvestSeasonModel) {
        selectedHarvestSeason = harvestSeason
        balance = null
        updateView()
        loadSelectedSeasonBalance()
    }

    private fun updateTransactionCommodityBalance() {
        val commodity = transaction?.commodity ?: return
        transaction = transaction?.copy(
            commodity = commodity.copy(balance = balance)
        )
    }

    private fun getBalanceText(): String? {
        val commodity = transaction?.commodity ?: return null
        return commodity.copy(balance = balance).getBalanceText()
    }

    private fun isInsufficientBalance(): Boolean {
        return transaction?.action == TransactionAction.Selling &&
                balance != null &&
                volume != null &&
                volume!! > 0 &&
                balance!! < volume!!
    }

    private fun isAutomaticTransactionAllowed(): Boolean {
        return selectedHarvestSeason?.status == HarvestSeasonStatus.Active
    }

    private fun getMissingVolumeText(): String {
        val missingVolume = ((volume ?: 0f) - (balance ?: 0f)).coerceAtLeast(0f)
        val unit = transaction?.commodity?.unit.orEmpty()

        return "${missingVolume.toQuantityText()} $unit".trim()
    }

    private fun confirmedCommodity(): CommodityModel? {
        val commodity = transaction?.commodity ?: return null
        return if (transaction?.action == TransactionAction.Selling && balance != null) {
            commodity.copy(balance = balance)
        } else {
            commodity
        }
    }

    private fun isConfirmEnabled(): Boolean {
        val hasRequiredSeason = selectedHarvestSeason != null || !isHarvestSeasonRequired()
        val insufficientBalanceBlocksConfirm = isInsufficientBalance() && !isAutomaticTransactionAllowed()

        return transaction?.commodity != null &&
                !isHarvestSeasonsLoading &&
                !isBalanceLoading &&
                volume != null &&
                volume!! > 0 &&
                hasRequiredSeason &&
                !insufficientBalanceBlocksConfirm
    }

    private fun isHarvestSeasonRequired(): Boolean {
        return transaction?.isProducerTransaction == false
    }

    private fun isHarvestSeasonSelectorVisible(): Boolean {
        return transaction?.isProducerTransaction == false
    }

    private fun onVolumeChanged(volume: String?) {
        this.volume = volume?.toFloatOrNull()

        updateView()
    }

    private fun onConfirm() {
        if (isConfirmEnabled()) {
            setEffect(
                CommodityVolumeContract.Effect.VolumeConfirmed(
                    volume = volume,
                    harvestSeason = selectedHarvestSeason,
                    commodity = confirmedCommodity(),
                )
            )
        }
    }
}
