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
package com.whimo.data.harvestseasons.model.mappers

import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HarvestSeasonMappersTest {

    @Test
    fun `toEntity stores distinct commodity ids and toDomain restores season fields`() {
        val season = HarvestSeasonModel(
            id = "season-id",
            name = "Harvest season 2026/27",
            status = HarvestSeasonStatus.Active,
            country = "Cameroon",
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2027, 8, 31),
        )

        val entity = season.toEntity(listOf("commodity-1", "commodity-1", "commodity-2"))

        assertEquals(listOf("commodity-1", "commodity-2"), entity.getCommodityIds())
        assertEquals(season, entity.toDomain())
    }
}
