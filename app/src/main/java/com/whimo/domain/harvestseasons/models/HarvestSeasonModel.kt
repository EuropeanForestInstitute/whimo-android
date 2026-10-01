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
package com.whimo.domain.harvestseasons.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class HarvestSeasonModel(
    val id: String,
    val name: String = id,
    val status: HarvestSeasonStatus? = null,
    val country: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) : Parcelable

fun HarvestSeasonModel.getYearRangeText(): String {
    val startYear = startDate?.year
    val endYear = endDate?.year

    return when {
        startYear != null && endYear != null -> "$startYear/${(endYear % 100).toString().padStart(2, '0')}"
        startYear != null -> startYear.toString()
        endYear != null -> endYear.toString()
        else -> id
    }
}

enum class HarvestSeasonStatus(val statusName: String) {
    Active("active"),
    Past("past"),
    Archived("archived"),
}

data class HarvestSeasonFilter(
    var commodityIds: List<String>? = null,
    var status: HarvestSeasonStatus? = null,
    var allowUnlinkedFallback: Boolean = true,
)
