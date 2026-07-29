package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

/**
 * حقل إدخال موحد — خلفية SurfaceVariant مع حدود ديناميكية
 */
@Composable
fun FajrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) FajrLoopColors.Primary else FajrLoopColors.Border
    val shape = RoundedCornerShape(Radius.md)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .background(FajrLoopColors.SurfaceVariant, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        textStyle = TextStyle(
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = FajrLoopColors.TextPrimary
        ),
        singleLine = singleLine,
        enabled = enabled,
        cursorBrush = SolidColor(FajrLoopColors.Primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = PpNmArabic,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextTertiary
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview
@Composable
private fun FajrTextFieldPreview() {
    FajrLoopTheme {
        var text by remember { mutableStateOf("") }
        FajrTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "اكتب رسالتك...",
            modifier = Modifier.padding(Spacing.lg)
        )
    }
}
