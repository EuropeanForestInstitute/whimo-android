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

import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.auth.AuthInteractor
import com.whimo.network.ErrorHandler
import com.whimo.network.error.ServerError
import com.whimo.providers.ResourceProvider

private enum class OtpRequestAction { Registration, PasswordReset }

class EnterCodeViewModel(
    private val authInteractor: AuthInteractor,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel<EnterCodeContract.Binding>() {

    private var state: EnterCodeScreenState = EnterCodeScreenState.Registration
    private var email: String? = null
    private var phone: String? = null
    private var username: String = ""
    private var code: String = ""
    private var codeError: String = ""

    private var autoConfirm: Boolean = true
    private var pendingOtpRequestAction: OtpRequestAction? = null

    override fun createBinding(): EnterCodeContract.Binding {
        return EnterCodeContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is EnterCodeContract.Event.OnCreate -> onCreate(event.state, event.email, event.phone)
            is EnterCodeContract.Event.OnCodeChange -> onCodeChange(event.code)
            is EnterCodeContract.Event.OnConfirm -> onConfirm()
            is EnterCodeContract.Event.OnRequestAgain -> onRequestAgain()
            is EnterCodeContract.Event.OnCaptchaTokenReceived -> onCaptchaTokenReceived(event.token)
            is EnterCodeContract.Event.OnCaptchaFailed -> onCaptchaFailed()
            is EnterCodeContract.Event.OnCaptchaUnavailable -> onCaptchaUnavailable()
            is EnterCodeContract.Event.OnCaptchaDismissed -> onCaptchaDismissed()
        }
    }

    override fun copyBinding(binding: EnterCodeContract.Binding): EnterCodeContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        updateBinding { b ->
            if (!email.isNullOrEmpty()) {
                b.title = resourceProvider.getString(R.string.check_your_email_inbox)
            } else {
                b.title = resourceProvider.getString(R.string.check_your_sms_messages)
            }
            if (state == EnterCodeScreenState.Registration) {
                b.description = resourceProvider.getString(R.string.check_inbox_instructions_verify, username)
            } else {
                b.description = resourceProvider.getString(R.string.check_inbox_instructions, username)
            }
            b.username = username
            b.code = code
            b.codeError = codeError
            b.confirmEnabled = code.length == 6
        }
    }

    private fun onCreate(state: EnterCodeScreenState, email: String?, phone: String?) {
        this.state = state
        this.email = email
        this.phone = phone
        this.username = email ?: phone ?: ""
        updateView()

        onRequestAgain()
    }

    private fun onCodeChange(code: String) {
        this.code = code
        this.codeError = ""
        updateView()

        if (code.length == 6 && autoConfirm) {
            autoConfirm = false
            onConfirm()
        }
    }

    private fun onConfirm() {
        when (state) {
            EnterCodeScreenState.Registration -> confirmAuthOtpCode()
            EnterCodeScreenState.ForgotPassword -> confirmForgotPassOtpCode()
        }
    }

    private fun onRequestAgain() {
        pendingOtpRequestAction = when (state) {
            EnterCodeScreenState.Registration -> OtpRequestAction.Registration
            EnterCodeScreenState.ForgotPassword -> OtpRequestAction.PasswordReset
        }
        setEffect(EnterCodeContract.Effect.RequestCaptcha)
    }

    private fun onCaptchaTokenReceived(captchaToken: String) {
        val action = pendingOtpRequestAction ?: when (state) {
            EnterCodeScreenState.Registration -> OtpRequestAction.Registration
            EnterCodeScreenState.ForgotPassword -> OtpRequestAction.PasswordReset
        }
        pendingOtpRequestAction = null

        if (captchaToken.isBlank()) {
            onCaptchaFailed()
            return
        }

        when (action) {
            OtpRequestAction.Registration -> requestOtp(captchaToken)
            OtpRequestAction.PasswordReset -> resetPassword(captchaToken)
        }
    }

    private fun onCaptchaFailed() {
        pendingOtpRequestAction = null
        setEffect(EnterCodeContract.Effect.ShowMessage(resourceProvider.getString(R.string.captcha_verification_failed)))
    }

    private fun onCaptchaUnavailable() {
        pendingOtpRequestAction = null
        setEffect(EnterCodeContract.Effect.ShowMessage(resourceProvider.getString(R.string.captcha_not_configured)))
    }

    private fun onCaptchaDismissed() {
        pendingOtpRequestAction = null
    }

    private fun requestOtp(captchaToken: String) {
        setEffect(EnterCodeContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.sendOtp(username, captchaToken)
                .onSuccess {
                    setEffect(EnterCodeContract.Effect.ToggleLoader(false))
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        EnterCodeContract.Effect.ToggleLoader(false),
                        EnterCodeContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun resetPassword(captchaToken: String) {
        setEffect(EnterCodeContract.Effect.ToggleLoader(true))

        launch {
            authInteractor.passwordResetSend(username, captchaToken)
                .onSuccess {
                    setEffect(EnterCodeContract.Effect.ToggleLoader(false))
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        EnterCodeContract.Effect.ToggleLoader(false),
                        EnterCodeContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun confirmAuthOtpCode() {
        launch {
            setEffect(EnterCodeContract.Effect.ToggleLoader(true))

            authInteractor.verifyOtp(username = username, code = code)
                .onSuccess {
                    setEffect(
                        EnterCodeContract.Effect.ToggleLoader(false),
                        EnterCodeContract.Effect.PopBack(username)
                    )
                }
                .onError {
                    setEffect(EnterCodeContract.Effect.ToggleLoader(false))

                    if (it is ServerError && it.code == 400) {
                        codeError = "Invalid code"
                        updateView()

                    } else {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(EnterCodeContract.Effect.ShowMessage(errorMessage))
                    }
                }
        }
    }

    private fun confirmForgotPassOtpCode() {
        launch {
            authInteractor.passwordResetOtpCheck(username = username, code = code)
                .onSuccess {
                    setEffect(
                        EnterCodeContract.Effect.ToggleLoader(false),
                        EnterCodeContract.Effect.NavigateCreateNewPassScreen(username, code)
                    )
                }
                .onError {
                    setEffect(EnterCodeContract.Effect.ToggleLoader(false))

                    if (it is ServerError && it.code == 400) {
                        codeError = "Invalid code"
                        updateView()

                    } else {
                        val errorMessage = errorHandler.parseError(it)
                        setEffect(EnterCodeContract.Effect.ShowMessage(errorMessage))
                    }
                }
        }
    }
}
