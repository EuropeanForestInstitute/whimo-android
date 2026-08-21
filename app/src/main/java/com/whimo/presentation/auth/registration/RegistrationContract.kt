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
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.presentation.ui.models.Languages
import com.whimo.utils.PhoneNumberUtils
import java.io.Serializable

const val REGISTRATION_ALREADY_REGISTERED_RESULT_KEY = "registrationAlreadyRegistered"

data class RegistrationAlreadyRegisteredResult(
    val registrationType: RegistrationTypeTab,
    val email: String,
    val phoneCountryCode: String,
    val phoneNumber: String,
    val password: String,
) : Serializable

enum class RegistrationTypeTab(val tabNameRes: Int) {
    Email(R.string.register_with_email),
    Phone(R.string.register_with_phone)
}

object RegistrationContract {
    data class Binding(
        var currentTab: RegistrationTypeTab = RegistrationTypeTab.Email,
        var email: String = "",
        var phoneRegion: PhoneNumberUtils.PhoneRegion = PhoneNumberUtils.getDefaultPhoneRegion(),
        var phoneNumber: String = "",
        var password: String = "",
        var confirmPassword: String = "",

        var emailError: String = "",
        var phoneError: String = "",
        var passwordError: String = "",
        var confirmPasswordError: String = "",

        var termsAccepted: Boolean = false,
        var registrationEnabled: Boolean = false,

        var selectedLanguage: String = Languages.ENGLISH.languageCode,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data class OnCreate(val context: Context) : Event()

        data class OnTabChanged(val tab: RegistrationTypeTab) : Event()

        data class OnEmailChanged(val email: String) : Event()
        data class OnPhoneRegionChanged(val phoneRegion: PhoneNumberUtils.PhoneRegion) : Event()
        data class OnPhoneChanged(val phone: String) : Event()
        data class OnPhoneFocusChanged(val isFocused: Boolean) : Event()
        data class OnPasswordChanged(val password: String) : Event()
        data class OnConfirmPasswordChanged(val confirmPassword: String) : Event()
        data class OnTermsAcceptanceChange(val termsAccepted: Boolean) : Event()
        data object OnTermsClick : Event()

        data object OnRegisterClick : Event()

        data class OnGoogleClick(val activity: Activity) : Event()
        data object OnLoginClick : Event()
        data class OnChangeLanguage(val context: Context, val languageCode: String) : Event()

        data class OnOtpSuccess(val username: String) : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data object RequestLocationPermission: Effect()
        data class ToggleLoader(val isLoading: Boolean) : Effect()
        data class ShowMessage(val message: String): Effect()

        data object NavigateTerms : Effect()

        data object NavigateLogin : Effect()
        data class NavigateLoginWithAlreadyRegisteredAlert(
            val result: RegistrationAlreadyRegisteredResult,
        ) : Effect()

        data class NavigateMainActivity(val call: Boolean?) : Effect()

        data class NavigateToEmailOtp(val email: String) : Effect()
        data class NavigateToPhoneOtp(val phone: String) : Effect()
    }
}
