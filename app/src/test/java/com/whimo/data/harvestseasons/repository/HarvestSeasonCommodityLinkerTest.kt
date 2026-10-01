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

import com.whimo.data.commodity.model.entity.CommodityBalanceEntity
import com.whimo.data.commodity.model.mappers.toEntity
import com.whimo.data.commodity.service.CommodityBalancesDao
import com.whimo.data.harvestseasons.model.entity.HarvestSeasonEntity
import com.whimo.data.harvestseasons.model.mappers.getCommodityIds
import com.whimo.data.harvestseasons.service.HarvestSeasonsDao
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.utils.gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HarvestSeasonCommodityLinkerTest {

    @Test
    fun `linkHarvestSeasonsToBalances creates season links from fetched balances`() = runTest {
        val harvestSeasonsDao = TestHarvestSeasonsDao()
        val linker = HarvestSeasonCommodityLinker(
            harvestSeasonsDao = harvestSeasonsDao,
            balancesDao = TestCommodityBalancesDao(),
        )

        linker.linkHarvestSeasonsToBalances(
            listOf(
                balance(
                    commodityId = "cocoa-beans",
                    harvestSeason = season("active-season"),
                )
            )
        )

        val linkedSeason = harvestSeasonsDao.getAll().single()
        assertEquals("active-season", linkedSeason.id)
        assertEquals(listOf("cocoa-beans"), linkedSeason.getCommodityIds())
    }

    @Test
    fun `linkStoredHarvestSeasonsToBalances merges cached balance links into existing seasons`() = runTest {
        val harvestSeasonsDao = TestHarvestSeasonsDao(
            seasons = listOf(
                entity(
                    id = "active-season",
                    commodityIds = listOf("existing-commodity"),
                )
            )
        )
        val balancesDao = TestCommodityBalancesDao(
            balances = listOf(
                balance(
                    commodityId = "cocoa-beans",
                    harvestSeason = season("active-season"),
                )
            )
        )
        val linker = HarvestSeasonCommodityLinker(
            harvestSeasonsDao = harvestSeasonsDao,
            balancesDao = balancesDao,
        )

        linker.linkStoredHarvestSeasonsToBalances()

        val linkedSeason = harvestSeasonsDao.getAll().single()
        assertEquals(listOf("existing-commodity", "cocoa-beans"), linkedSeason.getCommodityIds())
    }

    private class TestHarvestSeasonsDao(
        seasons: List<HarvestSeasonEntity> = emptyList(),
    ) : HarvestSeasonsDao {
        private val seasons = seasons.toMutableList()

        override suspend fun getAll(): List<HarvestSeasonEntity> {
            return seasons.toList()
        }

        override suspend fun insertAll(items: List<HarvestSeasonEntity>) {
            items.forEach { item ->
                seasons.removeAll { it.id == item.id }
                seasons += item
            }
        }

        override suspend fun clearAll() {
            seasons.clear()
        }
    }

    private class TestCommodityBalancesDao(
        balances: List<CommodityBalanceModel> = emptyList(),
    ) : CommodityBalancesDao {
        private val balances = balances.map { it.toEntity() }.toMutableList()

        override suspend fun getAll(): List<CommodityBalanceEntity> {
            return balances.toList()
        }

        override suspend fun insertAll(items: List<CommodityBalanceEntity>) {
            items.forEach { item ->
                balances.removeAll { it.id == item.id }
                balances += item
            }
        }

        override suspend fun clearAll() {
            balances.clear()
        }
    }

    companion object {
        private fun balance(
            commodityId: String,
            harvestSeason: HarvestSeasonModel?,
        ): CommodityBalanceModel {
            return CommodityBalanceModel(
                id = "balance-$commodityId-${harvestSeason?.id}",
                volume = 100f,
                commodity = CommodityModel(
                    id = commodityId,
                    code = "1801",
                    name = "Cocoa beans",
                    unit = "kg",
                    hasRecipe = false,
                    group = CommodityGroupModel(
                        id = "cocoa",
                        name = "Cocoa",
                        commodities = null,
                    ),
                    balance = 100f,
                ),
                harvestSeason = harvestSeason,
                hasRecipe = false,
            )
        }

        private fun season(id: String): HarvestSeasonModel {
            return HarvestSeasonModel(
                id = id,
                name = "Active 2026/27",
                status = HarvestSeasonStatus.Active,
                startDate = LocalDate.of(2026, 9, 1),
                endDate = LocalDate.of(2027, 9, 1),
            )
        }

        private fun entity(
            id: String,
            commodityIds: List<String>,
        ): HarvestSeasonEntity {
            return HarvestSeasonEntity(
                id = id,
                name = "Active 2026/27",
                status = HarvestSeasonStatus.Active.statusName,
                country = null,
                startDate = "2026-09-01",
                endDate = "2027-09-01",
                commodityIdsJson = commodityIds.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) },
            )
        }
    }
}
