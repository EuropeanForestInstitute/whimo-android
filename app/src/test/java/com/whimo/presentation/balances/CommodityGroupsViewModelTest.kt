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

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.base.CoreViewModel
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityFilter
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractor
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CommodityGroupsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var commodityInteractor: TestCommodityInteractor
    private lateinit var harvestSeasonsInteractor: TestHarvestSeasonsInteractor
    private lateinit var viewModel: CommodityGroupsViewModel

    private val season = HarvestSeasonModel(
        id = "season-id",
        name = "Harvest season 2026/27",
        startDate = LocalDate.of(2026, 9, 1),
        endDate = LocalDate.of(2027, 9, 1),
        status = HarvestSeasonStatus.Active,
    )
    private val group = CommodityGroupModel(
        id = "group-id",
        name = "Cocoa",
        commodities = emptyList(),
    )
    private val commodity = CommodityModel(
        id = "commodity-id",
        code = "1801",
        name = "Cocoa beans",
        unit = "kg",
        hasRecipe = false,
        group = group.copy(commodities = null),
        balance = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        commodityInteractor = TestCommodityInteractor(
            groups = listOf(group.copy(commodities = listOf(commodity))),
            balances = listOf(
                CommodityBalanceModel(
                    id = "balance-id",
                    volume = 24f,
                    commodity = commodity,
                    harvestSeason = season,
                    hasRecipe = true,
                    traceabilityStatus = TraceabilityStatus.Conditional,
                )
            )
        )
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(listOf(season))
        viewModel = CommodityGroupsViewModel(
            interactor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(TestResourceProvider()),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreate displays balances from balance endpoint traceability`() = runTest {
        viewModel.handleEvents(CommodityGroupsContract.Event.OnCreate)
        advanceUntilIdle()

        val binding = viewModel.binding()
        val item = binding.balances!!.single()

        assertEquals(TraceabilityStatus.Conditional, item.traceabilityStatus)
        assertEquals(season, item.harvestSeason)
        assertEquals(24f, item.commodity.balance ?: -1f, 0.0f)
        assertEquals(true, item.commodity.hasRecipe)
        assertEquals(commodity.id, item.commodity.id)
        assertNull(commodityInteractor.requestedBalanceFilters.single().groupId)
        assertNull(commodityInteractor.requestedBalanceFilters.single().harvestSeason)
    }

    private class TestCommodityInteractor(
        private val groups: List<CommodityGroupModel>,
        private val balances: List<CommodityBalanceModel>,
    ) : CommodityInteractor {
        val requestedBalanceFilters = mutableListOf<CommodityBalanceFilter>()

        override suspend fun getCommodities(filter: CommodityFilter): BaseResult<List<CommodityGroupModel>> {
            return BaseResult.Success(groups)
        }

        override suspend fun getCommoditiesFromDB(): List<CommodityGroupModel> {
            return emptyList()
        }

        override suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?) = Unit

        override suspend fun getBalancesFromDB(filter: CommodityBalanceFilter): List<CommodityBalanceModel> {
            return emptyList()
        }

        override suspend fun getBalances(filter: CommodityBalanceFilter): BaseResult<List<CommodityBalanceModel>> {
            requestedBalanceFilters += filter.copy()
            return BaseResult.Success(balances)
        }
    }

    private class TestHarvestSeasonsInteractor(
        private val seasons: List<HarvestSeasonModel>,
    ) : HarvestSeasonsInteractor {

        override suspend fun getHarvestSeasons(filter: HarvestSeasonFilter): BaseResult<List<HarvestSeasonModel>> {
            return BaseResult.Success(seasons)
        }

        override suspend fun getHarvestSeasonsFromDB(filter: HarvestSeasonFilter): List<HarvestSeasonModel> {
            return emptyList()
        }
    }

    private class TestResourceProvider : ResourceProvider {
        override fun resources(): Resources {
            throw NotImplementedError()
        }

        override fun getString(res: Int, vararg args: Any): String {
            return "string-$res"
        }

        override fun getColor(color: Int): Int {
            throw NotImplementedError()
        }

        override fun getDrawable(icon: Int): Drawable? {
            throw NotImplementedError()
        }

        override fun cacheDir(): File {
            throw NotImplementedError()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun CommodityGroupsViewModel.binding(): CommodityGroupsContract.Binding {
        val field = CoreViewModel::class.java.getDeclaredField("binding")
        field.isAccessible = true
        return field.get(this) as CommodityGroupsContract.Binding
    }
}
