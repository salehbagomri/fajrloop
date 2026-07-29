package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

/**
 * شريط علوي موحد — خلفية شفافة، أيقونة رجوع Outlined
 * يتناسب مع حواشي شريط النظام (Status Bar Safe Padding)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FajrLoopTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = FajrLoopColors.TextPrimary
            )
        },
        modifier = modifier.statusBarsPadding(),
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = FajrIcons.Back,
                        contentDescription = "رجوع",
                        tint = FajrLoopColors.TextPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Preview
@Composable
private fun FajrLoopTopBarPreview() {
    FajrLoopTheme {
        FajrLoopTopBar(
            title = "الإعدادات",
            onBackClick = {}
        )
    }
}
