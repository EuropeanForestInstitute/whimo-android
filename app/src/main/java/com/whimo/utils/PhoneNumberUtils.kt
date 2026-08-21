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
package com.whimo.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.whimo.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale

object PhoneNumberUtils {
    val SupportedPhoneRegions = getPhoneRegions()

    private fun getPhoneRegions(): List<PhoneRegion> {
        val countryCodes = PhoneNumberUtil.getInstance().supportedRegions.sorted()
        val phoneRegions = countryCodes.map { countryCode ->
            PhoneRegion(
                code = countryCode,
                flag = getFlagEmoji(countryCode),
                countryName = getLocalizedCountryName(countryCode),
                phoneCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode),
            )
        }
        return phoneRegions
    }

    private fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return ""

        val upperCaseCode = countryCode.uppercase()
        val firstChar = Character.codePointAt(upperCaseCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upperCaseCode, 1) - 0x41 + 0x1F1E6

        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    private fun getLocalizedCountryName(countryCode: String, locale: Locale = Locale.getDefault()): String {
        return Locale.Builder().setRegion(countryCode).build().getDisplayCountry(locale)
    }

    suspend fun getCountryCodeFromLocation(context: Context, location: Location): String? {
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            cont.resumeWith(Result.success(addresses.firstOrNull()?.countryCode))
                        }
                        override fun onError(errorMessage: String?) {
                            cont.resumeWith(Result.success(null))
                        }
                    }
                )
            }
        } else {
            try {
                val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                result?.firstOrNull()?.countryCode
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getDefaultPhoneRegion(): PhoneRegion {
        return SupportedPhoneRegions.find { it.code == BuildConfig.DEFAULT_COUNTRY_CODE } ?: SupportedPhoneRegions.first()
    }

    fun getPhoneRegion(countryCode: String): PhoneRegion {
        return SupportedPhoneRegions.find { it.code == countryCode } ?: getDefaultPhoneRegion()
    }

    fun parsePhone(phone: String): Pair<PhoneRegion, Long> {
        if (phone.startsWith("0")) {
            return getDefaultPhoneRegion() to phone.removePrefix("0").toLong()
        }

        val phoneUtil = PhoneNumberUtil.getInstance()
        var numberProto: PhoneNumber?
        try {
            numberProto = phoneUtil.parse(phone, null)
        } catch (e: Exception) {
            e.printStackTrace()
            numberProto = null
        }

        val phoneRegion = SupportedPhoneRegions.find { it.phoneCode == numberProto?.countryCode } ?: getDefaultPhoneRegion()

        return phoneRegion to (numberProto?.nationalNumber ?: phone.toLong())
    }

    data class PhoneRegion(
        val code: String,
        val flag: String,
        val countryName: String,
        val phoneCode: Int,
    )
}

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {

        val formattedPhone = buildString {
            text.forEachIndexed { index, char ->
                append(char)
                if ((index + 1) % 3 == 0 && index != text.lastIndex) {
                    append(" ")
                }
            }
        }

        return TransformedText(
            text = AnnotatedString(formattedPhone),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val safeOffset = offset.coerceIn(0, text.length)
                    val separatorsBeforeOffset = if (safeOffset == 0) 0 else (safeOffset - 1) / 3
                    return (safeOffset + separatorsBeforeOffset).coerceAtMost(formattedPhone.length)
                }
                override fun transformedToOriginal(offset: Int): Int {
                    val safeOffset = offset.coerceIn(0, formattedPhone.length)
                    val separatorsBeforeOffset = safeOffset / 4
                    return (safeOffset - separatorsBeforeOffset).coerceIn(0, text.length)
                }
            }
        )
    }
}
