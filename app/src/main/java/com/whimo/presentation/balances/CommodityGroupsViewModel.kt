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
package com.whimo.presentation.balances

import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityFilter
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractor
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.network.ErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.time.LocalDate

class CommodityGroupsViewModel(
    private val interactor: CommodityInteractor,
    private val harvestSeasonsInteractor: HarvestSeasonsInteractor,
    private val errorHandler: ErrorHandler,
) : BaseViewModel<CommodityGroupsContract.Binding>() {

    private val balanceFilter = CommodityBalanceFilter()
    private var searchJob: Job? = null
    private var commodityGroups: List<CommodityGroupModel> = emptyList()
    private var balances: List<CommodityBalanceModel> = emptyList()
    private var balanceItems: List<BalanceCommodityItemModel>? = null
    private var harvestSeasons: List<HarvestSeasonModel> = emptyList()
    private var harvestSeasonCommodityIds: List<String>? = null
    private var selectedCommodityGroup: CommodityGroupModel? = null
    private var selectedHarvestSeason: HarvestSeasonModel? = null

    override fun createBinding(): CommodityGroupsContract.Binding {
        return CommodityGroupsContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is CommodityGroupsContract.Event.OnCreate -> onCreate()
            is CommodityGroupsContract.Event.Refresh -> refresh()
            is CommodityGroupsContract.Event.QueryChanged -> queryChanged(event.query)
            is CommodityGroupsContract.Event.HarvestSeasonFilterCommodityGroupChanged -> {
                loadHarvestSeasons(event.commodityGroup)
            }
            is CommodityGroupsContract.Event.HarvestSeasonFilterChanged -> {
                harvestSeasonFilterChanged(event.commodityGroup, event.harvestSeason)
            }
            is CommodityGroupsContract.Event.HarvestSeasonChanged -> harvestSeasonChanged(event.harvestSeason)
        }
    }

    override fun copyBinding(binding: CommodityGroupsContract.Binding): CommodityGroupsContract.Binding {
        return binding.copy()
    }

    private fun onCreate() {
        loadCommodityGroups()
        loadHarvestSeasons(selectedCommodityGroup)
        getBalances()
    }

    private fun refresh() {
        loadCommodityGroups()
        loadHarvestSeasons(selectedCommodityGroup)
        getBalances(showRefresh = true, forceRefreshIndicator = true)
    }

    private fun loadCommodityGroups() {
        launch {
            val cachedGroups = interactor.getCommoditiesFromDB()
            if (cachedGroups.isNotEmpty()) {
                updateCommodityGroups(cachedGroups)
                updateView()
            }

            interactor.getCommodities(CommodityFilter())
                .onSuccess {
                    updateCommodityGroups(it)
                    interactor.updateCommoditiesDB(it)
                    updateView()
                }
                .onError {
                    it.printStackTrace()
                    val errorMessage = errorHandler.parseError(it)
                    setEffect(CommodityGroupsContract.Effect.ShowMessage(errorMessage))
                }
        }
    }

    private fun loadHarvestSeasons(commodityGroup: CommodityGroupModel?) {
        val commodityIds = commodityGroup.commodityIdsOrNull()

        if (commodityGroup != null && commodityIds.isNullOrEmpty()) {
            harvestSeasonCommodityIds = commodityIds
            harvestSeasons = emptyList()
            updateView()
            return
        }

        launch {
            harvestSeasonCommodityIds = commodityIds
            val seasonFilter = HarvestSeasonFilter(commodityIds = commodityIds)
            val cachedSeasons = harvestSeasonsInteractor.getHarvestSeasonsFromDB(seasonFilter)
                .ifEmpty { collectHarvestSeasons(balances, commodityGroup) }

            harvestSeasons = cachedSeasons
            updateView()

            harvestSeasonsInteractor.getHarvestSeasons(seasonFilter)
                .onSuccess { seasons ->
                    if (harvestSeasonCommodityIds != commodityIds) return@onSuccess

                    val loadedSeasons = seasons.orEmpty()
                    val selectedSeasonId = selectedHarvestSeason?.id
                    if (selectedCommodityGroup.sameGroupAs(commodityGroup)
                        && selectedSeasonId != null
                        && loadedSeasons.none { it.id == selectedSeasonId }
                    ) {
                        selectedHarvestSeason = null
                        balanceFilter.harvestSeason = null
                        getBalances(showRefresh = false)
                    }
                    harvestSeasons = loadedSeasons
                    updateView()
                }
                .onError {
                    if (harvestSeasonCommodityIds != commodityIds) return@onError

                    harvestSeasons = cachedSeasons
                    updateView()
                }
        }
    }

    private fun queryChanged(query: String) {
        balanceFilter.query = query.takeIf { it.isNotBlank() }
        updateBalanceItems()
        updateView()

        searchJob?.cancel()
        searchJob = launch {
            delay(600)
            getBalances(showRefresh = false)
        }
    }

    private fun harvestSeasonChanged(harvestSeason: HarvestSeasonModel?) {
        harvestSeasonFilterChanged(selectedCommodityGroup, harvestSeason)
    }

    private fun harvestSeasonFilterChanged(
        commodityGroup: CommodityGroupModel?,
        harvestSeason: HarvestSeasonModel?,
    ) {
        selectedCommodityGroup = commodityGroup
        selectedHarvestSeason = harvestSeason
        balanceFilter.groupId = commodityGroup?.id
        balanceFilter.harvestSeason = harvestSeason
        loadHarvestSeasons(commodityGroup)
        updateBalanceItems()
        updateView()
        getBalances()
    }

    private fun getBalances(
        showRefresh: Boolean = balanceItems.isNullOrEmpty(),
        forceRefreshIndicator: Boolean = false,
    ) {
        val requestFilter = balanceFilter.copy()

        launch {
            val cachedBalances = interactor.getBalancesFromDB(requestFilter)
            if (cachedBalances.isNotEmpty() && requestFilter.matchesBalanceFilter(balanceFilter)) {
                updateBalances(cachedBalances)
                updateView()
            }

            updateRefreshing(
                requestFilter = requestFilter,
                isRefreshing = forceRefreshIndicator || (showRefresh && cachedBalances.isEmpty()),
            )
            interactor.getBalances(requestFilter)
                .onSuccess {
                    if (!requestFilter.matchesBalanceFilter(balanceFilter)) return@onSuccess

                    updateBalances(it)
                    updateRefreshing(requestFilter, false)
                    updateView()
                }
                .onError {
                    if (!requestFilter.matchesBalanceFilter(balanceFilter)) return@onError

                    it.printStackTrace()
                    if (balanceItems == null) {
                        balanceItems = emptyList()
                    }
                    val errorMessage = errorHandler.parseError(it)
                    updateRefreshing(requestFilter, false)
                    setEffect(CommodityGroupsContract.Effect.ShowMessage(errorMessage))
                    updateView()
                }
        }
    }

    private fun updateRefreshing(
        requestFilter: CommodityBalanceFilter,
        isRefreshing: Boolean,
    ) {
        if (!requestFilter.matchesBalanceFilter(balanceFilter)) return

        updateBinding { b ->
            b.isRefreshing = isRefreshing
        }
    }

    private fun updateCommodityGroups(commodities: List<CommodityGroupModel>?) {
        commodityGroups = commodities.orEmpty()
        selectedCommodityGroup = selectedCommodityGroup.refreshFrom(commodityGroups)
        balanceFilter.groupId = selectedCommodityGroup?.id
    }

    private fun updateBalances(loadedBalances: List<CommodityBalanceModel>?) {
        balances = loadedBalances.orEmpty()
        if (harvestSeasons.isEmpty()
            && harvestSeasonCommodityIds == selectedCommodityGroup.commodityIdsOrNull()
        ) {
            harvestSeasons = collectHarvestSeasons(balances, selectedCommodityGroup)
        }
        updateBalanceItems()
    }

    private fun updateBalanceItems() {
        balanceItems = balances
            .filterByCommodityGroup(selectedCommodityGroup)
            .filterByHarvestSeason(selectedHarvestSeason)
            .filterByQuery(balanceFilter.query)
            .map { it.toBalanceCommodityItemModel() }
    }

    private fun updateView() {
        updateBinding { b ->
            b.query = balanceFilter.query
            b.balances = balanceItems
            b.commodityGroups = commodityGroups
            b.selectedCommodityGroup = selectedCommodityGroup
            b.harvestSeasons = harvestSeasons
            b.selectedHarvestSeason = selectedHarvestSeason
        }
    }

    private fun collectHarvestSeasons(
        balances: List<CommodityBalanceModel>,
        commodityGroup: CommodityGroupModel?,
    ): List<HarvestSeasonModel> {
        return balances
            .filterByCommodityGroup(commodityGroup)
            .mapNotNull { it.harvestSeason }
            .distinctBy { it.id }
            .sortedWith(
                compareBy<HarvestSeasonModel> { it.status?.ordinal ?: Int.MAX_VALUE }
                    .thenByDescending { it.startDate ?: LocalDate.MIN }
                    .thenBy { it.name }
            )
    }
}

private fun CommodityGroupModel?.commodityIdsOrNull(): List<String>? {
    return this?.commodities
        ?.map { it.id }
        ?.distinct()
        ?.sorted()
}

private fun CommodityGroupModel?.sameGroupAs(other: CommodityGroupModel?): Boolean {
    if (this == null && other == null) return true
    return this?.id == other?.id
}

private fun CommodityGroupModel?.refreshFrom(groups: List<CommodityGroupModel>): CommodityGroupModel? {
    return this?.let { selected -> groups.find { it.id == selected.id } ?: selected }
}

private fun CommodityBalanceFilter.matchesBalanceFilter(other: CommodityBalanceFilter): Boolean {
    return query == other.query &&
            groupId == other.groupId &&
            commodityId == other.commodityId &&
            harvestSeason.matchesSeason(other.harvestSeason)
}

private fun HarvestSeasonModel?.matchesSeason(other: HarvestSeasonModel?): Boolean {
    if (this == null || other == null) return this == other
    return sameSeasonAs(other)
}

private fun List<CommodityBalanceModel>.filterByCommodityGroup(
    commodityGroup: CommodityGroupModel?,
): List<CommodityBalanceModel> {
    if (commodityGroup == null) return this
    return filter { it.commodity.group?.id == commodityGroup.id }
}

private fun List<CommodityBalanceModel>.filterByHarvestSeason(
    harvestSeason: HarvestSeasonModel?,
): List<CommodityBalanceModel> {
    if (harvestSeason == null) return this
    return filter { it.harvestSeason?.sameSeasonAs(harvestSeason) == true }
}

private fun List<CommodityBalanceModel>.filterByQuery(query: String?): List<CommodityBalanceModel> {
    val normalizedQuery = query?.trim()?.takeIf { it.isNotEmpty() } ?: return this
    return filter {
        it.commodity.code.contains(normalizedQuery, ignoreCase = true) ||
                it.commodity.name.contains(normalizedQuery, ignoreCase = true) ||
                it.commodity.group?.name?.contains(normalizedQuery, ignoreCase = true) == true
    }
}
