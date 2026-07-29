package com.bagomri.fajrloop.ui.chat

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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    "الصلاة خير من النوم",
    "همّتكم يا أبطال الفجر",
    "فجر مبارك",
    "لا تنسوا الأذكار"
)

val motivationalPresets = listOf(
    Pair("الصلاة خير من النوم", "نقل عن الحديث الشريف"),
    Pair("همّتكم يا أبطال الفجر", "دعوة للتنافس الرفيع"),
    Pair("فجر مبارك للجميع", "تحية الصباح المبارك"),
    Pair("لا تنسوا أذكار الصباح", "تذكير بالتحصين اليومي"),
    Pair("ألا إن سلعة الله غالية، ألا إن سلعة الله الجنة", "تذكير بعظيم الأجر"),
    Pair("من صلى الفجر في جماعة فهو في ذمة الله", "صحيح مسلم")
)

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

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            FajrLoopTopBar(
                title = title.ifEmpty { "دردشة الحلقة" },
                onBackClick = onBackClick
            )

            // Messages list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "لا يوجد رسائل حتى الآن، كن أول من يرسل",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
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

            // Quick suggestion chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(quickChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.full))
                            .background(FajrLoopColors.SurfaceVariant)
                            .border(
                                1.dp,
                                FajrLoopColors.Primary.copy(alpha = 0.3f),
                                RoundedCornerShape(Radius.full)
                            )
                            .clickable { onSendMessage(chipText, "normal") }
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    ) {
                        Text(
                            text = chipText,
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.Primary
                        )
                    }
                }
            }

            // Input Bar
            Surface(
                color = FajrLoopColors.Surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Star button for motivational dialog
                    IconButton(
                        onClick = { showMotivationalSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(FajrLoopColors.PrimaryContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "رسائل تحفيزية",
                            tint = FajrLoopColors.Primary
                        )
                    }

                    // Input TextField
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "اكتب رسالتك...",
                                fontFamily = PpNmArabic,
                                fontSize = 14.sp,
                                color = FajrLoopColors.TextTertiary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Radius.full),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FajrLoopColors.Primary,
                            unfocusedBorderColor = FajrLoopColors.Border,
                            focusedTextColor = FajrLoopColors.TextPrimary,
                            unfocusedTextColor = FajrLoopColors.TextPrimary,
                            focusedContainerColor = FajrLoopColors.SurfaceVariant,
                            unfocusedContainerColor = FajrLoopColors.SurfaceVariant
                        ),
                        singleLine = true
                    )

                    // Send button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim(), "normal")
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(FajrLoopColors.Primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = FajrIcons.Send,
                            contentDescription = "إرسال",
                            tint = FajrLoopColors.Background
                        )
                    }
                }
            }
        }

        // Motivational BottomSheet
        if (showMotivationalSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMotivationalSheet = false },
                containerColor = FajrLoopColors.Surface,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl)
                ) {
                    Text(
                        text = "اختر رسالة تحفيزية",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.Primary,
                        modifier = Modifier.padding(bottom = Spacing.lg)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        motivationalPresets.forEach { (text, category) ->
                            FajrCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSendMessage(text, "motivational")
                                        showMotivationalSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.lg),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = text,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = FajrLoopColors.TextPrimary
                                    )
                                    Text(
                                        text = category,
                                        fontFamily = PpNmArabic,
                                        fontSize = 11.sp,
                                        color = FajrLoopColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
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
                    size = 32.dp,
                    modifier = Modifier.padding(end = Spacing.sm, bottom = Spacing.xxs)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = Radius.lg,
                            topEnd = Radius.lg,
                            bottomStart = if (isCurrentUser) Radius.lg else Radius.xs,
                            bottomEnd = if (isCurrentUser) Radius.xs else Radius.lg
                        )
                    )
                    .background(
                        if (isCurrentUser) FajrLoopColors.PrimaryContainer
                        else FajrLoopColors.Surface
                    )
                    .border(
                        1.dp,
                        if (isCurrentUser) FajrLoopColors.Primary.copy(alpha = 0.3f)
                        else FajrLoopColors.Border,
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
                            text = message.senderName,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FajrLoopColors.Primary,
                            modifier = Modifier.padding(bottom = Spacing.xxs)
                        )
                    }

                    Text(
                        text = message.message,
                        fontFamily = PpNmArabic,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextPrimary,
                        lineHeight = 20.sp
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

@Preview
@Composable
private fun ChatScreenPreview() {
    FajrLoopTheme {
        ChatScreen(
            title = "حلقة الأبرار",
            messages = listOf(
                ChatMessage("1", "u1", "صالح", "", "السلام عليكم ورحمة الله", "normal", System.currentTimeMillis()),
                ChatMessage("2", "u2", "أحمد", "", "وعليكم السلام، جاهز لصلاة الفجر إن شاء الله", "normal", System.currentTimeMillis())
            ),
            currentUid = "u1",
            onSendMessage = { _, _ -> },
            onBackClick = {}
        )
    }
}
