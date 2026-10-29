package exe.kabachok.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.sin
import kotlin.math.cos
import kotlinx.coroutines.isActive

@Composable
fun Character(state: GameState, animated: Boolean, burst: Int, modifier: Modifier, onTap: () -> Unit) {
    var time by remember { mutableFloatStateOf(0f) }
    var confetti by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(animated) {
        if (animated) {
            var previous = 0L
            while (isActive) withFrameNanos { now ->
                if (previous != 0L) time = (time + (now - previous).coerceAtMost(50_000_000L) / 1_000_000_000f) % 3600f
                previous = now
            }
        }
    }
    LaunchedEffect(burst, animated) {
        confetti = 1f
        if (burst > 0 && animated) {
            var start = 0L
            while (isActive && (start == 0L || confetti < 1f)) withFrameNanos { now ->
                if (start == 0L) start = now
                confetti = ((now - start) / 2_300_000_000f).coerceIn(0f, 1f)
            }
        }
    }
    Canvas(modifier.semantics { contentDescription = "Живой кабачок. ${state.stageName}. ${state.mood}. Нажмите, чтобы пощекотать." }.clickable(onClickLabel = "Пощекотать кабачка", onClick = onTap)) {
        val unit = minOf(size.width / 360f, size.height / 280f)
        val t = if (animated) time else 0.5f
        withTransform({ translate((size.width - 360 * unit) / 2f, (size.height - 280 * unit) / 2f); scale(unit, unit, Offset.Zero) }) {
            val grid = Color(0xFF30392B)
            for (i in 0..12) drawLine(grid.copy(alpha = 0.5f), Offset(i * 30f, 0f), Offset(i * 30f, 280f), 0.7f)
            for (i in 0..9) drawLine(grid.copy(alpha = 0.5f), Offset(0f, i * 30f), Offset(360f, i * 30f), 0.7f)
            drawCircle(Lime.copy(alpha = 0.035f), 118f, Offset(180f, 137f))
            drawCircle(Lime.copy(alpha = 0.07f), 92f, Offset(180f, 137f), style = Stroke(1f))
            drawOval(Color.Black.copy(alpha = 0.25f), Offset(101f, 245f), Size(158f, 17f))
            drawLine(Lime.copy(alpha = 0.28f), Offset(25f, 255f), Offset(335f, 255f), 1f)
            if (state.mode == Move.CARDIO) {
                for (i in 0..6) {
                    val x = ((i * 61 + t * 210) % 390) - 20
                    drawLine(Lime.copy(alpha = 0.4f), Offset(x, 260f + i % 3 * 5), Offset(x + 25, 260f + i % 3 * 5), 2f)
                }
                for (i in 0..4) drawLine(Cream.copy(alpha = 0.3f), Offset(30f + i * 4, 90f + i * 23), Offset(75f + i * 4, 90f + i * 23), 2f)
            }
            if (state.event == 0) {
                val x = 180 + sin(t * 2) * 55
                drawPath(Path().apply { moveTo(x - 22, 34f); lineTo(x - 90, 240f); lineTo(x + 90, 240f); lineTo(x + 22, 34f); close() }, Lime.copy(alpha = 0.09f))
                drawOval(Violet, Offset(x - 38, 15f), Size(76f, 20f))
                drawOval(Pink, Offset(x - 17, 5f), Size(34f, 22f))
                for (i in 0..3) drawRect(Lime, Offset(x - 27 + i * 16, 26f), Size(5f, 4f))
            }
            if (state.event == 2) {
                drawLine(Muted, Offset(180f, 0f), Offset(180f, 20f), 1f)
                drawCircle(Violet, 15f, Offset(180f, 26f))
                for (i in 0..6) {
                    val a = t * 0.7f + i * 0.9f
                    drawLine(listOf(Pink, Lime, Violet)[i % 3].copy(alpha = 0.23f), Offset(180f, 27f), Offset(180f + cos(a) * 280, 27f + sin(a) * 280), 6f)
                }
            }
            if (state.event == 3) for (i in 0..7) potato(Offset(25f + i * 44, ((t * 65 + i * 43) % 310) - 25), 0.6f)
            if (state.event == 7) for (i in 0..10) {
                val x = 13f + (i * 79 % 330)
                val y = (t * 45 + i * 31) % 280
                drawRect(Lime.copy(alpha = 0.6f), Offset(x, y), Size(4f, 9f))
                drawRect(Lime.copy(alpha = 0.2f), Offset(x, y - 15f), Size(4f, 9f))
            }
            val isSleep = state.mode == Move.SLEEP
            val isRun = state.mode == Move.CARDIO
            val isDance = state.mode == Move.POTATO || state.event == 2
            val isLift = state.mode == Move.LIFT
            val wave = sin(t * if (isRun) 16 else if (isDance) 9 else if (isLift) 10 else 2)
            val bob = if (isSleep) sin(t) * 2 else if (isRun || isDance) -kotlin.math.abs(wave) * 10 else wave * 3
            val lean = if (isRun) 10f else if (isDance) wave * 10 else if (isLift) wave * 1.5f else -3f
            withTransform({
                if (state.event == 1) rotate(180f, Offset(180f, 137f))
                translate(0f, bob)
                rotate(lean, Offset(180f, 164f))
            }) {
                val skin = if (state.event == 5) Color(0xFFB0DDCF) else Color(0xFFADE34C)
                val dark = Color(0xFF172219)
                val bulk = state.stage * 5f
                val leftFoot = if (isRun) wave * 22 else if (isDance) wave * 9 else 0f
                limb(Offset(158f, 204f), Offset(153f - leftFoot, 241f), dark, 15f)
                limb(Offset(202f, 204f), Offset(210f + leftFoot, 241f), dark, 15f)
                limb(Offset(158f, 204f), Offset(153f - leftFoot, 241f), skin, 9f)
                limb(Offset(202f, 204f), Offset(210f + leftFoot, 241f), skin, 9f)
                drawRoundRect(Cream, Offset(134f - leftFoot, 236f), Size(28f, 13f), CornerRadius(5f))
                drawRoundRect(Cream, Offset(202f + leftFoot, 236f), Size(28f, 13f), CornerRadius(5f))
                val leftHand = when { isLift -> Offset(91f, 93f + wave * 8); isDance -> Offset(85f, 110f + wave * 30); isRun -> Offset(110f, 145f + wave * 27); else -> Offset(99f, 179f) }
                val rightHand = when { isLift -> Offset(269f, 93f + wave * 8); isDance -> Offset(275f, 110f - wave * 30); isRun -> Offset(248f, 145f - wave * 27); else -> Offset(261f, 179f) }
                val leftElbow = Offset(102f - bulk / 2, if (isLift) 139f else 154f)
                val rightElbow = Offset(258f + bulk / 2, if (isLift) 139f else 154f)
                listOf(Triple(Offset(145f, 128f), leftElbow, leftHand), Triple(Offset(215f, 128f), rightElbow, rightHand)).forEach { (shoulder, elbow, hand) ->
                    limb(shoulder, elbow, dark, 19f + bulk)
                    limb(elbow, hand, dark, 16f + bulk / 2)
                    limb(shoulder, elbow, skin, 12f + bulk)
                    limb(elbow, hand, skin, 10f + bulk / 2)
                    drawCircle(skin, 10f + bulk / 5, hand)
                    if (state.stage > 0) drawArc(Color(0xFF5C902F), 200f, 135f, false, Offset(elbow.x - 12, elbow.y - 13), Size(22f, 23f), style = Stroke(2f))
                }
                if (isLift) { dumbbell(leftHand); dumbbell(rightHand) }
                val body = Path().apply {
                    moveTo(164f, 50f)
                    cubicTo(188f, 37f, 213f, 55f, 213f + bulk, 91f)
                    cubicTo(221f + bulk, 121f, 239f + bulk, 181f, 212f, 210f)
                    cubicTo(191f, 231f, 143f, 222f, 133f, 198f)
                    cubicTo(119f - bulk, 171f, 138f - bulk, 118f, 142f, 87f)
                    cubicTo(144f, 68f, 150f, 56f, 164f, 50f)
                    close()
                }
                drawPath(body, dark, style = Stroke(7f))
                drawPath(body, Brush.linearGradient(listOf(Color(0xFFD6F783), skin, Color(0xFF79B638)), Offset(130f, 90f), Offset(232f, 185f)))
                drawPath(Path().apply { moveTo(154f, 73f); cubicTo(150f, 125f, 131f, 183f, 154f, 204f) }, Color(0xFF78A73C).copy(alpha = 0.6f), style = Stroke(3f))
                drawPath(Path().apply { moveTo(202f, 73f); cubicTo(210f, 125f, 225f, 183f, 206f, 204f) }, Color(0xFF5E9632).copy(alpha = 0.6f), style = Stroke(3f))
                limb(Offset(174f, 53f), Offset(170f, 31f), dark, 15f)
                limb(Offset(174f, 53f), Offset(170f, 31f), Color(0xFF709936), 9f)
                drawPath(Path().apply { moveTo(171f, 38f); quadraticBezierTo(190f, 19f, 199f, 32f); quadraticBezierTo(185f, 46f, 171f, 38f) }, skin)
                if (state.stage >= 2) {
                    for (i in 0..2) {
                        drawArc(Color(0xFF608B30), 0f, 150f, false, Offset(161f, 163f + i * 13), Size(17f, 9f), style = Stroke(2f))
                        drawArc(Color(0xFF608B30), 25f, 150f, false, Offset(183f, 163f + i * 13), Size(17f, 9f), style = Stroke(2f))
                    }
                }
                val blink = (t % 4.3f) > 4.12f
                if (isSleep || blink) {
                    drawArc(dark, 0f, 170f, false, Offset(150f, 102f), Size(25f, 12f), style = Stroke(4f))
                    drawArc(dark, 0f, 170f, false, Offset(186f, 100f), Size(25f, 12f), style = Stroke(4f))
                } else {
                    drawOval(Cream, Offset(148f, 87f), Size(30f, 37f))
                    drawOval(Cream, Offset(184f, 83f), Size(31f, 39f))
                    drawOval(dark, Offset(159f + if (isRun) 5 else 0, 98f), Size(10f, 18f))
                    drawOval(dark, Offset(193f + if (isRun) 5 else 0, 94f), Size(10f, 19f))
                    drawCircle(Color.White, 2f, Offset(165f, 101f))
                    drawCircle(Color.White, 2f, Offset(199f, 97f))
                }
                if (isLift || isRun) {
                    limb(Offset(149f, 82f), Offset(175f, 89f), dark, 5f)
                    limb(Offset(188f, 85f), Offset(214f, 76f), dark, 5f)
                } else {
                    limb(Offset(150f, 80f), Offset(169f, 76f), dark, 3f)
                    limb(Offset(189f, 73f), Offset(210f, 78f), dark, 3f)
                }
                drawOval(Pink.copy(alpha = 0.6f), Offset(142f, 123f), Size(17f, 8f))
                drawOval(Pink.copy(alpha = 0.6f), Offset(204f, 120f), Size(17f, 8f))
                when {
                    isSleep -> drawOval(dark, Offset(177f, 133f), Size(12f, 17f))
                    isRun -> { drawOval(dark, Offset(169f, 132f), Size(27f, 24f)); drawOval(Pink, Offset(178f, 146f), Size(15f, 7f)) }
                    isLift -> { drawRoundRect(dark, Offset(159f, 135f), Size(44f, 15f), CornerRadius(4f)); drawRect(Cream, Offset(163f, 138f), Size(36f, 6f)); drawLine(dark, Offset(181f, 138f), Offset(181f, 145f), 2f) }
                    else -> { drawArc(dark, 0f, 180f, true, Offset(160f, 125f), Size(43f, 31f)); drawRoundRect(Cream, Offset(166f, 140f), Size(29f, 5f), CornerRadius(2f)) }
                }
                if (isSleep) {
                    drawPath(Path().apply { moveTo(141f, 70f); lineTo(190f, 17f); lineTo(220f, 68f); quadraticBezierTo(175f, 56f, 141f, 70f); close() }, Violet)
                    drawLine(Cream, Offset(145f, 69f), Offset(215f, 66f), 9f, StrokeCap.Round)
                    drawCircle(Cream, 9f, Offset(190f, 17f))
                }
                if (state.event == 4 || state.stage == 4) {
                    drawPath(Path().apply { moveTo(146f, 62f); lineTo(140f, 33f); lineTo(160f, 43f); lineTo(176f, 21f); lineTo(193f, 42f); lineTo(214f, 29f); lineTo(210f, 60f); close() }, Color(0xFFFFD976))
                    drawRect(Pink, Offset(173f, 45f), Size(8f, 8f))
                }
                if (isRun) for (i in 0..2) drawOval(Color(0xFF99E5F5), Offset(230f + i * 9, 80f + i * 15 + wave * 5), Size(5f, 11f))
            }
            if (isSleep) for (i in 0..2) {
                val y = 80f - i * 22 - (t * 12 % 20)
                val x = 242f + i * 18
                val s = 9f + i * 3
                drawLine(Violet, Offset(x, y), Offset(x + s, y), 2f)
                drawLine(Violet, Offset(x + s, y), Offset(x, y + s), 2f)
                drawLine(Violet, Offset(x, y + s), Offset(x + s, y + s), 2f)
            }
            if (isDance) { potato(Offset(53f, 215f + sin(t * 9) * 9), 1f); potato(Offset(302f, 215f - sin(t * 9) * 9), 0.8f) }
            if (state.event == 6) {
                drawRoundRect(Cream, Offset(265f, 100f), Size(52f, 70f), CornerRadius(3f))
                drawRect(Violet, Offset(278f, 95f), Size(25f, 10f))
                for (i in 0..3) drawLine(Ink, Offset(275f, 117f + i * 12), Offset(306f, 117f + i * 12), 2f)
                drawCircle(Pink, 12f, Offset(302f, 162f), style = Stroke(3f))
            }
            if (confetti < 1f && animated) for (i in 0..44) {
                val x = (i * 83 % 360).toFloat() + sin(t * 4 + i) * 15
                val y = -60 + confetti * (360 + i % 5 * 30) - i % 7 * 12
                rotate(i * 17f + confetti * 200, Offset(x, y)) {
                    drawRect(listOf(Lime, Pink, Violet, Cream)[i % 4], Offset(x, y), Size(5f, 9f), alpha = 1f - confetti * 0.5f)
                }
            }
        }
    }
}

private fun DrawScope.limb(from: Offset, to: Offset, color: Color, width: Float) = drawLine(color, from, to, width, StrokeCap.Round)
private fun DrawScope.dumbbell(hand: Offset) {
    drawLine(Cream, Offset(hand.x - 23, hand.y - 9), Offset(hand.x + 23, hand.y - 9), 6f)
    for (side in listOf(-1, 1)) {
        drawRoundRect(Ink, Offset(hand.x + side * 22 - 8, hand.y - 30), Size(16f, 42f), CornerRadius(3f))
        drawRoundRect(Violet, Offset(hand.x + side * 22 - 6, hand.y - 28), Size(12f, 38f), CornerRadius(2f))
        drawLine(Cream.copy(alpha = 0.4f), Offset(hand.x + side * 22 - 3, hand.y - 24), Offset(hand.x + side * 22 - 3, hand.y + 5), 2f)
    }
}
private fun DrawScope.potato(center: Offset, factor: Float) {
    withTransform({ scale(factor, factor, center) }) {
        drawOval(Color(0xFFE5B866), Offset(center.x - 18, center.y - 23), Size(36f, 46f))
        drawCircle(Ink, 2.5f, Offset(center.x - 6, center.y - 4))
        drawCircle(Ink, 2.5f, Offset(center.x + 6, center.y - 4))
        drawArc(Ink, 0f, 180f, false, Offset(center.x - 6, center.y + 1), Size(12f, 8f), style = Stroke(2f))
        drawCircle(Color(0xFFB58E51), 2f, Offset(center.x - 10, center.y - 14))
        drawCircle(Color(0xFFB58E51), 2f, Offset(center.x + 11, center.y + 13))
        limb(Offset(center.x - 16, center.y + 2), Offset(center.x - 25, center.y - 9), Color(0xFFE5B866), 3f)
        limb(Offset(center.x + 16, center.y + 2), Offset(center.x + 25, center.y - 9), Color(0xFFE5B866), 3f)
    }
}
