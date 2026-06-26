package com.harnie.app.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.harnie.app.ui.theme.MonoFontFamily

@Composable
fun MonoText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 1
) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = MonoFontFamily,
            fontSize = fontSize,
            fontWeight = fontWeight,
        ),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines
    )
}