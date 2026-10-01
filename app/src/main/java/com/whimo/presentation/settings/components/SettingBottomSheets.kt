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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.BuildConfig
import com.whimo.R
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        SettingsOptionsBottomSheet(
            onLegalInfoClick = {},
            onLogoutClick = {},
            onDeleteAccountClick = {},
            onDismissRequest = {},
        )
    }
}

@Composable
fun SettingsOptionsBottomSheet(
    onLegalInfoClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    isAccountActionsEnabled: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    BottomSheetBase(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            SettingsOptionItem2(
                iconRes = R.drawable.ic_app_version,
                title = stringResource(R.string.app_version),
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            )

            SettingsOptionItem1(
                iconRes = R.drawable.ic_shield,
                title = stringResource(R.string.legal_information),
                onClick = onLegalInfoClick,
            )

            SettingsOptionItemBase(
                iconRes = R.drawable.ic_logout,
                title = stringResource(R.string.log_out),
                isEnabled = isAccountActionsEnabled,
                onClick = onLogoutClick,
            )

            SettingsOptionItemBase(
                iconRes = R.drawable.ic_delete,
                title = stringResource(R.string.delete_account),
                isEnabled = isAccountActionsEnabled,
                onClick = onDeleteAccountClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetBase(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.wrapContentHeight(),
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}
