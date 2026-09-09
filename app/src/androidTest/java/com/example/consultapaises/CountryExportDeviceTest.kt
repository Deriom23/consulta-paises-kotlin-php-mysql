package com.example.consultapaises

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.app.Instrumentation.ActivityResult
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CountryExportDeviceTest {
    @Test(timeout = 90000) fun searchRecreateCancelAndExportDisplayedCountry() {
        assumeTrue(Build.VERSION.SDK_INT >= 29)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val application = context.applicationContext as Application
        var currentActivity: MainActivity? = null
        var resumed = CountDownLatch(1)
        val lifecycle = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                if (activity is MainActivity) { currentActivity = activity; resumed.countDown() }
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        application.registerActivityLifecycleCallbacks(lifecycle)
        context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        assertTrue("La actividad debe abrirse", resumed.await(15, TimeUnit.SECONDS))
        val scenario = object {
            fun onActivity(action: (MainActivity) -> Unit) = instrumentation.runOnMainSync { action(checkNotNull(currentActivity)) }
            fun recreate() {
                resumed = CountDownLatch(1)
                onActivity { it.recreate() }
                assertTrue(resumed.await(15, TimeUnit.SECONDS))
            }
        }
        try {
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.countryInput).setText("Peru")
                activity.findViewById<Button>(R.id.searchButton).performClick()
            }
            var visible = false
            val deadline = System.currentTimeMillis() + 45000
            while (!visible && System.currentTimeMillis() < deadline) {
                scenario.onActivity { visible = it.findViewById<View>(R.id.resultCard).visibility == View.VISIBLE }
                if (!visible) Thread.sleep(200)
            }
            assertTrue("La búsqueda real de Peru debe terminar", visible)
            scenario.recreate()
            var expected = ""
            scenario.onActivity { activity ->
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.resultCard).visibility)
                val title = activity.findViewById<TextView>(R.id.countryName).text.toString()
                assertEquals("Peru (PE)", title)
                val details = activity.findViewById<TextView>(R.id.countryDetails).text.toString()
                expected = "$title\r\n\r\n${details.replace("\n", "\r\n")}\r\n"
                val scroll = activity.findViewById<View>(R.id.resultCard).parent.parent as ScrollView
                scroll.fullScroll(View.FOCUS_DOWN)
            }
            Thread.sleep(800)
            val evidence = File(context.getExternalFilesDir(null), "export_evidence").apply { mkdirs() }
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(evidence, "resultado_txt.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            // Simular Cancelar del selector y verificar que el usuario puede volver a exportar.
            Intents.init()
            try {
                intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
                scenario.onActivity { it.findViewById<Button>(R.id.downloadButton).performClick() }
                Thread.sleep(1000)
                intended(hasAction(Intent.ACTION_CREATE_DOCUMENT))
                scenario.onActivity { assertTrue(it.findViewById<Button>(R.id.downloadButton).isEnabled) }
            } finally { Intents.release() }

            val uri = checkNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "Consulta_Peru_verificacion.txt")
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/ConsultaPaises")
                }))
            // Sustituir únicamente la elección del destino; el callback y la escritura son los de producción.
            Intents.init()
            try {
                intending(hasAction(Intent.ACTION_CREATE_DOCUMENT)).respondWith(ActivityResult(Activity.RESULT_OK, Intent().setData(uri)))
                scenario.onActivity { it.findViewById<Button>(R.id.downloadButton).performClick() }
                Thread.sleep(1000)
                intended(hasAction(Intent.ACTION_CREATE_DOCUMENT))
                var actual = ""
                val writeDeadline = System.currentTimeMillis() + 10000
                while (actual != expected && System.currentTimeMillis() < writeDeadline) {
                    actual = context.contentResolver.openInputStream(uri)!!.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    if (actual != expected) Thread.sleep(100)
                }
                assertEquals(expected, actual)
                File(evidence, "Consulta_Peru.txt").writeText(actual, Charsets.UTF_8)
            } finally { Intents.release() }
        } finally { application.unregisterActivityLifecycleCallbacks(lifecycle) }
    }
}
