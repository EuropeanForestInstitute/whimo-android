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
package com.whimo.providers

import com.whimo.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestEnvironmentManager(
    private val sharedPreferencesProvider: SharedPreferencesProvider,
) {
    private val _isTestEnvironment = MutableStateFlow(
        sharedPreferencesProvider.isTestEnvironmentEnabled()
    )

    val isTestEnvironment: StateFlow<Boolean> = _isTestEnvironment.asStateFlow()

    fun isTestEnvironmentEnabled(): Boolean {
        return _isTestEnvironment.value
    }

    fun setTestEnvironmentEnabled(isEnabled: Boolean) {
        sharedPreferencesProvider.setTestEnvironmentEnabled(isEnabled)
        _isTestEnvironment.value = isEnabled
    }

    fun baseUrl(): String {
        return if (isTestEnvironmentEnabled()) {
            BuildConfig.TEST_BASE_URL
        } else {
            BuildConfig.BASE_URL
        }
    }
}
