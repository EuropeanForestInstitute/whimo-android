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
package com.whimo.data.commodity.model.response

import com.whimo.data.base.common.Pagination
import com.whimo.data.harvestseasons.model.response.HarvestSeasonData

data class CommoditiesGroupsResponse(
    val success: Boolean,
    val pagination: Pagination,
    val data: List<CommodityGroup>,
    val message: String
)

data class CommodityGroup(
    val id: String,
    val name: String,
    val commodities: List<Commodity>?,
)

data class Commodity(
    val id: String,
    val code: String,
    val name: String,
    val unit: String,
    val has_recipe: Boolean,
    val group: CommodityGroup?,
    val balance: Float?,
)

data class CommodityBalancesResponse(
    val success: Boolean,
    val pagination: Pagination,
    val data: List<CommodityBalance>,
    val message: String
)

data class CommodityBalance(
    val id: String,
    val volume: Float,
    val commodity: Commodity,
    val harvest_season: HarvestSeasonData?,
    val traceability: String?,
    val has_recipe: Boolean,
)
