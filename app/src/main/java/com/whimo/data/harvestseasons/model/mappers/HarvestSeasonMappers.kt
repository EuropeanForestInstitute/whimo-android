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
package com.whimo.data.harvestseasons.model.mappers

import com.google.gson.reflect.TypeToken
import com.whimo.data.base.common.toDomain
import com.whimo.data.harvestseasons.model.entity.HarvestSeasonEntity
import com.whimo.data.harvestseasons.model.response.HarvestSeasonData
import com.whimo.data.harvestseasons.model.response.HarvestSeasonsResponse
import com.whimo.domain.common.PaginationModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonStatus
import com.whimo.utils.gson
import java.time.LocalDate

fun HarvestSeasonsResponse.toDomain(): Pair<PaginationModel, List<HarvestSeasonModel>> {
    return pagination.toDomain() to data.mapNotNull { it.toDomainOrNull() }
}

fun HarvestSeasonData.toDomainOrNull(): HarvestSeasonModel? {
    val seasonId = id ?: return null
    val startDate = start_date?.toLocalDateOrNull()
    val endDate = end_date?.toLocalDateOrNull()

    return HarvestSeasonModel(
        id = seasonId,
        name = name?.takeIf { it.isNotBlank() } ?: getSeasonName(seasonId, startDate, endDate),
        startDate = startDate,
        endDate = endDate,
        status = HarvestSeasonStatus.entries.find { it.statusName == status },
    )
}

fun HarvestSeasonModel.toEntity(
    commodityIds: List<String>? = null,
): HarvestSeasonEntity {
    return HarvestSeasonEntity(
        id = id,
        name = name,
        status = status?.statusName,
        country = country,
        startDate = startDate?.toString(),
        endDate = endDate?.toString(),
        commodityIdsJson = commodityIds
            ?.distinct()
            ?.takeIf { it.isNotEmpty() }
            ?.let { gson.toJson(it) },
    )
}

fun HarvestSeasonEntity.toDomain(): HarvestSeasonModel {
    return HarvestSeasonModel(
        id = id,
        name = name,
        status = HarvestSeasonStatus.entries.find { it.statusName == status },
        country = country,
        startDate = startDate?.toLocalDateOrNull(),
        endDate = endDate?.toLocalDateOrNull(),
    )
}

fun HarvestSeasonEntity.getCommodityIds(): List<String> {
    val json = commodityIdsJson ?: return emptyList()
    val type = object : TypeToken<List<String>>() {}.type

    return runCatching {
        gson.fromJson<List<String>>(json, type)
    }.getOrNull().orEmpty()
}

private fun getSeasonName(
    id: String,
    startDate: LocalDate?,
    endDate: LocalDate?,
): String {
    val startYear = startDate?.year
    val endYear = endDate?.year

    val yearRange = when {
        startYear != null && endYear != null -> "$startYear/${(endYear % 100).toString().padStart(2, '0')}"
        startYear != null -> startYear.toString()
        endYear != null -> endYear.toString()
        else -> return id
    }

    return "Harvest season $yearRange"
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching { LocalDate.parse(this) }.getOrNull()
}
