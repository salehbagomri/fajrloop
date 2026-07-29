package com.bagomri.fajrloop.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.data.ChatMessage
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

data class QuickChipItem(
    val text: String,
    val icon: ImageVector
)

data class MotivationalPresetItem(
    val text: String,
    val category: String,
    val icon: ImageVector
)

val quickChips = listOf(
    QuickChipItem("الصلاة خير من النوم", Icons.Outlined.WbTwilight),
    QuickChipItem("همّتكم يا أبطال الفجر", Icons.Outlined.Bolt),
    QuickChipItem("فجر مبارك للجميع", Icons.Outlined.AutoAwesome),
    QuickChipItem("لا تنسوا الأذكار", Icons.Outlined.MenuBook)
)

val motivationalPresets = listOf(
    MotivationalPresetItem("الصلاة خير من النوم", "حديث شريف", Icons.Outlined.WbTwilight),
    MotivationalPresetItem("همّتكم يا أبطال الفجر", "تنافس إيماني", Icons.Outlined.Bolt),
    MotivationalPresetItem("فجر مبارك للجميع", "تحية الصباح", Icons.Outlined.AutoAwesome),
    MotivationalPresetItem("لا تنسوا أذكار الصباح", "تذكير بالتحصين", Icons.Outlined.MenuBook),
    MotivationalPresetItem("ألا إن سلعة الله غالية، ألا إن سلعة الله الجنة", "حديث شريف", Icons.Outlined.Diamond),
    MotivationalPresetItem("من صلى الفجر في جماعة فهو في ذمة الله", "صحيح مسلم", Icons.Outlined.Shield)
)

private fun formatShortName(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return if (parts.size > 2) {
        "${parts[0]} ${parts[1]}..."
    } else {
        fullName
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    title: String,
    messages: List<ChatMessage>,
    currentUid: String,
    onSendMessage: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showMotivationalSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Top Motivational Button
            FajrLoopTopBar(
                title = title.ifEmpty { "محادثة الحلقة" },
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showMotivationalSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "رسائل تحفيزية",
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )

            // Messages Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262033)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = FajrLoopColors.Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Text(
                            text = "لا توجد رسائل حتى الآن",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FajrLoopColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Text(
                            text = "كن أول من يحيي أعضاء الحلقة ويكتب رسالة تذكيرية!",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(vertical = Spacing.md)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatBubbleItem(
                                message = message,
                                isCurrentUser = message.senderId == currentUid
                            )
                        }
                    }
                }
            }

            // Quick Chips Bar with Outlined Icons
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(quickChips) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(Color(0xFF1E1C30))
                            .border(
                                1.dp,
                                FajrLoopColors.Primary.copy(alpha = 0.35f),
                                RoundedCornerShape(Radius.full)
                            )
                            .clickable { onSendMessage(chip.text, "normal") }
                            .padding(horizontal = Spacing.md, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = chip.icon,
                                contentDescription = null,
                                tint = FajrLoopColors.Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = chip.text,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = FajrLoopColors.Primary
                            )
                        }
                    }
                }
            }

            // Floating Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                // Input Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "اكتب رسالة للحلقة...",
                            fontFamily = PpNmArabic,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextTertiary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.full),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FajrLoopColors.Primary,
                        unfocusedBorderColor = Color(0xFF332F4B),
                        focusedTextColor = FajrLoopColors.TextPrimary,
                        unfocusedTextColor = FajrLoopColors.TextPrimary,
                        focusedContainerColor = Color(0xFF1B192C),
                        unfocusedContainerColor = Color(0xFF1B192C)
                    ),
                    singleLine = true
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim(), "normal")
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(FajrLoopColors.Primary)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "إرسال",
                        tint = FajrLoopColors.Background,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Motivational BottomSheet with Outlined Icons
        if (showMotivationalSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMotivationalSheet = false },
                containerColor = Color(0xFF161528),
                contentColor = FajrLoopColors.TextPrimary,
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.xl, vertical = Spacing.md)
                ) {
                    Text(
                        text = "اختر رسالة تحفيزية وإيمانية ✨",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.Primary,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        motivationalPresets.forEach { preset ->
                            Surface(
                                onClick = {
                                    onSendMessage(preset.text, "motivational")
                                    showMotivationalSheet = false
                                },
                                shape = RoundedCornerShape(Radius.md),
                                color = Color(0xFF1E1D33),
                                border = BorderStroke(1.dp, FajrLoopColors.Primary.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = preset.icon,
                                            contentDescription = null,
                                            tint = FajrLoopColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.sm))
                                        Text(
                                            text = preset.text,
                                            fontFamily = PpNmArabic,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = FajrLoopColors.TextPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(FajrLoopColors.PrimaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = preset.category,
                                            fontFamily = PpNmArabic,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = FajrLoopColors.Primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale("ar")) }
    val formattedTime = timeFormat.format(Date(message.timestamp))
    val isMotivational = message.type == "motivational"
    val senderShortName = formatShortName(message.senderName)

    if (isMotivational) {
        // Full-width Centered Banner for Motivational Messages
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xs, horizontal = Spacing.xs)
                .clip(RoundedCornerShape(Radius.md))
                .background(Color(0xFF221C12))
                .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.5f), RoundedCornerShape(Radius.md))
                .padding(Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "رسالة تحفيزية إيمانية",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = FajrLoopColors.Primary
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = message.message,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "بواسطة: $senderShortName · $formattedTime",
                    fontFamily = PpNmArabic,
                    fontSize = 10.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Regular Chat Bubble
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Row(
                horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isCurrentUser) {
                    UserAvatar(
                        photoUrl = message.senderPhotoUrl,
                        userName = message.senderName,
                        size = 34.dp,
                        modifier = Modifier.padding(end = Spacing.xs, bottom = Spacing.xxs)
                    )
                }

                Box(
                    modifier = Modifier
                        .widthIn(max = 290.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = Radius.lg,
                                topEnd = Radius.lg,
                                bottomStart = if (isCurrentUser) Radius.lg else Radius.xs,
                                bottomEnd = if (isCurrentUser) Radius.xs else Radius.lg
                            )
                        )
                        .background(
                            if (isCurrentUser) Color(0xFF2B2518)
                            else Color(0xFF1C1A2E)
                        )
                        .border(
                            1.dp,
                            if (isCurrentUser) FajrLoopColors.Primary.copy(alpha = 0.45f)
                            else Color(0xFF2D2A45),
                            RoundedCornerShape(
                                topStart = Radius.lg,
                                topEnd = Radius.lg,
                                bottomStart = if (isCurrentUser) Radius.lg else Radius.xs,
                                bottomEnd = if (isCurrentUser) Radius.xs else Radius.lg
                            )
                        )
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                ) {
                    Column {
                        if (!isCurrentUser) {
                            Text(
                                text = senderShortName,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FajrLoopColors.Primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Text(
                            text = message.message,
                            fontFamily = PpNmArabic,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            lineHeight = 21.sp
                        )

                        Text(
                            text = formattedTime,
                            fontFamily = PpNmArabic,
                            fontSize = 10.sp,
                            color = FajrLoopColors.TextTertiary,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = Spacing.xs)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    FajrLoopTheme {
        ChatScreen(
            title = "حلقة الأبرار",
            messages = listOf(
                ChatMessage("1", "u1", "صالح باقومري", "", "السلام عليكم ورحمة الله وبركاته 🌅", "normal", System.currentTimeMillis()),
                ChatMessage("2", "u2", "أحمد عبدالله", "", "الصلاة خير من النوم", "motivational", System.currentTimeMillis())
            ),
            currentUid = "u1",
            onSendMessage = { _, _ -> },
            onBackClick = {}
        )
    }
}
