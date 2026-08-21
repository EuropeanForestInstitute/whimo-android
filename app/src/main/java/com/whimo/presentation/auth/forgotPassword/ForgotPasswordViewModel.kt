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
package com.whimo.presentation.auth.forgotPassword

import android.content.Context
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.domain.auth.AuthInteractor
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.ValidationUtils
import com.whimo.utils.getLastLocation

class ForgotPasswordViewModel(
    private val authInteractor: AuthInteractor,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel<ForgotPasswordContract.Binding>() {

    private var state: ForgotPasswordScreenState = ForgotPasswordScreenState.Email
    private var email: String = ""
    private var phoneRegion = PhoneNumberUtils.getDefaultPhoneRegion()
    private var phoneNumber: String = ""
    private var password: String = ""
    private var emailError: String = ""
    private var phoneError: String = ""
    private var passwordError: String = ""
    private var phoneRegionSelectedManually: Boolean = false

    override fun createBinding(): ForgotPasswordContract.Binding {
        return ForgotPasswordContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is ForgotPasswordContract.Event.OnCreate -> onCreate(event.context, event.email, event.phone)
            is ForgotPasswordContract.Event.OnEmailChanged -> onEmailChanged(event.email)
            is ForgotPasswordContract.Event.OnPhoneRegionChanged -> onPhoneRegionChanged(event.phoneRegion)
            is ForgotPasswordContract.Event.OnPhoneChanged -> onPhoneChanged(event.phone)
            is ForgotPasswordContract.Event.OnSendCodeClick -> onSendCodeClick()
            is ForgotPasswordContract.Event.OnChangeStatusClick -> onChangeStatusClick()
        }
    }

    override fun copyBinding(binding: ForgotPasswordContract.Binding): ForgotPasswordContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        updateBinding { b ->
            b.state = state
            b.email = email
            b.phoneNumber = phoneNumber
            b.phoneRegion = phoneRegion
            b.password = password
            b.emailError = emailError
            b.phoneError = phoneError
            b.passwordError = passwordError

            when (state) {
                ForgotPasswordScreenState.Email -> {
                    b.description = resourceProvider.getString(R.string.forgot_pass_instructions_email)
                    b.changeStatusButtonTitle = resourceProvider.getString(R.string.continue_with_phone_number)
                    b.sendCodeEnabled = email.isNotEmpty()
                }
                ForgotPasswordScreenState.Phone -> {
                    b.description = resourceProvider.getString(R.string.forgot_pass_instructions_phone)
                    b.changeStatusButtonTitle = resourceProvider.getString(R.string.continue_with_email)
                    b.sendCodeEnabled = phoneNumber.isNotEmpty()
                }
            }
        }
    }

    private fun onCreate(context: Context, email: String?, phone: String?) {
        if (phone != null) {
            this.state = ForgotPasswordScreenState.Phone
            val parsedPhone = PhoneNumberUtils.parsePhone(phone)
            this.phoneRegion = parsedPhone.first
            this.phoneNumber = "${parsedPhone.second}"
            this.phoneRegionSelectedManually = true
        }

        if (email != null) {
            state = ForgotPasswordScreenState.Email
        }

        updateView()

        if (phoneRegionSelectedManually) {
            return
        }

        launch {
            val location = getLastLocation(context)

            if (location == null) {
                setEffect(ForgotPasswordContract.Effect.RequestLocationPermission)

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

    private fun onSendCodeClick() {
        when (state) {
            ForgotPasswordScreenState.Email -> {
                val validationStatus = ValidationUtils.validateEmail(email)

                emailError = when(validationStatus) {
                    ValidationUtils.ValidationState.Empty -> "empty"
                    ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_email)
                    ValidationUtils.ValidationState.Valid -> ""
                }

                if (validationStatus == ValidationUtils.ValidationState.Valid) {
                    setEffect(ForgotPasswordContract.Effect.NavigateToEmailOtp(email))
                } else {
                    updateView()
                }
            }
            ForgotPasswordScreenState.Phone -> {
                val validationStatus = ValidationUtils.validatePhoneNumber(phoneRegion.phoneCode, phoneNumber)

                phoneError = when(validationStatus) {
                    ValidationUtils.ValidationState.Empty -> "empty"
                    ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_phone)
                    ValidationUtils.ValidationState.Valid -> ""
                }

                if (validationStatus == ValidationUtils.ValidationState.Valid) {
                    val phone = "+${phoneRegion.phoneCode}$phoneNumber"
                    setEffect(ForgotPasswordContract.Effect.NavigateToPhoneOtp(phone))
                } else {
                    updateView()
                }
            }
        }
    }

    private fun onChangeStatusClick() {
        state = if (state == ForgotPasswordScreenState.Email) {
            ForgotPasswordScreenState.Phone
        } else {
            ForgotPasswordScreenState.Email
        }
        updateView()
    }
}
