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

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.commodity.models.ConvertQuantityModel
import com.whimo.extensions.findActivity
import com.whimo.presentation.balances.ConvertCommodityArgs
import com.whimo.presentation.createtransaction.components.CreateTransactionDialog
import com.whimo.presentation.createtransaction.components.CreateTransactionWarning
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.baseScreen.LoadingButton
import com.whimo.presentation.ui.components.BaseTextField
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleBodyXS
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        ConvertCommodityScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun ConvertCommodityScreen(
    modifier: Modifier,
    navController: NavHostController,
    args: ConvertCommodityArgs? = null,
    viewModel: ConvertCommodityViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: ConvertCommodityContract.Binding()

    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is ConvertCommodityContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
                is ConvertCommodityContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is ConvertCommodityContract.Effect.ShowConfirmDialog -> {
                    showConfirmDialog = true
                }
                is ConvertCommodityContract.Effect.ConversionSuccess -> {
                    val activity = context.findActivity()
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(ConvertCommodityContract.Event.OnCreate(args))
    }

    Column(
        modifier = modifier
    ) {
        Toolbar2(
            navController = navController,
            title = stringResource(R.string.convert_commodity),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CreateTransactionWarning(
                modifier = Modifier.padding(16.dp),
                iconRes = R.drawable.ic_information,
                title = stringResource(R.string.convert_commodity_info),
            )

            if (!binding.inputs.isNullOrEmpty()) {
                val balances = binding.inputs!!.joinToString(separator = ", ") {
                    it.commodity.getBalanceText()
                }

                QuantitiesList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    header = stringResource(R.string.from),
                    headerText = stringResource(R.string.your_balance, balances),
                    items = binding.inputs!!,
                    onQuantityChange = { model, quantity ->
                        viewModel?.setEvent(ConvertCommodityContract.Event.OnQuantityChange(model, quantity))
                    },
                )
            }

            if (!binding.outputs.isNullOrEmpty()) {
                QuantitiesList(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    header = stringResource(R.string.to),
                    items = binding.outputs!!,
                    onQuantityChange = { model, quantity ->
                        viewModel?.setEvent(ConvertCommodityContract.Event.OnQuantityChange(model, quantity))
                    },
                )
            }
        }

        LoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            isLoading = isLoading,
            title = stringResource(R.string.convert),
            onClick = {
                viewModel?.setEvent(ConvertCommodityContract.Event.OnConvertClick)
            }
        )
    }


    if (showConfirmDialog) {
        CreateTransactionDialog(
            title = stringResource(R.string.convert_commodity_dialog_title),
            description = stringResource(R.string.convert_commodity_dialog_description),
            actionButtonTitle = stringResource(R.string.convert),
            secondButtonTitle = stringResource(R.string.cancel),
            onActionClick = {
                viewModel?.setEvent(ConvertCommodityContract.Event.Convert)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }
}

@Composable
fun QuantitiesList(
    modifier: Modifier = Modifier,
    header: String,
    headerText: String = "",
    items: List<ConvertQuantityModel>,
    onQuantityChange: (ConvertQuantityModel, String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = header,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = headerText,
                textAlign = TextAlign.End,
                style = TextStyleBodyXS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items.forEach { item ->
                QuantityItem(
                    model = item,
                    onQuantityChange = {
                        onQuantityChange(item, it)
                    }
                )
            }
        }
    }
}

@Composable
fun QuantityItem(
    model: ConvertQuantityModel,
    onQuantityChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BaseTextField(
            modifier = Modifier.weight(2f),
            isReadOnly = true,
            hintText = stringResource(R.string.enter_number),
            text = model.commodity.name,

            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.outline,

            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        BaseTextField(
            modifier = Modifier.weight(1f),

            text = model.quantity.toString(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),

            suffix = {
                Text(
                    text = model.commodity.unit,
                    style = TextStyleBodyS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },

            onValueChange = onQuantityChange,
        )
    }
}
