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
package com.whimo.presentation.auth.login

import android.app.Activity
import android.content.Context
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.auth.AuthInteractor
import com.whimo.network.ErrorHandler
import com.whimo.network.error.ServerError
import com.whimo.presentation.auth.registration.RegistrationAlreadyRegisteredResult
import com.whimo.presentation.auth.registration.RegistrationTypeTab
import com.whimo.presentation.ui.models.Languages
import com.whimo.providers.ResourceProvider
import com.whimo.providers.SharedPreferencesProvider
import com.whimo.utils.AppLocaleManager
import com.whimo.utils.GoogleSignInHelper
import com.whimo.utils.GoogleSignInResult
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.ValidationUtils
import com.whimo.utils.getLastLocation

class LoginViewModel(
    private val authInteractor: AuthInteractor,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val appLocaleManager: AppLocaleManager,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel<LoginContract.Binding>() {

    private var currentTab: LoginTypeTab = LoginTypeTab.entries.first()
    private var email: String = ""
    private var phoneRegion = PhoneNumberUtils.getDefaultPhoneRegion()
    private var phoneNumber: String = ""
    private var password: String = ""
    private var emailError: String = ""
    private var phoneError: String = ""
    private var passwordError: String = ""
    private var selectedLanguage: String = Languages.ENGLISH.languageCode
    private var phoneRegionSelectedManually: Boolean = false
    private var prefillGeneration: Long = 0L

    override fun createBinding(): LoginContract.Binding {
        return LoginContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is LoginContract.Event.OnCreate -> onCreate(event.context)
            is LoginContract.Event.OnTabChanged -> onTabChanged(event.tab)
            is LoginContract.Event.OnEmailChanged -> onEmailChanged(event.email)
            is LoginContract.Event.OnPhoneRegionChanged -> onPhoneRegionChanged(event.phoneRegion)
            is LoginContract.Event.OnPhoneChanged -> onPhoneChanged(event.phone)
            is LoginContract.Event.OnPasswordChanged -> onPasswordChanged(event.password)
            is LoginContract.Event.OnForgotPasswordClick -> onForgotPasswordClick()
            is LoginContract.Event.OnLoginClick -> onLoginClick()
            is LoginContract.Event.OnGoogleClick -> onGoogleClick(event.activity)
            is LoginContract.Event.OnGoogleLongClick -> onGoogleLongClick(event.activity)
            is LoginContract.Event.OnRegisterClick -> onRegisterClick()
            is LoginContract.Event.OnChangeLanguage -> onChangeLanguage(event.context, event.languageCode)
            is LoginContract.Event.OnRegistrationAlreadyRegistered -> onRegistrationAlreadyRegistered(event.result)
            is LoginContract.Event.OnOtpSuccess -> onOtpSuccess(event.username)
        }
    }

    override fun copyBinding(binding: LoginContract.Binding): LoginContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        updateBinding { b ->
            b.currentTab = currentTab
            b.email = email
            b.phoneNumber = phoneNumber
            b.phoneRegion = phoneRegion
            b.password = password
            b.emailError = emailError
            b.phoneError = phoneError
            b.passwordError = passwordError
            b.loginEnabled = when (currentTab) {
                LoginTypeTab.Email -> {
                    email.isNotEmpty() && password.isNotEmpty()
                }
                LoginTypeTab.Phone -> {
                    phoneNumber.isNotEmpty() && password.isNotEmpty()
                }
            }
            b.selectedLanguage = selectedLanguage
            b.prefillGeneration = prefillGeneration
        }
    }

    private fun onCreate(context: Context) {
        selectedLanguage = appLocaleManager.getLanguageCode(context)
        updateView()

        if (phoneRegionSelectedManually) {
            return
        }

        launch {
            val location = getLastLocation(context)

            if (location == null) {
                setEffect(LoginContract.Effect.RequestLocationPermission)

            } else {
                PhoneNumberUtils.getCountryCodeFromLocation(context, location)?.let { countryCode ->
                    if (!phoneRegionSelectedManually) {
                        phoneRegion = PhoneNumberUtils.getPhoneRegion(countryCode)
                        updateView()
                    }
                }
            }
        }
    }

    private fun onTabChanged(tab: LoginTypeTab) {
        currentTab = tab
        updateView()
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

    private fun onPhoneRegionChanged(phoneRegion: PhoneNumberUtils.PhoneRegion) {
        this.phoneRegion = phoneRegion
        this.phoneRegionSelectedManually = true
        updateView()
    }

    private fun onPasswordChanged(password: String) {
        this.password = password
        this.passwordError = ""
        updateView()
    }

    private fun onForgotPasswordClick() {
        setEffect(LoginContract.Effect.NavigateForgotPassword)
    }

    private fun onLoginClick() {
        when (currentTab) {
            LoginTypeTab.Email -> {
                val validationStatus = ValidationUtils.validateEmail(email)

                emailError = when(validationStatus) {
                    ValidationUtils.ValidationState.Empty -> "empty"
                    ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_email)
                    ValidationUtils.ValidationState.Valid -> ""
                }

                if (validationStatus == ValidationUtils.ValidationState.Valid) {
                    login(email, password) {
                        setEffect(LoginContract.Effect.NavigateToEmailOtp(email))
                    }
                } else {
                    updateView()
                }
            }
            LoginTypeTab.Phone -> {
                val validationStatus = ValidationUtils.validatePhoneNumber(phoneRegion.phoneCode, phoneNumber)

                phoneError = when(validationStatus) {
                    ValidationUtils.ValidationState.Empty -> "empty"
                    ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_phone)
                    ValidationUtils.ValidationState.Valid -> ""
                }

                if (validationStatus == ValidationUtils.ValidationState.Valid) {
                    val phone = "+${phoneRegion.phoneCode}$phoneNumber"
                    login(phone, password) {
                        setEffect(LoginContract.Effect.NavigateToPhoneOtp(phone))
                    }
                } else {
                    updateView()
                }
            }
        }
    }

    private fun onGoogleClick(activity: Activity) {
        launch {
            handleGoogleSignInResult(GoogleSignInHelper.signIn(activity))
        }
    }

    private fun onGoogleLongClick(activity: Activity) {
        launch {
            handleGoogleSignInResult(GoogleSignInHelper.signIn(activity, false))
        }
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Success -> authGoogle(result.idToken)
            GoogleSignInResult.Cancelled -> Unit
            GoogleSignInResult.NoCredential -> {
                setEffect(
                    LoginContract.Effect.ShowMessage(
                        resourceProvider.getString(R.string.google_sign_in_no_credentials)
                    )
                )
            }
            GoogleSignInResult.Failed -> {
                setEffect(
                    LoginContract.Effect.ShowMessage(
                        resourceProvider.getString(R.string.google_sign_in_failed)
                    )
                )
            }
        }
    }

    private fun authGoogle(token: String) {
        setEffect(LoginContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.authGoogle(token)
                .onSuccess {
                    sharedPreferencesProvider.saveAuthToken(it?.accessToken)
                    sharedPreferencesProvider.saveRefreshToken(it?.refreshToken)

                    setEffect(
                        LoginContract.Effect.ToggleLoader(false),
                        LoginContract.Effect.NavigateMainActivity(true)
                    )
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        LoginContract.Effect.ToggleLoader(false),
                        LoginContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun onRegisterClick() {
        setEffect(LoginContract.Effect.NavigateRegistration)
    }

    private fun onChangeLanguage(context: Context, languageCode: String) {
        appLocaleManager.changeLanguage(context, languageCode)
        selectedLanguage = languageCode
    }

    private fun onRegistrationAlreadyRegistered(result: RegistrationAlreadyRegisteredResult) {
        emailError = ""
        phoneError = ""
        passwordError = ""
        password = result.password

        when (result.registrationType) {
            RegistrationTypeTab.Email -> {
                currentTab = LoginTypeTab.Email
                email = result.email
                phoneNumber = ""
            }
            RegistrationTypeTab.Phone -> {
                currentTab = LoginTypeTab.Phone
                email = ""
                phoneRegion = PhoneNumberUtils.getPhoneRegion(result.phoneCountryCode)
                phoneNumber = result.phoneNumber
                phoneRegionSelectedManually = true
            }
        }

        prefillGeneration += 1
        updateView()
    }

    private fun onOtpSuccess(username: String) {
        login(username, password) {}
    }

    private fun login(username: String, password: String, needConfirmOtp: () -> Unit) {
        setEffect(LoginContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.login(username, password)
                .onSuccess {
                    sharedPreferencesProvider.saveAuthToken(it?.accessToken)
                    sharedPreferencesProvider.saveRefreshToken(it?.refreshToken)

                    setEffect(
                        LoginContract.Effect.ToggleLoader(false),
                        LoginContract.Effect.NavigateMainActivity(true)
                    )
                }
                .onError {
                    if (it is ServerError && it.code == 403) {
                        setEffect(LoginContract.Effect.ToggleLoader(false))
                        needConfirmOtp()
                    } else {
                        val errorMessage = errorHandler.parseError(it)

                        setEffect(
                            LoginContract.Effect.ToggleLoader(false),
                            LoginContract.Effect.ShowMessage(errorMessage)
                        )
                    }
                }
        }
    }
} 
