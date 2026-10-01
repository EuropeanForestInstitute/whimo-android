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
package com.whimo.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.presentation.ui.theme.ColorMidnightBlue
import com.whimo.presentation.ui.theme.ColorMidnightBlue10
import com.whimo.presentation.ui.theme.ColorMulberryPurple
import com.whimo.presentation.ui.theme.ColorMulberryPurple10
import com.whimo.presentation.ui.theme.ColorSeaBlue
import com.whimo.presentation.ui.theme.ColorSeaBlue10
import com.whimo.presentation.ui.theme.ColorSuccess
import com.whimo.presentation.ui.theme.ColorSuccess10
import com.whimo.presentation.ui.theme.TextStyleMediumS
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TraceabilityStatusView(status = TraceabilityStatus.Full)
            TraceabilityStatusView(status = TraceabilityStatus.Conditional)
            TraceabilityStatusView(status = TraceabilityStatus.Partial)
            TraceabilityStatusView(status = TraceabilityStatus.Incomplete)
        }
    }
}

@Composable
fun TraceabilityStatusView(
    status: TraceabilityStatus,
    modifier: Modifier = Modifier,
    shortName: Boolean = false,
) {
    var bgColor = ColorSuccess10
    var text = stringResource(if (shortName) R.string.full else R.string.full_traceability)
    var textColor = ColorSuccess

    when (status) {
        TraceabilityStatus.Full -> {
            bgColor = ColorSuccess10
            text = stringResource(if (shortName) R.string.full else R.string.full_traceability)
            textColor = ColorSuccess
        }
        TraceabilityStatus.Conditional -> {
            bgColor = ColorSeaBlue10
            text = stringResource(if (shortName) R.string.conditional else R.string.conditional_traceability)
            textColor = ColorSeaBlue
        }
        TraceabilityStatus.Partial -> {
            bgColor = ColorMulberryPurple10
            text = stringResource(if (shortName) R.string.partial else R.string.partial_traceability)
            textColor = ColorMulberryPurple
        }
        TraceabilityStatus.Incomplete -> {
            bgColor = ColorMidnightBlue10
            text = stringResource(if (shortName) R.string.incomplete else R.string.incomplete_traceability)
            textColor = ColorMidnightBlue
        }
    }


    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            color = textColor,
            style = TextStyleMediumS,
        )
    }
}
