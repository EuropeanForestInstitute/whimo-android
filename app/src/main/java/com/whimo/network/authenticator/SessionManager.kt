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
package com.whimo.network.authenticator

import com.whimo.data.AppDatabase
import com.whimo.data.auth.model.request.RefreshRequest
import com.whimo.data.auth.model.response.toDomain
import com.whimo.data.base.common.BaseResult
import com.whimo.domain.auth.models.AuthModel
import com.whimo.network.error.ServerError
import com.whimo.network.handleResponse
import com.whimo.network.mapResult
import com.whimo.providers.SharedPreferencesProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionManager(
    private val refreshService: RefreshService,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
    private val appDatabase: AppDatabase,
) {
    private var onLogoutCallback: (() -> Unit)? = null

    private var cachedRefreshToken: String? = null
    private val mutex = Mutex()
    private var ongoingRequest: Deferred<String?>? = null

    suspend fun refreshAccessToken(refreshToken: String): String? {
        if (cachedRefreshToken == refreshToken && ongoingRequest != null) {
            return ongoingRequest?.await()
        }

        return mutex.withLock {
            ongoingRequest?.cancel()

            val newRequest = CoroutineScope(Dispatchers.IO).async {
                delay(1000)

                var newAccessToken: String? = null
                val result = refresh(RefreshRequest(refreshToken))

                if (result is BaseResult.Success) {
                    newAccessToken = result.data?.accessToken

                    sharedPreferencesProvider.saveAuthToken(newAccessToken)
                    sharedPreferencesProvider.saveRefreshToken(result.data?.refreshToken)

                } else if (result is BaseResult.Error) {
                    if (result.exception is ServerError && result.exception.code == 401) {
                        logout()
                    }
                }

                newAccessToken
            }

            ongoingRequest = newRequest
            cachedRefreshToken = refreshToken

            try {
                newRequest.await()
            } finally {
                ongoingRequest = null
            }
        }
    }

    private suspend fun refresh(request: RefreshRequest): BaseResult<AuthModel> {
        return handleResponse { refreshService.refresh(request) }.mapResult { it?.toDomain() }
    }

    private suspend fun logout() {
        clearAllData()
        onLogoutCallback?.invoke()
    }

    fun setLogoutCallback(callback: (() -> Unit)?) {
        onLogoutCallback = callback
    }

    suspend fun clearAllData() {
        sharedPreferencesProvider.clearAllData()
        appDatabase.clearAllTables()
    }
}
