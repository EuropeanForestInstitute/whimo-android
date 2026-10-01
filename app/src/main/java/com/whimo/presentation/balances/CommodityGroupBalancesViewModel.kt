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
package com.whimo.presentation.balances

import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.commodity.CommodityInteractor
import com.whimo.domain.commodity.models.CommodityBalanceFilter
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.network.ErrorHandler

class CommodityGroupBalancesViewModel(
    private val interactor: CommodityInteractor,
    private val errorHandler: ErrorHandler,
) : BaseViewModel<CommodityGroupBalancesContract.Binding>() {

    private var commodityGroup: CommodityGroupModel? = null

    private val filter = CommodityBalanceFilter()
    private var balances: List<BalanceCommodityItemModel>? = null

    override fun createBinding(): CommodityGroupBalancesContract.Binding {
        return CommodityGroupBalancesContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is CommodityGroupBalancesContract.Event.OnCreate -> onCreate(event.commodityGroup)
        }
    }

    override fun copyBinding(binding: CommodityGroupBalancesContract.Binding): CommodityGroupBalancesContract.Binding {
        return binding.copy()
    }

    private fun onCreate(commodityGroup: CommodityGroupModel?) {
        this.commodityGroup = commodityGroup
        filter.groupId = commodityGroup?.id

        getBalances()
    }

    private fun getBalances() {
        launch {
            val cachedBalances = interactor.getBalancesFromDB(filter)
            if (cachedBalances.isNotEmpty()) {
                balances = cachedBalances.map { balance -> balance.toBalanceCommodityItemModel() }
                updateView()
            }

            setEffect(CommodityGroupBalancesContract.Effect.ToggleLoader(balances.isNullOrEmpty()))
            interactor.getBalances(filter)
                .onSuccess {
                    balances = it.orEmpty().map { balance -> balance.toBalanceCommodityItemModel() }
                    setEffect(CommodityGroupBalancesContract.Effect.ToggleLoader(false))
                    updateView()
                }
                .onError {
                    it.printStackTrace()
                    val errorMessage = errorHandler.parseError(it)
                    setEffect(
                        CommodityGroupBalancesContract.Effect.ToggleLoader(false),
                        CommodityGroupBalancesContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }

    private fun updateView() {
        updateBinding { b ->
            b.title = commodityGroup?.name ?: ""
            b.query = filter.query
            b.balances = balances
        }
    }
}
