package com.plakaneresi.app.plates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProvincesTest {

    @Test
    fun `there are 81 provinces with codes 1 through 81`() {
        assertEquals(81, Provinces.all.size)
        assertEquals((1..81).toSet(), Provinces.all.map { it.code }.toSet())
    }

    @Test
    fun `names and codes are unique`() {
        assertEquals(81, Provinces.all.map { it.name }.distinct().size)
        assertEquals(81, Provinces.all.map { it.code }.distinct().size)
        // Folded names must stay unique too, or search becomes ambiguous.
        assertEquals(81, Provinces.all.map { TurkishText.fold(it.name) }.distinct().size)
    }

    @Test
    fun `plate codes are zero padded to two digits`() {
        assertEquals("01", Provinces.byCode(1)?.plateCode)
        assertEquals("09", Provinces.byCode(9)?.plateCode)
        assertEquals("48", Provinces.byCode(48)?.plateCode)
        assertTrue(Provinces.all.all { it.plateCode.length == 2 })
    }

    @Test
    fun `spot check well known codes`() {
        assertEquals("Adana", Provinces.byCode(1)?.name)
        assertEquals("Ankara", Provinces.byCode(6)?.name)
        assertEquals("İstanbul", Provinces.byCode(34)?.name)
        assertEquals("İzmir", Provinces.byCode(35)?.name)
        assertEquals("Muğla", Provinces.byCode(48)?.name)
        assertEquals("Düzce", Provinces.byCode(81)?.name)
    }

    @Test
    fun `lookup outside the range returns null`() {
        assertNull(Provinces.byCode(0))
        assertNull(Provinces.byCode(82))
        assertNotNull(Provinces.byCode(81))
    }

    @Test
    fun `the default list is ordered by code`() {
        assertEquals((1..81).toList(), Provinces.all.map { it.code })
    }

    @Test
    fun `alphabetical order follows the Turkish alphabet`() {
        val names = Provinces.alphabetical.map { it.name }
        assertEquals(81, names.size)

        // ç sorts between c and d, not after z.
        assertTrue(names.indexOf("Çanakkale") > names.indexOf("Bursa"))
        assertTrue(names.indexOf("Çanakkale") < names.indexOf("Denizli"))

        // ı sorts before i.
        assertTrue(names.indexOf("Isparta") < names.indexOf("İstanbul"))

        // ş sorts between s and t.
        assertTrue(names.indexOf("Şanlıurfa") > names.indexOf("Sivas"))
        assertTrue(names.indexOf("Şanlıurfa") < names.indexOf("Tekirdağ"))

        assertEquals("Adana", names.first())
        assertEquals("Zonguldak", names.last())
    }

    @Test
    fun `every province belongs to a region`() {
        assertTrue(Provinces.all.all { it.region.displayName.isNotBlank() })
        // All seven regions are represented.
        assertEquals(Region.entries.toSet(), Provinces.all.map { it.region }.toSet())
    }
}
