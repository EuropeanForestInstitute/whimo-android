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
package com.whimo.presentation.auth.registration

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.BuildConfig
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.extensions.findActivity
import com.whimo.navigation.Screens
import com.whimo.presentation.createtransaction.components.DashedDivider
import com.whimo.presentation.main.MainActivity
import com.whimo.presentation.main.components.Toolbar3
import com.whimo.presentation.ui.baseScreen.GoogleButton
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.components.EmailField
import com.whimo.presentation.ui.components.LinkSpec
import com.whimo.presentation.ui.components.PasswordField
import com.whimo.presentation.ui.components.PhoneNumberField
import com.whimo.presentation.ui.components.RichTextWithLinks
import com.whimo.presentation.ui.components.bottomsheets.LanguagesBottomSheet
import com.whimo.presentation.ui.components.bottomsheets.VerificationMethodBottomSheet
import com.whimo.presentation.ui.components.dialogs.PhoneRegionDialog
import com.whimo.presentation.ui.models.Languages
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.LocationPermissionRequester
import com.whimo.utils.getResult
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
fun RegistrationScreenPreview() {
    WhimoTheme {
        RegistrationScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun RegistrationScreen(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: RegistrationViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: RegistrationContract.Binding()
    val context = LocalContext.current

    var requestPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showVerificationMethodBottomSheet by remember { mutableStateOf(false) }
    var showLanguageBottomSheet by remember { mutableStateOf(false) }
    var showPhoneRegionDialog by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is RegistrationContract.Effect.RequestLocationPermission -> {
                    requestPermission = true
                }
                is RegistrationContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
                is RegistrationContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is RegistrationContract.Effect.NavigateTerms -> {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            BuildConfig.TERMS_OF_USE.toUri()
                        )
                    )
                }
                is RegistrationContract.Effect.NavigateLogin -> {
                    navController.popBackStack()
                }
                is RegistrationContract.Effect.NavigateMainActivity -> {
                    MainActivity.openMain(context.findActivity())
                }
                is RegistrationContract.Effect.ShowVerificationMethodBottomSheet -> {
                    showVerificationMethodBottomSheet = true
                }
                is RegistrationContract.Effect.NavigateToEmailOtp -> {
                    navController.navigate(
                        Screens.RegistrationConfirmEmailCodeScreen.putArgs(Screens.ARG_KEY_EMAIL to effect.email)
                    )
                }
                is RegistrationContract.Effect.NavigateToPhoneOtp -> {
                    navController.navigate(
                        Screens.RegistrationConfirmPhoneCodeScreen.putArgs(Screens.ARG_KEY_PHONE to effect.phone)
                    )
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(RegistrationContract.Event.OnCreate(context))
    }

    if (requestPermission) {
        LocationPermissionRequester { result ->
            requestPermission = false
            if (result) {
                viewModel?.setEvent(RegistrationContract.Event.OnCreate(context))
            }
        }
    }

    val confirmCodeUsername = navController.getResult<String>("confirmCodeUsername")

    if (confirmCodeUsername != null) {
        viewModel?.setEvent(RegistrationContract.Event.OnOtpSuccess(confirmCodeUsername))
    }

    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        val emailLabel = stringResource(id = R.string.email) + if (binding.emailRequired) "*" else ""

        Toolbar3(
            title = stringResource(id = R.string.create_account),
            description = stringResource(id = R.string.create_account_instructions),
        )

        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            EmailField(
                labelText = emailLabel,
                hintText = stringResource(id = R.string.enter_email),
                errorText = binding.emailError,
                email = binding.email,
                onValueChange = {
                    viewModel?.setEvent(RegistrationContract.Event.OnEmailChanged(it))
                },
            )

            PhoneNumberField(
                labelText = stringResource(id = R.string.phone),
                hintText = stringResource(id = R.string.phone_digits),
                errorText = binding.phoneError,
                phoneRegion = binding.phoneRegion,
                phoneNumber = binding.phoneNumber,
                onValueChange = {
                    viewModel?.setEvent(RegistrationContract.Event.OnPhoneChanged(it))
                },
                onFocusChanged = {
                    viewModel?.setEvent(RegistrationContract.Event.OnPhoneFocusChanged(it))
                },
                onPhoneRegionClicked = {
                    showPhoneRegionDialog = true
                },
            )

            PasswordField(
                labelText = stringResource(id = R.string.password),
                hintText = stringResource(id = R.string.create_password),
                errorText = binding.passwordError,
                password = binding.password,
                onValueChange = {
                    viewModel?.setEvent(RegistrationContract.Event.OnPasswordChanged(it))
                },
            )

            PasswordField(
                labelText = stringResource(id = R.string.confirm_password),
                hintText = stringResource(id = R.string.confirm_password),
                errorText = binding.confirmPasswordError,
                password = binding.confirmPassword,
                onValueChange = {
                    viewModel?.setEvent(RegistrationContract.Event.OnConfirmPasswordChanged(it))
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = binding.termsAccepted,
                    onCheckedChange = {
                        viewModel?.setEvent(RegistrationContract.Event.OnTermsAcceptanceChange(it))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                val termsTag = "TERMS"
                val termsText = stringResource(R.string.terms_of_use)
                val fullText = stringResource(R.string.terms_accept_text, termsText)

                RichTextWithLinks(
                    fullText = fullText,
                    linkSpecs = listOf(LinkSpec(text = termsText, tag = termsTag)),
                    onLinkClick = { tag ->
                        if (tag == termsTag) {
                            viewModel?.setEvent(RegistrationContract.Event.OnTermsClick)
                        }
                    }
                )
            }

            LoadingButton(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = binding.registrationEnabled,
                isLoading = isLoading,
                title = stringResource(R.string.register),
                onClick = {
                    viewModel?.setEvent(RegistrationContract.Event.OnRegisterClick)
                },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashedDivider(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(id = R.string.or),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurface
                )

                DashedDivider(modifier = Modifier.weight(1f))
            }

            GoogleButton(
                modifier = Modifier.fillMaxWidth(),
                onClick =  {
                    viewModel?.setEvent(
                        RegistrationContract.Event.OnGoogleClick(context.findActivity())
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.have_account),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier
                        .clickable {
                            viewModel?.setEvent(RegistrationContract.Event.OnLoginClick)
                        },
                    text = stringResource(R.string.log_in),
                    style = TextStyleButtonM,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.clickable { showLanguageBottomSheet = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.ic_translation),
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = Languages.fromCode(binding.selectedLanguage)?.languageName ?: Languages.ENGLISH.languageName,
                        style = TextStyleButtonM,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showVerificationMethodBottomSheet) {
        VerificationMethodBottomSheet(
            onDismissRequest = { showVerificationMethodBottomSheet = false },
            onEmailMethodSelected = {
                viewModel?.setEvent(RegistrationContract.Event.OnEmailVerificationMethodChosen)
            },
            onPhoneMethodSelected = {
                viewModel?.setEvent(RegistrationContract.Event.OnPhoneVerificationMethodChosen)
            },
        )
    }

    if (showLanguageBottomSheet) {
        LanguagesBottomSheet(
            selectedLanguageCode = binding.selectedLanguage,
            onDismiss = { showLanguageBottomSheet = false },
            onLanguageSelected = {
                viewModel?.setEvent(RegistrationContract.Event.OnChangeLanguage(context, it))
            }
        )
    }

    if (showPhoneRegionDialog) {
        PhoneRegionDialog(
            selectedRegion = binding.phoneRegion,
            onDismiss = { showPhoneRegionDialog = false },
            onRegionSelected = {
                showPhoneRegionDialog = false
                viewModel?.setEvent(RegistrationContract.Event.OnPhoneRegionChanged(it))
            }
        )
    }
}
