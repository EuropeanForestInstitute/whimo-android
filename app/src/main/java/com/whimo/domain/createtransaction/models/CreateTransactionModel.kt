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
package com.whimo.domain.createtransaction.models

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.extensions.toText
import com.whimo.extensions.toQuantityText
import com.whimo.utils.stringOrNull
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateTransactionModel(
    val action: TransactionAction? = null,
    val isProducerTransaction: Boolean = false,
    val isBuyingFromFarmer: Boolean = false,
    val isOnFarm: Boolean = false,
    val locationProvider: LocationProvider? = null,
    val location: LatLng? = null,
    val file: MFile? = null,
    val qr: String? = null,
    val commodity: CommodityModel? = null,
    val volume: Float? = null,
    val userInfo: UserInfoModel? = null,
    val creationLocation: LatLng? = null,
    val harvestSeason: HarvestSeasonModel? = null,
) : Parcelable

enum class LocationProvider(val providerName: String) {
    GPS("gps"),
    QR("qr"),
    Manual("manual"),
    File("file"),
}

@Parcelize
data class MFile(
    var uri: Uri? = null,
    var name: String? = null,
    var size: Long? = null,
) : Parcelable

@Parcelize
data class UserInfoModel(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
) : Parcelable

fun CreateTransactionModel?.getLocationText(): String? {
    return when(this?.locationProvider) {
        LocationProvider.GPS, LocationProvider.QR, LocationProvider.Manual -> {
            "${locationProvider.providerName}: ${location?.toText()}"
        }

        LocationProvider.File -> {
            "${locationProvider.providerName}: ${file?.name} ${file?.size}b"
        }

        else -> null
    }
}

fun CreateTransactionModel?.getCommodityText(): String? {
    if (this?.commodity == null) return null

    return "${commodity.code} ${commodity.name}"
}

fun CreateTransactionModel?.getCommodityVolumeText(): String? {
    if (this?.volume == null || commodity == null) return null

    return "${volume.toQuantityText()} ${commodity.unit}"
}

fun CreateTransactionModel?.getCommodityFullText(): String? {
    if (this?.volume == null || commodity == null) return null

    return "${getCommodityText()}, ${getCommodityVolumeText()}"
}

fun CreateTransactionModel?.getCommodityShortText(): String? {
    if (this?.volume == null || commodity == null) return null

    return "${commodity.group?.name}, ${getCommodityVolumeText()}"
}

fun CreateTransactionModel?.getUserInfoText(): String? {
    if (this?.userInfo == null) return null

    return userInfo.name.stringOrNull() ?: userInfo.email.stringOrNull() ?: userInfo.phone.stringOrNull()
}
