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
package com.whimo.presentation.transactions.transactiondetails

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.R
import com.whimo.base.CoreViewModel
import com.whimo.data.base.common.BaseResult
import com.whimo.data.geodata.model.request.UpdateGeoDataRequest
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityBalanceModel
import com.whimo.domain.commodity.models.CommodityFilter
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.geodata.GeoDataInteractor
import com.whimo.domain.geodata.models.DownloadGeoDataModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.domain.settings.models.AccountModel
import com.whimo.domain.transactions.TransactionDetailsInteractor
import com.whimo.domain.transactions.models.BaseModel
import com.whimo.domain.transactions.models.TraceabilityCountsModel
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.TransactionType
import com.whimo.domain.transactions.models.UserModel
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import com.whimo.providers.SharedPreferencesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var interactor: TestTransactionDetailsInteractor
    private lateinit var commodityInteractor: TestCommodityInteractor
    private lateinit var viewModel: TransactionDetailsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        interactor = TestTransactionDetailsInteractor()
        commodityInteractor = TestCommodityInteractor()
        val resourceProvider = TestResourceProvider()
        viewModel = TransactionDetailsViewModel(
            interactor = interactor,
            commodityInteractor = commodityInteractor,
            geoDataInteractor = TestGeoDataInteractor(),
            resourceProvider = resourceProvider,
            errorHandler = ErrorHandler(resourceProvider),
            sharedPreferencesProvider = TestSharedPreferencesProvider(
                account = AccountModel(
                    id = SELLER_ID,
                    username = "seller",
                    gadgets = emptyList(),
                )
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `seller pending active season shortage loads balance shows automatic warning and allows accept`() = runTest {
        val transaction = transaction(
            balance = null,
            volume = 800f,
            action = TransactionAction.Selling,
            harvestSeason = activeSeason,
        )
        interactor.remoteTransaction = transaction
        commodityInteractor.balancesBySeason[activeSeason.id] = 400f

        viewModel.handleEvents(TransactionDetailsContract.Event.OnCreate(transaction))
        advanceUntilIdle()

        val binding = viewModel.binding()

        assertEquals("Sales transaction", binding.toolbarTitle)
        assertTrue(binding.showRecipientActionButtons)
        assertTrue(binding.acceptEnabled)
        assertFalse(binding.acceptBlocked)
        assertFalse(binding.acceptBalanceLoading)
        assertEquals("Not enough available balance.", binding.commodityWarningText)
        assertEquals(
            "Not enough available balance.\n\nAn automatic transaction will be created for the missing 400 kg. This amount will have incomplete traceability.",
            binding.acceptWarningText,
        )
        assertEquals(listOf("commodity-id" to activeSeason.id), commodityInteractor.balanceRequests)

        viewModel.handleEvents(TransactionDetailsContract.Event.AcceptTransaction)
        advanceUntilIdle()

        assertEquals(listOf(TransactionStatus.Accepted.statusName), interactor.statusUpdates)
    }

    @Test
    fun `seller pending past season shortage loads balance blocks accept and prepares dialog`() = runTest {
        val transaction = transaction(
            balance = null,
            volume = 800f,
            action = TransactionAction.Selling,
            harvestSeason = pastSeason,
        )
        interactor.remoteTransaction = transaction
        commodityInteractor.balancesBySeason[pastSeason.id] = 400f

        viewModel.handleEvents(TransactionDetailsContract.Event.OnCreate(transaction))
        advanceUntilIdle()
        viewModel.handleEvents(TransactionDetailsContract.Event.AcceptTransaction)
        advanceUntilIdle()

        val binding = viewModel.binding()

        assertEquals("Sales transaction", binding.toolbarTitle)
        assertTrue(binding.showRecipientActionButtons)
        assertFalse(binding.acceptEnabled)
        assertTrue(binding.acceptBlocked)
        assertFalse(binding.acceptBalanceLoading)
        assertEquals("Not enough available balance.", binding.commodityWarningText)
        assertNull(binding.acceptWarningText)
        assertEquals(
            "You have 400 kg available from the 2025/26 Harvest Season, but the buyer requested 800 kg.\n\n" +
                    "You can't accept this transaction until you have enough balance available.\n\n" +
                    "Alternatively, reject this request and create a new sale for the available amount.",
            binding.acceptBlockedDialogText,
        )
        assertEquals(listOf("commodity-id" to pastSeason.id), commodityInteractor.balanceRequests)
        assertTrue(interactor.statusUpdates.isEmpty())
    }

    @Test
    fun `seller pending transaction with loaded enough balance keeps normal accept enabled`() = runTest {
        val transaction = transaction(
            balance = null,
            volume = 800f,
            action = TransactionAction.Selling,
            harvestSeason = pastSeason,
        )
        interactor.remoteTransaction = transaction
        commodityInteractor.balancesBySeason[pastSeason.id] = 900f

        viewModel.handleEvents(TransactionDetailsContract.Event.OnCreate(transaction))
        advanceUntilIdle()
        viewModel.handleEvents(TransactionDetailsContract.Event.AcceptTransaction)
        advanceUntilIdle()

        val binding = viewModel.binding()

        assertTrue(binding.acceptEnabled)
        assertFalse(binding.acceptBlocked)
        assertFalse(binding.acceptBalanceLoading)
        assertNull(binding.commodityWarningText)
        assertNull(binding.acceptWarningText)
        assertEquals(listOf(TransactionStatus.Accepted.statusName), interactor.statusUpdates)
    }

    private fun transaction(
        balance: Float?,
        volume: Float,
        action: TransactionAction = TransactionAction.Buying,
        harvestSeason: HarvestSeasonModel,
    ) = TransactionModel(
        id = "transaction-id",
        createdDate = LocalDateTime.of(2025, 7, 22, 11, 10, 0),
        updatedDate = LocalDateTime.of(2025, 7, 22, 11, 10, 0),
        expiresDate = LocalDateTime.of(2025, 8, 22, 11, 10, 0),
        type = TransactionType.Downstream,
        status = TransactionStatus.Pending,
        action = action,
        locationProvider = null,
        location = null,
        commodity = CommodityModel(
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
        ),
        volume = volume,
        traceability = null,
        seller = UserModel(id = SELLER_ID, username = "#155796"),
        buyer = UserModel(id = BUYER_ID, username = "#124856"),
        isBuyingFromFarmer = false,
        isAutomatic = false,
        createdById = BUYER_ID,
        harvestSeason = harvestSeason,
    )

    private class TestTransactionDetailsInteractor : TransactionDetailsInteractor {
        var remoteTransaction: TransactionModel? = null
        val statusUpdates = mutableListOf<String>()

        override suspend fun getTransactions(): List<TransactionModel> = emptyList()

        override suspend fun getTransactionDetails(transactionId: String): BaseResult<TransactionModel> {
            return BaseResult.Success(remoteTransaction)
        }

        override suspend fun getTransactionDetailsFromDB(transactionId: String): TransactionModel? = null

        override suspend fun saveTransactionDetailsToDB(transactionModel: TransactionModel) = Unit

        override suspend fun getTransactionTraceability(transactionId: String): BaseResult<TraceabilityCountsModel> {
            throw NotImplementedError()
        }

        override suspend fun getTraceabilityCountsFromDB(transactionId: String): TraceabilityCountsModel? = null

        override suspend fun saveTraceabilityCountsToDB(
            transactionId: String,
            traceabilityCounts: TraceabilityCountsModel
        ) = Unit

        override suspend fun updateTransactionStatus(transactionId: String, status: String): BaseResult<Boolean> {
            statusUpdates += status
            return BaseResult.Success(true)
        }

        override suspend fun resendNotification(transactionId: String): BaseResult<Boolean> {
            throw NotImplementedError()
        }
    }

    private class TestCommodityInteractor : CommodityInteractor {
        val balancesBySeason = mutableMapOf<String, Float>()
        val balanceRequests = mutableListOf<Pair<String?, String?>>()

        override suspend fun getCommodities(filter: CommodityFilter): BaseResult<List<CommodityGroupModel>> {
            return BaseResult.Success(emptyList())
        }

        override suspend fun getCommoditiesFromDB(): List<CommodityGroupModel> = emptyList()

        override suspend fun updateCommoditiesDB(items: List<CommodityGroupModel>?) = Unit

        override suspend fun getBalancesFromDB(filter: CommodityBalanceFilter): List<CommodityBalanceModel> {
            return emptyList()
        }

        override suspend fun getBalances(filter: CommodityBalanceFilter): BaseResult<List<CommodityBalanceModel>> {
            val season = filter.harvestSeason
            balanceRequests += filter.commodityId to season?.id

            return BaseResult.Success(
                listOf(
                    CommodityBalanceModel(
                        id = "balance-${season?.id}",
                        volume = balancesBySeason[season?.id] ?: 0f,
                        commodity = CommodityModel(
                            id = filter.commodityId ?: "commodity-id",
                            code = "1801",
                            name = "Cocoa beans, whole or broken, raw or roasted",
                            unit = "kg",
                            hasRecipe = false,
                            group = CommodityGroupModel(
                                id = filter.groupId ?: "group-id",
                                name = "Cocoa",
                                commodities = null,
                            ),
                            balance = balancesBySeason[season?.id] ?: 0f,
                        ),
                        harvestSeason = season,
                        hasRecipe = false,
                    )
                )
            )
        }
    }

    private class TestGeoDataInteractor : GeoDataInteractor {
        override suspend fun updateTransactionGeoData(
            transactionId: String,
            request: UpdateGeoDataRequest
        ): BaseResult<Boolean> {
            throw NotImplementedError()
        }

        override suspend fun requestTransactionGeoData(transactionId: String): BaseResult<BaseModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionGeoData(transactionId: String): BaseResult<DownloadGeoDataModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionGeoJson(transactionId: String): BaseResult<DownloadGeoDataModel> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionCSV(transactionId: String): BaseResult<ResponseBody> {
            throw NotImplementedError()
        }

        override suspend fun downloadTransactionBundle(transactionId: String): BaseResult<ResponseBody> {
            throw NotImplementedError()
        }
    }

    private class TestSharedPreferencesProvider(
        private val account: AccountModel?,
    ) : SharedPreferencesProvider {
        override fun clearAllData() = Unit
        override fun removeItem(key: String) = Unit
        override fun getString(key: String): String? = null
        override fun saveString(key: String, value: String?) = Unit
        override fun getInt(key: String): Int = -1
        override fun saveInt(key: String, value: Int) = Unit
        override fun getBoolean(key: String, default: Boolean): Boolean = default
        override fun saveBoolean(key: String, value: Boolean) = Unit
        override fun isAuthorized(): Boolean = true
        override fun deleteAuthToken() = Unit
        override fun getAuthToken(): String? = "token"
        override fun saveAuthToken(value: String?) = Unit
        override fun getRefreshToken(): String? = "refresh"
        override fun saveRefreshToken(value: String?) = Unit
        override fun getAccount(): AccountModel? = account
        override fun setAccount(account: AccountModel) = Unit
        override fun getFCMToken(): String? = null
        override fun setFCMToken(token: String) = Unit
        override fun isNotificationsAllowed(): Boolean = true
        override fun setNotificationsAllowed(isAllowed: Boolean) = Unit
    }

    private class TestResourceProvider : ResourceProvider {
        override fun resources(): Resources {
            throw NotImplementedError()
        }

        override fun getString(res: Int, vararg args: Any): String {
            return when (res) {
                R.string.sales_transaction -> "Sales transaction"
                R.string.transaction_details -> "Transaction details"
                R.string.not_enough_available_balance -> "Not enough available balance."
                R.string.automatic_transaction_balance_message ->
                    "Not enough available balance.\n\nAn automatic transaction will be created for the missing ${args[0]}. This amount will have incomplete traceability."
                R.string.accept_transaction_insufficient_balance_dialog ->
                    "You have ${args[0]} available from the ${args[1]} Harvest Season, but the buyer requested ${args[2]}.\n\n" +
                            "You can't accept this transaction until you have enough balance available.\n\n" +
                            "Alternatively, reject this request and create a new sale for the available amount."
                R.string.downloading_transaction_details -> "Download ${args[0]}"
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
    private fun TransactionDetailsViewModel.binding(): TransactionDetailsContract.Binding {
        val field = CoreViewModel::class.java.getDeclaredField("binding")
        field.isAccessible = true
        return field.get(this) as TransactionDetailsContract.Binding
    }

    companion object {
        private const val SELLER_ID = "seller-id"
        private const val BUYER_ID = "buyer-id"

        private val activeSeason = HarvestSeasonModel(
            id = "active-season",
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2027, 9, 1),
            status = HarvestSeasonStatus.Active,
        )

        private val pastSeason = HarvestSeasonModel(
            id = "past-season",
            startDate = LocalDate.of(2025, 9, 1),
            endDate = LocalDate.of(2026, 9, 1),
            status = HarvestSeasonStatus.Past,
        )
    }
}
