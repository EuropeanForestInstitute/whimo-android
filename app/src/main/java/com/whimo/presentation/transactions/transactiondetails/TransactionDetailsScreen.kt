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

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.UserModel
import com.whimo.domain.transactions.models.getAccountText
import com.whimo.domain.transactions.models.getShortName
import com.whimo.extensions.findActivity
import com.whimo.extensions.toShortFormattedDateString
import com.whimo.navigation.Screens
import com.whimo.presentation.createtransaction.components.CreateTransactionWarning
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity
import com.whimo.presentation.main.components.LoadingState
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.settings.components.BottomSheetBase
import com.whimo.presentation.settings.components.DialogButtonsItem
import com.whimo.presentation.settings.components.SettingsOptionItemBase
import com.whimo.presentation.transactions.transactiondetails.components.BaseDialog
import com.whimo.presentation.transactions.transactiondetails.components.DialogTextItem
import com.whimo.presentation.transactions.transactiondetails.components.SectionHeader
import com.whimo.presentation.transactions.transactiondetails.components.SupplierHistoryItem
import com.whimo.presentation.transactions.transactiondetails.components.TitleDescriptionView
import com.whimo.presentation.transactions.transactiondetails.components.TraceabilityStatusesDialog
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoItem1
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoItem2
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoItem3
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoHarvestSeasonItem
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoStatusItem
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoTraceabilityItem
import com.whimo.presentation.transactions.transactiondetails.components.TransactionStatusDialog
import com.whimo.presentation.transactions.transactiondetails.components.UserInfoDialog
import com.whimo.presentation.ui.baseScreen.LightLoadingButton
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.theme.ColorGray30
import com.whimo.presentation.ui.theme.ColorHarvestSeasonActiveBackground
import com.whimo.presentation.ui.theme.ColorHarvestSeasonActiveContent
import com.whimo.presentation.ui.theme.ColorHarvestSeasonPastBackground
import com.whimo.presentation.ui.theme.ColorHarvestSeasonPastContent
import com.whimo.presentation.ui.theme.ColorLightOrange
import com.whimo.presentation.ui.theme.ColorWarning
import com.whimo.presentation.ui.theme.ColorWarning10
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.toJsonArgs
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        TransactionDetailsScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            transactionModel = null,
            viewModel = null,
            historyViewModel = null,
        )
    }
}

private sealed class TransactionDetailsDialog {
    data object None : TransactionDetailsDialog()
    data class TraceabilityStatusDialog(val status: TraceabilityStatus?) : TransactionDetailsDialog()
    data class SellerInfo(val userModel: UserModel) : TransactionDetailsDialog()
    data class BuyerInfo(val userModel: UserModel) : TransactionDetailsDialog()
    data class TransactionStatusDialog(val status: TransactionStatus) : TransactionDetailsDialog()
    data class HarvestSeasonInfo(val season: HarvestSeasonModel) : TransactionDetailsDialog()
    data class AcceptTransactionBlocked(val description: String) : TransactionDetailsDialog()
    data class DownloadDetailsDialog(val description: String) : TransactionDetailsDialog()
    data object DownloadBundleDialog : TransactionDetailsDialog()
}

@Composable
fun TransactionDetailsScreen(
    modifier: Modifier,
    navController: NavHostController,
    transactionModel: TransactionModel?,
    viewModel: TransactionDetailsViewModel? = koinViewModel(),
    historyViewModel: SuppliersHistoryViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: TransactionDetailsContract.Binding()
    val historyBinding = historyViewModel?.observeViewBinding() ?: SuppliersHistoryContract.Binding()
    val context = LocalContext.current

    val createZipFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            historyViewModel?.setEvent(SuppliersHistoryContract.Event.SaveToFile(context, uri))
        }
    }

    val createCSVFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            historyViewModel?.setEvent(SuppliersHistoryContract.Event.SaveToFile(context, uri))
        }
    }

    val createTextFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            historyViewModel?.setEvent(SuppliersHistoryContract.Event.SaveToFile(context, uri))
        }
    }

    var resendIsLoading by remember { mutableStateOf(false) }
    var rejectIsLoading by remember { mutableStateOf(false) }
    var acceptIsLoading by remember { mutableStateOf(false) }

    var downloadLocationsLoading by remember { mutableStateOf(false) }
    var showDownloadDataBottomSheet by remember { mutableStateOf(false) }

    var dialog by remember { mutableStateOf<TransactionDetailsDialog>(TransactionDetailsDialog.None) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel?.setEvent(TransactionDetailsContract.Event.Refresh)
                val activity = context.findActivity()
                activity.setResult(Activity.RESULT_OK)
            }
        }
    )

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is TransactionDetailsContract.Effect.NavigateInitialSupplierHistory -> {
                    navController.navigate(
                        Screens.InitialSuppliersHistory.putArgs(Screens.ARG_KEY_JSON to effect.transaction.toJsonArgs())
                    )
                }
                is TransactionDetailsContract.Effect.NavigateSupplierHistory -> {
                    navController.navigate(
                        Screens.SuppliersHistory.putArgs(Screens.ARG_KEY_JSON to effect.transaction.toJsonArgs())
                    )
                }
                is TransactionDetailsContract.Effect.NavigateAddGeolocation -> {
                    FarmGeoDataActivity.openFarmGeoData(context, launcher, effect.transaction)
                }
                is TransactionDetailsContract.Effect.StatusChanged -> {
                    val activity = context.findActivity()
                    activity.setResult(Activity.RESULT_OK)
                }
                is TransactionDetailsContract.Effect.RefreshHistory -> {
                    historyViewModel?.setEvent(SuppliersHistoryContract.Event.OnCreate(effect.transaction))
                }

                is TransactionDetailsContract.Effect.ToggleResendLoader -> {
                    resendIsLoading = effect.isLoading
                }
                is TransactionDetailsContract.Effect.ToggleRejectLoader -> {
                    rejectIsLoading = effect.isLoading
                }
                is TransactionDetailsContract.Effect.ToggleAcceptLoader -> {
                    acceptIsLoading = effect.isLoading
                }
                is TransactionDetailsContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (historyViewModel != null) {
        ObserveEffects(historyViewModel) { effect ->
            when (effect) {
                is SuppliersHistoryContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is SuppliersHistoryContract.Effect.TextDataDownloaded -> {
                    createTextFileLauncher.launch(effect.filename)
                }
                is SuppliersHistoryContract.Effect.ZipDataDownloaded -> {
                    createZipFileLauncher.launch(effect.filename)
                }
                is SuppliersHistoryContract.Effect.CSVDataDownloaded -> {
                    createCSVFileLauncher.launch(effect.filename)
                }
                is SuppliersHistoryContract.Effect.ToggleDataLoader -> {
                    downloadLocationsLoading = effect.isLoading
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        if (transactionModel != null) {
            viewModel?.setEvent(TransactionDetailsContract.Event.OnCreate(transactionModel))
        }
    }

    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            var onIconClick: (() -> Unit)? = null
            if (binding.downloadEnabled) {
                onIconClick = { showDownloadDataBottomSheet = true }
            }

            Toolbar2(
                navController = navController,
                title = binding.toolbarTitle.ifBlank { stringResource(R.string.transaction_details) },
                iconRes = R.drawable.ic_download,
                onIconClick = onIconClick
            )

            binding.commodityText?.let { commodityText ->
                TitleDescriptionView(
                    title = stringResource(R.string.commodity_type),
                    description = commodityText,
                    warningText = binding.commodityWarningText,
                )
            }

            binding.harvestSeason?.let { harvestSeason ->
                TransactionInfoHarvestSeasonItem(
                    season = harvestSeason,
                ) {
                    dialog = TransactionDetailsDialog.HarvestSeasonInfo(harvestSeason)
                }
            }

            if (binding.showLocation) {
//                when (binding.locationProvider) {
//                    LocationProvider.GPS, LocationProvider.QR, LocationProvider.Manual -> {
//                        TransactionInfoItem1(
//                            title = "Farm geodata",
//                            description = binding.locationText,
//                        )
//                    }
//
//                    LocationProvider.File -> {
//                        TransactionInfoItem1(
//                            title = "Farm geodata file",
//                            description = binding.locationText,
//                        )
//                    }
//
//                    null -> {}
                if (binding.locationProvider == null) {
                    TransactionInfoItem3(
                        title = stringResource(R.string.farm_geodata),
                        description = stringResource(R.string.farm_geodata_missing),
                    ) {
                        viewModel?.setEvent(TransactionDetailsContract.Event.AddGeoDataClicked)
                    }
                }
            }

            binding.traceability?.let { traceability ->
                TransactionInfoTraceabilityItem(
                    status = traceability,
                    chartTitle = stringResource(R.string.traders),
                    chartItems = binding.chartItems,
                ) {
                    dialog = TransactionDetailsDialog.TraceabilityStatusDialog(null)
                }
            }

            binding.buyer?.let { buyer ->
                if (binding.accountId == buyer.id) {
                    TransactionInfoItem1(
                        title = stringResource(R.string.buyer_id),
                        description = stringResource(R.string.you, buyer.getAccountText()),
                    )
                } else {
                    TransactionInfoItem2(
                        title = stringResource(R.string.buyer_information),
                        description = buyer.getAccountText(),
                    ) {
                        dialog = TransactionDetailsDialog.BuyerInfo(buyer)
                    }
                }
            }

            binding.seller?.let { seller ->
                if (binding.accountId == seller.id) {
                    TransactionInfoItem1(
                        title = stringResource(R.string.seller_id),
                        description = stringResource(R.string.you, seller.getAccountText()),
                    )
                } else {
                    TransactionInfoItem2(
                        title = stringResource(R.string.supplier_information),
                        description = seller.getAccountText(),
                    ) {
                        dialog = TransactionDetailsDialog.SellerInfo(seller)
                    }
                }
            }

            binding.status?.let { status ->
                TransactionInfoStatusItem(
                    status = status,
                ) {
                    dialog = TransactionDetailsDialog.TransactionStatusDialog(status)
                }
            }

            binding.createdDateText?.let { createdDateText ->
                TransactionInfoItem1(
                    title = stringResource(R.string.transaction_date),
                    description = createdDateText,
                )
            }

            binding.expiryDateText?.let { expiryDateText ->
                TransactionInfoItem1(
                    title = stringResource(R.string.expiry_date),
                    description = expiryDateText,
                )
            }

            if (historyBinding.transactions.isNotEmpty()) {
                SuppliersList(
                    viewModel = viewModel,
                    transactionModel = transactionModel,
                    transactions = historyBinding.transactions,
                    onTraceabilityStatusClick = {
                        dialog = TransactionDetailsDialog.TraceabilityStatusDialog(it)
                    }
                )
            }
        }

        if (binding.showInitiatorActionButtons) {
            InitiatorActionButtons(
                viewModel = viewModel,
                rejectIsLoading = rejectIsLoading,
                resendIsLoading = resendIsLoading,
            )
        }

        if (binding.showRecipientActionButtons) {
            RecipientActionButtons(
                viewModel = viewModel,
                rejectIsLoading = rejectIsLoading,
                acceptIsLoading = acceptIsLoading,
                acceptEnabled = binding.acceptEnabled,
                acceptBlocked = binding.acceptBlocked,
                acceptBalanceLoading = binding.acceptBalanceLoading,
                acceptWarningText = binding.acceptWarningText,
                onAcceptBlockedClick = {
                    dialog = TransactionDetailsDialog.AcceptTransactionBlocked(binding.acceptBlockedDialogText)
                },
            )
        }
    }

    HandleDialog(
        dialog = dialog,
        onDismiss = {
            dialog = TransactionDetailsDialog.None
        },
        historyViewModel = historyViewModel,
    )

    if (showDownloadDataBottomSheet) {
        DownloadDataBottomSheet(
            onDismissRequest = { showDownloadDataBottomSheet = false },
            onBundleSelected = {
                showDownloadDataBottomSheet = false
                dialog = TransactionDetailsDialog.DownloadDetailsDialog(binding.dialogDescription)
            },
            onGeoJsonSelected = {
                showDownloadDataBottomSheet = false
                dialog = TransactionDetailsDialog.DownloadBundleDialog
            },
        )
    }

    if (downloadLocationsLoading) {
        LoadingState()
    }
}

@Composable
private fun SuppliersList(
    viewModel: TransactionDetailsViewModel?,
    transactionModel: TransactionModel?,
    transactions: List<TransactionModel>,
    onTraceabilityStatusClick: ((TraceabilityStatus?) -> Unit)? = null,
) {
    SectionHeader(
        title = stringResource(R.string.my_supplier_history),
        onIconClick = {
            viewModel?.setEvent(TransactionDetailsContract.Event.OnInitialSuppliersHistoryClick)
        },
    )

    val transactions = transactions.subList(0, min(transactions.size, 4))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(transactions) {
            var onClick: (() -> Unit)? = null
            if (it.seller != null) {
                onClick = {
                    viewModel?.setEvent(TransactionDetailsContract.Event.OnSuppliersHistoryClick(it))
                }
            }

            SupplierHistoryItem(
                transaction = it,
                onTraceabilityStatusClick = onTraceabilityStatusClick,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun HandleDialog(
    dialog: TransactionDetailsDialog,
    onDismiss: () -> Unit,
    historyViewModel: SuppliersHistoryViewModel?,
) {
    when (dialog) {
        is TransactionDetailsDialog.None -> {}
        is TransactionDetailsDialog.TraceabilityStatusDialog -> {
            TraceabilityStatusesDialog(
                status = dialog.status,
                onDismiss = onDismiss
            )
        }
        is TransactionDetailsDialog.SellerInfo -> {
            UserInfoDialog(
                title = stringResource(R.string.seller_information),
                userModel = dialog.userModel,
                onDismiss = onDismiss
            )
        }
        is TransactionDetailsDialog.BuyerInfo -> {
            UserInfoDialog(
                title = stringResource(R.string.buyer_information),
                userModel = dialog.userModel,
                onDismiss = onDismiss
            )
        }
        is TransactionDetailsDialog.TransactionStatusDialog -> {
            TransactionStatusDialog(
                status = dialog.status,
                onDismiss = onDismiss
            )
        }
        is TransactionDetailsDialog.HarvestSeasonInfo -> {
            HarvestSeasonInfoDialog(
                season = dialog.season,
                onDismiss = onDismiss,
            )
        }
        is TransactionDetailsDialog.AcceptTransactionBlocked -> {
            AcceptTransactionBlockedDialog(
                description = dialog.description,
                onDismiss = onDismiss,
            )
        }
        is TransactionDetailsDialog.DownloadDetailsDialog -> {
            BaseDialog(
                title = stringResource(R.string.download_transaction_details_q),
                onDismiss = onDismiss,
            ) {
                DialogTextItem(
                    title = dialog.description,
                )
                DialogButtonsItem(
                    actionButtonTitle = stringResource(R.string.download),
                    actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
                    actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                    onActionClick = {
                        onDismiss()
                        historyViewModel?.setEvent(SuppliersHistoryContract.Event.DownloadCSV)
                    },
                    onSecondClick = onDismiss,
                )
            }
        }
        is TransactionDetailsDialog.DownloadBundleDialog -> {
            BaseDialog(
                title = stringResource(R.string.download_farms_locations),
                onDismiss = onDismiss,
            ) {
                DialogTextItem(
                    title = stringResource(R.string.download_farms_locations_description),
                )
                DialogButtonsItem(
                    actionButtonTitle = stringResource(R.string.download),
                    actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
                    actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                    onActionClick = {
                        onDismiss()
                        historyViewModel?.setEvent(SuppliersHistoryContract.Event.DownloadBundle)
                    },
                    onSecondClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun HarvestSeasonInfoDialog(
    season: HarvestSeasonModel,
    onDismiss: () -> Unit,
) {
    BaseDialog(
        title = stringResource(R.string.harvest_season),
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.harvest_season_description),
                style = TextStyleBodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.harvest_season_assignment_description),
                style = TextStyleBodyM,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HarvestSeasonInfoLabel(season = season)

            val startDateText = season.startDate?.toShortFormattedDateString()
            val endDateText = season.endDate?.toShortFormattedDateString()

            if (startDateText != null && endDateText != null) {
                val periodText = if (season.country.isNullOrBlank()) {
                    stringResource(R.string.harvest_season_period_no_country, startDateText, endDateText)
                } else {
                    stringResource(R.string.harvest_season_period, season.country, startDateText, endDateText)
                }

                Text(
                    text = periodText,
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HarvestSeasonInfoLabel(
    season: HarvestSeasonModel,
) {
    val status = season.status
    val bgColor = when (status) {
        HarvestSeasonStatus.Active -> ColorHarvestSeasonActiveBackground
        HarvestSeasonStatus.Past -> ColorHarvestSeasonPastBackground
        HarvestSeasonStatus.Archived -> ColorGray30
        null -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (status) {
        HarvestSeasonStatus.Active -> ColorHarvestSeasonActiveContent
        HarvestSeasonStatus.Past -> ColorHarvestSeasonPastContent
        HarvestSeasonStatus.Archived -> MaterialTheme.colorScheme.surface
        null -> MaterialTheme.colorScheme.onSurface
    }
    val text = when (status) {
        HarvestSeasonStatus.Active -> stringResource(R.string.active_season, season.getShortName())
        HarvestSeasonStatus.Past -> stringResource(R.string.past_season, season.getShortName())
        HarvestSeasonStatus.Archived -> stringResource(R.string.archived_season, season.getShortName())
        null -> season.name
    }

    Text(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = text,
        style = TextStyleMediumM,
        color = textColor,
    )
}

@Composable
private fun AcceptTransactionBlockedDialog(
    description: String,
    onDismiss: () -> Unit,
) {
    BaseDialog(
        title = stringResource(R.string.accept_transaction),
        onDismiss = onDismiss,
    ) {
        DialogTextItem(title = description)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
        ) {
            LoadingButton(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.got_it),
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun InitiatorActionButtons(
    viewModel: TransactionDetailsViewModel?,
    rejectIsLoading: Boolean,
    resendIsLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LightLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            isLoading = rejectIsLoading,
            title = stringResource(R.string.cancel_transaction),
            onClick = {
                viewModel?.setEvent(TransactionDetailsContract.Event.RejectTransaction)
            }
        )

        LoadingButton(
            modifier = Modifier.fillMaxWidth(),
            isLoading = resendIsLoading,
            title = stringResource(R.string.resend_notification),
            onClick = {
                viewModel?.setEvent(TransactionDetailsContract.Event.ResendNotification)
            }
        )
    }
}

@Composable
private fun RecipientActionButtons(
    viewModel: TransactionDetailsViewModel?,
    rejectIsLoading: Boolean,
    acceptIsLoading: Boolean,
    acceptEnabled: Boolean,
    acceptBlocked: Boolean,
    acceptBalanceLoading: Boolean,
    acceptWarningText: String?,
    onAcceptBlockedClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!acceptWarningText.isNullOrBlank()) {
            CreateTransactionWarning(
                backgroundColor = ColorLightOrange,
                borderColor = ColorWarning10,
                iconRes = R.drawable.ic_warning,
                iconTint = ColorWarning,
                title = acceptWarningText,
            )
        }

        LightLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            isLoading = rejectIsLoading,
            title = stringResource(R.string.reject_transaction),
            onClick = {
                viewModel?.setEvent(TransactionDetailsContract.Event.RejectTransaction)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LoadingButton(
                modifier = Modifier.weight(1f),
                isEnabled = acceptEnabled,
                isLoading = acceptIsLoading || acceptBalanceLoading,
                title = stringResource(R.string.accept_transaction),
                onClick = {
                    viewModel?.setEvent(TransactionDetailsContract.Event.AcceptTransaction)
                }
            )

            if (acceptBlocked) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onAcceptBlockedClick() }
                        .padding(12.dp),
                    painter = painterResource(id = R.drawable.ic_warning),
                    contentDescription = stringResource(R.string.not_enough_available_balance),
                    tint = ColorWarning,
                )
            }
        }
    }
}

@Composable
fun DownloadDataBottomSheet(
    onDismissRequest: () -> Unit,
    onBundleSelected: () -> Unit,
    onGeoJsonSelected: () -> Unit,
) {
    BottomSheetBase(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SettingsOptionItemBase(
                iconRes = R.drawable.ic_list,
                title = stringResource(R.string.download_transaction_details),
                onClick = onBundleSelected,
            )

            SettingsOptionItemBase(
                iconRes = R.drawable.ic_map_marker,
                title = stringResource(R.string.download_farm_geolocation),
                onClick = onGeoJsonSelected,
            )
        }
    }
}
