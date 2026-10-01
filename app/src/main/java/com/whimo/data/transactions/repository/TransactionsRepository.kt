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
package com.whimo.data.transactions.repository

import com.whimo.data.base.common.BaseResult
import com.whimo.data.transactions.model.mappers.toDomain
import com.whimo.data.transactions.model.mappers.toEntity
import com.whimo.data.transactions.model.request.UpdateTransactionStatusRequest
import com.whimo.data.transactions.service.TraceabilityCountsDao
import com.whimo.data.transactions.service.TransactionsDao
import com.whimo.data.transactions.service.TransactionsService
import com.whimo.domain.common.PaginationModel
import com.whimo.domain.transactions.models.TraceabilityCountsModel
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.getCommodityFullText
import com.whimo.network.handleResponse
import com.whimo.network.mapResult
import java.time.LocalDateTime

interface TransactionsRepository {
    suspend fun getTransactions(
        search: String?,
        page: Int,
        pageSize: Int,
        status: String?,
        action: String?,
        createdAtFrom: String?,
        createdAtTo: String?,
        commodityGroupId: String?,
        commodityId: String?,
        buyerId: String?,
        harvestSeasonId: String?,
    ): BaseResult<Pair<PaginationModel, List<TransactionModel>>>

    suspend fun getTransactionsFromDB(
        search: String?,
        status: String?,
        action: String?,
        createdAtFrom: LocalDateTime?,
        createdAtTo: LocalDateTime?,
        commodityGroupId: String?,
        commodityId: String?,
        buyerId: String?,
        harvestSeasonId: String?,
    ): List<TransactionModel>

    suspend fun updateTransactionsDB(items: List<TransactionModel>?)

    suspend fun getTransactionDetails(transactionId: String): BaseResult<TransactionModel>

    suspend fun getTransactionFromDB(transactionId: String?): TransactionModel?

    suspend fun saveTransactionDB(transactionModel: TransactionModel)

    suspend fun getTransactionTraceabilityCounts(
        transactionId: String,
    ): BaseResult<TraceabilityCountsModel>

    suspend fun getTraceabilityCountsFromDB(
        transactionId: String,
    ): TraceabilityCountsModel?

    suspend fun saveTraceabilityCountsToDB(
        transactionId: String,
        traceabilityCounts: TraceabilityCountsModel,
    )

    suspend fun updateTransactionStatus(
        transactionId: String,
        request: UpdateTransactionStatusRequest,
    ): BaseResult<Boolean>

    suspend fun resendNotification(transactionId: String): BaseResult<Boolean>
}

class TransactionsRepositoryImpl(
    private val service: TransactionsService,
    private val transactionsDao: TransactionsDao,
    private val traceabilityCountsDao: TraceabilityCountsDao,
) : TransactionsRepository {

    override suspend fun getTransactions(
        search: String?,
        page: Int,
        pageSize: Int,
        status: String?,
        action: String?,
        createdAtFrom: String?,
        createdAtTo: String?,
        commodityGroupId: String?,
        commodityId: String?,
        buyerId: String?,
        harvestSeasonId: String?,
    ): BaseResult<Pair<PaginationModel, List<TransactionModel>>> {
        return handleResponse {
            service.getTransactions(
                search = search,
                page = page,
                pageSize = pageSize,
                status = status,
                action = action,
                createdAtFrom = createdAtFrom,
                createdAtTo = createdAtTo,
                commodityGroupId = commodityGroupId,
                commodityId = commodityId,
                buyerId = buyerId,
                harvestSeasonId = harvestSeasonId,
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun getTransactionsFromDB(
        search: String?,
        status: String?,
        action: String?,
        createdAtFrom: LocalDateTime?,
        createdAtTo: LocalDateTime?,
        commodityGroupId: String?,
        commodityId: String?,
        buyerId: String?,
        harvestSeasonId: String?,
    ): List<TransactionModel> {
        return transactionsDao.getAll(status = status, action = action)
            .map { it.toDomain() }
            .filter {
                val validSearch = search == null
                        || it.getCommodityFullText().contains(search, true)
                        || it.commodity.group?.name == null
                        || it.commodity.group.name.contains(search, true)
                val validCommodityGroup = commodityGroupId == null || it.commodity.group?.id == commodityGroupId
                val validCommodity = commodityId == null || it.commodity.id == commodityId
                val validHarvestSeason = harvestSeasonId == null || it.harvestSeason?.id == harvestSeasonId
                val validStartDate = createdAtFrom == null || it.createdDate.isAfter(createdAtFrom)
                val validEndDate = createdAtTo == null || it.createdDate.isBefore(createdAtTo)

                validSearch && validCommodityGroup && validCommodity && validHarvestSeason && validStartDate && validEndDate
            }
    }

    override suspend fun updateTransactionsDB(items: List<TransactionModel>?) {
        transactionsDao.clearAll()
        if (items != null) {
            transactionsDao.insertAll(items.map { it.toEntity() })
        }
    }

    override suspend fun getTransactionDetails(transactionId: String): BaseResult<TransactionModel> {
        return handleResponse {
            service.getTransactionDetails(transactionId = transactionId)
        }.mapResult { it?.toDomain() }
    }

    override suspend fun getTransactionFromDB(transactionId: String?): TransactionModel? {
        return transactionsDao.get(id = transactionId)?.toDomain()
    }

    override suspend fun saveTransactionDB(transactionModel: TransactionModel) {
        transactionsDao.insert(transactionModel.toEntity())
    }

    override suspend fun getTransactionTraceabilityCounts(transactionId: String): BaseResult<TraceabilityCountsModel> {
        return handleResponse {
            service.getTransactionTraceabilityCounts(
                transactionId = transactionId,
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun getTraceabilityCountsFromDB(transactionId: String): TraceabilityCountsModel? {
        return traceabilityCountsDao.get(transactionId)?.toDomain()
    }

    override suspend fun saveTraceabilityCountsToDB(
        transactionId: String,
        traceabilityCounts: TraceabilityCountsModel
    ) {
        traceabilityCountsDao.insert(traceabilityCounts.toEntity(transactionId))
    }

    override suspend fun updateTransactionStatus(
        transactionId: String,
        request: UpdateTransactionStatusRequest
    ): BaseResult<Boolean> {
        return handleResponse {
            service.updateTransactionStatus(
                transactionId = transactionId,
                request = request
            )
        }.mapResult { it?.success }
    }

    override suspend fun resendNotification(transactionId: String): BaseResult<Boolean> {
        return handleResponse {
            service.resendNotification(transactionId = transactionId)
        }.mapResult { it?.success }
    }
}
