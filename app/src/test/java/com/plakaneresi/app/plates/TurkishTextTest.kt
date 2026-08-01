package com.plakaneresi.app.plates

import org.junit.Assert.assertEquals
import org.junit.Test

class TurkishTextTest {

    @Test
    fun `folds Turkish letters to ASCII`() {
        assertEquals("mugla", TurkishText.fold("Muğla"))
        assertEquals("canakkale", TurkishText.fold("Çanakkale"))
        assertEquals("sanliurfa", TurkishText.fold("Şanlıurfa"))
        assertEquals("kirsehir", TurkishText.fold("Kırşehir"))
        assertEquals("gumushane", TurkishText.fold("Gümüşhane"))
        assertEquals("duzce", TurkishText.fold("Düzce"))
    }

    @Test
    fun `both Turkish i variants collapse to plain i`() {
        // The whole point of not using lowercase(): these four must agree.
        assertEquals("izmir", TurkishText.fold("İzmir"))
        assertEquals("izmir", TurkishText.fold("IZMIR"))
        assertEquals("izmir", TurkishText.fold("izmir"))
        assertEquals("izmir", TurkishText.fold("İZMİR"))
        assertEquals("isparta", TurkishText.fold("Isparta"))
        assertEquals("igdir", TurkishText.fold("Iğdır"))
    }

    @Test
    fun `folding never emits combining marks`() {
        // lowercase() in the root locale turns İ into i + U+0307, which silently
        // lengthens the string and breaks equality. Ours must stay 5 characters.
        assertEquals(5, TurkishText.fold("İZMİR").length)
    }

    @Test
    fun `circumflexes are folded`() {
        assertEquals("hakkari", TurkishText.fold("Hakkâri"))
    }

    @Test
    fun `punctuation and spacing are dropped`() {
        assertEquals("kmaras", TurkishText.fold("K. Maraş"))
        assertEquals("", TurkishText.fold("   "))
        assertEquals("", TurkishText.fold("!?-"))
    }

    @Test
    fun `digits survive folding`() {
        assertEquals("48", TurkishText.fold("48"))
        assertEquals("48", TurkishText.fold(" 48 "))
    }

    @Test
    fun `tokens split on spaces and punctuation`() {
        assertEquals(listOf("k", "maras"), TurkishText.tokens("K. Maraş"))
        assertEquals(listOf("kahraman", "maras"), TurkishText.tokens("Kahraman Maraş"))
        assertEquals(listOf("mugla"), TurkishText.tokens("Muğla"))
        assertEquals(emptyList<String>(), TurkishText.tokens("  "))
    }
}
