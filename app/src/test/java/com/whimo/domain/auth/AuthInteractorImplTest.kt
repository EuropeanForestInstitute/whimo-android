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
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthInteractorImplTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var sharedPreferencesProvider: SharedPreferencesProvider
    private lateinit var authInteractor: AuthInteractorImpl

    @Before
    fun setUp() {
        authRepository = mockk()
        sharedPreferencesProvider = mockk()
        authInteractor = AuthInteractorImpl(authRepository, sharedPreferencesProvider)
    }

    @Test
    fun `register calls repository with correct parameters`() = runTest {
        // Given
        val email = "test@example.com"
        val phone = "1234567890"
        val password = "password123"
        val expectedResult = BaseResult.Success(
            AuthModel(
                accessToken = "token",
                refreshToken = "refresh",
                code = "200",
                message = "Success",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.register(email, phone, password) } returns expectedResult

        // When
        val result = authInteractor.register(email, phone, password)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `register forwards null for unselected phone gadget`() = runTest {
        // Given
        val email = "test@example.com"
        val phone: String? = null
        val password = "password123"
        val expectedResult = BaseResult.Success(
            AuthModel(
                accessToken = "token",
                refreshToken = "refresh",
                code = "200",
                message = "Success",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.register(email, phone, password) } returns expectedResult

        // When
        val result = authInteractor.register(email, phone, password)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `register forwards null for unselected email gadget`() = runTest {
        // Given
        val email: String? = null
        val phone = "+1234567890"
        val password = "password123"
        val expectedResult = BaseResult.Success(
            AuthModel(
                accessToken = "token",
                refreshToken = "refresh",
                code = "200",
                message = "Success",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.register(email, phone, password) } returns expectedResult

        // When
        val result = authInteractor.register(email, phone, password)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `authGoogle calls repository with correct token`() = runTest {
        // Given
        val token = "google_token"
        val expectedResult = BaseResult.Success(
            AuthModel(
                accessToken = "token",
                refreshToken = "refresh",
                code = "200",
                message = "Success",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.authGoogle(token) } returns expectedResult

        // When
        val result = authInteractor.authGoogle(token)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `sendOtp calls repository with correct username and captcha token`() = runTest {
        // Given
        val username = "test@example.com"
        val captchaToken = "captcha-token"
        val expectedResult = BaseResult.Success(
            BaseModel(
                success = true,
                message = "OTP sent",
                code = "200",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.sendOtp(username, captchaToken) } returns expectedResult

        // When
        val result = authInteractor.sendOtp(username, captchaToken)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `verifyOtp calls repository with correct parameters`() = runTest {
        // Given
        val username = "test@example.com"
        val code = "123456"
        val expectedResult = BaseResult.Success(
            BaseModel(
                success = true,
                message = "OTP verified",
                code = "200",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.verifyOtp(username, code) } returns expectedResult

        // When
        val result = authInteractor.verifyOtp(username, code)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `passwordResetSend calls repository with correct username and captcha token`() = runTest {
        // Given
        val username = "test@example.com"
        val captchaToken = "captcha-token"
        val expectedResult = BaseResult.Success(
            BaseModel(
                success = true,
                message = "Reset email sent",
                code = "200",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.passwordResetSend(username, captchaToken) } returns expectedResult

        // When
        val result = authInteractor.passwordResetSend(username, captchaToken)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `passwordResetVerify calls repository with correct parameters`() = runTest {
        // Given
        val username = "test@example.com"
        val password = "newpassword123"
        val code = "123456"
        val expectedResult = BaseResult.Success(
            BaseModel(
                success = true,
                message = "Password reset successful",
                code = "200",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.passwordResetVerify(username, password, code) } returns expectedResult

        // When
        val result = authInteractor.passwordResetVerify(username, password, code)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `passwordResetOtpCheck calls repository with correct parameters`() = runTest {
        // Given
        val username = "test@example.com"
        val code = "123456"
        val expectedResult = BaseResult.Success(
            BaseModel(
                success = true,
                message = "OTP valid",
                code = "200",
                usernameError = emptyList(),
                passwordError = emptyList()
            )
        )

        coEvery { authRepository.passwordResetOtpCheck(username, code) } returns expectedResult

        // When
        val result = authInteractor.passwordResetOtpCheck(username, code)

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `isAuthenticated returns true when auth token exists`() = runTest {
        // Given
        val authToken = "valid_token"
        coEvery { sharedPreferencesProvider.getAuthToken() } returns authToken

        // When
        val result = authInteractor.isAuthenticated()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isAuthenticated returns false when auth token is null`() = runTest {
        // Given
        coEvery { sharedPreferencesProvider.getAuthToken() } returns null

        // When
        val result = authInteractor.isAuthenticated()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAuthenticated returns false when auth token is empty`() = runTest {
        // Given
        coEvery { sharedPreferencesProvider.getAuthToken() } returns ""

        // When
        val result = authInteractor.isAuthenticated()

        // Then
        assertFalse(result)
    }

    @Test
    fun `register handles repository error correctly`() = runTest {
        // Given
        val email = "test@example.com"
        val phone = "1234567890"
        val password = "password123"
        val exception = RuntimeException("Network error")
        val expectedResult = BaseResult.Error(exception)

        coEvery { authRepository.register(email, phone, password) } returns expectedResult

        // When
        val result = authInteractor.register(email, phone, password)

        // Then
        assertTrue(result is BaseResult.Error)
        assertEquals(exception, (result as BaseResult.Error).exception)
    }

    @Test
    fun `authGoogle handles repository error correctly`() = runTest {
        // Given
        val token = "google_token"
        val exception = RuntimeException("Google auth failed")
        val expectedResult = BaseResult.Error(exception)

        coEvery { authRepository.authGoogle(token) } returns expectedResult

        // When
        val result = authInteractor.authGoogle(token)

        // Then
        assertTrue(result is BaseResult.Error)
        assertEquals(exception, (result as BaseResult.Error).exception)
    }
}
