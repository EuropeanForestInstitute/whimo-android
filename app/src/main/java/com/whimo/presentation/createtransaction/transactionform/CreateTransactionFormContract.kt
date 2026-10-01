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
package com.whimo.presentation.createtransaction.transactionform

import android.content.Context
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus

object CreateTransactionFormContract {
    data class Binding(
        var toolbarTitle: String = "Producer transaction",
        var farmGeoDataVisible: Boolean = true,
        var farmGeoDataText: String = "Tap to add data",
        var commodityTypeText: String = "Tap to add data",
        var commodityVolumeText: String = "Tap to add data",
        var commodityVolumeBreakdown: CommodityVolumeBreakdown? = null,
        var commodityVolumeHarvestSeasonText: String? = null,
        var commodityVolumeHarvestSeasonStatus: HarvestSeasonStatus? = null,
        var userInfoVisible: Boolean = true,
        var userInfoTitle: String = "Supplier info",
        var userInfoText: String = "Tap to add data",
        var inviteUserVisible: Boolean = true,
        var inviteUserText: String = "Tap to invite a user to WHIMO",
        var buttonEnabled: Boolean = false,
        var isTestEnvironmentEnabled: Boolean = false,
    ) : CoreViewBinding

    data class CommodityVolumeBreakdown(
        val harvestSeasonAmountText: String,
        val harvestSeasonText: String,
        val harvestSeasonStatus: HarvestSeasonStatus?,
        val automaticTransactionAmountText: String,
        val automaticTransactionStatus: HarvestSeasonStatus?,
    )

    sealed class Event : CoreViewEvent {
        data class OnCreate(val context: Context, val transaction: CreateTransactionModel) : Event()
        data object ValidateTransaction : Event()
        data class CreateTransaction(val context: Context) : Event()
        data object OnFarmGeoDataClick : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data class ToggleLoader(val isLoading: Boolean): Effect()
        data class ShowError(val message: String): Effect()
        data object ShowConfirmDialog: Effect()
        data object ShowGeoDataDialog: Effect()
        data object CreateTransactionSuccess: Effect()
        data class NavigateFarmGeoData(val transaction: CreateTransactionModel): Effect()
        data object RequestLocationPermission: Effect()
    }
}
