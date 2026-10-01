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
import com.whimo.data.base.common.BaseResult
import com.whimo.data.transactions.repository.TransactionsRepository
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState
import com.whimo.domain.transactions.models.allFieldsNull
import com.whimo.extensions.isNetworkAvailable
import com.whimo.extensions.toUtcIsoString
import com.whimo.network.ErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TransactionsInteractor {
    val stateFlow: SharedFlow<TransactionsState>

    suspend fun refresh(filter: TransactionsFilter, useCache: Boolean = true)
    suspend fun loadNextPage(filter: TransactionsFilter)
}

class TransactionsInteractorImpl(
    private val repository: TransactionsRepository,
    private val context: Context,
    private val errorHandler: ErrorHandler,
) : TransactionsInteractor {

    private val transactions = mutableListOf<TransactionModel>()
    private var page = FIRST_PAGE
    private var requestVersion = 0

    private var state = MutableStateFlow<TransactionsState>(TransactionsState.Empty)

    override val stateFlow: StateFlow<TransactionsState>
        get() = state.asStateFlow()

    override suspend fun refresh(filter: TransactionsFilter, useCache: Boolean) {
        val currentRequestVersion = ++requestVersion

        if (useCache) {
            val dbTransactions = repository.getTransactionsFromDB(
                search = filter.query,
                status = filter.status?.statusName,
                action = filter.action?.actionName,
                createdAtFrom = filter.dateStart,
                createdAtTo = filter.dateEnd,
                commodityGroupId = filter.commodityGroup?.id,
                commodityId = filter.commodity?.id,
                buyerId = filter.user?.id,
                harvestSeasonId = filter.harvestSeason?.id,
            )
            transactions.clear()
            transactions.addAll(dbTransactions.filterByHarvestSeason(filter.harvestSeason))
        }

        state.emit(TransactionsState.Reloading)
        delay(300)
        if (currentRequestVersion != requestVersion) return

        if (context.isNetworkAvailable()) {
            transactions.clear()
            loadPage(FIRST_PAGE, filter, currentRequestVersion)?.let { state.emit(it) }

        } else {
            if (transactions.isEmpty()) {
                state.emit(TransactionsState.Empty)
            } else {
                state.emit(TransactionsState.End(transactions))
            }
        }
    }

    override suspend fun loadNextPage(filter: TransactionsFilter) {
        val currentRequestVersion = requestVersion
        state.emit(TransactionsState.PageLoading(transactions))
        loadPage(page + 1, filter, currentRequestVersion)?.let { state.emit(it) }
    }

    private suspend fun loadPage(
        page: Int,
        filter: TransactionsFilter,
        currentRequestVersion: Int,
    ): TransactionsState? {
        val result = repository.getTransactions(
            search = filter.query,
            page = page,
            pageSize = DEFAULT_PAGE_SIZE,
            status = filter.status?.statusName,
            action = filter.action?.actionName,
            createdAtFrom = filter.dateStart?.toUtcIsoString(),
            createdAtTo = filter.dateEnd?.toUtcIsoString(),
            commodityGroupId = filter.commodityGroup?.id,
            commodityId = filter.commodity?.id,
            buyerId = filter.user?.id,
            harvestSeasonId = filter.harvestSeason?.id,
        )
        if (currentRequestVersion != requestVersion) return null

        if (result is BaseResult.Success && result.data != null) {
            transactions.addAllMissing(result.data.second.filterByHarvestSeason(filter.harvestSeason))

            if (filter.allFieldsNull()) {
                repository.updateTransactionsDB(result.data.second)
            }

            this.page = page

            return if (page == result.data.first.totalPages) {
                if (transactions.isEmpty()) {
                    TransactionsState.Empty
                } else {
                    TransactionsState.End(transactions)
                }
            } else {
                TransactionsState.Success(transactions)
            }
        }

        if (result is BaseResult.Error) {
            val errorMessage = errorHandler.parseError(result.exception)
            return TransactionsState.Error(errorMessage)
        }

        return TransactionsState.Error("unexpected")
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 20
    }
}

private fun MutableList<TransactionModel>.addAllMissing(items: List<TransactionModel>) {
    val existingIds = map { it.id }.toMutableSet()
    items.forEach { transaction ->
        if (existingIds.add(transaction.id)) {
            add(transaction)
        }
    }
}

private fun List<TransactionModel>.filterByHarvestSeason(
    harvestSeason: HarvestSeasonModel?,
): List<TransactionModel> {
    if (harvestSeason == null) return this

    return filter { transaction ->
        val transactionSeason = transaction.harvestSeason
        when {
            transactionSeason?.id == harvestSeason.id -> true
            transactionSeason?.name != null -> transactionSeason.name == harvestSeason.name
            else -> false
        }
    }
}
