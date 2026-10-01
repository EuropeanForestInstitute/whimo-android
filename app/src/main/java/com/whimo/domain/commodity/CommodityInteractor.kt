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
package com.whimo.domain.commodity

import com.whimo.data.base.common.BaseResult
import com.whimo.data.commodity.repository.CommodityRepository
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityFilter
import com.whimo.domain.commodity.models.CommodityGroupModel

interface CommodityInteractor {
    suspend fun getCommodities(filter: CommodityFilter): BaseResult<List<CommodityGroupModel>>
    suspend fun getCommoditiesFromDB(): List<CommodityGroupModel>
    suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?)
    suspend fun getBalancesFromDB(filter: CommodityBalanceFilter): List<CommodityBalanceModel>
    suspend fun getBalances(filter: CommodityBalanceFilter): BaseResult<List<CommodityBalanceModel>>
}

class CommodityInteractorImpl(
    private val repository: CommodityRepository,
) : CommodityInteractor {

    override suspend fun getCommodities(filter: CommodityFilter): BaseResult<List<CommodityGroupModel>> {
        return repository.getCommodities(
            search = filter.query,
            page = FIRST_PAGE,
            pageSize = DEFAULT_PAGE_SIZE,
        )
    }

    override suspend fun getCommoditiesFromDB(): List<CommodityGroupModel> {
        return repository.getCommoditiesFromDB()
    }

    override suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?) {
        repository.updateCommoditiesDB(items)
    }

    override suspend fun getBalancesFromDB(filter: CommodityBalanceFilter): List<CommodityBalanceModel> {
        return repository.getBalancesFromDB(
            search = filter.query,
            commodityGroupId = filter.groupId,
            commodityId = filter.commodityId,
            harvestSeasonId = filter.harvestSeason?.id,
        )
    }

    override suspend fun getBalances(filter: CommodityBalanceFilter): BaseResult<List<CommodityBalanceModel>> {
        return repository.getBalances(
            search = filter.query,
            page = FIRST_PAGE,
            pageSize = DEFAULT_PAGE_SIZE,
            commodityGroupId = filter.groupId,
            commodityId = filter.commodityId,
            harvestSeasonId = filter.harvestSeason?.id,
        )
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 1000
    }
}
