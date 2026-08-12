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

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.whimo.BuildConfig
import com.whimo.R
import com.whimo.domain.config.RegistrationPhoneRegionPolicy
import kotlinx.coroutines.suspendCancellableCoroutine
import org.xmlpull.v1.XmlPullParser
import kotlin.coroutines.resume

interface RemoteConfigProvider {
    suspend fun refresh()
    fun getRegistrationPhoneRegionPolicy(): RegistrationPhoneRegionPolicy
}

class FirebaseRemoteConfigProvider(
    private val remoteConfig: FirebaseRemoteConfig,
    private val gson: Gson,
    private val resourceProvider: ResourceProvider,
) : RemoteConfigProvider {

    init {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0L else 3600L)
                .build()
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    override suspend fun refresh() {
        runCatching { remoteConfig.fetchAndActivate().awaitCompletion() }
    }

    override fun getRegistrationPhoneRegionPolicy(): RegistrationPhoneRegionPolicy {
        val remotePolicyJson = remoteConfig.getString(REGISTRATION_PHONE_REGION_POLICY_KEY)

        return RegistrationPhoneRegionPolicy.fromJson(
            json = remotePolicyJson.ifBlank {
                getDefaultConfigValue(REGISTRATION_PHONE_REGION_POLICY_KEY)
            },
            gson = gson,
        )
    }

    private suspend fun <T> Task<T>.awaitCompletion(): T? {
        return suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (continuation.isActive) {
                    continuation.resume(task.takeIf { it.isSuccessful }?.result)
                }
            }
        }
    }

    private fun getDefaultConfigValue(key: String): String? {
        val parser = resourceProvider.resources().getXml(R.xml.remote_config_defaults)
        var currentKey: String? = null
        var readingKey = false
        var readingValue = false
        val valueBuilder = StringBuilder()

        try {
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "key" -> readingKey = true
                            "value" -> {
                                readingValue = currentKey == key
                                if (readingValue) {
                                    valueBuilder.clear()
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        when {
                            readingKey -> currentKey = parser.text.trim()
                            readingValue -> valueBuilder.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "key" -> readingKey = false
                            "value" -> {
                                if (readingValue) {
                                    return valueBuilder.toString()
                                }
                                readingValue = false
                            }
                        }
                    }
                }

                parser.next()
            }
        } finally {
            parser.close()
        }

        return null
    }

    private companion object {
        const val REGISTRATION_PHONE_REGION_POLICY_KEY = "registration_phone_region_policy"
    }
}
