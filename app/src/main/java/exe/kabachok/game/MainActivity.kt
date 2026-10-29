package exe.kabachok.game

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Ink = Color(0xFF101014)
val Panel = Color(0xFF1B1B24)
val Lime = Color(0xFFCEFF51)
val Pink = Color(0xFFFF79CE)
val Violet = Color(0xFFB2A0FF)
val Muted = Color(0xFFAAAAAE)
val Cream = Color(0xFFF2F0E8)

class MainActivity : ComponentActivity() {
    private var tone: ToneGenerator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tone = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 45) }.getOrNull()
        val store = GameStore(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Lime, secondary = Pink, surface = Panel, background = Ink, onSurface = Cream, onBackground = Cream)) {
                GameScreen(store) { move, s ->
                    if (s.sound) tone?.startTone(when (move) {
                        Move.LIFT -> ToneGenerator.TONE_DTMF_1
                        Move.POTATO -> ToneGenerator.TONE_DTMF_6
                        Move.SLEEP -> ToneGenerator.TONE_DTMF_0
                        Move.CARDIO -> ToneGenerator.TONE_DTMF_9
                        Move.CHAOS, null -> ToneGenerator.TONE_PROP_BEEP2
                    }, 140)
                    if (s.vibration) {
                        val vibrator = getSystemService(Vibrator::class.java)
                        if (vibrator?.hasVibrator() == true) vibrator.vibrate(VibrationEffect.createOneShot(if (move == Move.CHAOS) 65L else 25L, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
        }
    }
    override fun onStop() { tone?.stopTone(); super.onStop() }
    override fun onDestroy() { tone?.release(); tone = null; super.onDestroy() }
}

@Composable
fun GameScreen(store: GameStore, feedback: (Move?, GameState) -> Unit) {
    var state by remember { mutableStateOf(store.load()) }
    var dialog by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var burst by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(true) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> active = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    fun update(next: GameState) {
        val newAwards = next.unlocked - state.unlocked
        if (newAwards.isNotEmpty()) {
            notice = trophies.filter { it.id in newAwards }.joinToString(" • ") { it.name }
            burst++
        } else if (next.event >= 0 || next.mode == Move.POTATO) burst++
        state = next
        store.save(next)
    }
    fun action(move: Move?) {
        if (!ready) return
        ready = false
        scope.launch { delay(150); ready = true }
        val next = if (move == null) poke(state) else perform(state, move)
        update(next)
        feedback(move, next)
    }
    LaunchedEffect(state.serial) {
        if (state.serial > 0) {
            delay(5500)
            state = state.copy(mode = if (state.mode == Move.SLEEP) Move.SLEEP else null, event = -1)
        }
    }
    LaunchedEffect(notice, burst) { if (notice.isNotEmpty()) { delay(4500); notice = "" } }
    Box(Modifier.fillMaxSize().background(Ink).safeDrawingPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable { update(poke(state, title = true)) }.padding(vertical = 6.dp)) {
                    Mono("КАБАЧОК", 25, Cream, FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Mono(".EXE", 14, Lime, FontWeight.Bold)
                        Mono("СИМУЛЯТОР СУШКИ", 9, Muted)
                    }
                }
                TextButton(onClick = { dialog = "trophies" }, contentPadding = PaddingValues(8.dp)) { Mono("★ ${state.unlocked.size}", 16, Lime) }
                TextButton(onClick = { dialog = "settings" }, contentPadding = PaddingValues(8.dp)) { Mono("[≡]", 21, Muted) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(Modifier.weight(1.25f), color = Lime, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Mono("СЧЁТЧИК АБСУРДА", 10, Ink, FontWeight.Bold)
                        Mono("${state.absurd} ↗", 30, Ink, FontWeight.Black)
                    }
                }
                Surface(Modifier.weight(1f), color = Panel, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Mono("ЗАРЯД ДУРЬЮ", 10, Muted)
                        Mono("${state.energy}%", 30, Cream, FontWeight.Bold)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF191D19), border = BorderStroke(1.dp, Color(0xFF353C2D))) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Mono("● ОБЪЕКТ № 001", 10, Lime)
                        Mono("LVL.0${state.stage + 1}", 10, Muted)
                    }
                    val height = (LocalConfiguration.current.screenHeightDp * 0.30f).coerceIn(200f, 300f)
                    Character(state, active && !state.calm && dialog.isEmpty(), burst, Modifier.fillMaxWidth().height(height.dp)) { action(null) }
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Mono(if (state.event >= 0) eventNames[state.event] else state.mood, 10, if (state.event >= 0) Pink else Lime)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Mono(state.stageName, 9, Muted)
                            Mono(if (state.stage == 4) "MAX" else "${state.lifts}/${state.nextStage}", 10, Cream)
                        }
                        LinearProgressIndicator(progress = { if (state.stage == 4) 1f else state.lifts.toFloat() / state.nextStage }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Lime, trackColor = Color(0xFF353C2D))
                    }
                }
            }
            Surface(color = Panel, shape = RoundedCornerShape(topStart = 2.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)) {
                Column(Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Mono("КАБАЧОК ГОВОРИТ:", 9, Pink)
                    Text(state.message, color = Cream, fontSize = 14.sp, lineHeight = 20.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(Move.LIFT, Lime, Modifier.weight(1f)) { action(Move.LIFT) }
                    ActionButton(Move.POTATO, Color(0xFFF3CC79), Modifier.weight(1f)) { action(Move.POTATO) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(Move.SLEEP, Violet, Modifier.weight(1f)) { action(Move.SLEEP) }
                    ActionButton(Move.CARDIO, Pink, Modifier.weight(1f)) { action(Move.CARDIO) }
                }
                ActionButton(Move.CHAOS, Cream, Modifier.fillMaxWidth()) { action(Move.CHAOS) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Mono("СОХРАНЕНО НА ГРЯДКЕ", 8, Muted)
                Mono("НИ ОДНОЙ КАЛОРИИ", 8, Muted)
            }
        }
        if (notice.isNotEmpty()) Surface(Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth(), color = Lime, shape = RoundedCornerShape(12.dp), shadowElevation = 12.dp) {
            Column(Modifier.padding(14.dp)) { Mono("★ ДОСТИЖЕНИЕ ОТКОПАНО", 10, Ink, FontWeight.Bold); Text(notice, color = Ink, fontWeight = FontWeight.Bold) }
        }
    }
    if (dialog == "trophies") AlertDialog(
        onDismissRequest = { dialog = "" }, title = { Mono("МУЗЕЙ СТРАННОГО", 19, Lime) },
        text = { Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("${state.unlocked.size} / ${trophies.size} достижений · ${state.seenEvents.size} / 8 аномалий", color = Muted)
            trophies.forEach { trophy -> Column {
                Text("${if (trophy.id in state.unlocked) "★" else "◇"} ${trophy.name}", color = if (trophy.id in state.unlocked) Lime else Cream, fontWeight = FontWeight.Bold)
                Text(trophy.hint, color = Muted, fontSize = 12.sp)
            } }
        } }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("В ОГОРОД") } }
    )
    if (dialog == "settings") AlertDialog(
        onDismissRequest = { dialog = "" }, title = { Mono("ПАНЕЛЬ АГРОНОМА", 18, Lime) },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            Setting("Писк и прочие звуки", state.sound) { update(state.copy(sound = it)) }
            Setting("Тактильный абсурд", state.vibration) { update(state.copy(vibration = it)) }
            Setting("Спокойные анимации", state.calm) { update(state.copy(calm = it)) }
            Spacer(Modifier.height(14.dp))
            Text("Мышцы растут от кнопки «КАЧАТЬСЯ». Энергия — настроение, она не мешает играть. Трогай кабачка, исследуй кнопки и ищи секреты.\n\nВсё хранится только на этом устройстве. Интернет не нужен. Звуки используют громкость мультимедиа.\n\nКАБАЧОК.EXE / 1.0\nИгра. Не фитнес-трекер. Овощ не врач.", color = Muted, fontSize = 12.sp)
            TextButton(onClick = { dialog = "reset" }) { Text("Начать новую грядку", color = Pink) }
        } }, confirmButton = { TextButton(onClick = { dialog = "" }) { Text("ПОНЯТНО. НЕТ.") } }
    )
    if (dialog == "reset") AlertDialog(onDismissRequest = { dialog = "settings" }, title = { Text("Выкопать весь прогресс?") }, text = { Text("Счётчик, мышцы и достижения обнулятся. Назад посадить не получится.") }, confirmButton = { TextButton(onClick = {
        state = GameState(sound = state.sound, vibration = state.vibration, calm = state.calm)
        store.save(state); notice = ""; burst = 0; dialog = ""
    }) { Text("НОВАЯ ГРЯДКА", color = Pink) } }, dismissButton = { TextButton(onClick = { dialog = "settings" }) { Text("ОСТАВИТЬ") } })
}

@Composable
private fun Setting(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun Mono(text: String, size: Int, color: Color, weight: FontWeight = FontWeight.Normal) {
    Text(text, color = color, fontSize = size.sp, fontFamily = FontFamily.Monospace, fontWeight = weight)
}

@Composable
private fun ActionButton(move: Move, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 64.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Ink), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Mono(move.label, 16, Ink, FontWeight.Black)
            Mono(move.caption, 9, Ink.copy(alpha = 0.7f))
        }
    }
}
