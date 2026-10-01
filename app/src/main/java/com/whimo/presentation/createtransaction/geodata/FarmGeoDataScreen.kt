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
package com.whimo.presentation.createtransaction.geodata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.extensions.findActivity
import com.whimo.navigation.Screens
import com.whimo.presentation.createtransaction.components.CreateTransactionButton4
import com.whimo.presentation.main.components.LoadingState
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.LocationPermissionRequester
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        FarmGeoDataScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun FarmGeoDataScreen(
    modifier: Modifier,
    navController: NavHostController,
    transactionModel: TransactionModel? = null,
    createTransactionModel: CreateTransactionModel? = null,
    viewModel: FarmGeoDataViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: FarmGeoDataContract.Binding()
    val context = LocalContext.current

    var requestPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is FarmGeoDataContract.Effect.RequestLocationPermission -> {
                    requestPermission = true
                }
                is FarmGeoDataContract.Effect.LocationRecorded -> {
                    val activity = context.findActivity() as FarmGeoDataActivity
                    activity.setResult(
                        locationProvider = LocationProvider.GPS,
                        location = effect.location,
                    )
                    activity.finish()
                }
                is FarmGeoDataContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        if (transactionModel != null) {
            viewModel?.setEvent(FarmGeoDataContract.Event.OnCreate2(transactionModel))
        }
        if (createTransactionModel != null) {
            viewModel?.setEvent(FarmGeoDataContract.Event.OnCreate(createTransactionModel))
        }
    }

    if (requestPermission) {
        LocationPermissionRequester { result ->
            requestPermission = false
            if (result) {
                viewModel?.setEvent(FarmGeoDataContract.Event.OnRecordLocationClick(context))
            }
        }
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar2(
                navController = navController,
                title = stringResource(R.string.farm_geodata),
            )

            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                if (binding.qrButtonVisible) {
                    CreateTransactionButton4(
                        iconRes = R.drawable.ic_qr,
                        title = stringResource(R.string.scan_qr_code),
                        description = stringResource(R.string.scan_qr_description_long),
                    ) {
                        navController.navigate(Screens.QrScan.route)
                    }
                }

                if (binding.uploadFileButtonVisible) {
                    CreateTransactionButton4(
                        iconRes = R.drawable.ic_upload,
                        title = stringResource(R.string.upload_file),
                        description = stringResource(R.string.upload_file_description_long),
                    ) {
                        navController.navigate(Screens.UploadFile.route)
                    }
                }

                if (binding.gpsButtonVisible) {
                    CreateTransactionButton4(
                        iconRes = R.drawable.ic_map_marker,
                        title = stringResource(R.string.record_my_geodata),
                        description = stringResource(R.string.record_geolocation_description_long),
                    ) {
                        viewModel?.setEvent(FarmGeoDataContract.Event.OnRecordLocationClick(context))
                    }
                }

                if (binding.mapButtonVisible) {
                    CreateTransactionButton4(
                        iconRes = R.drawable.ic_map,
                        title = stringResource(R.string.add_gps_point),
                        description = stringResource(R.string.add_gps_point_description),
                    ) {
                        navController.navigate(Screens.Map.route)
                    }
                }
            }
        }

        if (isLoading) {
            LoadingState()
        }
    }
}
