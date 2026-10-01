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
package com.whimo.presentation.transactions.transactiondetails

import com.google.android.gms.maps.model.LatLng
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.TraceabilityCountsModel
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.UserModel
import com.whimo.presentation.transactions.transactiondetails.components.PieChartItem

enum class EditLocationType {
    All, UploadFile, Map, QR
}

object TransactionDetailsContract {
    data class Binding(
        var transactionDetails: TransactionModel? = null,
        var traceabilityCounts: TraceabilityCountsModel? = null,

        var toolbarTitle: String = "",
        var commodityText: String? = null,
        var commodityWarningText: String? = null,
        var harvestSeason: HarvestSeasonModel? = null,

        var showLocation: Boolean = false,
        var locationProvider: LocationProvider? = null,
        var location: LatLng? = null,
        var editLocationType: EditLocationType = EditLocationType.All,

        var traceability: TraceabilityStatus? = null,
        var chartItems: List<PieChartItem> = emptyList(),

        var accountId: String? = null,
        var buyer: UserModel? = null,
        var seller: UserModel? = null,

        var status: TransactionStatus? = null,

        var createdDateText: String? = null,
        var expiryDateText: String? = null,

        var showInitiatorActionButtons: Boolean = false,
        var showRecipientActionButtons: Boolean = false,
        var acceptEnabled: Boolean = true,
        var acceptBlocked: Boolean = false,
        var acceptBalanceLoading: Boolean = false,
        var acceptWarningText: String? = null,
        var acceptBlockedDialogText: String = "",

        var downloadEnabled: Boolean = false,
        var dialogDescription: String = "",

    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data class OnCreate(val transactionModel: TransactionModel) : Event()
        data object Refresh : Event()
        data object AddGeoDataClicked : Event()
        data object AcceptTransaction : Event()
        data object RejectTransaction : Event()
        data object ResendNotification : Event()
        data object OnInitialSuppliersHistoryClick : Event()
        data class OnSuppliersHistoryClick(val transaction: TransactionModel) : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data class NavigateInitialSupplierHistory(val transaction: TransactionModel) : Effect()
        data class NavigateSupplierHistory(val transaction: TransactionModel) : Effect()
        data class NavigateAddGeolocation(val transaction: TransactionModel) : Effect()
        data object StatusChanged : Effect()
        data class RefreshHistory(val transaction: TransactionModel) : Effect()

        data class ToggleResendLoader(val isLoading: Boolean) : Effect()
        data class ToggleRejectLoader(val isLoading: Boolean) : Effect()
        data class ToggleAcceptLoader(val isLoading: Boolean) : Effect()
        data class ShowMessage(val message: String) : Effect()
    }
}
