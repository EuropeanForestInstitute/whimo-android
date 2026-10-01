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

import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionModel

data class BalanceCommodityItemModel(
    val commodity: CommodityModel,
    val harvestSeason: HarvestSeasonModel? = null,
    val traceabilityStatus: TraceabilityStatus? = null,
)

data class BalanceDetailsArgs(
    val commodity: CommodityModel,
    val harvestSeason: HarvestSeasonModel? = null,
    val traceabilityStatus: TraceabilityStatus? = null,
)

internal fun HarvestSeasonModel.sameSeasonAs(other: HarvestSeasonModel?): Boolean {
    if (other == null) return false
    return id == other.id
}

internal fun List<TransactionModel>.latestBalanceTransaction(): TransactionModel? {
    return maxByOrNull { it.updatedDate ?: it.createdDate }
}

internal fun CommodityBalanceModel.toBalanceCommodityItemModel(): BalanceCommodityItemModel {
    return BalanceCommodityItemModel(
        commodity = commodity.copy(
            balance = volume,
            hasRecipe = hasRecipe,
        ),
        harvestSeason = harvestSeason,
        traceabilityStatus = traceabilityStatus,
    )
}
