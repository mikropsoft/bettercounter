package org.kde.bettercounter.extensions

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kde.bettercounter.ViewModel
import org.kde.bettercounter.persistence.Entry
import java.time.temporal.ChronoUnit
import java.util.Date

class ImportTest {

    private val testTimestamp = System.currentTimeMillis()

    @Test
    fun `happy case`() {
        val namesToImport: MutableList<String> = mutableListOf()
        val entriesToImport: MutableList<Entry> = mutableListOf()
        val line = "hola,$testTimestamp,$testTimestamp,42"
        ViewModel.parseImportLine(line, namesToImport, entriesToImport)
        assertArrayEquals(namesToImport.toTypedArray(), arrayOf("hola"))
        assertEquals(entriesToImport.size, 3)
        assertEquals(entriesToImport.firstOrNull()?.date?.time, testTimestamp)
        assertEquals(entriesToImport.lastOrNull()?.date?.time, 42L)
    }

    @Test
    fun `names can be numbers`() {
        val namesToImport: MutableList<String> = mutableListOf()
        val entriesToImport: MutableList<Entry> = mutableListOf()
        val line = "0,$testTimestamp"
        ViewModel.parseImportLine(line, namesToImport, entriesToImport)
        assertArrayEquals(namesToImport.toTypedArray(), arrayOf("0"))
        assertEquals(entriesToImport.firstOrNull()?.date?.time, testTimestamp)
    }

    @Test
    fun `names can contain commas`() {
        val namesToImport: MutableList<String> = mutableListOf()
        val entriesToImport: MutableList<Entry> = mutableListOf()
        val line = "0,0,$testTimestamp"
        ViewModel.parseImportLine(line, namesToImport, entriesToImport)
        assertArrayEquals(namesToImport.toTypedArray(), arrayOf("0,0"))
        assertEquals(entriesToImport.firstOrNull()?.date?.time, testTimestamp)
    }

    @Test
    fun `works if no entries`() {
        val namesToImport: MutableList<String> = mutableListOf()
        val entriesToImport: MutableList<Entry> = mutableListOf()
        val line = "hola"
        ViewModel.parseImportLine(line, namesToImport, entriesToImport)
        assertArrayEquals(namesToImport.toTypedArray(), arrayOf("hola"))
        assertTrue(entriesToImport.isEmpty())
    }

    @Test(expected = NumberFormatException::class)
    fun `fails if text after timestamps`() {
        val namesToImport: MutableList<String> = mutableListOf()
        val entriesToImport: MutableList<Entry> = mutableListOf()
        val line = "hola,$testTimestamp,hola"
        ViewModel.parseImportLine(line, namesToImport, entriesToImport)
    }

}
