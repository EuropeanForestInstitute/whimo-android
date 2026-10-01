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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.presentation.ui.theme.ColorGray20
import com.whimo.presentation.ui.theme.ColorGray40
import com.whimo.presentation.ui.theme.ColorGray50
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {

            SettingsItem(
                iconRes = R.drawable.ic_user_circle,
                title = stringResource(R.string.account_info),
            )

            SettingsOptionItem1(
                iconRes = R.drawable.ic_shield,
                title = stringResource(R.string.legal_information),
            )

            SettingsOptionItem2(
                iconRes = R.drawable.ic_app_version,
                title = stringResource(R.string.app_version),
                value = "0.1",
            )

            SettingsOptionItemBase(
                iconRes = R.drawable.ic_logout,
                title = stringResource(R.string.log_out),
            )
        }
    }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (isEnabled) ColorGray50 else ColorGray20,
        )

        Text(
            modifier = modifier.weight(1f),
            text = title,
            style = TextStyleButtonM,
            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else ColorGray40,
        )

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = if (isEnabled) ColorGray40 else ColorGray20,
        )
    }
}

@Composable
fun SettingsInfoItem(
    modifier: Modifier = Modifier,
    text: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceBright,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SettingsOptionItem1(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    onClick: () -> Unit = {},
) {
    SettingsOptionItemBase(
        modifier = modifier,
        iconRes = iconRes,
        title = title,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = ColorGray40,
        )
    }
}

@Composable
fun SettingsOptionItem2(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    value: String,
    onClick: () -> Unit = {},
) {
    SettingsOptionItemBase(
        modifier = modifier,
        iconRes = iconRes,
        title = title,
        onClick = onClick,
    ) {
        Text(
            text = value,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingsOptionItemBase(
    modifier: Modifier = Modifier,
    iconRes: Int,
    iconTint: Color = ColorGray50,
    title: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
    endContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled) { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (isEnabled) iconTint else ColorGray20,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = TextStyleBodyS,
            color = if (isEnabled) MaterialTheme.colorScheme.onSurface else ColorGray40,
        )

        endContent()
    }
}
