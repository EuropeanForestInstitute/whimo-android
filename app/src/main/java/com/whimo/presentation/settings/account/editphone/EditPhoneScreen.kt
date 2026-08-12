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
package com.whimo.presentation.settings.account.editphone

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.whimo.navigation.Screens
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.components.PhoneNumberField
import com.whimo.presentation.ui.components.dialogs.PhoneRegionDialog
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.ForceUpdateController
import com.whimo.utils.LocationPermissionRequester
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        EditPhoneScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
            phone = null,
        )
    }
}

@Composable
fun EditPhoneScreen(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: EditPhoneViewModel? = koinViewModel(),
    phone: String?,
) {
    val binding = viewModel?.observeViewBinding() ?: EditPhoneContract.Binding()
    val context = LocalContext.current

    var requestPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showPhoneRegionDialog by remember { mutableStateOf(false) }
    val forceUpdateController = remember { ForceUpdateController() }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is EditPhoneContract.Effect.RequestLocationPermission -> {
                    requestPermission = true
                }
                is EditPhoneContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
                is EditPhoneContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is EditPhoneContract.Effect.NavigateToPhoneOtp -> {
                    navController.popBackStack()
                    navController.navigate(
                        Screens.ConfirmPhoneCodeScreen.putArgs(Screens.ARG_KEY_PHONE to effect.phone)
                    )
                }
                is EditPhoneContract.Effect.ForceUpdateFields -> {
                    forceUpdateController.apply()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(EditPhoneContract.Event.OnCreate(context, phone))
    }

    if (requestPermission) {
        LocationPermissionRequester { result ->
            requestPermission = false
            if (result) {
                viewModel?.setEvent(EditPhoneContract.Event.OnCreate(context, phone))
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Toolbar2(
            navController = navController,
            title = stringResource(id = R.string.phone_number),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.send_verification_code),
                style = TextStyleBodyM,
                color = MaterialTheme.colorScheme.onSurface,
            )

            PhoneNumberField(
                controller = forceUpdateController,
                labelText = stringResource(id = R.string.phone),
                hintText = stringResource(id = R.string.phone_digits),
                errorText = binding.phoneError,
                phoneRegion = binding.phoneRegion,
                phoneNumber = binding.phoneNumber,
                onValueChange = {
                    viewModel?.setEvent(EditPhoneContract.Event.OnPhoneChanged(it))
                },
                onPhoneRegionClicked = {
                    showPhoneRegionDialog = true
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            LoadingButton(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = binding.saveEnabled,
                isLoading = isLoading,
                title = stringResource(R.string.save),
                onClick = {
                    viewModel?.setEvent(EditPhoneContract.Event.OnSaveClick)
                }
            )
        }
    }

    if (showPhoneRegionDialog) {
        PhoneRegionDialog(
            selectedRegion = binding.phoneRegion,
            onDismiss = { showPhoneRegionDialog = false },
            onRegionSelected = {
                showPhoneRegionDialog = false
                viewModel?.setEvent(EditPhoneContract.Event.OnPhoneRegionChanged(it))
            }
        )
    }
}
