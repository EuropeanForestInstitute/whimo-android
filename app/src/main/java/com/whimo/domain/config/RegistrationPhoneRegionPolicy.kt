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
package com.whimo.domain.config

import com.google.gson.Gson
import com.whimo.utils.PhoneNumberUtils
import java.util.Locale

data class RegistrationPhoneRegionPolicy(
    val schemaVersion: Int = 1,
    val enabled: Boolean = false,
    val allowedRegions: List<RegistrationPhoneRegion> = emptyList(),
) {
    fun isPhoneRegionSupported(phoneRegion: PhoneNumberUtils.PhoneRegion): Boolean {
        if (!enabled) return true

        val regionCode = phoneRegion.code.uppercase(Locale.US)

        return allowedRegions.orEmpty().any { allowedRegion ->
            allowedRegion.matches(regionCode, phoneRegion.phoneCode)
        }
    }

    fun requiresEmailForRegistration(
        phoneRegion: PhoneNumberUtils.PhoneRegion,
        email: String,
    ): Boolean {
        return email.isBlank() && !isPhoneRegionSupported(phoneRegion)
    }

    companion object {
        val Disabled = RegistrationPhoneRegionPolicy()

        fun fromJson(json: String?, gson: Gson): RegistrationPhoneRegionPolicy {
            if (json.isNullOrBlank()) return Disabled

            return runCatching {
                gson.fromJson(json, RegistrationPhoneRegionPolicy::class.java) ?: Disabled
            }.getOrDefault(Disabled)
        }
    }
}

data class RegistrationPhoneRegion(
    val regionCode: String? = null,
    val regionName: String? = null,
    val callingCode: Int? = null,
    val e164Prefix: String? = null,
) {
    fun matches(regionCode: String, callingCode: Int): Boolean {
        val normalizedRegionCode = this.regionCode?.uppercase(Locale.US)
        val normalizedE164Prefix = e164Prefix?.trim()

        return normalizedRegionCode == regionCode ||
                this.callingCode == callingCode ||
                normalizedE164Prefix == "+$callingCode"
    }
}
