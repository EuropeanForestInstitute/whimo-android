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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.presentation.ui.theme.ColorGray5
import com.whimo.presentation.ui.theme.ColorGray50
import com.whimo.presentation.ui.theme.ColorWarning
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.ForceUpdateController
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.PhoneVisualTransformation

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BaseTextField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Label",
                hintText = "Hint",
                text = "",
            )
            BaseTextField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Label",
                hintText = "Hint",
                text = "",
                supportingText = "Text"
            )
            BaseTextField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Label",
                hintText = "Hint",
                text = "Text",
                warningText = "Text"
            )
            BaseTextField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Label",
                hintText = "Hint",
                errorText = "Error",
                text = "",
            )

            PhoneNumberField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Phone",
                hintText = "000 000",
                phoneNumber = "123 456",
            )

            PhoneNumberField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Phone",
                hintText = "000 000",
                phoneNumber = "123 456",
                onPhoneRegionClicked = {},
            )

            EmailField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Email",
                hintText = "Enter email",
                email = "",
            )

            PasswordField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Password",
                hintText = "Enter password",
                password = "",
            )

            PasswordField(
                modifier = Modifier.fillMaxWidth(),
                labelText = "Password",
                hintText = "Enter password",
                password = "Password",
            )
        }
    }
}

@Composable
fun PasswordField(
    modifier: Modifier = Modifier,
    controller: ForceUpdateController = ForceUpdateController(),

    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,

    labelText: String = "",
    hintText: String = "",
    errorText: String = "",

    password: String,

    keyboardActions: KeyboardActions = KeyboardActions.Default,

    onValueChange: (String) -> Unit = {},
    onForgotPasswordClick: (() -> Unit)? = null,
) {
    var passwordIsVisible by remember { mutableStateOf(false) }

    BaseTextField(
        modifier = modifier,
        controller = controller,

        isEnabled = isEnabled,
        isReadOnly = isReadOnly,

        labelText = labelText,
        hintText = hintText,
        errorText = errorText,
        text = password,

        visualTransformation = if (passwordIsVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = keyboardActions,

        labelEndContent = {
            if (onForgotPasswordClick != null) {
                Text(
                    modifier = Modifier.clickable { onForgotPasswordClick() },
                    text = stringResource(R.string.forgot_password_q),
                    style = TextStyleButtonM,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                tint = ColorGray50
            )
        },
        trailingIcon = {
            val image = if (passwordIsVisible) {
                Icons.Outlined.Visibility
            } else {
                Icons.Outlined.VisibilityOff
            }

            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        passwordIsVisible = !passwordIsVisible
                    },
                imageVector = image,
                contentDescription = null,
                tint = ColorGray50
            )
        },

        onValueChange = onValueChange,
    )
}

@Composable
fun EmailField(
    modifier: Modifier = Modifier,
    controller: ForceUpdateController = ForceUpdateController(),

    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,

    labelText: String = "",
    hintText: String = "",
    errorText: String = "",

    email: String,

    keyboardActions: KeyboardActions = KeyboardActions.Default,

    onValueChange: (String) -> Unit = {},
    onContactsClicked: (() -> Unit)? = null,
) {
    BaseTextField(
        modifier = modifier,
        controller = controller,

        isEnabled = isEnabled,
        isReadOnly = isReadOnly,

        labelText = labelText,
        hintText = hintText,
        errorText = errorText,
        text = email,

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = keyboardActions,

        leadingIcon = {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_envelope),
                contentDescription = null,
                tint = ColorGray50
            )
        },

        trailingIcon = if (onContactsClicked != null) {
            {
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onContactsClicked() },
                    painter = painterResource(id = R.drawable.ic_contacts),
                    contentDescription = null,
                    tint = ColorGray50
                )
            }
        } else {
            null
        },

        onValueChange = onValueChange,
    )
}

@Composable
fun PhoneNumberField(
    modifier: Modifier = Modifier,
    controller: ForceUpdateController = ForceUpdateController(),

    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,

    labelText: String = "",
    hintText: String = "",
    errorText: String = "",

    phoneRegion: PhoneNumberUtils.PhoneRegion = PhoneNumberUtils.getDefaultPhoneRegion(),
    phoneNumber: String,

    keyboardActions: KeyboardActions = KeyboardActions.Default,

    onValueChange: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    onPhoneRegionClicked: (() -> Unit)? = null,
    onContactsClicked: (() -> Unit)? = null,
) {
    BaseTextField(
        modifier = modifier,
        controller = controller,

        isEnabled = isEnabled,
        isReadOnly = isReadOnly,

        labelText = labelText,
        hintText = hintText,
        errorText = errorText,
        text = phoneNumber,

        filterValue = { it.isDigit() },
        visualTransformation = PhoneVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        keyboardActions = keyboardActions,

        prefix = {
            Row(
                modifier = Modifier
                    .clickable(onPhoneRegionClicked != null) {
                        onPhoneRegionClicked?.invoke()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = phoneRegion.flag,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "+${phoneRegion.phoneCode}",
                    style = TextStyleBodyS,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (onPhoneRegionClicked != null) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },

        trailingIcon = if (onContactsClicked != null) {
            {
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onContactsClicked() },
                    painter = painterResource(id = R.drawable.ic_contacts),
                    contentDescription = null,
                    tint = ColorGray50
                )
            }
        } else {
            null
        },

        onValueChange = onValueChange,
        onFocusChanged = onFocusChanged,
    )
}

@Composable
fun BaseTextField(
    modifier: Modifier = Modifier,
    controller: ForceUpdateController = ForceUpdateController(),

    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,

    labelText: String = "",
    hintText: String = "",
    errorText: String = "",
    warningText: String = "",
    supportingText: String = "",
    text: String = "",

    filterValue: (Char) -> Boolean = { true },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,

    labelEndContent: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,

    onValueChange: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},

    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
    disabledBorderColor: Color = ColorGray5,

    focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,

    focusedLeadingIconColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor: Color = ColorGray50,
    errorLeadingIconColor: Color = ColorGray50,

    focusedTrailingIconColor: Color = ColorGray50,
    unfocusedTrailingIconColor: Color = ColorGray50,
    errorTrailingIconColor: Color = ColorGray50,

    errorBorderColor: Color = MaterialTheme.colorScheme.error,
    errorCursorColor: Color = MaterialTheme.colorScheme.error,
    errorLabelColor: Color = MaterialTheme.colorScheme.error,
    errorContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    var mText by remember { mutableStateOf(text) }

    val generation by controller.generation.collectAsState()
    var appliedGeneration by remember { mutableLongStateOf(-1) }

    LaunchedEffect(generation, text) {
        if (generation > appliedGeneration) {
            mText = text
            appliedGeneration = generation
        }
    }

    var supportingView: @Composable (() -> Unit)? = null

    if (errorText.isNotEmpty()) {
        supportingView = {
            Text(
                text = errorText,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.error,
            )
        }
    } else if (warningText.isNotEmpty()) {
        supportingView = {
            Text(
                text = warningText,
                style = TextStyleBodyS,
                color = ColorWarning,
            )
        }
    } else if (supportingText.isNotEmpty()) {
        supportingView = {
            Text(
                text = supportingText,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {

        if (labelText.isNotEmpty() || labelEndContent != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = labelText,
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (labelEndContent != null) {
                    Spacer(modifier = Modifier.weight(1f))

                    labelEndContent()
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            shape = RoundedCornerShape(8.dp),

            enabled = isEnabled,
            readOnly = isReadOnly,
            isError = errorText.isNotEmpty(),
            singleLine = true,

            value = if (isReadOnly) text else mText,

            textStyle = TextStyleBodyS,
            placeholder = {
                Text(
                    text = hintText,
                    style = TextStyleBodyS,
                    color = ColorGray50,
                )
            },
            supportingText = supportingView,

            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,

            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,

            onValueChange = {
                mText = it.filter(filterValue)
                onValueChange(it)
            },

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focusedBorderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = disabledBorderColor,

                focusedContainerColor = focusedContainerColor,
                unfocusedContainerColor = unfocusedContainerColor,
                disabledContainerColor = disabledContainerColor,

                focusedLeadingIconColor = focusedLeadingIconColor,
                unfocusedLeadingIconColor = unfocusedLeadingIconColor,
                errorLeadingIconColor = errorLeadingIconColor,

                focusedTrailingIconColor = focusedTrailingIconColor,
                unfocusedTrailingIconColor = unfocusedTrailingIconColor,
                errorTrailingIconColor = errorTrailingIconColor,

                errorBorderColor = errorBorderColor,
                errorCursorColor = errorCursorColor,
                errorLabelColor = errorLabelColor,
                errorContainerColor = errorContainerColor,
            ),
        )
    }
}
