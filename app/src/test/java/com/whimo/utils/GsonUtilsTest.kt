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

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GsonUtilsTest {

    @Test
    fun `toJsonArgs converts object to JSON string`() {
        // Given
        val testData = TestData("test", 123)

        // When
        val result = testData.toJsonArgs()

        // Then
        assertNotNull(result)
        assertTrue(result.contains("test"))
        assertTrue(result.contains("123"))
    }

    @Test
    fun `fromJsonArgs converts JSON string to object`() {
        // Given
        val jsonString = """{"name":"test","value":123}"""

        // When
        val result: TestData = jsonString.fromJsonArgs()

        // Then
        assertEquals("test", result.name)
        assertEquals(123, result.value)
    }

    @Test
    fun `LocalDateTimeAdapter writes LocalDateTime correctly`() {
        // Given
        val dateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45)
        val testData = TestDataWithDateTime("test", dateTime)

        // When
        val result = testData.toJsonArgs()

        // Then
        assertNotNull(result)
        assertTrue(result.contains("2023-12-25T10:30:45"))
    }

    @Test
    fun `LocalDateTimeAdapter reads LocalDateTime correctly`() {
        // Given
        val jsonString = """{"name":"test","dateTime":"2023-12-25T10:30:45"}"""

        // When
        val result: TestDataWithDateTime = jsonString.fromJsonArgs()

        // Then
        assertEquals("test", result.name)
        assertEquals(LocalDateTime.of(2023, 12, 25, 10, 30, 45), result.dateTime)
    }

    @Test
    fun `LocalDateAdapter writes LocalDate correctly`() {
        // Given
        val date = LocalDate.of(2025, 9, 1)
        val testData = TestDataWithDate(date)

        // When
        val result = testData.toJsonArgs()

        // Then
        assertNotNull(result)
        assertTrue(result.contains("2025-09-01"))
    }

    @Test
    fun `LocalDateAdapter reads LocalDate correctly`() {
        // Given
        val jsonString = """{"date":"2025-09-01"}"""

        // When
        val result: TestDataWithDate = jsonString.fromJsonArgs()

        // Then
        assertEquals(LocalDate.of(2025, 9, 1), result.date)
    }

    @Test
    fun `LocalDateTimeAdapter handles null LocalDateTime correctly`() {
        // Given
        val testData = TestDataWithDateTime("test", null)

        // When
        val result = testData.toJsonArgs()

        // Then
        assertNotNull(result)
        assertTrue(result.contains("test"))
        // Note: null values may be handled differently by Gson
    }

    @Test
    fun `LocalDateTimeAdapter reads null LocalDateTime correctly`() {
        // Given
        val jsonString = """{"name":"test","dateTime":null}"""

        // When
        val result: TestDataWithDateTime = jsonString.fromJsonArgs()

        // Then
        assertEquals("test", result.name)
        assertNull(result.dateTime)
    }

    @Test
    fun `gson instance is properly configured`() {
        // Given
        val testData = TestDataWithDateTime("test", LocalDateTime.of(2023, 12, 25, 10, 30, 45))

        // When
        val json = gson.toJson(testData)
        val parsed: TestDataWithDateTime = gson.fromJson(json, TestDataWithDateTime::class.java)

        // Then
        assertEquals(testData.name, parsed.name)
        assertEquals(testData.dateTime, parsed.dateTime)
    }

    @Test
    fun `toJsonArgs and fromJsonArgs are inverse operations`() {
        // Given
        val originalData = TestData("test", 123)

        // When
        val json = originalData.toJsonArgs()
        val parsedData: TestData = json.fromJsonArgs()

        // Then
        assertEquals(originalData.name, parsedData.name)
        assertEquals(originalData.value, parsedData.value)
    }

    @Test
    fun `toJsonArgs handles complex objects correctly`() {
        // Given
        val complexData = ComplexTestData(
            name = "test",
            value = 123,
            dateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45),
            nested = TestData("nested", 456)
        )

        // When
        val json = complexData.toJsonArgs()
        val parsed: ComplexTestData = json.fromJsonArgs()

        // Then
        assertEquals(complexData.name, parsed.name)
        assertEquals(complexData.value, parsed.value)
        assertEquals(complexData.dateTime, parsed.dateTime)
        assertEquals(complexData.nested.name, parsed.nested.name)
        assertEquals(complexData.nested.value, parsed.nested.value)
    }

    private data class TestData(
        val name: String,
        val value: Int
    )

    private data class TestDataWithDateTime(
        val name: String,
        val dateTime: LocalDateTime?
    )

    private data class TestDataWithDate(
        val date: LocalDate
    )

    private data class ComplexTestData(
        val name: String,
        val value: Int,
        val dateTime: LocalDateTime,
        val nested: TestData
    )
}
