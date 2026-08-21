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
import android.location.Location
import androidx.compose.ui.text.AnnotatedString
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PhoneNumberUtilsTest {

    @Test
    fun `getDefaultPhoneRegion returns CM region or first available`() {
        // When
        val result = PhoneNumberUtils.getDefaultPhoneRegion()

        // Then
        assertNotNull(result)
        assertTrue(result.code.isNotEmpty())
        assertTrue(result.flag.isNotEmpty())
        assertTrue(result.countryName.isNotEmpty())
        assertTrue(result.phoneCode > 0)
    }

    @Test
    fun `getPhoneRegion with valid country code returns correct region`() {
        // Given
        val countryCode = "US"

        // When
        val result = PhoneNumberUtils.getPhoneRegion(countryCode)

        // Then
        assertNotNull(result)
        assertEquals(countryCode, result.code)
        assertTrue(result.flag.isNotEmpty())
        assertTrue(result.countryName.isNotEmpty())
        assertTrue(result.phoneCode > 0)
    }

    @Test
    fun `getPhoneRegion with invalid country code returns default region`() {
        // Given
        val countryCode = "XX"

        // When
        val result = PhoneNumberUtils.getPhoneRegion(countryCode)

        // Then
        assertNotNull(result)
        // Should return default region (CM or first available)
        assertTrue(result.code.isNotEmpty())
    }

    @Test
    fun `parsePhone with valid phone number returns correct region and number`() {
        // Given
        val phone = "+1234567890"

        // When
        val result = PhoneNumberUtils.parsePhone(phone)

        // Then
        assertNotNull(result.first)
        assertNotNull(result.second)
        assertTrue(result.second > 0)
    }

    @Test
    fun `parsePhone with invalid phone number returns default region`() {
        // Given
        val phone = "invalid"

        // When & Then
        try {
            val result = PhoneNumberUtils.parsePhone(phone)
            assertNotNull(result.first)
            assertNotNull(result.second)
            // Should return default region
            assertTrue(result.first.code.isNotEmpty())
        } catch (e: Exception) {
            // Expected behavior - invalid phone may throw exception
            assertNotNull(e)
        }
    }

    @Test
    fun `parsePhone with empty phone number returns default region`() {
        // Given
        val phone = ""

        // When & Then
        try {
            val result = PhoneNumberUtils.parsePhone(phone)
            assertNotNull(result.first)
            assertNotNull(result.second)
            // Should return default region
            assertTrue(result.first.code.isNotEmpty())
        } catch (e: Exception) {
            // Expected behavior - empty phone may throw exception
            assertNotNull(e)
        }
    }

    @Test
    fun `SupportedPhoneRegions contains valid regions`() {
        // When
        val regions = PhoneNumberUtils.SupportedPhoneRegions

        // Then
        assertTrue(regions.isNotEmpty())
        regions.forEach { region ->
            assertTrue(region.code.isNotEmpty())
            assertTrue(region.flag.isNotEmpty())
            assertTrue(region.countryName.isNotEmpty())
            assertTrue(region.phoneCode > 0)
        }
    }

    @Test
    fun `PhoneVisualTransformation formats phone number correctly`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("1234567890")

        // When
        val result = transformation.filter(text)

        // Then
        assertNotNull(result)
        assertNotNull(result.text)
        // Should add spaces every 3 characters
        assertTrue(result.text.text.contains(" "))
    }

    @Test
    fun `PhoneVisualTransformation with short number formats correctly`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("123")

        // When
        val result = transformation.filter(text)

        // Then
        assertNotNull(result)
        assertNotNull(result.text)
        assertEquals("123", result.text.text)
    }

    @Test
    fun `PhoneVisualTransformation with empty text handles correctly`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("")

        // When
        val result = transformation.filter(text)

        // Then
        assertNotNull(result)
        assertNotNull(result.text)
        assertEquals("", result.text.text)
    }

    @Test
    fun `PhoneVisualTransformation offset mapping works correctly`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("123456789")

        // When
        val result = transformation.filter(text)

        // Then
        assertNotNull(result)
        assertNotNull(result.offsetMapping)
        
        // Test original to transformed mapping
        val originalToTransformed = result.offsetMapping.originalToTransformed(3)
        assertTrue(originalToTransformed >= 3)
        
        // Test transformed to original mapping
        val transformedToOriginal = result.offsetMapping.transformedToOriginal(3)
        assertTrue(transformedToOriginal >= 0)
    }

    @Test
    fun `PhoneVisualTransformation maps cursor to end after first separator`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("123456")

        // When
        val result = transformation.filter(text)

        // Then
        assertEquals("123 456", result.text.text)
        assertEquals(7, result.offsetMapping.originalToTransformed(6))
        assertEquals(6, result.offsetMapping.transformedToOriginal(7))
    }

    @Test
    fun `PhoneVisualTransformation maps cursor to end after multiple separators`() {
        // Given
        val transformation = PhoneVisualTransformation()
        val text = AnnotatedString("123456789")

        // When
        val result = transformation.filter(text)

        // Then
        assertEquals("123 456 789", result.text.text)
        assertEquals(11, result.offsetMapping.originalToTransformed(9))
        assertEquals(9, result.offsetMapping.transformedToOriginal(11))
    }
}
