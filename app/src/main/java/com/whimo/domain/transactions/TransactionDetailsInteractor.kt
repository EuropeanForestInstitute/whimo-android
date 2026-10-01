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

import com.whimo.data.base.common.BaseResult
import com.whimo.data.transactions.model.request.UpdateTransactionStatusRequest
import com.whimo.data.transactions.repository.TransactionsRepository
import com.whimo.domain.transactions.models.TraceabilityCountsModel
import com.whimo.domain.transactions.models.TransactionModel

interface TransactionDetailsInteractor {
    suspend fun getTransactions(): List<TransactionModel>
    suspend fun getTransactionDetails(transactionId: String): BaseResult<TransactionModel>
    suspend fun getTransactionDetailsFromDB(transactionId: String): TransactionModel?
    suspend fun saveTransactionDetailsToDB(transactionModel: TransactionModel)

    suspend fun getTransactionTraceability(transactionId: String): BaseResult<TraceabilityCountsModel>
    suspend fun getTraceabilityCountsFromDB(transactionId: String): TraceabilityCountsModel?
    suspend fun saveTraceabilityCountsToDB(transactionId: String, traceabilityCounts: TraceabilityCountsModel)

    suspend fun updateTransactionStatus(transactionId: String, status: String): BaseResult<Boolean>
    suspend fun resendNotification(transactionId: String): BaseResult<Boolean>
}

class TransactionDetailsInteractorImpl(
    private val repository: TransactionsRepository,
) : TransactionDetailsInteractor {
    override suspend fun getTransactions(): List<TransactionModel> {
        return repository.getTransactionsFromDB(
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
    }

    override suspend fun getTransactionDetails(transactionId: String): BaseResult<TransactionModel> {
        return repository.getTransactionDetails(transactionId = transactionId)
    }

    override suspend fun getTransactionDetailsFromDB(transactionId: String): TransactionModel? {
        return repository.getTransactionFromDB(transactionId = transactionId)
    }

    override suspend fun saveTransactionDetailsToDB(transactionModel: TransactionModel) {
        repository.saveTransactionDB(transactionModel = transactionModel)
    }

    override suspend fun getTransactionTraceability(transactionId: String): BaseResult<TraceabilityCountsModel> {
        return repository.getTransactionTraceabilityCounts(transactionId = transactionId)
    }

    override suspend fun getTraceabilityCountsFromDB(transactionId: String): TraceabilityCountsModel? {
        return repository.getTraceabilityCountsFromDB(transactionId)
    }

    override suspend fun saveTraceabilityCountsToDB(
        transactionId: String,
        traceabilityCounts: TraceabilityCountsModel
    ) {
        repository.saveTraceabilityCountsToDB(transactionId, traceabilityCounts)
    }

    override suspend fun updateTransactionStatus(
        transactionId: String,
        status: String
    ): BaseResult<Boolean> {
        return repository.updateTransactionStatus(
            transactionId = transactionId,
            request = UpdateTransactionStatusRequest(status),
        )
    }

    override suspend fun resendNotification(transactionId: String): BaseResult<Boolean> {
        return repository.resendNotification(transactionId = transactionId)
    }
}
