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

import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.transactions.models.HarvestSeasonModel

object CommodityGroupsContract {
    data class Binding(
        var query: String? = null,
        var balances: List<BalanceCommodityItemModel>? = null,
        var commodityGroups: List<CommodityGroupModel> = emptyList(),
        var selectedCommodityGroup: CommodityGroupModel? = null,
        var harvestSeasons: List<HarvestSeasonModel> = emptyList(),
        var selectedHarvestSeason: HarvestSeasonModel? = null,
        var isRefreshing: Boolean = false,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data object OnCreate : Event()
        data object Refresh : Event()
        data class QueryChanged(val query: String) : Event()
        data class HarvestSeasonFilterCommodityGroupChanged(val commodityGroup: CommodityGroupModel?) : Event()
        data class HarvestSeasonFilterChanged(
            val commodityGroup: CommodityGroupModel?,
            val harvestSeason: HarvestSeasonModel?,
        ) : Event()
        data class HarvestSeasonChanged(val harvestSeason: HarvestSeasonModel?) : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data class ShowMessage(val message: String): Effect()
    }
}
