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
            res0 = RustAPI.encryptNp(bytesToHex(ByteArray(32)), bytesToHex(ByteArray(564)))
            Timber.d("res = %s", Arrays.toString(res0))
        }

        val res1 = RustAPI.testEncryptNp(ByteArray(32), ByteArray(564))
        Timber.d("res1 = %s", Arrays.toString(res1))

        if (isDeviceOk) assert(res0.contentEquals(res1))
    }
}