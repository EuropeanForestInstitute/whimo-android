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

import android.content.Context
import com.whimo.domain.settings.models.AccountModel
import com.whimo.utils.fromJsonArgs
import com.whimo.utils.toJsonArgs

interface SharedPreferencesProvider {
    fun clearAllData()
    fun removeItem(key: String)

    fun getString(key: String): String?
    fun saveString(key: String, value: String?)
    fun getInt(key: String): Int
    fun saveInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun saveBoolean(key: String, value: Boolean)

    fun isAuthorized(): Boolean
    fun deleteAuthToken()
    fun getAuthToken(): String?
    fun saveAuthToken(value: String?)

    fun getRefreshToken(): String?
    fun saveRefreshToken(value: String?)

    fun isTestEnvironmentEnabled(): Boolean = false
    fun setTestEnvironmentEnabled(isEnabled: Boolean) = Unit

    fun getAccount(): AccountModel?
    fun setAccount(account: AccountModel)

    fun getFCMToken(): String?
    fun setFCMToken(token: String)

    fun isNotificationsAllowed(): Boolean
    fun setNotificationsAllowed(isAllowed: Boolean)
}

class SharedPreferencesProviderImpl(context: Context) : SharedPreferencesProvider {

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val authPrefs = context.getSharedPreferences(AUTH_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        authPrefs.edit().clear().apply()
    }

    override fun removeItem(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }


    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun saveString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, -1)
    }

    override fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }


    override fun isAuthorized(): Boolean {
        return getAuthToken() != null
    }

    override fun deleteAuthToken() {
        authPrefs.edit().remove(AUTH_TOKEN).apply()
    }

    override fun getAuthToken(): String? {
        return authPrefs.getString(AUTH_TOKEN, null)
    }

    override fun saveAuthToken(value: String?) {
        authPrefs.edit().putString(AUTH_TOKEN, value).apply()
    }


    override fun getRefreshToken(): String? {
        return getString(REFRESH_TOKEN)
    }

    override fun saveRefreshToken(value: String?) {
        saveString(REFRESH_TOKEN, value)
    }

    override fun isTestEnvironmentEnabled(): Boolean {
        return getBoolean(TEST_ENVIRONMENT_ENABLED_KEY, false)
    }

    override fun setTestEnvironmentEnabled(isEnabled: Boolean) {
        saveBoolean(TEST_ENVIRONMENT_ENABLED_KEY, isEnabled)
    }


    override fun getAccount(): AccountModel? {
        return getString(ACCOUNT_KEY)?.fromJsonArgs<AccountModel>()
    }

    override fun setAccount(account: AccountModel) {
        saveString(ACCOUNT_KEY, account.toJsonArgs())
    }

    override fun getFCMToken(): String? {
        return getString(FCM_TOKEN_KEY)
    }

    override fun setFCMToken(token: String) {
        saveString(FCM_TOKEN_KEY, token)
    }

    override fun isNotificationsAllowed(): Boolean {
        return getBoolean(NOTIFICATIONS_ALLOWED_KEY, true)
    }

    override fun setNotificationsAllowed(isAllowed: Boolean) {
        saveBoolean(NOTIFICATIONS_ALLOWED_KEY, isAllowed)
    }

    companion object {
        private const val PREFERENCES_NAME = "whimo_preferences"
        private const val AUTH_PREFERENCES_NAME = "whimo_auth_prefs"
        private const val AUTH_TOKEN = "auth_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val ACCOUNT_KEY = "account_key"
        private const val FCM_TOKEN_KEY = "fcm_token_key"
        private const val NOTIFICATIONS_ALLOWED_KEY = "notifications_allowed_key"
        private const val TEST_ENVIRONMENT_ENABLED_KEY = "test_environment_enabled_key"
    }
}
