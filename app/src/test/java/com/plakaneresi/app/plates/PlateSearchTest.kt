package com.plakaneresi.app.plates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateSearchTest {

    private fun firstName(query: String): String? =
        PlateSearch.query(query).firstOrNull()?.province?.name

    @Test
    fun `the case from the brief`() {
        assertEquals("Muğla", firstName("48"))
        assertEquals(1, PlateSearch.query("48").size)
    }

    @Test
    fun `two digit codes resolve to exactly one province`() {
        assertEquals("İstanbul", firstName("34"))
        assertEquals("Ankara", firstName("06"))
        assertEquals("İzmir", firstName("35"))
        assertEquals("Düzce", firstName("81"))
        assertEquals(1, PlateSearch.query("34").size)
    }

    @Test
    fun `a single digit offers the exact code first, then the range`() {
        // "4" is Ağrı (04) but the user may be halfway through typing 41.
        val hits = PlateSearch.query("4")
        assertEquals("Ağrı", hits.first().province.name)
        assertEquals(MatchKind.EXACT_CODE, hits.first().kind)
        assertEquals(11, hits.size) // 04 plus 40..49
        assertTrue(hits.drop(1).all { it.kind == MatchKind.CODE_PREFIX })
    }

    @Test
    fun `codes that do not exist return nothing`() {
        assertTrue(PlateSearch.query("82").isEmpty())
        assertTrue(PlateSearch.query("99").isEmpty())
        assertTrue(PlateSearch.query("00").isEmpty())
        assertTrue(PlateSearch.query("123").isEmpty())
    }

    @Test
    fun `names typed on a keyboard without Turkish characters`() {
        assertEquals("Muğla", firstName("mugla"))
        assertEquals("Muğla", firstName("MUGLA"))
        assertEquals("Çanakkale", firstName("canakkale"))
        assertEquals("Şanlıurfa", firstName("sanliurfa"))
        assertEquals("Kırşehir", firstName("kirsehir"))
        assertEquals("Ağrı", firstName("agri"))
        assertEquals("Iğdır", firstName("igdir"))
        assertEquals("Uşak", firstName("usak"))
        assertEquals("Hakkâri", firstName("hakkari"))
    }

    @Test
    fun `the dotted and dotless i both work`() {
        assertEquals("İzmir", firstName("izmir"))
        assertEquals("İzmir", firstName("IZMIR"))
        assertEquals("İzmir", firstName("İzmir"))
        assertEquals("Isparta", firstName("isparta"))
        assertEquals("Isparta", firstName("ısparta"))
    }

    @Test
    fun `old and colloquial names are searchable`() {
        assertEquals("Mersin", firstName("icel"))
        assertEquals("Mersin", firstName("İçel"))
        assertEquals("Şanlıurfa", firstName("urfa"))
        assertEquals("Gaziantep", firstName("antep"))
        assertEquals("Kocaeli", firstName("izmit"))
        assertEquals("Tunceli", firstName("dersim"))
        assertEquals("Sakarya", firstName("adapazari"))
    }

    @Test
    fun `multi word queries match a single run name`() {
        assertEquals("Kahramanmaraş", firstName("k. maras"))
        assertEquals("Kahramanmaraş", firstName("kahraman maras"))
        assertEquals("Afyonkarahisar", firstName("afyon kara"))
    }

    @Test
    fun `a name prefix outranks a substring match`() {
        val hits = PlateSearch.query("mu")
        assertEquals("Muğla", hits.first().province.name)
        assertEquals(MatchKind.NAME_PREFIX, hits.first().kind)
        // Gümüşhane contains "mu" but must not come first.
        assertTrue(hits.any { it.province.name == "Gümüşhane" })
    }

    @Test
    fun `substring matches still work`() {
        assertEquals("Afyonkarahisar", firstName("hisar"))
    }

    @Test
    fun `nonsense and empty input return nothing`() {
        assertTrue(PlateSearch.query("").isEmpty())
        assertTrue(PlateSearch.query("   ").isEmpty())
        assertTrue(PlateSearch.query("zzzz").isEmpty())
    }

    @Test
    fun `every province is reachable by its own code and name`() {
        for (province in Provinces.all) {
            assertEquals(
                "code ${province.plateCode}",
                province.name,
                firstName(province.plateCode),
            )
            assertEquals(
                "name ${province.name}",
                province.name,
                firstName(province.name),
            )
        }
    }
}
