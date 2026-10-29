package exe.kabachok.game

import android.content.Context

class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("kabachok_v1", Context.MODE_PRIVATE)
    fun load(): GameState = reward(GameState(
        absurd = prefs.getLong("absurd", 0).coerceIn(0, 999_999_999),
        lifts = count("lifts"), potatoes = count("potatoes"), naps = count("naps"),
        runs = count("runs"), mysteries = count("mysteries"), taps = count("taps"),
        titleTaps = count("titleTaps"), combo = count("combo").coerceAtMost(3), steps = count("steps"),
        energy = prefs.getInt("energy", 73).coerceIn(0, 100),
        unlocked = prefs.getStringSet("unlocked", emptySet())!!.toSet(),
        seenEvents = prefs.getStringSet("events", emptySet())!!.mapNotNull { it.toIntOrNull() }.filter { it in eventNames.indices }.toSet(),
        sound = prefs.getBoolean("sound", true), vibration = prefs.getBoolean("vibration", true),
        calm = prefs.getBoolean("calm", false),
        message = prefs.getString("message", null) ?: "Я кабачок. У меня сушка. И вопросы."
    ))
    private fun count(key: String) = prefs.getInt(key, 0).coerceIn(0, 999_999)
    fun save(s: GameState) {
        prefs.edit().putLong("absurd", s.absurd).putInt("lifts", s.lifts)
            .putInt("potatoes", s.potatoes).putInt("naps", s.naps).putInt("runs", s.runs)
            .putInt("mysteries", s.mysteries).putInt("taps", s.taps).putInt("titleTaps", s.titleTaps)
            .putInt("combo", s.combo).putInt("steps", s.steps).putInt("energy", s.energy)
            .putStringSet("unlocked", s.unlocked).putStringSet("events", s.seenEvents.map { it.toString() }.toSet())
            .putBoolean("sound", s.sound).putBoolean("vibration", s.vibration).putBoolean("calm", s.calm)
            .putString("message", s.message).apply()
    }
}
