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
package com.whimo.data.auth.repository

import com.whimo.data.auth.model.request.GoogleAuthRequest
import com.whimo.data.auth.model.request.LoginRequest
import com.whimo.data.auth.model.request.OtpRequest
import com.whimo.data.auth.model.request.RegistrationRequest
import com.whimo.data.auth.model.response.toDomain
import com.whimo.data.auth.service.AuthService
import com.whimo.data.auth.service.OtpService
import com.whimo.data.base.common.BaseResult
import com.whimo.data.base.response.toDomain
import com.whimo.domain.auth.models.AuthModel
import com.whimo.domain.auth.models.BaseModel
import com.whimo.network.handleResponse
import com.whimo.network.mapResult
import com.whimo.providers.SharedPreferencesProvider

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val otpService: OtpService,
    private val preferencesProvider: SharedPreferencesProvider
) : AuthRepository {

    override suspend fun login(username: String?, password: String?): BaseResult<AuthModel> {
        return handleResponse {
            authService.login(LoginRequest(username, password))
        }.mapResult { it?.toDomain() }
    }

    override suspend fun register(
        email: String?,
        phone: String?,
        password: String?
    ): BaseResult<AuthModel> {
        return handleResponse {
            authService.register(
                RegistrationRequest(
                    email = email,
                    phone = phone,
                    password = password
                )
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun authGoogle(token: String): BaseResult<AuthModel> {
        return handleResponse {
            authService.authGoogle(
                GoogleAuthRequest(id_token = token)
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun sendOtp(username: String?, captchaToken: String?): BaseResult<BaseModel> {
        return handleResponse {
            otpService.sendOtp(OtpRequest(identifier = username, captcha_token = captchaToken))
        }.mapResult { it?.toDomain() }
    }

    override suspend fun verifyOtp(username: String?, code: String?): BaseResult<BaseModel> {
        return handleResponse {
            otpService.verifyOtp(OtpRequest(identifier = username, code = code))
        }.mapResult { it?.toDomain() }
    }

    override suspend fun passwordResetSend(username: String?, captchaToken: String?): BaseResult<BaseModel> {
        return handleResponse {
            authService.passwordResetSend(OtpRequest(identifier = username, captcha_token = captchaToken))
        }.mapResult { it?.toDomain() }
    }

    override suspend fun passwordResetOtpCheck(
        username: String?,
        code: String?
    ): BaseResult<BaseModel> {
        return handleResponse {
            authService.passwordResetCheck(
                OtpRequest(
                    identifier = username,
                    code = code
                )
            )
        }.mapResult { it?.toDomain() }
    }

    override suspend fun passwordResetVerify(
        username: String?,
        password: String?,
        code: String?
    ): BaseResult<BaseModel> {
        return handleResponse {
            authService.passwordResetVerify(
                OtpRequest(
                    identifier = username,
                    password = password,
                    code = code
                )
            )
        }.mapResult { it?.toDomain() }
    }
} 
