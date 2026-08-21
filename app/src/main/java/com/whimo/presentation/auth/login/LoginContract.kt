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
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.presentation.auth.registration.RegistrationAlreadyRegisteredResult
import com.whimo.presentation.ui.models.Languages
import com.whimo.utils.PhoneNumberUtils

enum class LoginTypeTab(val tabNameRes: Int) {
    Email(R.string.login_with_email),
    Phone(R.string.login_with_phone)
}

object LoginContract {
    data class Binding(
        var currentTab: LoginTypeTab? = null,
        var email: String = "",
        var phoneRegion: PhoneNumberUtils.PhoneRegion = PhoneNumberUtils.getDefaultPhoneRegion(),
        var phoneNumber: String = "",
        var password: String = "",

        var emailError: String = "",
        var phoneError: String = "",
        var passwordError: String = "",

        var loginEnabled: Boolean = false,

        var selectedLanguage: String = Languages.ENGLISH.languageCode,
        var prefillGeneration: Long = 0L,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data class OnCreate(val context: Context) : Event()

        data class OnTabChanged(val tab: LoginTypeTab) : Event()

        data class OnEmailChanged(val email: String) : Event()
        data class OnPhoneRegionChanged(val phoneRegion: PhoneNumberUtils.PhoneRegion) : Event()
        data class OnPhoneChanged(val phone: String) : Event()
        data class OnPasswordChanged(val password: String) : Event()

        data object OnForgotPasswordClick : Event()

        data object OnLoginClick : Event()
        data class OnGoogleClick(val activity: Activity) : Event()
        data class OnGoogleLongClick(val activity: Activity) : Event()
        data object OnRegisterClick : Event()
        data class OnChangeLanguage(val context: Context, val languageCode: String) : Event()
        data class OnRegistrationAlreadyRegistered(
            val result: RegistrationAlreadyRegisteredResult,
        ) : Event()

        data class OnOtpSuccess(val username: String) : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data object RequestLocationPermission: Effect()
        data class ToggleLoader(val isLoading: Boolean) : Effect()
        data class ShowMessage(val message: String): Effect()

        data object NavigateRegistration : Effect()
        data object NavigateForgotPassword : Effect()

        data class NavigateMainActivity(val call: Boolean?) : Effect()
        data class NavigateToEmailOtp(val email: String) : Effect()
        data class NavigateToPhoneOtp(val phone: String) : Effect()
    }
}
