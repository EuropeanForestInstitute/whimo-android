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
package com.whimo.presentation.balances.convert

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.commodity.ConvertCommodityInteractor
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.commodity.models.ConvertQuantityModel
import com.whimo.domain.commodity.models.ConvertRecipeModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.BaseModel
import com.whimo.network.ErrorHandler
import com.whimo.presentation.balances.ConvertCommodityArgs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConvertCommodityViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var interactor: ConvertCommodityInteractor
    private lateinit var viewModel: ConvertCommodityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        interactor = mockk()
        viewModel = ConvertCommodityViewModel(
            interactor = interactor,
            errorHandler = ErrorHandler(mockk(relaxed = true)),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `convert passes harvest season from navigation args`() = runTest {
        val harvestSeason = HarvestSeasonModel(id = "season-id")
        val recipe = recipe()
        coEvery {
            interactor.convertCommodity(
                recipeId = recipe.id,
                inputs = recipe.inputs,
                outputs = recipe.outputs,
                harvestSeason = harvestSeason,
            )
        } returns BaseResult.Success(BaseModel(success = true, message = ""))

        viewModel.handleEvents(
            ConvertCommodityContract.Event.OnCreate(
                ConvertCommodityArgs(
                    recipe = recipe,
                    harvestSeason = harvestSeason,
                )
            )
        )
        viewModel.handleEvents(ConvertCommodityContract.Event.Convert)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            interactor.convertCommodity(
                recipeId = recipe.id,
                inputs = recipe.inputs,
                outputs = recipe.outputs,
                harvestSeason = harvestSeason,
            )
        }
    }

    private fun recipe(): ConvertRecipeModel {
        val commodity = CommodityModel(
            id = "commodity-id",
            code = "1801",
            name = "Cocoa beans",
            unit = "kg",
            hasRecipe = true,
            group = null,
            balance = 100f,
        )
        val quantity = ConvertQuantityModel(
            id = "quantity-id",
            commodity = commodity,
            quantity = 50f,
        )

        return ConvertRecipeModel(
            id = "recipe-id",
            name = "Cocoa conversion",
            inputs = listOf(quantity),
            outputs = listOf(quantity.copy(id = "output-id")),
        )
    }
}
