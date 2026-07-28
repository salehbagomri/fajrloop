package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FajrLoopColors.Gold,
            contentColor = FajrLoopColors.Background,
            disabledContainerColor = FajrLoopColors.Gold.copy(alpha = 0.4f),
            disabledContentColor = FajrLoopColors.Background.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun TransparentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FajrLoopColors.Gold.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FajrLoopColors.Gold
        )
    ) {
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FajrLoopColors.DangerRed.copy(alpha = 0.3f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = FajrLoopColors.DangerRed.copy(alpha = 0.1f),
            contentColor = FajrLoopColors.DangerRed
        )
    ) {
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Preview
@Composable
fun ButtonsPreview() {
    FajrLoopTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoldButton(text = "زر ذهبي رئيسي", onClick = {}, modifier = Modifier.fillMaxWidth())
            TransparentButton(text = "زر شفاف ثانوي", onClick = {}, modifier = Modifier.fillMaxWidth())
            DangerButton(text = "زر خطر / خروج", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
