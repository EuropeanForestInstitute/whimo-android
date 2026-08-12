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
package com.whimo.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ScreensTest {

    @Test
    fun `putArgs encodes slash in json path argument`() {
        // Given
        val json = """{"name":"Cocoa Paste / Liquor"}"""

        // When
        val route = Screens.ConvertCommodity.putArgs(Screens.ARG_KEY_JSON to json)

        // Then
        assertEquals(
            """screen_convert_commodity/%7B%22name%22%3A%22Cocoa%20Paste%20%2F%20Liquor%22%7D""",
            route
        )
        assertFalse(route.contains("Cocoa Paste / Liquor"))
    }

    @Test
    fun `convert commodity route has no whitespace before json argument`() {
        // Given
        val json = """{"id":"recipe"}"""

        // When
        val route = Screens.ConvertCommodity.putArgs(Screens.ARG_KEY_JSON to json)

        // Then
        assertFalse(route.startsWith("screen_convert_commodity "))
    }
}
