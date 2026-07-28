package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

@Composable
fun UserAvatar(
    photoUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderColor: Color = FajrLoopColors.Gold.copy(alpha = 0.5f)
) {
    val defaultAvatarPainter = painterResource(id = R.drawable.ic_default_avatar)

    AsyncImage(
        model = photoUrl.ifEmpty { null },
        contentDescription = "صورة المستخدم",
        placeholder = defaultAvatarPainter,
        error = defaultAvatarPainter,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
    )
}

@Preview
@Composable
fun UserAvatarPreview() {
    FajrLoopTheme {
        UserAvatar(photoUrl = "")
    }
}
