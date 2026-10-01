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
package com.whimo.presentation.balances

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.domain.transactions.TransactionsInteractor
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionsInteractor: TestTransactionsInteractor
    private lateinit var viewModel: BalanceDetailsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionsInteractor = TestTransactionsInteractor()
        viewModel = BalanceDetailsViewModel(transactionsInteractor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreate refreshes source transactions with balance harvest season`() = runTest {
        viewModel.handleEvents(
            BalanceDetailsContract.Event.OnCreate(
                BalanceDetailsArgs(
                    commodity = commodity,
                    harvestSeason = harvestSeason,
                )
            )
        )
        advanceUntilIdle()

        val filter = transactionsInteractor.refreshedFilter
        assertEquals(commodity.id, filter?.commodity?.id)
        assertEquals(harvestSeason.id, filter?.harvestSeason?.id)
        assertEquals(true, transactionsInteractor.refreshedUseCache)
    }

    private class TestTransactionsInteractor : TransactionsInteractor {
        override val stateFlow: SharedFlow<TransactionsState> = MutableSharedFlow()
        var refreshedFilter: TransactionsFilter? = null
        var refreshedUseCache: Boolean? = null

        override suspend fun refresh(filter: TransactionsFilter, useCache: Boolean) {
            refreshedFilter = filter.copy()
            refreshedUseCache = useCache
        }

        override suspend fun loadNextPage(filter: TransactionsFilter) = Unit
    }

    companion object {
        private val commodityGroup = CommodityGroupModel(
            id = "group-id",
            name = "Cocoa",
            commodities = null,
        )

        private val commodity = CommodityModel(
            id = "commodity-id",
            code = "1801",
            name = "Cocoa beans",
            unit = "kg",
            hasRecipe = false,
            group = commodityGroup,
            balance = 10f,
        )

        private val harvestSeason = HarvestSeasonModel(
            id = "harvest-season-id",
            name = "Harvest season 2025/26",
            startDate = LocalDate.of(2025, 9, 1),
            endDate = LocalDate.of(2026, 9, 1),
            status = HarvestSeasonStatus.Active,
        )
    }
}
