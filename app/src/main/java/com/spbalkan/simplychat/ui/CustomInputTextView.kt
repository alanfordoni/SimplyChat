package com.spbalkan.simplychat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

@Composable
fun CustomInputTextView(
    modifier: Modifier = Modifier,
    labelText: String? = null,
    inputText: String = "",
    inputHint: String = "",
    errorText: String = "",
    isError: Boolean = false,
    isDirty: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    minLines: Int = 1,
    onInputChanged: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        labelText?.let { mLabel ->
            Text(
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                text = mLabel,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
            )
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            value = inputText,
            placeholder = {
                Text(
                    inputHint,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                )
            },
            onValueChange = { onInputChanged(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = imeAction
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Black,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red,
            ),
            singleLine = minLines == 1,
            minLines = minLines,
            maxLines = if(minLines == 1) 1 else 5,
            shape = RoundedCornerShape(16.dp),
            isError = isError && isDirty
        )
        if (isError && errorText.isNotEmpty()) {
            Text(modifier = Modifier
                .padding(start = 16.dp, top = 2.dp),
                text = errorText,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red)
            )
        }
    }
}

@Preview
@Composable
fun CustomInputTextViewPreview() {
    SimplyChatTheme {
        CustomInputTextView(
            modifier = Modifier.padding(20.dp),
            "Input",
            "",
            "This is hint",
            "not valid",
            true,
            isDirty = true
        ) {}
    }
}

@Composable
fun CustomPasswordInputText(
    modifier: Modifier = Modifier,
    labelText: String,
    password: String,
    isError: Boolean = false,
    errorText: String = "",
    isDirty: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onPasswordChange: (String) -> Unit,
) {
    // State to toggle the password's visibility
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {

        Text(
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            text = labelText,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                ,
            // Use PasswordVisualTransformation to obscure the text
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
            // Add a trailing icon to toggle visibility
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = if (passwordVisible) painterResource(id = R.drawable.ic_visibility_off) else painterResource(
                            id = R.drawable.ic_visibility_on,
                        ),
                        /*tint = ,*/
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Black,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Gray, cursorColor = Color.Gray,
                errorBorderColor = Color.Red
            ),
            singleLine = true,
            isError = isError && isDirty,
        )

        if (isError && errorText.isNotEmpty()) {
            Text(modifier = Modifier
                .padding(start = 16.dp, top = 2.dp),
                text = errorText,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red)
            )
        }
    }
}