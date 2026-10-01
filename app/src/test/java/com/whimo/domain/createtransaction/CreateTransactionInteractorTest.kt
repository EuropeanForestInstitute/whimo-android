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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whimo.domain.createtransaction

import android.content.Context
import com.whimo.data.base.common.BaseResult
import com.whimo.data.createtransaction.model.entity.PendingTransactionEntity
import com.whimo.data.createtransaction.model.mappers.toEntity
import com.whimo.data.createtransaction.model.request.CreateDownstreamTransactionRequest
import com.whimo.data.createtransaction.model.request.CreateProducerTransactionRequest
import com.whimo.data.createtransaction.repository.CreateTransactionRepository
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.PendingTransactionsSyncProgress
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.extensions.isNetworkAvailable
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTransactionInteractorTest {

    @Test
    fun `createTransaction rejects downstream without harvest season before request or queue`() = runTest {
        val repository = TestCreateTransactionRepository()
        val interactor = CreateTransactionInteractorImpl(
            repository = repository,
            context = mockk<Context>(relaxed = true),
        )

        val result = interactor.createTransaction(
            CreateTransactionModel(
                action = TransactionAction.Buying,
                isProducerTransaction = false,
                commodity = commodity(),
                volume = 100f,
                harvestSeason = null,
            )
        )

        assertTrue(result is BaseResult.Error)
        assertFalse(repository.createProducerCalled)
        assertFalse(repository.createDownstreamCalled)
        assertFalse(repository.pendingTransactionAdded)
    }

    @Test
    fun `sendPendingTransactions reports progress and removes sent items`() = runTest {
        val context = mockk<Context>(relaxed = true)
        mockNetworkAvailable(context, true)
        try {
            val repository = TestCreateTransactionRepository(
                pendingTransactions = mutableListOf(
                    pendingEntity(id = 1),
                    pendingEntity(id = 2),
                )
            )
            val interactor = CreateTransactionInteractorImpl(
                repository = repository,
                context = context,
            )
            val progress = mutableListOf<PendingTransactionsSyncProgress>()

            val result = interactor.sendPendingTransactions { progress += it }

            assertTrue(result)
            assertEquals(
                listOf(
                    PendingTransactionsSyncProgress(processedTransactions = 0, totalTransactions = 2),
                    PendingTransactionsSyncProgress(processedTransactions = 1, totalTransactions = 2),
                    PendingTransactionsSyncProgress(processedTransactions = 2, totalTransactions = 2),
                ),
                progress,
            )
            assertEquals(2, repository.createDownstreamCalls)
            assertTrue(repository.pendingTransactions.isEmpty())
        } finally {
            unmockNetworkAvailable()
        }
    }

    @Test
    fun `sendPendingTransactions does not report progress while offline`() = runTest {
        val context = mockk<Context>(relaxed = true)
        mockNetworkAvailable(context, false)
        try {
            val repository = TestCreateTransactionRepository(
                pendingTransactions = mutableListOf(pendingEntity(id = 1))
            )
            val interactor = CreateTransactionInteractorImpl(
                repository = repository,
                context = context,
            )
            val progress = mutableListOf<PendingTransactionsSyncProgress>()

            val result = interactor.sendPendingTransactions { progress += it }

            assertFalse(result)
            assertTrue(progress.isEmpty())
            assertEquals(0, repository.createDownstreamCalls)
            assertEquals(1, repository.pendingTransactions.size)
        } finally {
            unmockNetworkAvailable()
        }
    }

    private class TestCreateTransactionRepository(
        val pendingTransactions: MutableList<PendingTransactionEntity> = mutableListOf(),
    ) : CreateTransactionRepository {
        var createProducerCalled = false
        var createDownstreamCalled = false
        var createDownstreamCalls = 0
        var pendingTransactionAdded = false

        override suspend fun createProducerTransaction(
            request: CreateProducerTransactionRequest,
        ): BaseResult<TransactionModel> {
            createProducerCalled = true
            return BaseResult.Success(null)
        }

        override suspend fun createDownstreamTransaction(
            request: CreateDownstreamTransactionRequest,
        ): BaseResult<TransactionModel> {
            createDownstreamCalled = true
            createDownstreamCalls += 1
            return BaseResult.Success(null)
        }

        override suspend fun getPendingTransactions(): List<PendingTransactionEntity> {
            return pendingTransactions.toList()
        }

        override suspend fun addPendingTransaction(transaction: PendingTransactionEntity) {
            pendingTransactionAdded = true
        }

        override suspend fun removePendingTransaction(transaction: PendingTransactionEntity) {
            pendingTransactions.remove(transaction)
        }

        override suspend fun removePendingTransaction(transactionId: Long) {
            pendingTransactions.removeAll { it.id == transactionId }
        }

        override suspend fun clearPendingTransactions() {
            pendingTransactions.clear()
        }
    }

    companion object {
        private const val EXTENSIONS_CLASS_NAME = "com.whimo.extensions.ContextExtKt"

        private fun mockNetworkAvailable(context: Context, isAvailable: Boolean) {
            mockkStatic(EXTENSIONS_CLASS_NAME)
            every { context.isNetworkAvailable() } returns isAvailable
        }

        private fun unmockNetworkAvailable() {
            unmockkStatic(EXTENSIONS_CLASS_NAME)
        }

        private fun pendingEntity(id: Long): PendingTransactionEntity {
            return validDownstreamTransaction().toEntity().copy(id = id)
        }

        private fun validDownstreamTransaction(): CreateTransactionModel {
            return CreateTransactionModel(
                action = TransactionAction.Buying,
                isProducerTransaction = false,
                commodity = commodity(),
                volume = 100f,
                harvestSeason = HarvestSeasonModel(id = "season-id"),
            )
        }

        private fun commodity(): CommodityModel {
            return CommodityModel(
                id = "commodity-id",
                code = "1801",
                name = "Cocoa beans",
                unit = "kg",
                hasRecipe = false,
                group = CommodityGroupModel(
                    id = "group-id",
                    name = "Cocoa",
                    commodities = null,
                ),
                balance = 1000f,
            )
        }
    }
}
