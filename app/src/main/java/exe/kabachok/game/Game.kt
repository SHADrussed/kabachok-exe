package exe.kabachok.game

import kotlin.random.Random

enum class Move(val label: String, val caption: String) {
    LIFT("КАЧАТЬСЯ", "мышцы из воздуха"),
    POTATO("КАРТОШКА", "углеводное счастье"),
    SLEEP("СПАТЬ", "уйти в овощной режим"),
    CARDIO("КАРДИО", "бежать от реальности"),
    CHAOS("???", "не нажимай. нажми.")
}

data class GameState(
    val absurd: Long = 0,
    val lifts: Int = 0,
    val potatoes: Int = 0,
    val naps: Int = 0,
    val runs: Int = 0,
    val mysteries: Int = 0,
    val taps: Int = 0,
    val titleTaps: Int = 0,
    val combo: Int = 0,
    val steps: Int = 0,
    val energy: Int = 73,
    val unlocked: Set<String> = emptySet(),
    val seenEvents: Set<Int> = emptySet(),
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val calm: Boolean = false,
    val message: String = "Я кабачок. У меня сушка. И вопросы.",
    val mode: Move? = null,
    val event: Int = -1,
    val serial: Int = 0
) {
    val stage: Int get() = when { lifts >= 60 -> 4; lifts >= 30 -> 3; lifts >= 12 -> 2; lifts >= 4 -> 1; else -> 0 }
    val stageName: String get() = listOf("ОВОЩ ОБЫКНОВЕННЫЙ", "РОБОКИЙ БИЦЕПС", "КАБАЧОК НА МАССЕ", "МИСТЕР ОГОРОД", "ФИНАЛЬНЫЙ КАБАЧОК")[stage]
    val nextStage: Int get() = listOf(4, 12, 30, 60, 60)[stage]
    val mood: String get() = when (mode) {
        Move.LIFT -> "НАПРЯЖЁН ЭКЗИСТЕНЦИАЛЬНО"
        Move.POTATO -> "СЧАСТЛИВ КРАХМАЛЬНО"
        Move.SLEEP -> "НЕ КАНТОВАТЬ"
        Move.CARDIO -> "ДРАМА / 100%"
        Move.CHAOS -> "РЕАЛЬНОСТЬ НЕ ОТВЕЧАЕТ"
        null -> listOf("СЛЕГКА НЕ В СЕБЕ", "ПОДОЗРИТЕЛЬНО ЗЕЛЁНЫЙ", "ГОТОВ К НЕПОНЯТНОМУ")[steps % 3]
    }
}

data class Trophy(val id: String, val name: String, val hint: String, val test: (GameState) -> Boolean)
val trophies = listOf(
    Trophy("hello", "Овощ, который смог", "Сделать первое действие.") { it.steps >= 1 },
    Trophy("muscle", "Бицепс пророс", "Покачаться 4 раза.") { it.lifts >= 4 },
    Trophy("garden", "Мистер Огород", "Покачаться 30 раз.") { it.lifts >= 30 },
    Trophy("boss", "Финальный кабачок", "Покачаться 60 раз.") { it.lifts >= 60 },
    Trophy("starch", "Орден крахмала", "Позвать картошку 10 раз.") { it.potatoes >= 10 },
    Trophy("sleep", "Спящий по ГОСТу", "Уснуть 5 раз.") { it.naps >= 5 },
    Trophy("runner", "От судьбы не убежишь", "Устроить 10 забегов.") { it.runs >= 10 },
    Trophy("chaos", "Штатный аномалист", "Нажать ??? 10 раз.") { it.mysteries >= 10 },
    Trophy("collector", "Видел всякое", "Увидеть все 8 случайных событий.") { it.seenEvents.size >= 8 },
    Trophy("absurd", "Тысяча причин лежать", "Набрать 1000 абсурда.") { it.absurd >= 1000 },
    Trophy("answer", "Ответ — овощ", "Сделать 42 действия.") { it.steps >= 42 },
    Trophy("poke", "Не тыкай в урожай", "Потрогать самого кабачка 10 раз.") { it.taps >= 10 },
    Trophy("developer", "Режим агронома", "Заголовок что-то скрывает. Семь раз.") { it.titleTaps >= 7 },
    Trophy("ritual", "Ритуал гарнира", "Картошка → сон → кардио.") { it.combo >= 3 }
)

val quotes: Map<Move, List<String>> = mapOf(
    Move.LIFT to listOf(
        "Ещё подход — и я шкаф. Овощной.", "Мой протеин — слухи о протеине.",
        "Бицепс растёт. Самооценка тоже. Интеллект подождёт.", "Тренер сказал: сушись. Я повесился на балконе.",
        "Жму воздух. Воздух в шоке.", "Это не складка. Это запасной трицепс.",
        "Ни дня без фотосинтеза и страданий.", "У меня кубики. Бульонные.",
        "К лету буду готов. К какому — секрет."
    ),
    Move.POTATO to listOf(
        "КАРТОШЕЧКА! Мой психолог в мундире!", "Мы просто друзья. С гарниром.",
        "Танцуй, пока тебя не почистили!", "Крахмал — это блёстки для души.",
        "Сушка отменяется на время счастья.", "Картошка верит в меня. Больше никто.",
        "Углеводы пришли. Грусть вышла.", "Сегодня читмил. Вчера тоже был сегодня."
    ),
    Move.SLEEP to listOf(
        "Не будить. Идёт обновление кабачка.", "Мне снится, что я огурец. Очень страшно.",
        "Хр-р-р. Фотосинтез в фоне.", "Я не ленюсь. Я мариную потенциал.",
        "Будильник отправлен в компост.", "Пять минут. По овощному календарю.",
        "Сон — это кардио для фантазии.", "Ушёл в спящий огород."
    ),
    Move.CARDIO to listOf(
        "Я бегу от супа!", "Пульс: техно. Дыхание: модем.",
        "Куда бежим? Откуда знаем?!", "Мой личный рекорд — не заплакать.",
        "Пот — это слёзы моего салата.", "Кабачок пошёл. Кабачок побежал. Кабачок пожалел.",
        "Дорожка стоит. Паника движется.", "На финише картошка? Тогда живём."
    ),
    Move.CHAOS to listOf(
        "Я нажал на реальность. Она зависла.", "В огороде обнаружен незадокументированный я.",
        "Кто поставил вселенную на разморозку?", "Ошибка 418: я кабачок.",
        "Кажется, у гравитации обед.", "Ничего не понятно, но очень зелено."
    )
)

val eventNames = listOf("ПРИБЫЛ ОВОЩНОЙ НЛО", "ГРАВИТАЦИЯ В ОТПУСКЕ", "ДИСКОТЕКА В КОМПОСТЕ", "КАРТОФЕЛЬНЫЙ ДОЖДЬ", "КОРОНАЦИЯ ГАРНИРА", "ПРИЗРАК САЛАТА", "ПРОВЕРКА АГРОНОМА", "СБОЙ МАТРИЦЫ ГРЯДОК")
val eventQuotes = listOf(
    "Они прилетели за разумной жизнью. Придётся притвориться.",
    "Потолок — это пол с амбициями.",
    "Диджей Перегной ставит мой любимый хруст.",
    "Прогноз: осадки в мундире. Зонтик бесполезен.",
    "Я король. Мой трон — перевёрнутое ведро.",
    "Бууу! Это твой салат из будущего.",
    "Документы? У меня только листик.",
    "Ложки нет. Вилки тоже. Вы спасены."
)

fun reward(s: GameState): GameState = s.copy(unlocked = s.unlocked + trophies.filter { it.test(s) }.map { it.id })

fun perform(s: GameState, move: Move, random: Random = Random.Default): GameState {
    val combo = if (s.combo >= 3) 3 else when {
        move == Move.POTATO -> 1
        move == Move.SLEEP && s.combo == 1 -> 2
        move == Move.CARDIO && s.combo == 2 -> 3
        else -> 0
    }
    val event = if (move == Move.CHAOS || random.nextInt(12) == 0) random.nextInt(eventNames.size) else -1
    val next = s.copy(
        absurd = (s.absurd + random.nextInt(9, 24) + if (event >= 0) 35 else 0).coerceAtMost(999_999_999),
        lifts = (s.lifts + if (move == Move.LIFT) 1 else 0).coerceAtMost(999_999),
        potatoes = (s.potatoes + if (move == Move.POTATO) 1 else 0).coerceAtMost(999_999),
        naps = (s.naps + if (move == Move.SLEEP) 1 else 0).coerceAtMost(999_999),
        runs = (s.runs + if (move == Move.CARDIO) 1 else 0).coerceAtMost(999_999),
        mysteries = (s.mysteries + if (move == Move.CHAOS) 1 else 0).coerceAtMost(999_999),
        steps = (s.steps + 1).coerceAtMost(999_999),
        energy = (s.energy + when (move) { Move.SLEEP -> 24; Move.POTATO -> 13; Move.LIFT -> -7; Move.CARDIO -> -11; Move.CHAOS -> random.nextInt(-20, 21) }).coerceIn(0, 100),
        message = if (event >= 0) eventQuotes[event] else quotes.getValue(move).random(random),
        mode = move, event = event, combo = combo,
        seenEvents = if (event >= 0) s.seenEvents + event else s.seenEvents,
        serial = s.serial + 1
    )
    return reward(next)
}

fun poke(s: GameState, title: Boolean = false): GameState = reward(s.copy(
    taps = (s.taps + if (title) 0 else 1).coerceAtMost(999_999),
    titleTaps = (s.titleTaps + if (title) 1 else 0).coerceAtMost(999_999),
    absurd = (s.absurd + 3).coerceAtMost(999_999_999),
    message = if (title) {
        if (s.titleTaps >= 6) "РЕЖИМ АГРОНОМА: урожай нестабилен, зато харизматичен."
        else "До секретного агронома: ${6 - s.titleTaps}. Это не баг. Это грядка."
    } else listOf("Руки прочь от экспоната!", "Я тебе не антистресс. Хотя…", "Щекотно в области самооценки.", "Осторожно: кожура с характером.")[s.taps % 4],
    serial = s.serial + 1, event = -1
))
