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

import android.app.Activity
import android.content.Context
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.auth.AuthInteractor
import com.whimo.domain.config.RegistrationPhoneRegionPolicy
import com.whimo.network.ErrorHandler
import com.whimo.presentation.ui.models.Languages
import com.whimo.providers.ResourceProvider
import com.whimo.providers.RemoteConfigProvider
import com.whimo.providers.SharedPreferencesProvider
import com.whimo.utils.AppLocaleManager
import com.whimo.utils.GoogleSignInHelper
import com.whimo.utils.GoogleSignInResult
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.ValidationUtils
import com.whimo.utils.getLastLocation

class RegistrationViewModel(
    private val authInteractor: AuthInteractor,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val appLocaleManager: AppLocaleManager,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
    private val remoteConfigProvider: RemoteConfigProvider,
) : BaseViewModel<RegistrationContract.Binding>() {

    private var email: String = ""
    private var phoneRegion = PhoneNumberUtils.getDefaultPhoneRegion()
    private var phoneNumber: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""
    private var emailError: String = ""
    private var phoneError: String = ""
    private var passwordError: String = ""
    private var confirmPasswordError: String = ""
    private var termsAccepted: Boolean = false
    private var phoneFocused: Boolean = false
    private var selectedLanguage: String = Languages.ENGLISH.languageCode
    private var phoneRegionPolicy = RegistrationPhoneRegionPolicy.Disabled

    override fun createBinding(): RegistrationContract.Binding {
        return RegistrationContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is RegistrationContract.Event.OnCreate -> onCreate(event.context)
            is RegistrationContract.Event.OnEmailChanged -> onEmailChanged(event.email)
            is RegistrationContract.Event.OnPhoneRegionChanged -> onPhoneRegionChanged(event.phoneRegion)
            is RegistrationContract.Event.OnPhoneChanged -> onPhoneChanged(event.phone)
            is RegistrationContract.Event.OnPhoneFocusChanged -> onPhoneFocusChanged(event.isFocused)
            is RegistrationContract.Event.OnPasswordChanged -> onPasswordChanged(event.password)
            is RegistrationContract.Event.OnConfirmPasswordChanged -> onConfirmPasswordChanged(event.confirmPassword)
            is RegistrationContract.Event.OnTermsAcceptanceChange -> onTermsAcceptanceChange(event.termsAccepted)
            is RegistrationContract.Event.OnTermsClick -> onTermsClick()
            is RegistrationContract.Event.OnRegisterClick -> onRegisterClick()
            is RegistrationContract.Event.OnEmailVerificationMethodChosen -> onEmailVerificationMethodChosen()
            is RegistrationContract.Event.OnPhoneVerificationMethodChosen -> onPhoneVerificationMethodChosen()
            is RegistrationContract.Event.OnGoogleClick -> onGoogleClick(event.activity)
            is RegistrationContract.Event.OnLoginClick -> onLoginClick()
            is RegistrationContract.Event.OnChangeLanguage -> onChangeLanguage(event.context, event.languageCode)
            is RegistrationContract.Event.OnOtpSuccess -> onOtpSuccess(event.username)
        }
    }

    override fun copyBinding(binding: RegistrationContract.Binding): RegistrationContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        val phoneRegionUnsupported = !phoneRegionPolicy.isPhoneRegionSupported(phoneRegion)
        val showPhoneRegionUnsupportedError = phoneRegionUnsupported &&
                (phoneFocused || (phoneNumber.isNotEmpty() && email.isBlank()))
        val emailRequiredByPhonePolicy = phoneRegionPolicy.requiresEmailForRegistration(
            phoneRegion = phoneRegion,
            email = email,
        )

        updateBinding { b ->
            b.email = email
            b.phoneNumber = phoneNumber
            b.phoneRegion = phoneRegion
            b.password = password
            b.confirmPassword = confirmPassword
            b.emailError = emailError
            b.phoneError = if (showPhoneRegionUnsupportedError) {
                resourceProvider.getString(R.string.phone_verification_unavailable_registration)
            } else {
                phoneError
            }
            b.passwordError = passwordError
            b.confirmPasswordError = confirmPasswordError
            b.termsAccepted = termsAccepted
            b.registrationEnabled = (email.isNotEmpty() || phoneNumber.isNotEmpty()) &&
                    password.isNotEmpty() &&
                    confirmPassword.isNotEmpty() &&
                    termsAccepted &&
                    !emailRequiredByPhonePolicy
            b.emailRequired = phoneRegionUnsupported
            b.selectedLanguage = selectedLanguage
        }
    }
    
    private fun onCreate(context: Context) {
        selectedLanguage = appLocaleManager.getLanguageCode(context)
        refreshPhoneRegionPolicy()

        launch {
            val location = getLastLocation(context)

            if (location == null) {
                setEffect(RegistrationContract.Effect.RequestLocationPermission)

            } else {
                PhoneNumberUtils.getCountryCodeFromLocation(context, location)?.let { countryCode ->
                    phoneRegion = PhoneNumberUtils.getPhoneRegion(countryCode)
                    updateView()
                }
            }
        }
    }

    private fun refreshPhoneRegionPolicy() {
        phoneRegionPolicy = remoteConfigProvider.getRegistrationPhoneRegionPolicy()
        updateView()

        launch {
            remoteConfigProvider.refresh()
            phoneRegionPolicy = remoteConfigProvider.getRegistrationPhoneRegionPolicy()
            updateView()
        }
    }

    private fun onEmailChanged(email: String) {
        this.email = email
        this.emailError = ""
        updateView()
    }

    private fun onPhoneChanged(phone: String) {
        this.phoneNumber = phone
        this.phoneError = ""
        updateView()
    }

    private fun onPhoneFocusChanged(isFocused: Boolean) {
        this.phoneFocused = isFocused
        updateView()
    }

    private fun onPhoneRegionChanged(phoneRegion: PhoneNumberUtils.PhoneRegion) {
        this.phoneRegion = phoneRegion
        updateView()
    }

    private fun onPasswordChanged(password: String) {
        this.password = password
        this.passwordError = ""
        this.confirmPasswordError = ""
        updateView()
    }

    private fun onConfirmPasswordChanged(confirmPassword: String) {
        this.confirmPassword = confirmPassword
        this.confirmPasswordError = ""
        updateView()
    }

    private fun onTermsAcceptanceChange(termsAccepted: Boolean) {
        this.termsAccepted = termsAccepted
        updateView()
    }

    private fun onTermsClick() {
        setEffect(RegistrationContract.Effect.NavigateTerms)
    }

    private fun onRegisterClick() {
        if (phoneRegionPolicy.requiresEmailForRegistration(phoneRegion, email)) {
            updateView()
            return
        }

        val emailValidationStatus = ValidationUtils.validateEmail(email)
        val phoneValidationStatus = ValidationUtils.validatePhoneNumber(phoneRegion.phoneCode, phoneNumber)
        val passwordValidationStatus = ValidationUtils.validatePassword(password)
        val confirmPasswordValidationStatus = ValidationUtils.validateConfirmPassword(password, confirmPassword)

        emailError = when(emailValidationStatus) {
            ValidationUtils.ValidationState.Empty -> ""
            ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_email)
            ValidationUtils.ValidationState.Valid -> ""
        }

        phoneError = when(phoneValidationStatus) {
            ValidationUtils.ValidationState.Empty -> ""
            ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_phone)
            ValidationUtils.ValidationState.Valid -> ""
        }

        passwordError = when(passwordValidationStatus) {
            ValidationUtils.ValidationState.Empty -> ""
            ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_password)
            ValidationUtils.ValidationState.Valid -> ""
        }

        confirmPasswordError = when(confirmPasswordValidationStatus) {
            ValidationUtils.ValidationState.Empty -> ""
            ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_confirm_password)
            ValidationUtils.ValidationState.Valid -> ""
        }

        if ((emailValidationStatus == ValidationUtils.ValidationState.Valid || phoneValidationStatus == ValidationUtils.ValidationState.Valid) &&
            passwordValidationStatus == ValidationUtils.ValidationState.Valid &&
            confirmPasswordValidationStatus == ValidationUtils.ValidationState.Valid &&
            termsAccepted) {

            val phone = if (phoneNumber.isNotEmpty()) {
                "+${phoneRegion.phoneCode}$phoneNumber"
            } else {
                ""
            }

            register(email, phone, password)

        } else {
            updateView()
        }
    }

    private fun register(email: String, phone: String, password: String) {
        launch {
            setEffect(RegistrationContract.Effect.ToggleLoader(true))

            authInteractor.register(email, phone, password)
                .onSuccess {
                    setEffect(RegistrationContract.Effect.ToggleLoader(false))

                    val phoneVerificationAvailable = phone.isNotEmpty() &&
                            phoneRegionPolicy.isPhoneRegionSupported(phoneRegion)

                    if (email.isNotEmpty() && phoneVerificationAvailable) {
                        setEffect(RegistrationContract.Effect.ShowVerificationMethodBottomSheet)

                    } else if (email.isNotEmpty()) {
                        setEffect(RegistrationContract.Effect.NavigateToEmailOtp(email))

                    } else if (phoneVerificationAvailable) {
                        setEffect(RegistrationContract.Effect.NavigateToPhoneOtp(phone))
                    }
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        RegistrationContract.Effect.ToggleLoader(false),
                        RegistrationContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun onEmailVerificationMethodChosen() {
        setEffect(RegistrationContract.Effect.NavigateToEmailOtp(email))
    }

    private fun onPhoneVerificationMethodChosen() {
        if (!phoneRegionPolicy.isPhoneRegionSupported(phoneRegion)) {
            setEffect(
                RegistrationContract.Effect.ShowMessage(
                    resourceProvider.getString(R.string.phone_verification_unavailable_settings)
                )
            )
            return
        }

        val phone = "+${phoneRegion.phoneCode}$phoneNumber"
        setEffect(RegistrationContract.Effect.NavigateToPhoneOtp(phone))
    }

    private fun onLoginClick() {
        setEffect(RegistrationContract.Effect.NavigateLogin)
    }

    private fun onGoogleClick(activity: Activity) {
        launch {
            when (val result = GoogleSignInHelper.signIn(activity)) {
                is GoogleSignInResult.Success -> authGoogle(result.idToken)
                GoogleSignInResult.Cancelled -> Unit
                GoogleSignInResult.NoCredential -> {
                    setEffect(
                        RegistrationContract.Effect.ShowMessage(
                            resourceProvider.getString(R.string.google_sign_in_no_credentials)
                        )
                    )
                }
                GoogleSignInResult.Failed -> {
                    setEffect(
                        RegistrationContract.Effect.ShowMessage(
                            resourceProvider.getString(R.string.google_sign_in_failed)
                        )
                    )
                }
            }
        }
    }

    private fun authGoogle(token: String) {
        setEffect(RegistrationContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.authGoogle(token)
                .onSuccess {
                    sharedPreferencesProvider.saveAuthToken(it?.accessToken)
                    sharedPreferencesProvider.saveRefreshToken(it?.refreshToken)

                    setEffect(
                        RegistrationContract.Effect.ToggleLoader(false),
                        RegistrationContract.Effect.NavigateMainActivity(true)
                    )
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        RegistrationContract.Effect.ToggleLoader(false),
                        RegistrationContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun onChangeLanguage(context: Context, languageCode: String) {
        appLocaleManager.changeLanguage(context, languageCode)
        selectedLanguage = languageCode
    }

    private fun onOtpSuccess(username: String) {
        login(username, password)
    }

    private fun login(username: String, password: String) {
        setEffect(RegistrationContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.login(username, password)
                .onSuccess {
                    sharedPreferencesProvider.saveAuthToken(it?.accessToken)
                    sharedPreferencesProvider.saveRefreshToken(it?.refreshToken)

                    setEffect(
                        RegistrationContract.Effect.ToggleLoader(false),
                        RegistrationContract.Effect.NavigateMainActivity(true)
                    )
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        RegistrationContract.Effect.ToggleLoader(false),
                        RegistrationContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }
}
