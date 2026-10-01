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
package com.whimo.domain.transactions

import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.getShortName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HarvestSeasonDisplayTest {

    @Test
    fun `getShortName uses year range from dates before backend name`() {
        val season = HarvestSeasonModel(
            id = "season-id",
            name = "Cocoa 2025/26",
            startDate = LocalDate.of(2025, 10, 1),
            endDate = LocalDate.of(2026, 9, 30),
        )

        val result = season.getShortName()

        assertEquals("2025/26", result)
    }

    @Test
    fun `getShortName keeps existing name fallback when dates are missing`() {
        val season = HarvestSeasonModel(
            id = "season-id",
            name = "Harvest season 2025/26",
        )

        val result = season.getShortName()

        assertEquals("2025/26", result)
    }
}
