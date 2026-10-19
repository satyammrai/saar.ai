package com.example

import com.example.data.api.GeminiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testYouTubeLiveUrlExtraction() {
        val testUrl1 = "https://www.youtube.com/live/TA43DMEkLXI?si=hVIEeCYA3y9tlD-u"
        val extracted1 = GeminiService.extractVideoId(testUrl1)
        assertEquals("TA43DMEkLXI", extracted1)

        val testUrl2 = "-https://www.youtube.com/live/TA43DMEkLXI?si=hVIEeCYA3y9tlD-u"
        val extracted2 = GeminiService.extractVideoId(testUrl2)
        assertEquals("TA43DMEkLXI", extracted2)

        val testUrl3 = "https://www.youtube.com/watch?v=aircAruvnKk"
        val extracted3 = GeminiService.extractVideoId(testUrl3)
        assertEquals("aircAruvnKk", extracted3)

        val testUrl4 = "https://youtu.be/sQK3Yr4Sc_k"
        val extracted4 = GeminiService.extractVideoId(testUrl4)
        assertEquals("sQK3Yr4Sc_k", extracted4)

        val testUrl5 = "TA43DMEkLXI"
        val extracted5 = GeminiService.extractVideoId(testUrl5)
        assertEquals("TA43DMEkLXI", extracted5)

        val testUrl6 = "https://www.youtube.com/live/TA43DMEkLXI?si=gmnrLpvj1lgtk_7F"
        val extracted6 = GeminiService.extractVideoId(testUrl6)
        assertEquals("TA43DMEkLXI", extracted6)
    }
}
