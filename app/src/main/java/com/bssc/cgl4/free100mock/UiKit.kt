package com.bssc.cgl4.free100mock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// V5: visual language matched to the reference mockups — royal purple, gold and ivory.
val Ink = Color(0xFFF4F0FC)
val CardInk = Color(0xFFFFFFFF)
val CardInkLight = Color(0xFFEEE3FF)
val Violet = Color(0xFF6424C8)
val Blue = Color(0xFF2266D8)
val Aqua = Color(0xFF007C58)
val Amber = Color(0xFFA85A00)
val SoftWhite = Color(0xFF23103D)
val Muted = Color(0xFF5D506F)
val Red = Color(0xFFC92349)
val SoftMint = Color(0xFFDAF7EA)
val SoftLavender = Color(0xFFE8D8FF)
val Royal = Color(0xFF46107A)
val RoyalDeep = Color(0xFF260642)
val Gold = Color(0xFFFFD45D)

@Composable fun RankTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Violet, secondary = Aqua, surface = CardInk, background = Ink,
        onPrimary = Color.White, onSurface = SoftWhite, onBackground = SoftWhite,
        error = Red
    ), typography = Typography(), content = content)
}

@Composable fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp))
        .background(CardInk).border(1.dp, Color(0xFFD9C7EC), RoundedCornerShape(19.dp))
        .padding(18.dp), content = content)
}

@Composable fun CapsLabel(value: String, color: Color = Muted) {
    Text(value.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.7.sp,
        fontWeight = FontWeight.Bold, color = color)
}

@Composable fun Headline(value: String, modifier: Modifier=Modifier) =
    Text(value, modifier=modifier, color=SoftWhite, fontWeight=FontWeight.ExtraBold, fontSize=25.sp, lineHeight=30.sp)

@Composable fun PrimaryAction(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Violet,
            contentColor = Color.White, disabledContainerColor=CardInkLight)) {
        Text(text, fontWeight=FontWeight.ExtraBold, fontSize=14.sp)
    }
}

@Composable fun Tag(text: String, color: Color = Aqua) {
    Box(Modifier.clip(RoundedCornerShape(100.dp)).background(color.copy(alpha=.16f)).padding(horizontal=10.dp, vertical=6.dp)) {
        Text(text, color=color, fontWeight=FontWeight.ExtraBold, fontSize=11.sp)
    }
}

@Composable fun Metric(label: String, value: String, color: Color = SoftWhite, modifier: Modifier=Modifier) {
    Column(modifier) {
        Text(value, color=color, fontSize=26.sp, fontWeight=FontWeight.ExtraBold)
        Spacer(Modifier.height(3.dp))
        Text(label, color=Muted, fontSize=11.sp)
    }
}

@Composable fun DividerLine() = HorizontalDivider(color=Color(0xFFE9EDF4), thickness=1.dp)

@Composable fun SectionTitle(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color=SoftWhite, fontWeight=FontWeight.ExtraBold, fontSize=19.sp)
            if (subtitle != null) Text(subtitle, color=Muted, fontSize=12.sp)
        }
        action?.invoke()
    }
}

@Composable fun ProgressTrack(value: Float, color: Color = Violet, modifier: Modifier = Modifier) {
    LinearProgressIndicator(progress={value.coerceIn(0f,1f)}, modifier=modifier.fillMaxWidth().height(7.dp)
        .clip(RoundedCornerShape(7.dp)), color=color, trackColor=Color(0xFFE9EDF4), gapSize=0.dp,
        drawStopIndicator={})
}

@Composable fun EmptyNotice(title: String, body: String, modifier: Modifier = Modifier) {
    Panel(modifier) {
        Text("✦", color=Violet, fontSize=30.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, color=SoftWhite, fontSize=18.sp, fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(body, color=Muted, fontSize=13.sp, lineHeight=20.sp)
    }
}

@Composable fun SelectablePill(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(14.dp))
        .background(if(selected) Violet else CardInk)
        .border(1.dp, if(selected) Violet else Color(0xFFEADDF6), RoundedCornerShape(14.dp))
        .clickable { onClick() }.padding(horizontal=13.dp, vertical=10.dp)) {
        Text(title, color=if(selected) Color.White else SoftWhite, fontWeight=FontWeight.Bold, fontSize=12.sp,
            maxLines=1, overflow=TextOverflow.Ellipsis)
    }
}
