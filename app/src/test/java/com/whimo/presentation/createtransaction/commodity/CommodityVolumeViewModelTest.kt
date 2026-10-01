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
package com.whimo.presentation.createtransaction.commodity

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.R
import com.whimo.base.CoreViewModel
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityFilter
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractor
import com.whimo.domain.harvestseasons.models.HarvestSeasonFilter
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CommodityVolumeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var commodityInteractor: TestCommodityInteractor
    private lateinit var harvestSeasonsInteractor: TestHarvestSeasonsInteractor
    private lateinit var viewModel: CommodityVolumeViewModel

    private val activeSeason = season(
        id = "active-season",
        startYear = 2026,
        endYear = 2027,
        status = HarvestSeasonStatus.Active,
    )
    private val pastSeason = season(
        id = "past-season",
        startYear = 2025,
        endYear = 2026,
        status = HarvestSeasonStatus.Past,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        commodityInteractor = TestCommodityInteractor()
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(listOf(activeSeason, pastSeason))
        val resourceProvider = TestResourceProvider()
        viewModel = CommodityVolumeViewModel(
            commodityInteractor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCreate selects active season and loads selected balance`() = runTest {
        commodityInteractor.balancesBySeason[activeSeason.id] = 500f

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = sellingTransaction(commodity = commodity(balance = 2000f))
            )
        )
        advanceUntilIdle()

        val binding = viewModel.binding()

        assertEquals(activeSeason, binding.selectedHarvestSeason)
        assertEquals(listOf(activeSeason, pastSeason), binding.harvestSeasons)
        assertEquals("Your balance: 500 kg", binding.supportingText)
        assertEquals(listOf("commodity-id"), harvestSeasonsInteractor.requestedCommodityIds)
        assertEquals(activeSeason.id, commodityInteractor.requestedHarvestSeasonIds.last())
    }

    @Test
    fun `active season insufficient balance remains confirmable for automatic transaction`() = runTest {
        commodityInteractor.balancesBySeason[activeSeason.id] = 500f
        val confirmed = async {
            viewModel.effect
                .filterIsInstance<CommodityVolumeContract.Effect.VolumeConfirmed>()
                .first()
        }

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = sellingTransaction(commodity = commodity(balance = 2000f))
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("1200"))

        val binding = viewModel.binding()

        assertTrue(binding.insufficientBalance)
        assertTrue(binding.buttonEnabled)
        assertEquals(
            "Not enough available balance.\n\nAn automatic transaction will be created for the missing 700 kg. This amount will have incomplete traceability.",
            binding.insufficientBalanceMessage,
        )

        viewModel.handleEvents(CommodityVolumeContract.Event.OnConfirm)
        advanceUntilIdle()

        val effect = confirmed.await()
        assertEquals(1200f, effect.volume)
        assertEquals(activeSeason, effect.harvestSeason)
        assertEquals(500f, effect.commodity?.balance)
    }

    @Test
    fun `past season insufficient balance blocks confirm`() = runTest {
        commodityInteractor.balancesBySeason[activeSeason.id] = 500f
        commodityInteractor.balancesBySeason[pastSeason.id] = 60f

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = sellingTransaction(commodity = commodity(balance = 2000f))
            )
        )
        advanceUntilIdle()

        viewModel.handleEvents(CommodityVolumeContract.Event.OnHarvestSeasonSelected(pastSeason))
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("300"))

        val binding = viewModel.binding()

        assertEquals(pastSeason, binding.selectedHarvestSeason)
        assertTrue(binding.insufficientBalance)
        assertFalse(binding.buttonEnabled)
        assertEquals("Your balance: 60 kg", binding.warningText)
        assertEquals(
            "Not enough available balance for the 2025/26 harvest season.",
            binding.insufficientBalanceMessage,
        )
    }

    @Test
    fun `downstream transaction without harvest seasons cannot confirm volume`() = runTest {
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(emptyList())
        val resourceProvider = TestResourceProvider()
        viewModel = CommodityVolumeViewModel(
            commodityInteractor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = sellingTransaction(commodity = commodity(balance = 2000f))
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("100"))

        val binding = viewModel.binding()

        assertEquals(null, binding.selectedHarvestSeason)
        assertEquals(emptyList<HarvestSeasonModel>(), binding.harvestSeasons)
        assertFalse(binding.buttonEnabled)
    }

    @Test
    fun `offline downstream transaction uses cached linked harvest seasons`() = runTest {
        commodityInteractor.balancesBySeason[activeSeason.id] = 500f
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(
            seasons = emptyList(),
            cachedSeasons = listOf(activeSeason),
            remoteError = true,
        )
        val resourceProvider = TestResourceProvider()
        viewModel = CommodityVolumeViewModel(
            commodityInteractor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = sellingTransaction(commodity = commodity(balance = 2000f))
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("100"))

        val binding = viewModel.binding()

        assertEquals(activeSeason, binding.selectedHarvestSeason)
        assertEquals(listOf(activeSeason), binding.harvestSeasons)
        assertEquals(listOf("commodity-id"), harvestSeasonsInteractor.requestedCommodityIds)
        assertFalse(harvestSeasonsInteractor.requestedAllowUnlinkedFallback)
        assertTrue(binding.buttonEnabled)
    }

    @Test
    fun `producer transaction hides season selector and confirms active season`() = runTest {
        val confirmed = async {
            viewModel.effect
                .filterIsInstance<CommodityVolumeContract.Effect.VolumeConfirmed>()
                .first()
        }

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = producerTransaction(commodity = commodity(balance = null))
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("100"))

        val binding = viewModel.binding()

        assertFalse(binding.harvestSeasonSelectorVisible)
        assertEquals(activeSeason, binding.selectedHarvestSeason)
        assertTrue(binding.buttonEnabled)
        assertEquals(HarvestSeasonStatus.Active, harvestSeasonsInteractor.requestedStatus)

        viewModel.handleEvents(CommodityVolumeContract.Event.OnConfirm)
        advanceUntilIdle()

        val effect = confirmed.await()
        assertEquals(100f, effect.volume)
        assertEquals(activeSeason, effect.harvestSeason)
    }

    @Test
    fun `producer transaction without active season can confirm without season`() = runTest {
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(listOf(pastSeason))
        val resourceProvider = TestResourceProvider()
        viewModel = CommodityVolumeViewModel(
            commodityInteractor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = producerTransaction(commodity = commodity(balance = null))
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("100"))
        val confirmed = async {
            viewModel.effect
                .filterIsInstance<CommodityVolumeContract.Effect.VolumeConfirmed>()
                .first()
        }

        val binding = viewModel.binding()

        assertFalse(binding.harvestSeasonSelectorVisible)
        assertNull(binding.selectedHarvestSeason)
        assertTrue(binding.buttonEnabled)

        viewModel.handleEvents(CommodityVolumeContract.Event.OnConfirm)
        advanceUntilIdle()

        val effect = confirmed.await()
        assertEquals(100f, effect.volume)
        assertNull(effect.harvestSeason)
    }

    @Test
    fun `producer transaction does not keep stale season when current commodity has no active season`() = runTest {
        harvestSeasonsInteractor = TestHarvestSeasonsInteractor(emptyList())
        val resourceProvider = TestResourceProvider()
        viewModel = CommodityVolumeViewModel(
            commodityInteractor = commodityInteractor,
            harvestSeasonsInteractor = harvestSeasonsInteractor,
            errorHandler = ErrorHandler(resourceProvider),
            resourceProvider = resourceProvider,
        )

        viewModel.handleEvents(
            CommodityVolumeContract.Event.OnCreate(
                transaction = producerTransaction(commodity = commodity(balance = null))
                    .copy(harvestSeason = activeSeason)
            )
        )
        advanceUntilIdle()
        viewModel.handleEvents(CommodityVolumeContract.Event.OnVolumeChanged("100"))
        val confirmed = async {
            viewModel.effect
                .filterIsInstance<CommodityVolumeContract.Effect.VolumeConfirmed>()
                .first()
        }

        val binding = viewModel.binding()

        assertFalse(binding.harvestSeasonSelectorVisible)
        assertNull(binding.selectedHarvestSeason)
        assertFalse(harvestSeasonsInteractor.requestedAllowUnlinkedFallback)
        assertTrue(binding.buttonEnabled)

        viewModel.handleEvents(CommodityVolumeContract.Event.OnConfirm)
        advanceUntilIdle()

        val effect = confirmed.await()
        assertEquals(100f, effect.volume)
        assertNull(effect.harvestSeason)
    }

    private fun sellingTransaction(commodity: CommodityModel): CreateTransactionModel {
        return CreateTransactionModel(
            action = com.whimo.domain.transactions.models.TransactionAction.Selling,
            isProducerTransaction = false,
            commodity = commodity,
        )
    }

    private fun producerTransaction(commodity: CommodityModel): CreateTransactionModel {
        return CreateTransactionModel(
            isProducerTransaction = true,
            commodity = commodity,
        )
    }

    private fun commodity(balance: Float?): CommodityModel {
        return CommodityModel(
            id = "commodity-id",
            code = "1801",
            name = "Cocoa beans, whole or broken, raw or roasted",
            unit = "kg",
            hasRecipe = false,
            group = CommodityGroupModel(
                id = "group-id",
                name = "Cocoa",
                commodities = null,
            ),
            balance = balance,
        )
    }

    private class TestCommodityInteractor : CommodityInteractor {
        val balancesBySeason = mutableMapOf<String, Float>()
        val requestedHarvestSeasonIds = mutableListOf<String?>()

        override suspend fun getCommodities(filter: CommodityFilter): BaseResult<List<CommodityGroupModel>> {
            throw NotImplementedError()
        }

        override suspend fun getCommoditiesFromDB(): List<CommodityGroupModel> {
            throw NotImplementedError()
        }

        override suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?) {
            throw NotImplementedError()
        }

        override suspend fun getBalancesFromDB(filter: CommodityBalanceFilter): List<CommodityBalanceModel> {
            return emptyList()
        }

        override suspend fun getBalances(filter: CommodityBalanceFilter): BaseResult<List<CommodityBalanceModel>> {
            val harvestSeason = filter.harvestSeason
            requestedHarvestSeasonIds += harvestSeason?.id
            val volume = balancesBySeason[harvestSeason?.id] ?: 0f

            return BaseResult.Success(
                listOf(
                    CommodityBalanceModel(
                        id = "balance-${harvestSeason?.id}",
                        volume = volume,
                        commodity = CommodityModel(
                            id = filter.commodityId.orEmpty(),
                            code = "1801",
                            name = "Cocoa beans",
                            unit = "kg",
                            hasRecipe = false,
                            group = null,
                            balance = volume,
                        ),
                        harvestSeason = harvestSeason,
                        hasRecipe = false,
                    )
                )
            )
        }
    }

    private class TestHarvestSeasonsInteractor(
        private val seasons: List<HarvestSeasonModel>,
        private val cachedSeasons: List<HarvestSeasonModel> = emptyList(),
        private val remoteError: Boolean = false,
    ) : HarvestSeasonsInteractor {
        var requestedCommodityIds: List<String>? = null
        var requestedStatus: HarvestSeasonStatus? = null
        var requestedAllowUnlinkedFallback: Boolean = true

        override suspend fun getHarvestSeasons(filter: HarvestSeasonFilter): BaseResult<List<HarvestSeasonModel>> {
            requestedCommodityIds = filter.commodityIds
            requestedStatus = filter.status
            requestedAllowUnlinkedFallback = filter.allowUnlinkedFallback
            if (remoteError) return BaseResult.Error(RuntimeException("No network"))
            return BaseResult.Success(seasons.filterByStatus(filter.status))
        }

        override suspend fun getHarvestSeasonsFromDB(filter: HarvestSeasonFilter): List<HarvestSeasonModel> {
            requestedCommodityIds = filter.commodityIds
            requestedStatus = filter.status
            requestedAllowUnlinkedFallback = filter.allowUnlinkedFallback
            return cachedSeasons.filterByStatus(filter.status)
        }

        private fun List<HarvestSeasonModel>.filterByStatus(
            status: HarvestSeasonStatus?,
        ): List<HarvestSeasonModel> {
            return if (status == null) this else filter { it.status == status }
        }
    }

    private class TestResourceProvider : ResourceProvider {
        override fun resources(): Resources {
            throw NotImplementedError()
        }

        override fun getString(res: Int, vararg args: Any): String {
            return when (res) {
                R.string.your_balance -> "Your balance: ${args[0]}"
                R.string.automatic_transaction_balance_message ->
                    "Not enough available balance.\n\nAn automatic transaction will be created for the missing ${args[0]}. This amount will have incomplete traceability."
                R.string.insufficient_season_balance_message ->
                    "Not enough available balance for the ${args[0]} harvest season."
                else -> "string-$res"
            }
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
    private fun CommodityVolumeViewModel.binding(): CommodityVolumeContract.Binding {
        val field = CoreViewModel::class.java.getDeclaredField("binding")
        field.isAccessible = true
        return field.get(this) as CommodityVolumeContract.Binding
    }

    companion object {
        private fun season(
            id: String,
            startYear: Int,
            endYear: Int,
            status: HarvestSeasonStatus,
        ): HarvestSeasonModel {
            return HarvestSeasonModel(
                id = id,
                startDate = LocalDate.of(startYear, 9, 1),
                endDate = LocalDate.of(endYear, 9, 1),
                status = status,
            )
        }
    }
}
