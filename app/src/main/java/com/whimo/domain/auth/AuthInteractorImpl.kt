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
package com.whimo.domain.auth

import com.whimo.data.auth.repository.AuthRepository
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.auth.models.AuthModel
import com.whimo.domain.auth.models.BaseModel
import com.whimo.providers.SharedPreferencesProvider

class AuthInteractorImpl(
    private val authRepository: AuthRepository,
    private val sharedPreferencesProvider: SharedPreferencesProvider
) : AuthInteractor {

    override suspend fun login(username: String, password: String): BaseResult<AuthModel> {
        return authRepository.login(username, password)
    }

    override suspend fun register(
        email: String?,
        phone: String?,
        password: String
    ): BaseResult<AuthModel> {
        return authRepository.register(email = email, phone = phone, password = password)
    }

    override suspend fun authGoogle(token: String): BaseResult<AuthModel> {
        return authRepository.authGoogle(token)
    }

    override suspend fun sendOtp(username: String?, captchaToken: String?): BaseResult<BaseModel> {
        return authRepository.sendOtp(username, captchaToken)
    }

    override suspend fun verifyOtp(username: String?, code: String?): BaseResult<BaseModel> {
        return authRepository.verifyOtp(username = username, code = code)
    }

    override suspend fun passwordResetSend(username: String?, captchaToken: String?): BaseResult<BaseModel> {
        return authRepository.passwordResetSend(
            username = username,
            captchaToken = captchaToken
        )
    }

    override suspend fun passwordResetVerify(
        username: String?,
        password: String?,
        code: String?
    ): BaseResult<BaseModel> {
        return authRepository.passwordResetVerify(
            username = username,
            password = password,
            code = code
        )
    }

    override suspend fun passwordResetOtpCheck(
        username: String?,
        code: String?
    ): BaseResult<BaseModel> {
        return authRepository.passwordResetOtpCheck(
            username = username,
            code = code
        )
    }

    override suspend fun isAuthenticated(): Boolean {
        return !sharedPreferencesProvider.getAuthToken().isNullOrEmpty()
    }
} 
