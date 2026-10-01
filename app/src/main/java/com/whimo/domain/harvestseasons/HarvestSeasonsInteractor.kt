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
package com.whimo.domain.harvestseasons

import com.whimo.data.base.common.BaseResult
import com.whimo.data.harvestseasons.repository.HarvestSeasonsRepository
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.network.mapResult
import java.time.LocalDate

interface HarvestSeasonsInteractor {
    suspend fun getHarvestSeasons(filter: HarvestSeasonFilter): BaseResult<List<HarvestSeasonModel>>
    suspend fun getHarvestSeasonsFromDB(filter: HarvestSeasonFilter): List<HarvestSeasonModel>
}

class HarvestSeasonsInteractorImpl(
    private val repository: HarvestSeasonsRepository,
) : HarvestSeasonsInteractor {

    override suspend fun getHarvestSeasons(filter: HarvestSeasonFilter): BaseResult<List<HarvestSeasonModel>> {
        val cachedSeasons = getHarvestSeasonsFromDB(filter)
        val result = repository.getHarvestSeasons(
            commodityIds = filter.commodityIds,
            status = filter.status?.statusName,
            page = FIRST_PAGE,
            pageSize = DEFAULT_PAGE_SIZE,
        ).mapResult { it?.second }

        return when {
            result is BaseResult.Success -> {
                BaseResult.Success(result.data.orEmpty().sortedHarvestSeasons())
            }
            cachedSeasons.isNotEmpty() -> {
                BaseResult.Success(cachedSeasons)
            }
            else -> result
        }
    }

    override suspend fun getHarvestSeasonsFromDB(filter: HarvestSeasonFilter): List<HarvestSeasonModel> {
        return repository.getHarvestSeasonsFromDB(
            commodityIds = filter.commodityIds,
            status = filter.status?.statusName,
            allowUnlinkedFallback = filter.allowUnlinkedFallback,
        ).sortedHarvestSeasons()
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 1000
    }
}

private fun List<HarvestSeasonModel>.sortedHarvestSeasons(): List<HarvestSeasonModel> {
    return distinctBy { it.id }
        .sortedWith(
            compareBy<HarvestSeasonModel> { it.status.sortPriority() }
                .thenByDescending { it.startDate ?: LocalDate.MIN }
                .thenBy { it.name }
        )
}

private fun HarvestSeasonStatus?.sortPriority(): Int {
    return when (this) {
        HarvestSeasonStatus.Active -> 0
        HarvestSeasonStatus.Past -> 1
        HarvestSeasonStatus.Archived -> 2
        null -> Int.MAX_VALUE
    }
}
