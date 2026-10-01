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
package com.whimo.data.commodity.model.mappers

import com.whimo.data.commodity.model.response.Commodity
import com.whimo.data.commodity.model.response.CommodityBalance
import com.whimo.data.commodity.model.response.CommodityGroup
import com.whimo.domain.transactions.models.TraceabilityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommodityMappersTest {

    @Test
    fun `balance toDomain maps traceability status and row volume`() {
        val result = commodityBalance(
            volume = 12.5f,
            traceability = "partial",
            hasRecipe = true,
        ).toDomain()

        assertEquals(TraceabilityStatus.Partial, result.traceabilityStatus)
        assertEquals(12.5f, result.volume, 0.0f)
        assertEquals(12.5f, result.commodity.balance ?: -1f, 0.0f)
        assertTrue(result.hasRecipe)
        assertTrue(result.commodity.hasRecipe)
    }

    @Test
    fun `balance toDomain ignores unknown traceability status`() {
        val result = commodityBalance(traceability = "unexpected").toDomain()

        assertNull(result.traceabilityStatus)
    }

    private fun commodityBalance(
        volume: Float = 7f,
        traceability: String? = "full",
        hasRecipe: Boolean = false,
    ): CommodityBalance {
        return CommodityBalance(
            id = "balance-id",
            volume = volume,
            commodity = Commodity(
                id = "commodity-id",
                code = "1801",
                name = "Cocoa beans",
                unit = "kg",
                has_recipe = false,
                group = CommodityGroup(
                    id = "group-id",
                    name = "Cocoa",
                    commodities = null,
                ),
                balance = null,
            ),
            harvest_season = null,
            traceability = traceability,
            has_recipe = hasRecipe,
        )
    }
}
