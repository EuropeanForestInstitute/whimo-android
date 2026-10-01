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
package com.whimo.presentation.transactions

import androidx.annotation.StringRes
import com.whimo.R
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.createtransaction.models.PendingTransactionModel
import com.whimo.domain.createtransaction.models.PendingTransactionsSyncProgress
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.TransactionsState
import java.time.LocalDateTime

enum class TransactionsTab(@StringRes val titleResId: Int, val icon: Int) {
    All(R.string.all, R.drawable.ic_home),
    Bought(R.string.bought, R.drawable.ic_status_buy),
    Sold(R.string.sold, R.drawable.ic_status_sell)
}

object TransactionsContract {
    data class Binding(
        var currentTab: TransactionsTab = TransactionsTab.entries.first(),
        var tabStates: MutableMap<TransactionsTab, TransactionsState> = TransactionsTab.entries
            .associateWith { TransactionsState.Empty }
            .toMutableMap(),
        var pendingTransactions: MutableMap<TransactionsTab, List<PendingTransactionModel>> = TransactionsTab.entries
            .associateWith { emptyList<PendingTransactionModel>() }
            .toMutableMap(),

        var query: String? = null,

        var commodityGroups: List<CommodityGroupModel> = emptyList(),
        var selectedCommodityGroup: CommodityGroupModel? = null,
        var harvestSeasons: List<HarvestSeasonModel> = emptyList(),
        var selectedHarvestSeason: HarvestSeasonModel? = null,

        var dateStart: LocalDateTime? = null,
        var dateEnd: LocalDateTime? = null,
        var status: TransactionStatus? = null,

        var pendingSyncProgress: PendingTransactionsSyncProgress? = null,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data object OnCreate : Event()

        data class TabChanged(val tab: TransactionsTab) : Event()
        data class Refresh(val tab: TransactionsTab) : Event()
        data class NextPage(val tab: TransactionsTab) : Event()

        data class QueryChanged(val query: String) : Event()
        data class HarvestSeasonFilterCommodityGroupChanged(val commodityGroup: CommodityGroupModel?) : Event()
        data class HarvestSeasonFilterChanged(
            val commodityGroup: CommodityGroupModel?,
            val harvestSeason: HarvestSeasonModel?,
        ) : Event()
        data class HarvestSeasonChanged(val harvestSeason: HarvestSeasonModel?) : Event()
        data class DatesChanged(val dateStart: LocalDateTime? = null, val dateEnd: LocalDateTime? = null) : Event()
        data class StatusChanged(val status: TransactionStatus) : Event()

        data class TransactionClicked(val transaction: TransactionModel) : Event()
        data class AddGeoDataClicked(val transaction: TransactionModel) : Event()
        data object NotificationsClicked : Event()
        data class NetworkAvailabilityChanged(val isAvailable: Boolean) : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data class NavigateTransactionDetails(val transaction: TransactionModel): Effect()
        data class NavigateAddGeolocation(val transaction: TransactionModel): Effect()
        data object NavigateNotifications: Effect()
    }
}
