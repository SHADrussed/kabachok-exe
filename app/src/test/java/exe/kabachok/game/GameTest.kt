package exe.kabachok.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GameTest {
    @Test fun bodybuildingUnlocksEveryStage() {
        var s = GameState()
        repeat(60) { s = perform(s, Move.LIFT, Random(it)) }
        assertEquals(4, s.stage)
        assertEquals(60, s.lifts)
        assertTrue(s.unlocked.containsAll(listOf("hello", "muscle", "garden", "boss", "answer")))
        assertEquals(0, s.energy)
        assertTrue(s.absurd > 0)
    }
    @Test fun secretRitualRequiresCorrectOrderAndPersists() {
        var s = perform(GameState(), Move.POTATO, Random(1))
        s = perform(s, Move.SLEEP, Random(2))
        s = perform(s, Move.CARDIO, Random(3))
        assertTrue("ritual" in s.unlocked)
        s = perform(s, Move.LIFT, Random(4))
        assertTrue("ritual" in s.unlocked)
        var wrong = perform(GameState(), Move.SLEEP, Random(1))
        wrong = perform(wrong, Move.POTATO, Random(2))
        wrong = perform(wrong, Move.CARDIO, Random(3))
        assertFalse("ritual" in wrong.unlocked)
    }
    @Test fun mysteriesAlwaysProduceValidEvents() {
        var s = GameState()
        repeat(200) {
            s = perform(s, Move.CHAOS, Random(it))
            assertTrue(s.event in eventNames.indices)
            assertTrue(s.energy in 0..100)
        }
        assertEquals(8, s.seenEvents.size)
        assertTrue("collector" in s.unlocked)
    }
    @Test fun easterEggsCanBeFound() {
        var s = GameState()
        repeat(10) { s = poke(s) }
        repeat(7) { s = poke(s, title = true) }
        assertTrue(s.unlocked.containsAll(listOf("poke", "developer")))
        assertEquals(51L, s.absurd)
    }
    @Test fun sleepingAndPotatoesRestoreEnergyWithoutBlockingActions() {
        val tired = GameState(energy = 0)
        assertEquals(24, perform(tired, Move.SLEEP, Random(1)).energy)
        assertEquals(13, perform(tired, Move.POTATO, Random(1)).energy)
        assertEquals(1, perform(tired, Move.LIFT, Random(1)).lifts)
        assertEquals(100, perform(GameState(energy = 99), Move.SLEEP, Random(1)).energy)
    }
    @Test fun contentHasAtLeastThirtyQuotes() {
        assertTrue(quotes.values.flatten().distinct().size >= 30)
        assertEquals(eventNames.size, eventQuotes.size)
        assertEquals(trophies.size, trophies.map { it.id }.distinct().size)
    }
}
