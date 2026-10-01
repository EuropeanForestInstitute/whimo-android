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
import com.whimo.domain.common.PaginationModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HarvestSeasonsInteractorImplTest {

    private lateinit var repository: HarvestSeasonsRepository
    private lateinit var interactor: HarvestSeasonsInteractorImpl

    @Before
    fun setUp() {
        repository = mockk()
        interactor = HarvestSeasonsInteractorImpl(repository)
    }

    @Test
    fun `getHarvestSeasons returns sorted remote seasons`() = runTest {
        val pastSeason = season("past", 2025, 2026, HarvestSeasonStatus.Past)
        val activeSeason = season("active", 2026, 2027, HarvestSeasonStatus.Active)
        val filter = HarvestSeasonFilter(status = HarvestSeasonStatus.Active)

        coEvery {
            repository.getHarvestSeasonsFromDB(
                commodityIds = null,
                status = HarvestSeasonStatus.Active.statusName,
                allowUnlinkedFallback = true,
            )
        } returns emptyList()
        coEvery {
            repository.getHarvestSeasons(
                commodityIds = null,
                status = HarvestSeasonStatus.Active.statusName,
                page = 1,
                pageSize = 1000,
            )
        } returns BaseResult.Success(pagination() to listOf(pastSeason, activeSeason))

        val result = interactor.getHarvestSeasons(filter)

        assertTrue(result is BaseResult.Success)
        assertEquals(listOf(activeSeason, pastSeason), (result as BaseResult.Success).data)
    }

    @Test
    fun `getHarvestSeasons returns cached seasons when remote fails`() = runTest {
        val pastSeason = season("past", 2025, 2026, HarvestSeasonStatus.Past)
        val activeSeason = season("active", 2026, 2027, HarvestSeasonStatus.Active)
        val filter = HarvestSeasonFilter(commodityIds = listOf("commodity-id"))
        val error = RuntimeException("No network")

        coEvery {
            repository.getHarvestSeasonsFromDB(
                commodityIds = listOf("commodity-id"),
                status = null,
                allowUnlinkedFallback = true,
            )
        } returns listOf(pastSeason, activeSeason)
        coEvery {
            repository.getHarvestSeasons(
                commodityIds = listOf("commodity-id"),
                status = null,
                page = 1,
                pageSize = 1000,
            )
        } returns BaseResult.Error(error)

        val result = interactor.getHarvestSeasons(filter)

        assertTrue(result is BaseResult.Success)
        assertEquals(listOf(activeSeason, pastSeason), (result as BaseResult.Success).data)
    }

    private fun pagination(): PaginationModel {
        return PaginationModel(
            count = 2,
            page = 1,
            pageSize = 1000,
            nextPage = null,
            previousPage = null,
            totalPages = 1,
        )
    }

    private fun season(
        id: String,
        startYear: Int,
        endYear: Int,
        status: HarvestSeasonStatus,
    ): HarvestSeasonModel {
        return HarvestSeasonModel(
            id = id,
            name = "Harvest season $startYear/$endYear",
            status = status,
            startDate = LocalDate.of(startYear, 9, 1),
            endDate = LocalDate.of(endYear, 9, 1),
        )
    }
}
