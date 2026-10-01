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
package com.whimo.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.presentation.transactions.transactiondetails.components.BaseDialog
import com.whimo.presentation.transactions.transactiondetails.components.DialogTextItem
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        BaseDialog(title = stringResource(R.string.log_out)) {
            DialogTextItem(
                title = stringResource(R.string.logout_confirm_message),
            )
            DialogButtonsItem(
                actionButtonTitle = stringResource(R.string.logout),
                actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
                actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                onActionClick = {},
                onSecondClick = {},
            )
        }
        BaseDialog(title = stringResource(R.string.delete_account)) {
            DialogTextItem(
                title = stringResource(R.string.delete_account_confirm_message),
            )
            DialogButtonsItem(
                actionButtonTitle = stringResource(R.string.delete),
                actionButtonBackgroundColor = MaterialTheme.colorScheme.error,
                actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                onActionClick = {},
                onSecondClick = {},
            )
        }
    }
}

@Composable
fun DialogButtonsItem(
    actionButtonTitle: String,
    actionButtonBackgroundColor: Color,
    actionButtonBorderColor: Color = actionButtonBackgroundColor,
    actionButtonTitleColor: Color,
    actionButtonEnabled: Boolean = true,
    secondButtonTitle: String = stringResource(R.string.cancel),
    secondButtonBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    secondButtonBorderColor: Color = MaterialTheme.colorScheme.outline,
    secondButtonTitleColor: Color = MaterialTheme.colorScheme.onSurface,
    secondButtonEnabled: Boolean = true,
    onActionClick: () -> Unit = {},
    onSecondClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        DialogButton(
            modifier = Modifier.weight(1f),
            title = secondButtonTitle,
            titleColor = secondButtonTitleColor,
            backgroundColor = secondButtonBackgroundColor,
            borderColor = secondButtonBorderColor,
            isEnabled = secondButtonEnabled,
            onClick = onSecondClick,
        )

        DialogButton(
            modifier = Modifier.weight(1f),
            title = actionButtonTitle,
            titleColor = actionButtonTitleColor,
            backgroundColor = actionButtonBackgroundColor,
            borderColor = actionButtonBorderColor,
            isEnabled = actionButtonEnabled,
            onClick = onActionClick,
        )
    }
}

@Composable
fun DialogButton(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    backgroundColor: Color,
    borderColor: Color = backgroundColor,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.alpha(if (isEnabled) 1f else 0.5f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            modifier = modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(8.dp),
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable(enabled = isEnabled) { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = title,
                style = TextStyleButtonM,
                color = titleColor,
            )
        }
    }
}
