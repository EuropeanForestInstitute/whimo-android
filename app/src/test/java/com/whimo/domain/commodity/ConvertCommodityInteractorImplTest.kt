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
import com.whimo.data.commodity.model.request.ConvertCommodityRequest
import com.whimo.data.commodity.repository.CommodityRepository
import com.whimo.data.commodity.repository.ConvertCommodityRepository
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.commodity.models.ConvertQuantityModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.BaseModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertCommodityInteractorImplTest {

    @Test
    fun `convertCommodity adds selected harvest season id to request`() = runTest {
        val requestSlot = slot<ConvertCommodityRequest>()
        val repository = mockk<ConvertCommodityRepository>()
        coEvery { repository.convertCommodity(capture(requestSlot)) } returns
                BaseResult.Success(BaseModel(success = true, message = ""))
        val interactor = ConvertCommodityInteractorImpl(
            repository = repository,
            commodityRepository = mockk<CommodityRepository>(relaxed = true),
        )
        val quantity = ConvertQuantityModel(
            id = "quantity-id",
            commodity = CommodityModel(
                id = "commodity-id",
                code = "1801",
                name = "Cocoa beans",
                unit = "kg",
                hasRecipe = true,
                group = null,
                balance = 100f,
            ),
            quantity = 50f,
        )

        interactor.convertCommodity(
            recipeId = "recipe-id",
            inputs = listOf(quantity),
            outputs = listOf(quantity),
            harvestSeason = HarvestSeasonModel(id = "season-id"),
        )

        coVerify(exactly = 1) { repository.convertCommodity(any()) }
        assertEquals("season-id", requestSlot.captured.harvest_season_id)
    }
}
