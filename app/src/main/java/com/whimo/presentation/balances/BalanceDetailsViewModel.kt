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

import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.domain.transactions.TransactionsInteractor
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionsFilter
import com.whimo.domain.transactions.models.TransactionsState

class BalanceDetailsViewModel(
    private val interactor: TransactionsInteractor,
) : BaseViewModel<BalanceDetailsContract.Binding>() {

    private var args: BalanceDetailsArgs? = null
    private var sourceTransactions: List<TransactionModel> = emptyList()
    private var isLoading = false

    init {
        launch {
            interactor.stateFlow.collect { handleTransactionsState(it) }
        }
    }

    override fun createBinding(): BalanceDetailsContract.Binding {
        return BalanceDetailsContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is BalanceDetailsContract.Event.OnCreate -> onCreate(event.args)
        }
    }

    override fun copyBinding(binding: BalanceDetailsContract.Binding): BalanceDetailsContract.Binding {
        return binding.copy()
    }

    private fun onCreate(args: BalanceDetailsArgs?) {
        if (args == null) return

        this.args = args
        updateView()
        refresh()
    }

    private fun refresh() {
        val currentArgs = args ?: return

        launch {
            interactor.refresh(
                filter = TransactionsFilter(
                    commodity = currentArgs.commodity,
                    harvestSeason = currentArgs.harvestSeason,
                ),
                useCache = true,
            )
        }
    }

    private fun handleTransactionsState(state: TransactionsState) {
        when (state) {
            is TransactionsState.End -> updateSourceTransactions(state.transactions, loading = false)
            is TransactionsState.PageLoading -> updateSourceTransactions(state.transactions, loading = true)
            is TransactionsState.Success -> updateSourceTransactions(state.transactions, loading = false)
            TransactionsState.Empty -> updateSourceTransactions(emptyList(), loading = false)
            TransactionsState.Reloading -> {
                isLoading = true
                updateView()
            }
            is TransactionsState.Error -> {
                isLoading = false
                setEffect(BalanceDetailsContract.Effect.ShowMessage(state.errorMessage))
                updateView()
            }
        }
    }

    private fun updateSourceTransactions(
        transactions: List<TransactionModel>,
        loading: Boolean,
    ) {
        sourceTransactions = transactions
        isLoading = loading
        updateView()
    }

    private fun updateView() {
        updateBinding { b ->
            b.commodity = args?.commodity
            b.harvestSeason = args?.harvestSeason
            b.sourceTransactions = sourceTransactions
            b.traceabilityStatus = args?.traceabilityStatus
            b.lastActivity = sourceTransactions.latestBalanceTransaction()?.let { it.updatedDate ?: it.createdDate }
            b.isLoading = isLoading
        }
    }
}
