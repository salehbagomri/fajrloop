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
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.data.ChatMessage
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

val quickChips = listOf(
    "🕌 الصلاة خير من النوم",
    "⚡ همتكم يا أبطال الفجر",
    "✨ فجر مبارك للجميع",
    "📖 لا تنسوا الأذكار"
)

val motivationalPresets = listOf(
    Pair("الصلاة خير من النوم 🕌", "حديث شريف"),
    Pair("همّتكم يا أبطال الفجر ⚡", "تنافس إيماني"),
    Pair("فجر مبارك للجميع ✨", "تحية الصباح"),
    Pair("لا تنسوا أذكار الصباح 📖", "تذكير بالتحصين"),
    Pair("ألا إن سلعة الله غالية، ألا إن سلعة الله الجنة 💎", "حديث شريف"),
    Pair("من صلى الفجر في جماعة فهو في ذمة الله 🛡️", "صحيح مسلم")
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            FajrLoopTopBar(
                title = title.ifEmpty { "محادثة الحلقة" },
                onBackClick = onBackClick
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

            // Quick Chips Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(quickChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(Color(0xFF1E1C30))
                            .border(
                                1.dp,
                                FajrLoopColors.Primary.copy(alpha = 0.35f),
                                RoundedCornerShape(Radius.full)
                            )
                            .clickable { onSendMessage(chipText, "normal") }
                            .padding(horizontal = Spacing.md, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FajrLoopColors.Primary
                        )
                    }
                }
            }

            // Input Bar Container
            Surface(
                color = Color(0xFF131222),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFF26233B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    // Star Button for Motivational Preset Dialog
                    IconButton(
                        onClick = { showMotivationalSheet = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF221E36))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "رسائل تحفيزية",
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

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
                            .size(44.dp)
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
        }

        // Motivational BottomSheet
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
                        motivationalPresets.forEach { (text, category) ->
                            Surface(
                                onClick = {
                                    onSendMessage(text, "motivational")
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
                                    Text(
                                        text = text,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = FajrLoopColors.TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(FajrLoopColors.PrimaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = category,
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
                    // Sender Name for other members
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

                    // Motivational Badge if applicable
                    if (isMotivational) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(Radius.xs))
                                .background(FajrLoopColors.PrimaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ رسالة تحفيزية",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = FajrLoopColors.Primary
                            )
                        }
                    }

                    // Message Content
                    Text(
                        text = message.message,
                        fontFamily = PpNmArabic,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextPrimary,
                        lineHeight = 21.sp
                    )

                    // Timestamp
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

@Preview
@Composable
private fun ChatScreenPreview() {
    FajrLoopTheme {
        ChatScreen(
            title = "حلقة الأبرار",
            messages = listOf(
                ChatMessage("1", "u1", "صالح باقومري", "", "السلام عليكم ورحمة الله وبركاته 🌅", "normal", System.currentTimeMillis()),
                ChatMessage("2", "u2", "أحمد عبدالله", "", "وعليكم السلام، جاهز لصلاة الفجر إن شاء الله! 🕌", "motivational", System.currentTimeMillis())
            ),
            currentUid = "u1",
            onSendMessage = { _, _ -> },
            onBackClick = {}
        )
    }
}
