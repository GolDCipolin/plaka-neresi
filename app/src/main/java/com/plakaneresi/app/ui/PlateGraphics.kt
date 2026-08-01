package com.plakaneresi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.plakaneresi.app.R
import com.plakaneresi.app.ui.theme.PlakaNeresiTheme
import com.plakaneresi.app.ui.theme.PlateBlue
import com.plakaneresi.app.ui.theme.PlateBorder
import com.plakaneresi.app.ui.theme.PlateInk
import com.plakaneresi.app.ui.theme.PlateWhite

/**
 * A miniature Turkish plate: blue "TR" strip on the left, the code on the white field.
 * Everything scales off [height] so the same composable works as a 36dp list bullet and
 * as the 84dp answer on the result card.
 */
@Composable
fun PlateGraphic(
    code: String,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    codeSize: TextUnit = 20.sp,
) {
    val shape = RoundedCornerShape(height * 0.16f)
    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(PlateWhite)
            .border(width = height * 0.04f, color = PlateBorder, shape = shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(height * 0.42f)
                .background(PlateBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TR",
                color = Color.White,
                fontSize = codeSize * 0.38f,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = code,
            color = PlateInk,
            fontSize = codeSize,
            fontWeight = FontWeight.Black,
            letterSpacing = codeSize * 0.06f,
            modifier = Modifier.padding(horizontal = height * 0.24f),
        )
    }
}

/**
 * The search box, drawn as a plate you type into.
 *
 * Input is force-uppercased by the keyboard because that is how plates read; the search
 * itself is case-blind either way. It grabs focus on launch — the app has exactly one
 * job and the user already has a plate in front of them.
 */
@Composable
fun PlateSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .height(78.dp)
            .clip(shape)
            .background(PlateWhite)
            .border(width = 2.dp, color = PlateBorder, shape = shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(34.dp)
                .background(PlateBlue),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TR",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = PlateInk,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            ),
            cursorBrush = SolidColor(PlateBlue),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            color = PlateInk.copy(alpha = 0.3f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (value.isNotEmpty()) {
            IconButton(onClick = onClear, modifier = Modifier.padding(end = 4.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.clear),
                    tint = PlateInk.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PlateGraphicPreview() {
    PlakaNeresiTheme {
        PlateGraphic(code = "48", height = 84.dp, codeSize = 46.sp)
    }
}
