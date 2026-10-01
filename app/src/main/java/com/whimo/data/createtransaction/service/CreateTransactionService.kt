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
package com.whimo.data.createtransaction.service

import com.whimo.data.createtransaction.model.response.CreateTransactionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface  CreateTransactionService {
    @Multipart
    @POST("transactions/producer/")
    suspend fun createProducerTransaction(
        @Part("commodity_id") commodityId: RequestBody?,
        @Part("volume") volume: RequestBody?,
        @Part("location") locationProvider: RequestBody?,
        @Part("farm_latitude") farmLatitude: RequestBody?,
        @Part("farm_longitude") farmLongitude: RequestBody?,
        @Part("transaction_latitude") transactionLatitude: RequestBody?,
        @Part("transaction_longitude") transactionLongitude: RequestBody?,
        @Part file: MultipartBody.Part?,
        @Part("is_buying_from_farmer") isBuyingFromFarmer: RequestBody?,
        @Part("recipient") recipient: RequestBody?,
        @Part("harvest_season_id") harvestSeasonId: RequestBody?,
    ): Response<CreateTransactionResponse>

    @Multipart
    @POST("transactions/downstream/")
    suspend fun createDownstreamTransaction(
        @Part("commodity_id") commodityId: RequestBody?,
        @Part("volume") volume: RequestBody?,
        @Part("transaction_latitude") transactionLatitude: RequestBody?,
        @Part("transaction_longitude") transactionLongitude: RequestBody?,
        @Part("action") action: RequestBody?,
        @Part("recipient") recipient: RequestBody?,
        @Part("harvest_season_id") harvestSeasonId: RequestBody?,
    ): Response<CreateTransactionResponse>
}
