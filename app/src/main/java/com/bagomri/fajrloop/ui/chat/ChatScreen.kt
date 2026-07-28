package com.bagomri.fajrloop.ui.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Star
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
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import java.text.SimpleDateFormat
import java.util.*

val quickChips = listOf(
    "الصلاة خير من النوم ⏰",
    "همّتكم يا أبطال الفجر!",
    "فجر مبارك للجميع",
    "لا تنسوا أذكار الصباح"
)

val motivationalPresets = listOf(
    Triple("✨", "الصلاة خير من النوم ⏰", "#FFD700"),
    Triple("💪", "همّتكم يا أبطال الفجر!", "#FF8C00"),
    Triple("🌅", "فجر مبارك للجميع", "#2ECC71"),
    Triple("📖", "لا تنسوا أذكار الصباح", "#B57CFF"),
    Triple("🕋", "ألا إن سلعة الله غالية، ألا إن سلعة الله الجنة", "#FFD700"),
    Triple("🟢", "من صلى الفجر في جماعة فهو في ذمة الله", "#2ECC71")
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
        AnimatedGradientBackground()

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
                    .padding(horizontal = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💬",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "لا يوجد رسائل حتى الآن، كن أول من يرسل 💬",
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
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
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
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FajrLoopColors.Surface.copy(alpha = 0.6f))
                            .border(
                                1.dp,
                                FajrLoopColors.Gold.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSendMessage(chipText, "normal") }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.Gold
                        )
                    }
                }
            }

            // Input Bar
            Surface(
                color = FajrLoopColors.Surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Star button for motivational dialog
                    IconButton(
                        onClick = { showMotivationalSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(FajrLoopColors.Gold.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "تحفيزي",
                            tint = FajrLoopColors.Gold
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
                                color = FajrLoopColors.TextSecondary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FajrLoopColors.Gold,
                            unfocusedBorderColor = FajrLoopColors.SurfaceBorder,
                            focusedTextColor = FajrLoopColors.TextPrimary,
                            unfocusedTextColor = FajrLoopColors.TextPrimary,
                            focusedContainerColor = FajrLoopColors.Background.copy(alpha = 0.5f),
                            unfocusedContainerColor = FajrLoopColors.Background.copy(alpha = 0.5f)
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
                            .background(FajrLoopColors.Gold, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
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
                        .padding(24.dp)
                ) {
                    Text(
                        text = "اختر رسالة تحفيزية سوبر 🌟",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.Gold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        motivationalPresets.forEach { (emoji, text, _) ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSendMessage("$emoji $text", "motivational")
                                        showMotivationalSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                    Text(
                                        text = text,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = FajrLoopColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
                    size = 32.dp,
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                            bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isCurrentUser) FajrLoopColors.Gold.copy(alpha = 0.2f)
                        else FajrLoopColors.Surface.copy(alpha = 0.8f)
                    )
                    .border(
                        1.dp,
                        if (isCurrentUser) FajrLoopColors.Gold.copy(alpha = 0.4f)
                        else FajrLoopColors.SurfaceBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                            bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (!isCurrentUser) {
                        Text(
                            text = message.senderName,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = FajrLoopColors.Gold,
                            modifier = Modifier.padding(bottom = 2.dp)
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
                        color = FajrLoopColors.TextSecondary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    FajrLoopTheme {
        ChatScreen(
            title = "حلقة الأبرار",
            messages = listOf(
                ChatMessage("1", "u1", "صالح", "", "السلام عليكم ورحمة الله", "normal", System.currentTimeMillis()),
                ChatMessage("2", "u2", "أحمد", "", "وعليكم السلام، جاهز لصلاة الفجر إن شاء الله 🌅", "normal", System.currentTimeMillis())
            ),
            currentUid = "u1",
            onSendMessage = { _, _ -> },
            onBackClick = {}
        )
    }
}
