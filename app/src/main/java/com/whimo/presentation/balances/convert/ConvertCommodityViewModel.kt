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
package com.whimo.presentation.balances.convert

import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.commodity.ConvertCommodityInteractor
import com.whimo.domain.commodity.models.ConvertQuantityModel
import com.whimo.domain.commodity.models.ConvertRecipeModel
import com.whimo.domain.harvestseasons.models.HarvestSeasonModel
import com.whimo.network.ErrorHandler
import com.whimo.presentation.balances.ConvertCommodityArgs

class ConvertCommodityViewModel(
    private val interactor: ConvertCommodityInteractor,
    private val errorHandler: ErrorHandler,
) : BaseViewModel<ConvertCommodityContract.Binding>() {

    private var recipe: ConvertRecipeModel? = null
    private var inputs: List<ConvertQuantityModel>? = null
    private var outputs: List<ConvertQuantityModel>? = null
    private var harvestSeason: HarvestSeasonModel? = null

    override fun createBinding(): ConvertCommodityContract.Binding {
        return ConvertCommodityContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is ConvertCommodityContract.Event.OnCreate -> onCreate(event.args)
            is ConvertCommodityContract.Event.OnQuantityChange -> onQuantityChange(event.model, event.quantity)
            is ConvertCommodityContract.Event.OnConvertClick -> onConvertClick()
            is ConvertCommodityContract.Event.Convert -> convert()
        }
    }

    override fun copyBinding(binding: ConvertCommodityContract.Binding): ConvertCommodityContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        updateBinding { b ->
            b.inputs = inputs
            b.outputs = outputs
        }
    }

    private fun onCreate(args: ConvertCommodityArgs?) {
        val recipe = args?.recipe
        this.recipe = recipe
        harvestSeason = args?.harvestSeason

        inputs = recipe?.inputs
        outputs = recipe?.outputs

        updateView()
    }

    private fun onQuantityChange(model: ConvertQuantityModel, quantity: String) {
        val quantityValue = quantity.toFloatOrNull() ?: 0f

        inputs?.find { it.id == model.id }?.quantity = quantityValue
        outputs?.find { it.id == model.id }?.quantity = quantityValue
    }

    private fun onConvertClick() {
        setEffect(ConvertCommodityContract.Effect.ShowConfirmDialog)
    }

    private fun convert() {
        launch {
            setEffect(ConvertCommodityContract.Effect.ToggleLoader(true))

            interactor.convertCommodity(recipe!!.id, inputs!!, outputs!!, harvestSeason)
                .onSuccess {
                    setEffect(
                        ConvertCommodityContract.Effect.ToggleLoader(false),
                        ConvertCommodityContract.Effect.ConversionSuccess
                    )
                    updateView()
                }
                .onError {
                    it.printStackTrace()
                    val errorMessage = errorHandler.parseError(it)
                    setEffect(
                        ConvertCommodityContract.Effect.ToggleLoader(false),
                        ConvertCommodityContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }
}
