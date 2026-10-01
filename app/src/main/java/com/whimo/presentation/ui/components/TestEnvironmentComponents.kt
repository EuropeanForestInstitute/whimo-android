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
package com.whimo.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.providers.TestEnvironmentManager
import com.whimo.presentation.ui.theme.ColorMulberryPurple10
import com.whimo.presentation.ui.theme.ColorPlumPurple
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.compose.koinInject

@Preview
@Composable
private fun TestEnvironmentBannerPreview() {
    WhimoTheme {
        TestEnvironmentBannerContent(
            modifier = Modifier.fillMaxWidth(),
            isTestEnvironment = true,
        )
    }
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun TestEnvironmentFramePreview() {
    WhimoTheme {
        TestEnvironmentFrameContent(
            modifier = Modifier.fillMaxSize(),
            isTestEnvironment = true,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(top = 60.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Authenticated content",
                    style = TextStyleBodyS,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun TestEnvironmentFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val testEnvironmentManager: TestEnvironmentManager = koinInject()
    val isTestEnvironment by testEnvironmentManager.isTestEnvironment.collectAsState()

    TestEnvironmentFrameContent(
        modifier = modifier,
        isTestEnvironment = isTestEnvironment,
        content = content,
    )
}

@Composable
private fun TestEnvironmentFrameContent(
    modifier: Modifier = Modifier,
    isTestEnvironment: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()

        if (isTestEnvironment) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val halfStrokeWidth = strokeWidth / 2
                        drawLine(
                            color = ColorPlumPurple,
                            start = Offset(halfStrokeWidth, 0f),
                            end = Offset(halfStrokeWidth, size.height),
                            strokeWidth = strokeWidth,
                        )
                        drawLine(
                            color = ColorPlumPurple,
                            start = Offset(size.width - halfStrokeWidth, 0f),
                            end = Offset(size.width - halfStrokeWidth, size.height),
                            strokeWidth = strokeWidth,
                        )
                        drawLine(
                            color = ColorPlumPurple,
                            start = Offset(0f, size.height - halfStrokeWidth),
                            end = Offset(size.width, size.height - halfStrokeWidth),
                            strokeWidth = strokeWidth,
                        )
                    }
            )
        }
    }
}

@Composable
fun TestEnvironmentBanner(
    modifier: Modifier = Modifier,
) {
    val testEnvironmentManager: TestEnvironmentManager = koinInject()
    val isTestEnvironment by testEnvironmentManager.isTestEnvironment.collectAsState()

    TestEnvironmentBannerContent(
        modifier = modifier,
        isTestEnvironment = isTestEnvironment,
    )
}

@Composable
private fun TestEnvironmentBannerContent(
    modifier: Modifier = Modifier,
    isTestEnvironment: Boolean,
) {
    if (!isTestEnvironment) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorMulberryPurple10)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val halfStrokeWidth = strokeWidth / 2
                drawLine(
                    color = ColorPlumPurple,
                    start = Offset(0f, halfStrokeWidth),
                    end = Offset(size.width, halfStrokeWidth),
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = ColorPlumPurple,
                    start = Offset(halfStrokeWidth, 0f),
                    end = Offset(halfStrokeWidth, size.height),
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = ColorPlumPurple,
                    start = Offset(size.width - halfStrokeWidth, 0f),
                    end = Offset(size.width - halfStrokeWidth, size.height),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.test_environment_banner),
            textAlign = TextAlign.Center,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
