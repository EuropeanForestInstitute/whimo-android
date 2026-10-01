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
package com.whimo.extensions

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun LocalDateTime.toUtcIsoString(sourceZoneId: ZoneId = ZoneId.systemDefault()): String {
    val zonedDateTime = this.atZone(sourceZoneId)
    val utcInstant = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant()
    return DateTimeFormatter.ISO_INSTANT.format(utcInstant)
}

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.parse(this), ZoneOffset.systemDefault())
}

//fun LocalDateTime.toFormattedDateString(): String {
//    return format(DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mma"))
//}

fun LocalDateTime.toShortFormattedDateString(): String {
    return format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
}

fun LocalDate.toShortFormattedDateString(): String {
    return format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
}

fun Long.millisToLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun LocalDateTime.toRelativeFormattedDateString(): String {
    val duration = Duration.between(this, LocalDateTime.now())

    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toMinutes() == 1L -> "1 min ago"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} mins ago"
        duration.toHours() == 1L -> "1 hour ago"
        duration.toHours() < 24 -> "${duration.toHours()} hours ago"
        duration.toDays() == 1L -> "1 day ago"
        duration.toDays() < 7 -> "${duration.toDays()} days ago"
        duration.toDays() < 30 -> {
            val weeks = duration.toDays() / 7
            if (weeks == 1L) "1 week ago" else "$weeks weeks ago"
        }
        else -> {
            format(DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mma"))
        }
    }
}

fun LocalDateTime.toFormattedDateString(): String {
    return format(DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mma"))
}
