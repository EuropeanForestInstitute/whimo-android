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
package com.whimo.data.transactions.model.response

import com.whimo.data.base.common.Pagination
import com.whimo.data.commodity.model.response.Commodity
import com.whimo.data.harvestseasons.model.response.HarvestSeasonData

data class TransactionsResponse(
    val success: Boolean,
    val pagination: Pagination,
    val data: List<TransactionData>,
    val message: String
)

data class TransactionDetailsResponse(
    val success: Boolean,
    val data: TransactionData,
    val message: String
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)

data class TransactionData(
    val id: String,
    val created_at: String,
    val updated_at: String?,
    val expires_at: String?,
    val type: String,
    val status: String,
    val action: String,
    val location: String?,
    val latitude: Double?,
    val longitude: Double?,
    val transaction_latitude: Double?,
    val transaction_longitude: Double?,
    val farm_latitude: Double?,
    val farm_longitude: Double?,
    val commodity: Commodity,
    val volume: Float,
    val traceability: String?,
    val seller: User?,
    val buyer: User?,
    val is_buying_from_farmer: Boolean,
    val is_automatic: Boolean,
    val harvest_season: HarvestSeasonData?,
    val created_by_id: String?,
)

data class User(
    val id: String,
    val username: String,
    val gadgets: List<Gadget>?
)

data class Gadget(
    val id: String,
    val type: String,
    val identifier: String,
    val is_verified: Boolean
)
