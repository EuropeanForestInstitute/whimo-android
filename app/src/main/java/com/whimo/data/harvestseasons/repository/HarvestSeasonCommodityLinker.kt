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
package com.whimo.data.harvestseasons.repository

import com.whimo.data.commodity.model.mappers.toDomain
import com.whimo.data.commodity.service.CommodityBalancesDao
import com.whimo.data.harvestseasons.model.mappers.getCommodityIds
import com.whimo.data.harvestseasons.model.mappers.toDomain
import com.whimo.data.harvestseasons.model.mappers.toEntity
import com.whimo.data.harvestseasons.service.HarvestSeasonsDao
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel

class HarvestSeasonCommodityLinker(
    private val harvestSeasonsDao: HarvestSeasonsDao,
    private val balancesDao: CommodityBalancesDao,
) {

    suspend fun linkStoredHarvestSeasonsToBalances() {
        linkHarvestSeasonsToBalances(balancesDao.getAll().map { it.toDomain() })
    }

    suspend fun linkHarvestSeasonsToBalances(balances: List<CommodityBalanceModel>) {
        val linksBySeason = balances
            .mapNotNull { balance -> balance.toHarvestSeasonCommodityLinkOrNull() }
            .groupBy { it.harvestSeason.id }

        if (linksBySeason.isEmpty()) return

        val existingSeasons = harvestSeasonsDao.getAll().associateBy { it.id }
        val updatedSeasons = linksBySeason.map { (seasonId, links) ->
            val existingSeason = existingSeasons[seasonId]
            val linkedCommodityIds = links.map { it.commodityId }
            val mergedCommodityIds = (existingSeason?.getCommodityIds().orEmpty() + linkedCommodityIds)
                .distinct()
            val harvestSeason = existingSeason?.toDomain() ?: links.first().harvestSeason

            harvestSeason.toEntity(mergedCommodityIds)
        }

        harvestSeasonsDao.insertAll(updatedSeasons)
    }
}

private data class HarvestSeasonCommodityLink(
    val harvestSeason: HarvestSeasonModel,
    val commodityId: String,
)

private fun CommodityBalanceModel.toHarvestSeasonCommodityLinkOrNull(): HarvestSeasonCommodityLink? {
    val harvestSeason = harvestSeason ?: return null
    val commodityId = commodity.id.takeIf { it.isNotBlank() } ?: return null

    return HarvestSeasonCommodityLink(
        harvestSeason = harvestSeason,
        commodityId = commodityId,
    )
}
