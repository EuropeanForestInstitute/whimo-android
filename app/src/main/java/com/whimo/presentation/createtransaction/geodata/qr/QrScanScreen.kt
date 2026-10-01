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
package com.whimo.presentation.createtransaction.geodata.qr

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.extensions.findActivity
import com.whimo.presentation.createtransaction.components.CameraPreview
import com.whimo.presentation.createtransaction.geodata.FarmGeoDataActivity
import com.whimo.presentation.main.components.EmptyState
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.CameraPermissionRequester
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        QrScanScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun QrScanScreen(
    modifier: Modifier,
    navController: NavHostController,
    transactionModel: TransactionModel? = null,
    viewModel: QrScanViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: QrScanContract.Binding()
    val context = LocalContext.current

    var mResult by remember { mutableStateOf(false) }
    var cameraAvailable by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is QrScanContract.Effect.QrResult -> {
                    val activity = context.findActivity() as FarmGeoDataActivity
                    activity.setResult(
                        locationProvider = LocationProvider.QR,
                        location = effect.location,
                        qrData = effect.qrData,
                    )
                    activity.finish()
                }
                is QrScanContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                    mResult = false
                }
                is QrScanContract.Effect.ToggleLoader -> {
                    Unit
                }
            }
        }
    }

    CameraPermissionRequester { result ->
        cameraAvailable = result
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        if (transactionModel != null) {
            viewModel?.setEvent(QrScanContract.Event.OnCreate(transactionModel))
        }
    }

    Column(modifier = modifier) {
        Toolbar2(
            navController = navController,
            title = stringResource(R.string.scan_qr),
            showOfflineBanner = false,
        )

        if (cameraAvailable) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                ) { data ->
                    if (!mResult) {
                        mResult = true

                        viewModel?.setEvent(QrScanContract.Event.QrScanned(context, data))
                    }
                }

                Icon(
                    modifier = Modifier
                        .size(300.dp)
                        .align(alignment = Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_scan_corners),
                    contentDescription = "",
                    tint = Color.Unspecified,
                )
            }
        } else {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                iconRes = R.drawable.ic_warning,
                title = stringResource(R.string.qr_scanner_unavailable),
                description = stringResource(R.string.qr_scanner_unavailable_description)
            )
        }
    }
}
