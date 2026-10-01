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
package com.whimo.presentation.createtransaction.commodity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.createtransaction.models.getCommodityText
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.navigation.Screens
import com.whimo.presentation.createtransaction.SharedTransactionViewModel
import com.whimo.presentation.createtransaction.components.CreateTransactionWarning
import com.whimo.presentation.main.components.HarvestSeasonSelector
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.baseScreen.MainButton
import com.whimo.presentation.ui.components.BaseTextField
import com.whimo.presentation.ui.theme.ColorLightOrange
import com.whimo.presentation.ui.theme.ColorWarning
import com.whimo.presentation.ui.theme.ColorWarning10
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.TextStyleMediumL
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        CommodityVolumeScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            sharedTransactionViewModel = null,
            viewModel = null,
        )
    }
}

@Composable
fun CommodityVolumeScreen(
    modifier: Modifier,
    navController: NavHostController,
    sharedTransactionViewModel: SharedTransactionViewModel? = null,
    viewModel: CommodityVolumeViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: CommodityVolumeContract.Binding()

    var volume by remember { mutableStateOf(sharedTransactionViewModel?.transaction?.volume?.toString()) }
    val draftCommodityId = sharedTransactionViewModel?.transaction?.commodity?.id
    val draftVolume = sharedTransactionViewModel?.transaction?.volume?.toString()

    LaunchedEffect(draftCommodityId, draftVolume) {
        if (volume != draftVolume) {
            volume = draftVolume
        }
    }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is CommodityVolumeContract.Effect.VolumeConfirmed -> {
                    sharedTransactionViewModel?.setCommodityVolume(
                        volume = effect.volume,
                        harvestSeason = effect.harvestSeason,
                        commodity = effect.commodity,
                    )
                    navController.popBackStack()
                }
                is CommodityVolumeContract.Effect.ShowMessage -> {
                    Toast.makeText(navController.context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        if (sharedTransactionViewModel != null) {
            viewModel?.setEvent(
                CommodityVolumeContract.Event.OnCreate(sharedTransactionViewModel.transaction)
            )
        }
    }

    Column(
        modifier = modifier
    ) {
        Toolbar2(
            navController = navController,
            title = stringResource(R.string.volume_commodities),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val commodityText = sharedTransactionViewModel?.transaction.getCommodityText()

                Text(
                    text = commodityText ?: stringResource(
                        if (sharedTransactionViewModel?.transaction?.action == TransactionAction.Selling) {
                            R.string.select_commodity_type_to_sell
                        } else {
                            R.string.select_commodity_type_to_buy
                        }
                    ),
                    modifier = Modifier.weight(1f),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (commodityText == null) {
                    Text(
                        modifier = Modifier
                            .clickable {
                                navController.navigate(Screens.CommodityTypes.route)
                            },
                        text = stringResource(R.string.select),
                        style = TextStyleButtonM,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(20.dp)
                            .clickable {
                                navController.navigate(Screens.CommodityTypes.route)
                            },
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (binding.harvestSeasonSelectorVisible) {
                HarvestSeasonSelector(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    selectedSeason = binding.selectedHarvestSeason,
                    seasons = binding.harvestSeasons,
                    enabled = binding.inputEnabled,
                    onSelect = {
                        viewModel?.setEvent(CommodityVolumeContract.Event.OnHarvestSeasonSelected(it))
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                BaseTextField(
                    modifier = Modifier.weight(2f),
                    hintText = stringResource(R.string.enter_number),
                    labelText = stringResource(R.string.number),
                    text = volume ?: "",
                    isEnabled = binding.inputEnabled,
                    warningText = binding.warningText,
                    supportingText = binding.supportingText,
                    onValueChange = {
                        volume = it
                        viewModel?.setEvent(CommodityVolumeContract.Event.OnVolumeChanged(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )

                BaseTextField(
                    modifier = Modifier.weight(1f),
                    isEnabled = false,
                    hintText = sharedTransactionViewModel?.transaction?.commodity?.unit ?: "",
                    labelText = stringResource(R.string.unit),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )
            }

            if (binding.insufficientBalance) {
                CreateTransactionWarning(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    backgroundColor = ColorLightOrange,
                    borderColor = ColorWarning10,
                    iconRes = R.drawable.ic_warning,
                    iconTint = ColorWarning,
                    title = binding.insufficientBalanceMessage,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            MainButton(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                isEnabled = binding.buttonEnabled,
                title = stringResource(R.string.confirm),
                onClick = {
                    viewModel?.setEvent(CommodityVolumeContract.Event.OnConfirm)
                }
            )
        }
    }
}
