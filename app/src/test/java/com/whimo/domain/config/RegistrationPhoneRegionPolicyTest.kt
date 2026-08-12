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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationPhoneRegionPolicyTest {

    private val gson = Gson()

    @Test
    fun `fromJson parses enabled allowed regions`() {
        val policy = RegistrationPhoneRegionPolicy.fromJson(
            json = """
                {
                    "schemaVersion": 1,
                    "enabled": true,
                    "allowedRegions": [
                        {
                            "regionCode": "GT",
                            "regionName": "Guatemala",
                            "callingCode": 502,
                            "e164Prefix": "+502"
                        }
                    ]
                }
            """.trimIndent(),
            gson = gson,
        )

        assertTrue(policy.enabled)
        assertTrue(policy.isPhoneRegionSupported(region(code = "GT", phoneCode = 502)))
        assertFalse(policy.isPhoneRegionSupported(region(code = "US", phoneCode = 1)))
    }

    @Test
    fun `fromJson returns disabled policy for invalid json`() {
        val policy = RegistrationPhoneRegionPolicy.fromJson("not-json", gson)

        assertFalse(policy.enabled)
        assertTrue(policy.isPhoneRegionSupported(region(code = "US", phoneCode = 1)))
    }

    @Test
    fun `requiresEmailForRegistration returns true for unsupported region and empty email`() {
        val policy = policy(allowedRegionCode = "GT", allowedCallingCode = 502)

        val requiresEmail = policy.requiresEmailForRegistration(
            phoneRegion = region(code = "US", phoneCode = 1),
            email = "",
        )

        assertTrue(requiresEmail)
    }

    @Test
    fun `requiresEmailForRegistration returns false for unsupported region and filled email`() {
        val policy = policy(allowedRegionCode = "GT", allowedCallingCode = 502)

        val requiresEmail = policy.requiresEmailForRegistration(
            phoneRegion = region(code = "US", phoneCode = 1),
            email = "user@example.com",
        )

        assertFalse(requiresEmail)
    }

    @Test
    fun `requiresEmailForRegistration returns false for supported region and empty email`() {
        val policy = policy(allowedRegionCode = "GT", allowedCallingCode = 502)

        val requiresEmail = policy.requiresEmailForRegistration(
            phoneRegion = region(code = "GT", phoneCode = 502),
            email = "",
        )

        assertFalse(requiresEmail)
    }

    @Test
    fun `disabled policy allows every phone region`() {
        val policy = RegistrationPhoneRegionPolicy.Disabled

        assertTrue(policy.isPhoneRegionSupported(region(code = "US", phoneCode = 1)))
        assertFalse(
            policy.requiresEmailForRegistration(
                phoneRegion = region(code = "US", phoneCode = 1),
                email = "",
            )
        )
    }

    private fun policy(
        allowedRegionCode: String,
        allowedCallingCode: Int,
    ): RegistrationPhoneRegionPolicy {
        return RegistrationPhoneRegionPolicy(
            enabled = true,
            allowedRegions = listOf(
                RegistrationPhoneRegion(
                    regionCode = allowedRegionCode,
                    callingCode = allowedCallingCode,
                    e164Prefix = "+$allowedCallingCode",
                )
            ),
        )
    }

    private fun region(
        code: String,
        phoneCode: Int,
    ): PhoneNumberUtils.PhoneRegion {
        return PhoneNumberUtils.PhoneRegion(
            code = code,
            flag = "",
            countryName = code,
            phoneCode = phoneCode,
        )
    }
}
