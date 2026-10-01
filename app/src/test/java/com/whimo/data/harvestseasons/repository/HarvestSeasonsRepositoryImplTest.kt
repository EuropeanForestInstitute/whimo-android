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

import com.whimo.data.harvestseasons.model.entity.HarvestSeasonEntity
import com.whimo.data.harvestseasons.service.HarvestSeasonsDao
import com.whimo.data.harvestseasons.service.HarvestSeasonsService
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.utils.gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HarvestSeasonsRepositoryImplTest {

    private lateinit var dao: HarvestSeasonsDao
    private lateinit var repository: HarvestSeasonsRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        repository = HarvestSeasonsRepositoryImpl(
            service = mockk<HarvestSeasonsService>(),
            dao = dao,
            harvestSeasonCommodityLinker = mockk(relaxed = true),
        )
    }

    @Test
    fun `getHarvestSeasonsFromDB filters by linked commodity ids`() = runTest {
        coEvery { dao.getAll() } returns listOf(
            entity(
                id = "cocoa-season",
                name = "Cocoa 2025/26",
                commodityIds = listOf("cocoa-beans", "cocoa-powder"),
            ),
            entity(
                id = "cattle-season",
                name = "Cattle 2025/26",
                commodityIds = listOf("cattle"),
            ),
            entity(
                id = "unlinked-soy-season",
                name = "Soy 2025/26",
                commodityIds = emptyList(),
            ),
        )

        val result = repository.getHarvestSeasonsFromDB(
            commodityIds = listOf("cocoa-beans"),
            status = null,
            allowUnlinkedFallback = true,
        )

        assertEquals(listOf("cocoa-season"), result.map { it.id })
    }

    @Test
    fun `getHarvestSeasonsFromDB falls back to unlinked seasons when no linked commodity match exists`() = runTest {
        coEvery { dao.getAll() } returns listOf(
            entity(
                id = "cattle-season",
                name = "Cattle 2025/26",
                commodityIds = listOf("cattle"),
            ),
            entity(
                id = "unlinked-active-season",
                name = "Active 2025/26",
                commodityIds = emptyList(),
            ),
        )

        val result = repository.getHarvestSeasonsFromDB(
            commodityIds = listOf("cocoa-beans"),
            status = null,
            allowUnlinkedFallback = true,
        )

        assertEquals(listOf("unlinked-active-season"), result.map { it.id })
    }

    @Test
    fun `getHarvestSeasonsFromDB does not fall back to unlinked seasons when disabled`() = runTest {
        coEvery { dao.getAll() } returns listOf(
            entity(
                id = "cattle-season",
                name = "Cattle 2025/26",
                commodityIds = listOf("cattle"),
            ),
            entity(
                id = "unlinked-active-season",
                name = "Active 2025/26",
                commodityIds = emptyList(),
            ),
        )

        val result = repository.getHarvestSeasonsFromDB(
            commodityIds = listOf("cocoa-beans"),
            status = null,
            allowUnlinkedFallback = false,
        )

        assertEquals(emptyList<String>(), result.map { it.id })
    }

    @Test
    fun `getHarvestSeasonsFromDB keeps unfiltered cache when no commodity ids requested`() = runTest {
        coEvery { dao.getAll() } returns listOf(
            entity(
                id = "cocoa-season",
                name = "Cocoa 2025/26",
                commodityIds = listOf("cocoa-beans"),
            ),
            entity(
                id = "unlinked-soy-season",
                name = "Soy 2025/26",
                commodityIds = emptyList(),
            ),
        )

        val result = repository.getHarvestSeasonsFromDB(
            commodityIds = null,
            status = null,
            allowUnlinkedFallback = true,
        )

        assertEquals(listOf("cocoa-season", "unlinked-soy-season"), result.map { it.id })
    }

    private fun entity(
        id: String,
        name: String,
        commodityIds: List<String>,
    ): HarvestSeasonEntity {
        return HarvestSeasonEntity(
            id = id,
            name = name,
            status = HarvestSeasonStatus.Active.statusName,
            country = null,
            startDate = "2025-09-01",
            endDate = "2026-09-01",
            commodityIdsJson = commodityIds.takeIf { it.isNotEmpty() }?.let { gson.toJson(it) },
        )
    }
}
