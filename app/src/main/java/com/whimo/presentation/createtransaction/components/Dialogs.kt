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
package com.whimo.presentation.createtransaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.presentation.settings.components.DialogButton
import com.whimo.presentation.transactions.transactiondetails.components.BaseDialog
import com.whimo.presentation.transactions.transactiondetails.components.DialogTextItem
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        CreateTransactionDialog(
            title = stringResource(R.string.save_transaction_title),
            description = stringResource(R.string.save_transaction_description),
            actionButtonTitle = stringResource(R.string.save_transaction),
            secondButtonTitle = stringResource(R.string.review_information),
        )
        CreateTransactionDialog(
            title = stringResource(R.string.save_without_location_title),
            description = stringResource(R.string.save_without_location_description),
            actionButtonTitle = stringResource(R.string.add_farm_geodata),
            secondButtonTitle = stringResource(R.string.save_without_location),
        )
        CreateTransactionMessage(
            title = stringResource(R.string.create_transaction_error),
            description = stringResource(R.string.something_went_wrong),
        )
    }
}

@Composable
fun CreateTransactionMessage(
    title: String,
    description: String,
    onDismiss: () -> Unit = {},
) {
    BaseDialog(
        title = title,
        onDismiss = onDismiss
    ) {
        DialogTextItem(title = description)
    }
}

@Composable
fun CreateTransactionDialog(
    title: String,
    description: String,
    warningDescription: String? = null,
    warningIconRes: Int = R.drawable.ic_information,
    warningBackgroundColor: Color = MaterialTheme.colorScheme.surfaceBright,
    warningBorderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    warningIconTint: Color = MaterialTheme.colorScheme.primary,
    actionButtonTitle: String,
    secondButtonTitle: String,
    onActionClick: () -> Unit = {},
    onSecondClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    BaseDialog(
        title = title,
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            DialogTextItem(title = description)

            if (!warningDescription.isNullOrBlank()) {
                CreateTransactionWarning(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp),
                    backgroundColor = warningBackgroundColor,
                    borderColor = warningBorderColor,
                    iconRes = warningIconRes,
                    iconTint = warningIconTint,
                    title = warningDescription,
                )
            }
        }

        DialogButtonsItemV(
            actionButtonTitle = actionButtonTitle,
            actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
            actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
            secondButtonTitle = secondButtonTitle,
            onActionClick = {
                onDismiss()
                onActionClick()
            },
            onSecondClick = {
                onDismiss()
                onSecondClick()
            },
        )
    }
}

@Composable
fun DialogButtonsItemV(
    actionButtonTitle: String,
    actionButtonBackgroundColor: Color,
    actionButtonBorderColor: Color = actionButtonBackgroundColor,
    actionButtonTitleColor: Color,
    secondButtonTitle: String = stringResource(R.string.cancel),
    secondButtonBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    secondButtonBorderColor: Color = MaterialTheme.colorScheme.outline,
    secondButtonTitleColor: Color = MaterialTheme.colorScheme.onSurface,
    onActionClick: () -> Unit = {},
    onSecondClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        DialogButton(
            modifier = Modifier.fillMaxWidth(),
            title = secondButtonTitle,
            titleColor = secondButtonTitleColor,
            backgroundColor = secondButtonBackgroundColor,
            borderColor = secondButtonBorderColor,
            onClick = onSecondClick,
        )

        DialogButton(
            modifier = Modifier.fillMaxWidth(),
            title = actionButtonTitle,
            titleColor = actionButtonTitleColor,
            backgroundColor = actionButtonBackgroundColor,
            borderColor = actionButtonBorderColor,
            onClick = onActionClick,
        )
    }
}
