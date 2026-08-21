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

import com.whimo.base.CoreViewBinding
import com.whimo.base.CoreViewEvent
import com.whimo.base.CoreViewSideEffect

enum class EnterCodeScreenState { Registration, ForgotPassword }

object EnterCodeContract {
    data class Binding(
        var title: String = "",
        var description: String = "",
        var username: String = "",
        var code: String = "",
        var codeError: String = "",
        var confirmEnabled: Boolean = false,
    ) : CoreViewBinding

    sealed class Event : CoreViewEvent {
        data class OnCreate(val state: EnterCodeScreenState, val email: String?, val phone: String?) : Event()
        data class OnCodeChange(val code: String) : Event()
        data object OnConfirm : Event()
        data object OnRequestAgain : Event()
        data class OnCaptchaTokenReceived(val token: String) : Event()
        data object OnCaptchaFailed : Event()
        data object OnCaptchaUnavailable : Event()
        data object OnCaptchaDismissed : Event()
    }

    sealed class Effect : CoreViewSideEffect {
        data object RequestCaptcha : Effect()
        data class ToggleLoader(val isLoading: Boolean) : Effect()
        data class ShowMessage(val message: String): Effect()
        data class NavigateCreateNewPassScreen(val username: String, val code: String) : Effect()
        data class PopBack(val username: String?) : Effect()
    }
}
