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
package com.whimo.data.commodity.repository

import com.whimo.data.base.common.BaseResult
import com.whimo.data.commodity.model.mappers.toBalanceDomain
import com.whimo.data.commodity.model.mappers.toDomain
import com.whimo.data.commodity.model.mappers.toEntity
import com.whimo.data.commodity.service.CommodityBalancesDao
import com.whimo.data.commodity.service.CommodityGroupsDao
import com.whimo.data.commodity.service.CommodityService
import com.whimo.data.harvestseasons.repository.HarvestSeasonCommodityLinker
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.network.handleResponse
import com.whimo.network.mapResult

interface CommodityRepository {
    suspend fun getCommodities(
        search: String?,
        page: Int,
        pageSize: Int,
    ): BaseResult<List<CommodityGroupModel>>

    suspend fun getCommoditiesFromDB(): List<CommodityGroupModel>

    suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?)

    suspend fun getBalancesFromDB(
        search: String?,
        commodityGroupId: String?,
        commodityId: String?,
        harvestSeasonId: String?,
    ): List<CommodityBalanceModel>

    suspend fun getBalances(
        search: String?,
        page: Int,
        pageSize: Int,
        commodityGroupId: String?,
        commodityId: String?,
        harvestSeasonId: String?,
    ): BaseResult<List<CommodityBalanceModel>>
}

class CommodityRepositoryImpl(
    private val service: CommodityService,
    private val dao: CommodityGroupsDao,
    private val balancesDao: CommodityBalancesDao,
    private val harvestSeasonCommodityLinker: HarvestSeasonCommodityLinker,
) : CommodityRepository {

    override suspend fun getCommodities(
        search: String?,
        page: Int,
        pageSize: Int,
    ): BaseResult<List<CommodityGroupModel>> {
        return handleResponse {
            service.getCommodities(
                search = search,
                page = page,
                pageSize = pageSize,
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun getCommoditiesFromDB(): List<CommodityGroupModel> {
        return dao.getAll().map { it.toDomain() }
    }

    override suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?) {
        dao.clearAll()
        if (items != null) {
            dao.insertAll(items.map { it.toEntity() })
        }
    }

    override suspend fun getBalancesFromDB(
        search: String?,
        commodityGroupId: String?,
        commodityId: String?,
        harvestSeasonId: String?,
    ): List<CommodityBalanceModel> {
        val cachedBalances = getCachedBalances()
        harvestSeasonCommodityLinker.linkHarvestSeasonsToBalances(cachedBalances)

        return cachedBalances
            .filterByBalanceRequest(
                search = search,
                commodityGroupId = commodityGroupId,
                commodityId = commodityId,
                harvestSeasonId = harvestSeasonId,
            )
    }

    override suspend fun getBalances(
        search: String?,
        page: Int,
        pageSize: Int,
        commodityGroupId: String?,
        commodityId: String?,
        harvestSeasonId: String?,
    ): BaseResult<List<CommodityBalanceModel>> {
        val result = handleResponse {
            service.getBalances(
                search = search,
                page = page,
                pageSize = pageSize,
                commodityGroupId = commodityGroupId,
                commodityId = commodityId,
                harvestSeasonId = harvestSeasonId,
            )
        }.mapResult { it?.toBalanceDomain() }

        if (result is BaseResult.Success) {
            val balances = result.data.orEmpty()
            updateBalancesDB(
                items = balances,
                replaceAll = search == null &&
                        commodityGroupId == null &&
                        commodityId == null &&
                        harvestSeasonId == null,
            )
            harvestSeasonCommodityLinker.linkHarvestSeasonsToBalances(balances)
            return result
        }

        val cachedBalances = getCachedBalances()
        if (cachedBalances.isNotEmpty()) {
            harvestSeasonCommodityLinker.linkHarvestSeasonsToBalances(cachedBalances)
            return BaseResult.Success(
                cachedBalances.filterByBalanceRequest(
                    search = search,
                    commodityGroupId = commodityGroupId,
                    commodityId = commodityId,
                    harvestSeasonId = harvestSeasonId,
                )
            )
        }

        return result
    }

    private suspend fun getCachedBalances(): List<CommodityBalanceModel> {
        return balancesDao.getAll().map { it.toDomain() }
    }

    private suspend fun updateBalancesDB(
        items: List<CommodityBalanceModel>,
        replaceAll: Boolean,
    ) {
        if (replaceAll) {
            balancesDao.clearAll()
        }
        if (items.isNotEmpty()) {
            balancesDao.insertAll(items.map { it.toEntity() })
        }
    }
}

private fun List<CommodityBalanceModel>.filterByBalanceRequest(
    search: String?,
    commodityGroupId: String?,
    commodityId: String?,
    harvestSeasonId: String?,
): List<CommodityBalanceModel> {
    val normalizedSearch = search?.trim()?.takeIf { it.isNotEmpty() }

    return filter { balance ->
        val validSearch = normalizedSearch == null ||
                balance.commodity.code.contains(normalizedSearch, ignoreCase = true) ||
                balance.commodity.name.contains(normalizedSearch, ignoreCase = true) ||
                balance.commodity.group?.name?.contains(normalizedSearch, ignoreCase = true) == true
        val validCommodityGroup = commodityGroupId == null || balance.commodity.group?.id == commodityGroupId
        val validCommodity = commodityId == null || balance.commodity.id == commodityId
        val validHarvestSeason = harvestSeasonId == null || balance.harvestSeason?.id == harvestSeasonId

        validSearch && validCommodityGroup && validCommodity && validHarvestSeason
    }
}
