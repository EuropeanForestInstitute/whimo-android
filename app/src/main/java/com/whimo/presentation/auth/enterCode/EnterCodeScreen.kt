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
package com.whimo.presentation.auth.enterCode

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.BuildConfig
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.navigation.Screens
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.components.OtpInputField
import com.whimo.presentation.ui.components.dialogs.TurnstileCaptchaDialog
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.setResult
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
fun EnterCodeScreenScreenPreview() {
    WhimoTheme {
        EnterCodeScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            state = EnterCodeScreenState.Registration,
            viewModel = null,
        )
    }
}

@Composable
fun EnterCodeScreen(
    modifier: Modifier,
    navController: NavHostController,
    state: EnterCodeScreenState,
    email: String? = null,
    phone: String? = null,
    viewModel: EnterCodeViewModel? = koinViewModel()
) {
    val binding = viewModel?.observeViewBinding() ?: EnterCodeContract.Binding()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var showCaptcha by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is EnterCodeContract.Effect.RequestCaptcha -> {
                    if (BuildConfig.CAPTCHA_URL.isBlank() ||
                        BuildConfig.CAPTCHA_URL == "DEFAULT_URL"
                    ) {
                        viewModel.setEvent(EnterCodeContract.Event.OnCaptchaUnavailable)
                    } else {
                        showCaptcha = true
                    }
                }
                is EnterCodeContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
                is EnterCodeContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is EnterCodeContract.Effect.NavigateCreateNewPassScreen -> {
                    navController.navigate(
                        Screens.CreateNewPasswordScreen.putArgs(
                            Screens.ARG_KEY_USERNAME to effect.username,
                            Screens.ARG_KEY_CODE to effect.code,
                        )
                    )
                }
                is EnterCodeContract.Effect.PopBack -> {
                    navController.setResult("confirmCodeUsername", effect.username)
                    navController.popBackStack()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(EnterCodeContract.Event.OnCreate(state, email, phone))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Toolbar2(
            navController = navController,
            title = binding.title,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = binding.description,
                style = TextStyleBodyM,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OtpInputField(
                modifier = Modifier.padding(vertical = 16.dp),
                otpText = binding.code,
                onOtpChange = {
                    viewModel?.setEvent(EnterCodeContract.Event.OnCodeChange(it))
                },
                error = binding.codeError,
            )

            LoadingButton(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = binding.confirmEnabled,
                isLoading = isLoading,
                title = stringResource(R.string.confirm),
                onClick = {
                    viewModel?.setEvent(EnterCodeContract.Event.OnConfirm)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.did_not_receive_code),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier
                        .clickable {
                            viewModel?.setEvent(EnterCodeContract.Event.OnRequestAgain)
                        },
                    text = stringResource(R.string.request_another),
                    style = TextStyleButtonM,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (showCaptcha) {
        TurnstileCaptchaDialog(
            captchaUrl = BuildConfig.CAPTCHA_URL,
            onTokenReceived = { token ->
                showCaptcha = false
                viewModel?.setEvent(EnterCodeContract.Event.OnCaptchaTokenReceived(token))
            },
            onError = {
                showCaptcha = false
                viewModel?.setEvent(EnterCodeContract.Event.OnCaptchaFailed)
            },
            onDismiss = {
                showCaptcha = false
                viewModel?.setEvent(EnterCodeContract.Event.OnCaptchaDismissed)
            },
        )
    }
}
