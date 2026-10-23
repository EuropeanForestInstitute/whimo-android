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

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.commodity.models.ConvertRecipeModel
import com.whimo.navigation.Screens
import com.whimo.presentation.balances.ConvertCommodityArgs
import com.whimo.presentation.balances.ConvertRecipesArgs
import com.whimo.presentation.balances.components.ConvertRecipeItem
import com.whimo.presentation.main.components.EmptyState
import com.whimo.presentation.main.components.LoadingState
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.toJsonArgs
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        ConvertRecipesScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null,
        )
    }
}

@Composable
fun ConvertRecipesScreen(
    modifier: Modifier,
    navController: NavHostController,
    args: ConvertRecipesArgs? = null,
    viewModel: ConvertRecipesViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: ConvertRecipesContract.Binding()

    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is ConvertRecipesContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }

                is ConvertRecipesContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(ConvertRecipesContract.Event.OnCreate(args?.commodity))
    }

    Column(
        modifier = modifier
    ) {
        Toolbar2(
            navController = navController,
            title = stringResource(R.string.convert_commodity),
        )

        if (!binding.recipes.isNullOrEmpty()) {
            ConvertRecipeList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                recipes = binding.recipes!!,
                onClick = {
                    navController.navigate(
                        Screens.ConvertCommodity.putArgs(
                            Screens.ARG_KEY_JSON to ConvertCommodityArgs(
                                recipe = it,
                                harvestSeason = args?.harvestSeason,
                            ).toJsonArgs()
                        )
                    )
                }
            )
        } else if (!isLoading) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                iconRes = R.drawable.ic_empty,
                title = stringResource(R.string.no_recipes),
                description = "",
            )
        }
    }

    if (isLoading) {
        LoadingState()
    }
}

@Composable
fun ConvertRecipeList(
    modifier: Modifier = Modifier,
    recipes: List<ConvertRecipeModel>,
    onClick: (ConvertRecipeModel) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.convert_commodity_text),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(recipes) { recipe ->
            val inputs = recipe.inputs.map { it.commodity.name }
            val outputs = recipe.outputs.map { it.commodity.name }

            ConvertRecipeItem(
                modifier = Modifier.fillMaxWidth(),
                title = recipe.name,
                description = inputs.joinToString(separator = " + ") + " > " + outputs.joinToString(separator = " + "),
                onClick = {
                    onClick(recipe)
                }
            )
        }
    }
}
