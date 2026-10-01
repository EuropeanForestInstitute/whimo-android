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

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.whimo.data.base.common.BaseResult
import com.whimo.data.transactions.repository.TransactionsRepository
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.common.PaginationModel
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.TransactionType
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState
import com.whimo.network.ErrorHandler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TransactionsInteractorImplTest {

    private lateinit var repository: TransactionsRepository
    private lateinit var interactor: TransactionsInteractorImpl

    @Before
    fun setUp() {
        repository = mockk()
        interactor = TransactionsInteractorImpl(
            repository = repository,
            context = networkContext(),
            errorHandler = mockk<ErrorHandler>(relaxed = true),
        )
    }

    @Test
    fun `refresh drops duplicate transactions from remote response`() = runTest {
        val transaction = transaction(id = "same-transaction-id")

        coEvery {
            repository.getTransactionsFromDB(
                search = null,
                status = null,
                action = null,
                createdAtFrom = null,
                createdAtTo = null,
                commodityGroupId = null,
                commodityId = null,
                buyerId = null,
                harvestSeasonId = null,
            )
        } returns emptyList()
        coEvery {
            repository.getTransactions(
                search = null,
                page = 1,
                pageSize = 20,
                status = null,
                action = null,
                createdAtFrom = null,
                createdAtTo = null,
                commodityGroupId = null,
                commodityId = null,
                buyerId = null,
                harvestSeasonId = null,
            )
        } returns BaseResult.Success(pagination(count = 2) to listOf(transaction, transaction))
        coEvery { repository.updateTransactionsDB(any()) } returns Unit

        interactor.refresh(TransactionsFilter())

        val state = interactor.stateFlow.value as TransactionsState.End
        assertEquals(listOf(transaction), state.transactions)
    }

    private fun networkContext(): Context {
        val context = mockk<Context>()
        val connectivityManager = mockk<ConnectivityManager>()
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        return context
    }

    private fun pagination(count: Int): PaginationModel {
        return PaginationModel(
            count = count,
            page = 1,
            pageSize = 20,
            nextPage = null,
            previousPage = null,
            totalPages = 1,
        )
    }

    private fun transaction(id: String): TransactionModel {
        val group = CommodityGroupModel(
            id = "group-id",
            name = "Cocoa",
            commodities = emptyList(),
        )
        val commodity = CommodityModel(
            id = "commodity-id",
            code = "1801",
            name = "Cocoa beans",
            unit = "kg",
            hasRecipe = false,
            group = group,
            balance = null,
        )

        return TransactionModel(
            id = id,
            createdDate = LocalDateTime.of(2026, 9, 14, 10, 0),
            updatedDate = null,
            expiresDate = null,
            type = TransactionType.Downstream,
            status = TransactionStatus.Pending,
            action = TransactionAction.Buying,
            locationProvider = null,
            location = null,
            commodity = commodity,
            volume = 100f,
            traceability = TraceabilityStatus.Full,
            seller = null,
            buyer = null,
            isBuyingFromFarmer = false,
            isAutomatic = false,
            createdById = null,
            harvestSeason = null,
        )
    }
}
