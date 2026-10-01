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
package com.whimo.presentation.settings

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.whimo.base.CoreViewModel
import com.whimo.base.CoreViewSideEffect
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.createtransaction.CreateTransactionInteractor
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.PendingTransactionModel
import com.whimo.domain.settings.SettingsInteractor
import com.whimo.domain.settings.models.AccountModel
import com.whimo.domain.settings.models.NotificationSettingsModel
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import com.whimo.providers.SharedPreferencesProvider
import com.whimo.providers.TestEnvironmentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesProvider: TestSharedPreferencesProvider
    private lateinit var createTransactionInteractor: TestCreateTransactionInteractor
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesProvider = TestSharedPreferencesProvider()
        createTransactionInteractor = TestCreateTransactionInteractor()
        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entering test environment with pending transactions shows unsynced dialog and stays live`() = runTest {
        createTransactionInteractor.pendingTransactions = listOf(
            PendingTransactionModel(id = 1L, transactionModel = CreateTransactionModel())
        )
        val effects = collectEffects()

        viewModel.handleEvents(SettingsContract.Event.TestEnvironmentChanged(isEnabled = true))
        advanceUntilIdle()

        assertFalse(preferencesProvider.isTestEnvironmentEnabled())
        assertFalse(viewModel.binding().isTestEnvironmentEnabled)
        assertFalse(createTransactionInteractor.clearPendingTransactionsCalled)
        assertTrue(effects.contains(SettingsContract.Effect.ShowUnsyncedChangesDialog))
    }

    @Test
    fun `leaving test environment with pending transactions clears queue and does not show unsynced dialog`() = runTest {
        preferencesProvider.setTestEnvironmentEnabled(true)
        createTransactionInteractor.pendingTransactions = listOf(
            PendingTransactionModel(id = 1L, transactionModel = CreateTransactionModel())
        )
        viewModel = createViewModel()
        val effects = collectEffects()

        viewModel.handleEvents(SettingsContract.Event.OnCreate)
        viewModel.handleEvents(SettingsContract.Event.TestEnvironmentChanged(isEnabled = false))
        advanceUntilIdle()

        assertFalse(preferencesProvider.isTestEnvironmentEnabled())
        assertFalse(viewModel.binding().isTestEnvironmentEnabled)
        assertEquals(emptyList<PendingTransactionModel>(), createTransactionInteractor.pendingTransactions)
        assertTrue(createTransactionInteractor.clearPendingTransactionsCalled)
        assertEquals(emptyList<CoreViewSideEffect>(), effects)
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            interactor = TestSettingsInteractor(),
            createTransactionInteractor = createTransactionInteractor,
            testEnvironmentManager = TestEnvironmentManager(preferencesProvider),
            errorHandler = ErrorHandler(TestResourceProvider()),
            resourceProvider = TestResourceProvider(),
        ).apply {
            initBinding(SettingsContract.Binding())
        }
    }

    private fun kotlinx.coroutines.test.TestScope.collectEffects(): MutableList<CoreViewSideEffect> {
        val effects = mutableListOf<CoreViewSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect {
                effects += it
            }
        }
        return effects
    }

    private class TestSettingsInteractor : SettingsInteractor {
        override suspend fun getAccountInfo(): BaseResult<AccountModel> = throw NotImplementedError()
        override suspend fun changePassword(currentPassword: String, newPassword: String): BaseResult<String> = throw NotImplementedError()
        override suspend fun logout() = Unit
        override suspend fun deleteAccount(): BaseResult<String> = throw NotImplementedError()
        override suspend fun getNotificationSettings(): BaseResult<List<NotificationSettingsModel>> = throw NotImplementedError()
        override suspend fun getNotificationSettingsFromDB(): List<NotificationSettingsModel> = throw NotImplementedError()
        override suspend fun updateNotificationSettings(settings: List<NotificationSettingsModel>): BaseResult<String> = throw NotImplementedError()
        override suspend fun updateNotificationSettingsDB(items: List<NotificationSettingsModel>?) = Unit
        override suspend fun addEmail(email: String): BaseResult<Boolean> = throw NotImplementedError()
        override suspend fun deleteEmail(email: String): BaseResult<Boolean> = throw NotImplementedError()
        override suspend fun addPhone(phone: String): BaseResult<Boolean> = throw NotImplementedError()
        override suspend fun deletePhone(phone: String): BaseResult<Boolean> = throw NotImplementedError()
    }

    private class TestCreateTransactionInteractor : CreateTransactionInteractor {
        var pendingTransactions: List<PendingTransactionModel> = emptyList()
        var clearPendingTransactionsCalled = false

        override suspend fun createTransaction(transaction: CreateTransactionModel): BaseResult<TransactionModel> {
            throw NotImplementedError()
        }

        override suspend fun getPendingTransactions(): List<PendingTransactionModel> {
            return pendingTransactions
        }

        override suspend fun clearPendingTransactions() {
            clearPendingTransactionsCalled = true
            pendingTransactions = emptyList()
        }

        override suspend fun sendPendingTransactions(): Boolean {
            throw NotImplementedError()
        }
    }

    private class TestSharedPreferencesProvider : SharedPreferencesProvider {
        private var isTestEnvironmentEnabled = false

        override fun clearAllData() = Unit
        override fun removeItem(key: String) = Unit
        override fun getString(key: String): String? = null
        override fun saveString(key: String, value: String?) = Unit
        override fun getInt(key: String): Int = -1
        override fun saveInt(key: String, value: Int) = Unit
        override fun getBoolean(key: String, default: Boolean): Boolean = default
        override fun saveBoolean(key: String, value: Boolean) = Unit
        override fun isAuthorized(): Boolean = false
        override fun deleteAuthToken() = Unit
        override fun getAuthToken(): String? = null
        override fun saveAuthToken(value: String?) = Unit
        override fun getRefreshToken(): String? = null
        override fun saveRefreshToken(value: String?) = Unit
        override fun isTestEnvironmentEnabled(): Boolean = isTestEnvironmentEnabled
        override fun setTestEnvironmentEnabled(isEnabled: Boolean) {
            isTestEnvironmentEnabled = isEnabled
        }
        override fun getAccount(): AccountModel? = null
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
    private fun SettingsViewModel.binding(): SettingsContract.Binding {
        val field = CoreViewModel::class.java.getDeclaredField("binding")
        field.isAccessible = true
        return field.get(this) as SettingsContract.Binding
    }
}
