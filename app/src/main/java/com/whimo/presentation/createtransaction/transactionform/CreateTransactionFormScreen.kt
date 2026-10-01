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

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.createtransaction.models.MFile
import com.whimo.extensions.findActivity
import com.whimo.extensions.getParcelableCompatExtra
import com.whimo.navigation.Screens
import com.whimo.presentation.createtransaction.SharedTransactionViewModel
import com.whimo.presentation.createtransaction.components.CreateTransactionDialog
import com.whimo.presentation.createtransaction.components.CreateTransactionItem
import com.whimo.presentation.createtransaction.components.CreateTransactionMessage
import com.whimo.presentation.createtransaction.components.CreateTransactionVolumeBreakdownItem
import com.whimo.presentation.createtransaction.components.CreateTransactionVolumeItem
import com.whimo.presentation.createtransaction.components.CreateTransactionWarning
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity.Companion.RESULT_FILE
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity.Companion.RESULT_LOCATION
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity.Companion.RESULT_LOCATION_PROVIDER
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity.Companion.RESULT_QR_DATA
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.LocationPermissionRequester
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        CreateTransactionFormScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun CreateTransactionFormScreen(
    modifier: Modifier,
    navController: NavHostController,
    sharedTransactionViewModel: SharedTransactionViewModel? = null,
    viewModel: CreateTransactionFormViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: CreateTransactionFormContract.Binding()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showGeoDataDialog by remember { mutableStateOf(false) }
    var requestPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val locationProvider = LocationProvider.entries.find { it.providerName == data.getStringExtra(RESULT_LOCATION_PROVIDER) }
                    val location = data.getParcelableCompatExtra<LatLng>(RESULT_LOCATION)
                    val qrData = data.getStringExtra(RESULT_QR_DATA)
                    val file = data.getParcelableCompatExtra<MFile>(RESULT_FILE)

                    sharedTransactionViewModel?.setLocationProvider(locationProvider)

                    when (locationProvider) {
                        LocationProvider.GPS -> {
                            sharedTransactionViewModel?.setLocation(location)
                        }
                        LocationProvider.QR -> {
                            sharedTransactionViewModel?.setQR(qrData)
                            sharedTransactionViewModel?.setLocation(location)
                        }
                        LocationProvider.Manual -> {
                            sharedTransactionViewModel?.setLocation(location)
                        }
                        LocationProvider.File -> {
                            sharedTransactionViewModel?.setFile(file)
                            sharedTransactionViewModel?.setLocation(location)
                        }
                        null -> {}
                    }

                    if (sharedTransactionViewModel != null) {
                        sharedTransactionViewModel.validateLocationProvider()
                        viewModel?.setEvent(
                            CreateTransactionFormContract.Event.OnCreate(context, sharedTransactionViewModel.transaction)
                        )
                    }
                }
            }
        }
    )

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is CreateTransactionFormContract.Effect.ToggleLoader -> isLoading = effect.isLoading
                is CreateTransactionFormContract.Effect.ShowError -> errorMessage = effect.message
                is CreateTransactionFormContract.Effect.ShowConfirmDialog -> showConfirmDialog = true
                is CreateTransactionFormContract.Effect.ShowGeoDataDialog -> showGeoDataDialog = true
                is CreateTransactionFormContract.Effect.CreateTransactionSuccess -> {
                    val activity = context.findActivity()
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                }
                is CreateTransactionFormContract.Effect.NavigateFarmGeoData -> {
                    FarmGeoDataActivity.openFarmGeoData(context, launcher, effect.transaction)
                }
                is CreateTransactionFormContract.Effect.RequestLocationPermission -> {
                    requestPermission = true
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        if (sharedTransactionViewModel != null) {
            sharedTransactionViewModel.validateLocationProvider()
            viewModel?.setEvent(
                CreateTransactionFormContract.Event.OnCreate(context, sharedTransactionViewModel.transaction)
            )
        }
    }

    if (requestPermission) {
        LocationPermissionRequester { result ->
            requestPermission = false
        }
    }

    Column(modifier = modifier) {
        Toolbar2(
            navController = navController,
            title = binding.toolbarTitle,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CreateTransactionWarning(
                modifier = Modifier.padding(16.dp),
                iconRes = R.drawable.ic_information,
                title = stringResource(R.string.please_provide_accurate_info),
            )

            if (binding.farmGeoDataVisible) {
                CreateTransactionItem(
                    iconRes = R.drawable.ic_map_marker,
                    title = stringResource(R.string.farm_geodata),
                    description = binding.farmGeoDataText,
                ) {
                    viewModel?.setEvent(CreateTransactionFormContract.Event.OnFarmGeoDataClick)
                }
            }

            CreateTransactionItem(
                iconRes = R.drawable.ic_add_commodity,
                title = stringResource(R.string.commodity_type_required),
                description = binding.commodityTypeText,
            ) {
                navController.navigate(Screens.CommodityTypes.route)
            }

            val volumeBreakdown = binding.commodityVolumeBreakdown
            if (volumeBreakdown != null) {
                CreateTransactionVolumeBreakdownItem(
                    iconRes = R.drawable.ic_commodity_type,
                    title = stringResource(R.string.volume_commodities_required),
                    breakdown = volumeBreakdown,
                ) {
                    navController.navigate(Screens.CommodityVolume.route)
                }
            } else {
                CreateTransactionVolumeItem(
                    iconRes = R.drawable.ic_commodity_type,
                    title = stringResource(R.string.volume_commodities_required),
                    description = binding.commodityVolumeText,
                    harvestSeasonText = binding.commodityVolumeHarvestSeasonText,
                    harvestSeasonStatus = binding.commodityVolumeHarvestSeasonStatus,
                ) {
                    navController.navigate(Screens.CommodityVolume.route)
                }
            }

            if (binding.userInfoVisible) {
                CreateTransactionItem(
                    iconRes = R.drawable.ic_user,
                    title = binding.userInfoTitle,
                    description = binding.userInfoText,
                ) {
                    navController.navigate(Screens.SupplierInfo.route)
                }
            }

            if (binding.inviteUserVisible) {
                CreateTransactionItem(
                    iconRes = R.drawable.ic_user,
                    title = stringResource(R.string.supplier_information2),
                    description = binding.inviteUserText,
                ) {
                    navController.navigate(Screens.InviteUser.route)
                }
            }
        }

        LoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            isEnabled = binding.buttonEnabled,
            isLoading = isLoading,
            title = stringResource(R.string.save_transaction),
            onClick = {
                viewModel?.setEvent(CreateTransactionFormContract.Event.ValidateTransaction)
            }
        )
    }

    if (showConfirmDialog) {
        CreateTransactionDialog(
            title = stringResource(R.string.save_transaction_title),
            description = stringResource(R.string.save_transaction_description),
            warningDescription = if (binding.isTestEnvironmentEnabled) {
                stringResource(R.string.test_environment_transaction_notice)
            } else null,
            actionButtonTitle = stringResource(R.string.save_transaction),
            secondButtonTitle = stringResource(R.string.review_information),
            onActionClick = {
                viewModel?.setEvent(CreateTransactionFormContract.Event.CreateTransaction(context))
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    if (showGeoDataDialog) {
        CreateTransactionDialog(
            title = stringResource(R.string.save_without_location_title),
            description = stringResource(R.string.save_without_location_description),
            actionButtonTitle = stringResource(R.string.add_farm_geodata),
            secondButtonTitle = stringResource(R.string.save_without_location),
            onActionClick = {
                viewModel?.setEvent(CreateTransactionFormContract.Event.OnFarmGeoDataClick)
            },
            onSecondClick = {
                viewModel?.setEvent(CreateTransactionFormContract.Event.CreateTransaction(context))
            },
            onDismiss = {
                showGeoDataDialog = false
            }
        )
    }

    if (errorMessage.isNotEmpty()) {
        CreateTransactionMessage(
            title = "Create transaction",
            description = errorMessage,
            onDismiss = {
                errorMessage = ""
            }
        )
    }
}
