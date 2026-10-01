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
package com.whimo.presentation.createtransaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.createtransaction.models.MFile
import com.whimo.domain.createtransaction.models.UserInfoModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TransactionAction

class SharedTransactionViewModel : ViewModel() {
    var transaction by mutableStateOf(CreateTransactionModel())
        private set

    fun setAction(action: TransactionAction) {
        transaction = transaction.copy(action = action)
    }

    fun setIsProducerTransaction(isProducerTransaction: Boolean) {
        transaction = transaction.copy(isProducerTransaction = isProducerTransaction)
    }

    fun setIsBuyingFromFarmer(isBuyingFromFarmer: Boolean) {
        transaction = transaction.copy(isBuyingFromFarmer = isBuyingFromFarmer)
    }

    fun setIsOnFarm(isOnFarm: Boolean) {
        transaction = transaction.copy(isOnFarm = isOnFarm)
    }

    fun validateLocationProvider() {
        val clearLocation = if (!transaction.isProducerTransaction) {
            true
        } else {
            if (transaction.isBuyingFromFarmer) {
                transaction.locationProvider == LocationProvider.File ||
                        (!transaction.isOnFarm && transaction.locationProvider == LocationProvider.GPS)
            } else {
                transaction.locationProvider != LocationProvider.File
            }
        }

        if (clearLocation) {
            setLocationProvider(null)
            setLocation(null)
            setQR(null)
            setFile(null)
        }
    }

    fun setLocationProvider(locationProvider: LocationProvider?) {
        transaction = transaction.copy(locationProvider = locationProvider)
    }

    fun setLocation(location: LatLng?) {
        transaction = transaction.copy(location = location)
    }

    fun setFile(file: MFile?) {
        val locationProvider = if (file != null) LocationProvider.File else null

        transaction = transaction.copy(
            locationProvider = locationProvider,
            file = file,
        )
    }

    fun setQR(qr: String?) {
        if (qr == null) {
            transaction = transaction.copy(
                locationProvider = null,
                qr = null,
                location = null,
            )
            return
        }

        transaction = transaction.copy(
            locationProvider = LocationProvider.QR,
            qr = qr,
        )
    }

    fun setVolume(volume: Float?) {
        transaction = transaction.copy(volume = volume)
    }

    fun setCommodityVolume(
        volume: Float?,
        harvestSeason: HarvestSeasonModel?,
        commodity: CommodityModel?,
    ) {
        transaction = transaction.copy(
            commodity = commodity ?: transaction.commodity,
            volume = volume,
            harvestSeason = harvestSeason,
        )
    }

    fun setCommodity(commodity: CommodityModel?) {
        transaction = if (transaction.commodity?.id == commodity?.id) {
            transaction.copy(commodity = commodity)
        } else {
            transaction.copy(
                commodity = commodity,
                volume = null,
                harvestSeason = null,
            )
        }
    }

    fun setUserInfo(userInfo: UserInfoModel?) {
        transaction = transaction.copy(userInfo = userInfo)
    }
}
