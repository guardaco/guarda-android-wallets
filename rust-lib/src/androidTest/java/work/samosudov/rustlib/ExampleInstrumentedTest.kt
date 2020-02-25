package work.samosudov.rustlib

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }


    @Test
    fun testRustApi() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("work.samosudov.rustlib.test", appContext.packageName)

        println("rust test=" + RustAPI.genr())

    }
}
