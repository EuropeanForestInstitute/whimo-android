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
package com.whimo.domain.commodity

import com.whimo.data.base.common.BaseResult
import com.whimo.data.commodity.model.request.ConvertCommodityRequest
import com.whimo.data.commodity.model.request.ConvertQuantityRequest
import com.whimo.data.commodity.repository.CommodityRepository
import com.whimo.data.commodity.repository.ConvertCommodityRepository
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.commodity.models.ConvertQuantityModel
import com.whimo.domain.commodity.models.ConvertRecipeModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.BaseModel
import com.whimo.network.mapResult

interface ConvertCommodityInteractor {
    suspend fun getRecipes(
        commodity: CommodityModel,
    ): BaseResult<List<ConvertRecipeModel>>

    suspend fun convertCommodity(
        recipeId: String,
        inputs: List<ConvertQuantityModel>,
        outputs: List<ConvertQuantityModel>,
        harvestSeason: HarvestSeasonModel? = null,
    ): BaseResult<BaseModel>
}

class ConvertCommodityInteractorImpl(
    private val repository: ConvertCommodityRepository,
    private val commodityRepository: CommodityRepository,
) : ConvertCommodityInteractor {

    override suspend fun getRecipes(
        commodity: CommodityModel,
    ): BaseResult<List<ConvertRecipeModel>> {
        val dbCommodities = commodityRepository.getCommoditiesFromDB()
            .flatMap { it.commodities ?: emptyList() }

        return repository.getRecipes(
            search = null,
            commodityId = commodity.id,
            page = FIRST_PAGE,
            pageSize = DEFAULT_PAGE_SIZE,
        ).mapResult { recipes ->
            recipes?.forEach { recipe ->
                recipe.inputs.forEach { input ->
                    dbCommodities.find { it.id == input.commodity.id }
                        ?.let {
                            input.commodity = it
                        }
                }
                recipe.outputs.forEach { output ->
                    dbCommodities.find { it.id == output.commodity.id }
                        ?.let {
                            output.commodity = it
                        }
                }
            }
            recipes
        }
    }

    override suspend fun convertCommodity(
        recipeId: String,
        inputs: List<ConvertQuantityModel>,
        outputs: List<ConvertQuantityModel>,
        harvestSeason: HarvestSeasonModel?,
    ): BaseResult<BaseModel> {
        return repository.convertCommodity(
            ConvertCommodityRequest(
                recipe_id = recipeId,
                harvest_season_id = harvestSeason?.id,
                input_overrides = inputs.map {
                    ConvertQuantityRequest(
                        commodity_id = it.commodity.id,
                        quantity = it.quantity
                    )
                },
                output_overrides = outputs.map {
                    ConvertQuantityRequest(
                        commodity_id = it.commodity.id,
                        quantity = it.quantity
                    )
                },
            )
        )
    }

    companion object {
        private const val FIRST_PAGE = 1
        private const val DEFAULT_PAGE_SIZE = 1000
    }
}
