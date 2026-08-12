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

import android.content.Context
import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect
import com.whimo.utils.PhoneNumberUtils

object EditPhoneContract {
    data class Binding(
        var phoneRegion: PhoneNumberUtils.PhoneRegion = PhoneNumberUtils.getDefaultPhoneRegion(),
        var phoneNumber: String = "",
        var phoneError: String = "",
        var saveEnabled: Boolean = true,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data class OnCreate(val context: Context, val phone: String?) : Event()
        data class OnPhoneRegionChanged(val phoneRegion: PhoneNumberUtils.PhoneRegion) : Event()
        data class OnPhoneChanged(val phone: String) : Event()
        data object OnSaveClick : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data object RequestLocationPermission: Effect()
        data class ToggleLoader(val isLoading: Boolean): Effect()
        data class ShowMessage(val message: String): Effect()
        data class NavigateToPhoneOtp(val phone: String): Effect()

        data object ForceUpdateFields : Effect()
    }
}
