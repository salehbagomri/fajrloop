package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

/**
 * صورة المستخدم — مع حرف أول كـ fallback
 */
@Composable
fun UserAvatar(
    photoUrl: String,
    modifier: Modifier = Modifier,
    userName: String = "",
    size: Dp = 40.dp,
    borderColor: Color = FajrLoopColors.Border
) {
    if (photoUrl.isNotEmpty()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "صورة المستخدم",
            placeholder = painterResource(id = R.drawable.ic_default_avatar),
            error = painterResource(id = R.drawable.ic_default_avatar),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, borderColor, CircleShape)
        )
    } else {
        // Fallback: الحرف الأول من الاسم
        val initial = userName.firstOrNull()?.toString() ?: "?"
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(FajrLoopColors.SurfaceVariant)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.4f).sp,
                color = FajrLoopColors.Primary
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    FajrLoopTheme {
        UserAvatar(photoUrl = "", userName = "صالح")
    }
}
