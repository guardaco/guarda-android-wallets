package com.guarda.zcash

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import work.samosudov.zecrustlib.ZecLibRustApi
import work.samosudov.zecrustlib.crypto.Utils
import java.util.*

class ZecLibRustApiTest {

    companion object {
        val note = byteArrayOf(1, 49, -56, 16, 72, -122, 103, 46, 68, 17, 99, 117, -96, 105, 117, 0, 0, 0, 0, 0, 76, -96, 22, 106, 90, -59, 124, 1, -55, 48, 96, 10, -60, 49, -63, 1, 15, -41, -72, -75, -104, -57, 42, 47, 3, 54, -24, -65, -62, -52, 77, 14)
        val key = byteArrayOf(69, 121, -20, 127, -124, -3, 114, -88, 7, -102, 47, 22, -12, -31, -92, -93, -41, 62, 32, -6, -80, -24, 115, 22, 115, -83, -91, -43, 98, 103, -54, -15)
        val ivk = Utils.hexToBytes("00c11ba0a37371bd41ba456fb95d36c09b55d2ac9fa9e62bc71d1b08d3bc78b4")
        val plainText = byteArrayOf(2, -40, -89, -3, 10, 80, 100, -7, 28, 26, 4, -16, 0, -31, -11, 5, 0, 0, 0, 0, 86, 78, -32, 112, -124, -14, 105, -55, -113, -75, -26, -78, -92, -45, 92, 37, 9, 10, -39, -9, -66, -40, -7, 23, -10, 31, -73, 73, -18, -43, -128, 44)

        val plainTextTestnet = byteArrayOf(2, -55, -39, 61, 63, -23, 111, -78, 26, 110, 44, 48, 27, 88, -38, -63, 65, -32, 59, 102, 35, 115, 37, -55, -73, 70, -4, 66, 54, -53, -125, 19, 75, 120, -52, -4, 27, 9, 24, 2, -113, 111, 28, 65, -37, -72, 39, 125, -42, 30, -114, 126)

        val ivkSecond = Utils.hexToBytes("0305649e356eb65e0e0ac758e9d429d8e0d88d3a88a7bc9d07ce792d7e315442")
        val plainTextSecond = byteArrayOf(2, -86, 3, -15, 91, -14, 88, 9, -68, -104, -71, 63, 0, -31, -11, 5, 0, 0, 0, 0, -127, 104, 110, -59, -98, -71, 94, -108, 72, -128, -69, 90, -50, -33, 92, -5, -76, 60, 54, -124, -1, -112, 12, -109, 123, -92, 65, -34, -22, 39, 48, -67)
    }

    @Before
    fun initNativeLibrary() {
        try {
            ZecLibRustApi.init(ApplicationProvider.getApplicationContext())
        } catch (e: Exception) {
            println("ZecLibRustApi.init e=${e.message}")
        }
    }

    @Test
    fun encryptNp() {
        val res1 = ZecLibRustApi.encryptNp(key, note + ByteArray(512))
        println("res1 = $res1")
    }

    @Test
    fun testCmRseed() {
        val cmuExpected = "40051aeb99e2b7cf32591ca710b2ba1b4e9413feb99d52b8c931184a4f094314"
        val res1 = ZecLibRustApi.cmRseed(Utils.reverseByteArray(ivk), (plainText))
        println("res1 = ${Arrays.toString(res1)}")
        println("res1 hex = ${Utils.bytesToHex(res1)}")

        val cmuFromLibRevertedHex = Utils.bytesToHex(Utils.reverseByteArray(res1))
        println("res1 hex reverted = $cmuFromLibRevertedHex")

        assertEquals(cmuExpected, cmuFromLibRevertedHex)
    }

    @Test
    fun testCmRseedSecond() {
        val cmuExpected = "101015eca2b76b0fb002d05eb1bd1bb8be61c26419dd5f4269bf51d399909ca4"
        val ivkBytes = Utils.reverseByteArray(ivkSecond)
        val snpBytes = plainTextSecond
        Timber.d("ZecLibRustApi ivkBytes=%s", Arrays.toString(ivkBytes))
        Timber.d("ZecLibRustApi snpBytes=%s", Arrays.toString(snpBytes))
        val res1 = ZecLibRustApi.cmRseed(ivkBytes, snpBytes)
        println("res1 = ${Arrays.toString(res1)}")
        println("res1 hex = ${Utils.bytesToHex(res1)}")

        val cmuFromLibRevertedHex = Utils.bytesToHex(Utils.reverseByteArray(res1))
        println("res1 hex reverted = $cmuFromLibRevertedHex")

        assertEquals(cmuExpected, cmuFromLibRevertedHex)
    }

    @Test
    fun testCmRseedTestnet() {
        val cmuExpected = "1c9228ce6fd9b5b408abfb988cb2d017fbd6c6ea4ebca1fd9f3eaf2a5a1da0db"
        val res1 = ZecLibRustApi.cmRseed(Utils.reverseByteArray(ivk), (plainTextTestnet))
        println("res1 = ${Arrays.toString(res1)}")
        println("res1 hex = ${Utils.bytesToHex(res1)}")

        val cmuFromLibRevertedHex = Utils.bytesToHex(Utils.reverseByteArray(res1))
        println("res1 hex reverted = $cmuFromLibRevertedHex")

        assertEquals(cmuExpected, cmuFromLibRevertedHex)
    }





}