package com.guarda.zcash

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.getkeepsafe.relinker.ReLinker
import com.guarda.zcash.crypto.Utils.bytesToHex
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.util.*

class RustAPITest {

    companion object {
        val note = byteArrayOf(1, 49, -56, 16, 72, -122, 103, 46, 68, 17, 99, 117, -96, 105, 117, 0, 0, 0, 0, 0, 76, -96, 22, 106, 90, -59, 124, 1, -55, 48, 96, 10, -60, 49, -63, 1, 15, -41, -72, -75, -104, -57, 42, 47, 3, 54, -24, -65, -62, -52, 77, 14)
        val key = byteArrayOf(69, 121, -20, 127, -124, -3, 114, -88, 7, -102, 47, 22, -12, -31, -92, -93, -41, 62, 32, -6, -80, -24, 115, 22, 115, -83, -91, -43, 98, 103, -54, -15)
    }

    @Before
    fun initNativeLibrary() {
        try {
            val logcatLogger = ReLinker.Logger { message: String? -> Timber.d("ReLinker %s", message) }
            ReLinker.log(logcatLogger).loadLibrary(ApplicationProvider.getApplicationContext(), "native-lib")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e("System.loadLibrary(\"native-lib\") e=%s", e.message)
        }
    }

    @Test
    fun encryptNp() {
        Timber.d("device info: %s, %s, %s, %s, %s", Build.MANUFACTURER, Build.DEVICE, Build.BRAND, Build.MODEL, Build.BOOTLOADER)

        //Java -> CPP -> Rust method doesn't work on the emulator
        //in the case we use only Java - Rust method
        val isDeviceOk = Build.MODEL != "Android SDK built for x86" && Build.DEVICE != "generic_x86"
        var res0 = ByteArray(0)
        if (isDeviceOk) {
            res0 = RustAPI.encryptNp(ByteArray(32), ByteArray(52))
            Timber.d("res = %s", Arrays.toString(res0))
        }

        val res1 = RustAPI.encryptNp(key, note + ByteArray(512))
        Timber.d("res1 = %s", Arrays.toString(res1))

        if (isDeviceOk) assert(res0.contentEquals(res1))
    }
}