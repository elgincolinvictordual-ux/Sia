package com.theunknown.siasgrades

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest
import android.os.Debug
import java.net.Socket
import java.net.InetSocketAddress
import java.io.File
import android.widget.ScrollView
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess
import java.io.StringWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

private const val SIAS_URL = "https://icloudph.com/prmsu-main/sias/"
private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

private const val PURPLE_LIGHT = 0xFF6D28D9.toInt()
private const val PURPLE_MID = 0xFF4C1D95.toInt()
private const val PURPLE_DARK = 0xFF2E1065.toInt()
private const val PURPLE_DEEP = 0xFF12062B.toInt()

// The hook script is XOR-encrypted and Base64-encoded so it does not
// appear as readable text in the compiled APK (defeats a plain string
// dump / grep of the app; it is still visible to anyone who hooks the
// running app or dumps memory, which no on-device obfuscation can stop).
private val HOOK_KEY = intArrayOf(94, 44, 47, 176, 195, 154, 184, 54, 163, 21, 71, 213, 35, 135, 205, 202, 235, 80, 19, 148, 223, 157, 120, 188, 34, 81, 71, 207, 185, 228, 192, 112)
private const val HOOK_JS_ENC = "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"
// SHA-256 of the release signing certificate. If an APK with this code
// is modified and re-signed with a different key, this will no longer
// match. This only flags a resigned/tampered copy - it cannot stop
// someone from reading or changing the code in the first place.
private const val EXPECTED_CERT_SHA256 = "716287e08f505d4d2699c2853ec397c00755071a531a8c42bcc9f7f5813036e5"

private val HOOK_JS: String by lazy {
    val raw = android.util.Base64.decode(HOOK_JS_ENC, android.util.Base64.NO_WRAP)
    val out = ByteArray(raw.size)
    for (i in raw.indices) {
        out[i] = ((raw[i].toInt() and 0xFF) xor HOOK_KEY[i % HOOK_KEY.size]).toByte()
    }
    String(out, Charsets.UTF_8)
}

// A 12-tick spinner in the style of iOS's activity indicator, drawn by hand
// since no image asset is used anywhere else in this app. The whole shape
// spins continuously via a rotation animation.
private class IosSpinner(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val outerR = width / 2f * 0.92f
        val innerR = outerR * 0.5f
        val tickWidth = width * 0.1f
        val ticks = 12
        for (i in 0 until ticks) {
            paint.alpha = (255 * (i + 1) / ticks.toFloat()).toInt()
            canvas.save()
            canvas.rotate(i * (360f / ticks), cx, cy)
            canvas.drawRoundRect(
                cx - tickWidth / 2, cy - outerR, cx + tickWidth / 2, cy - innerR,
                tickWidth / 2, tickWidth / 2, paint
            )
            canvas.restore()
        }
    }
}

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var spinner: IosSpinner
    private lateinit var fab: ImageView
    private lateinit var banner: LinearLayout
    private lateinit var bannerIcon: TextView
    private lateinit var bannerTitle: TextView
    private lateinit var bannerText: TextView

    private var lastJson: String? = null
    private var firstLoadDone = false

    // Fails open: if the check itself can't run cleanly on some device, we
    // never want that to block a legitimate user from using their own app.
    private fun isSignatureTrusted(): Boolean {
        return try {
            val sig: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signers = info.signingInfo?.apkContentsSigners
                if (signers.isNullOrEmpty()) return true
                signers[0].toByteArray()
            } else {
                @Suppress("DEPRECATION")
                val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                val signers = info.signatures
                if (signers.isNullOrEmpty()) return true
                signers[0].toByteArray()
            }
            val hex = MessageDigest.getInstance("SHA-256").digest(sig).joinToString("") { "%02x".format(it) }
            hex.equals(EXPECTED_CERT_SHA256, ignoreCase = true)
        } catch (e: Exception) {
            true
        }
    }
    // Runs for any uncaught exception anywhere in the app. The process is
    // already in a broken state once this fires, so we save what happened
    // and restart clean rather than try to keep running - the saved report
    // is what the next launch shows on the debug screen.
    private fun installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            try {
                val sw = StringWriter()
                ex.printStackTrace(PrintWriter(sw))
                val ver = try {
                    val pi = packageManager.getPackageInfo(packageName, 0)
                    @Suppress("DEPRECATION")
                    pi.versionName + " (" + pi.versionCode + ")"
                } catch (e: Exception) { "unknown" }
                val report = "Time: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()) +
                    "\nVersion: " + ver +
                    "\nThread: " + thread.name +
                    "\n\n" + sw.toString()
                getSharedPreferences("sg_debug", MODE_PRIVATE).edit().putString("crash_log", report).apply()
            } catch (e: Throwable) { /* best effort - do not throw from the handler itself */ }
            try {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                applicationContext.startActivity(intent)
            } catch (e: Throwable) { /* if even this fails, the process is about to die anyway */ }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun pendingCrashLog(): String? =
        getSharedPreferences("sg_debug", MODE_PRIVATE).getString("crash_log", null)

    // Blocks interaction with the app behind it, same as the kill switch
    // screen, but shows the actual error and lets you get back in once
    // you've read it - this is for your own debugging, not a security gate.
    private fun showDebugScreen(report: String) {
        handler.removeCallbacksAndMessages(null)
        if (::web.isInitialized) {
            try { web.stopLoading(); web.destroy() } catch (e: Exception) {}
        }
        val block = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(48), dp(20), dp(32))
        }
        val title = TextView(this).apply {
            text = "App crashed \u2014 debug info"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        val body = TextView(this).apply {
            text = report
            setTextColor(Color.parseColor("#D8D8D8"))
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(0, dp(16), 0, dp(16))
        }
        val btn = TextView(this).apply {
            text = "Continue to app"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#7C3AED"))
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(14), dp(24), dp(14))
            setOnClickListener {
                getSharedPreferences("sg_debug", MODE_PRIVATE).edit().remove("crash_log").apply()
                recreate()
            }
        }
        col.addView(title)
        col.addView(body)
        col.addView(btn, LinearLayout.LayoutParams(WRAP, WRAP).apply { topMargin = dp(24) })
        scroll.addView(col, FrameLayout.LayoutParams(MATCH, WRAP))
        block.addView(scroll, FrameLayout.LayoutParams(MATCH, MATCH))
        setContentView(block)
    }

    private fun checkTamper(): Boolean {
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) return true
        return fridaDetected()
    }

    // Looks for a handful of specific, well-known traces Frida leaves behind.
    // Deliberately narrow: these almost never appear on an ordinary phone, so
    // false positives should be rare. A generic root check is NOT included
    // here - lots of legitimate users root their own phone for unrelated
    // reasons, and that alone says nothing about someone inspecting this app.
    // Opening a socket is disallowed on the main thread, so that one check
    // runs on a short-lived background thread and we wait (briefly) for it.
    private fun fridaPortOpen(): Boolean {
        var found = false
        val t = Thread {
            try {
                Socket().use { it.connect(InetSocketAddress("127.0.0.1", 27042), 150); found = true }
            } catch (e: Exception) { /* closed, or blocked - not a signal either way */ }
        }
        t.start()
        t.join(300)
        return found
    }

    private fun fridaDetected(): Boolean {
        if (fridaPortOpen()) return true
        try {
            val maps = File("/proc/self/maps").readText()
            if (maps.contains("frida") || maps.contains("gum-js-loop") || maps.contains("linjector")) return true
        } catch (e: Exception) {}
        for (path in arrayOf("/data/local/tmp/frida-server", "/data/local/tmp/re.frida.server")) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun isKillSwitchTripped(): Boolean =
        getSharedPreferences("sg_sec", MODE_PRIVATE).getBoolean("tripped", false)

    private fun tripKillSwitch() {
        getSharedPreferences("sg_sec", MODE_PRIVATE).edit().putBoolean("tripped", true).apply()
        showBlockScreen()
    }

    // Replaces whatever is on screen, stops the WebView and all pending
    // work. Reinstalling the app (or clearing its storage) is what clears
    // the flag checked in isKillSwitchTripped() and brings it back.
    private fun showBlockScreen() {
        handler.removeCallbacksAndMessages(null)
        if (::web.isInitialized) {
            try { web.stopLoading(); web.loadUrl("about:blank"); web.destroy() } catch (e: Exception) {}
        }
        val block = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val msg = TextView(this).apply {
            text = "This app has detected it is being monitored or modified and has disabled itself.\n\nReinstall the app to continue."
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }
        block.addView(msg, FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER))
        setContentView(block)
    }

    private var sheetOpen = false
    private var lastBackPress = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val hideBannerTask = Runnable { dismissBanner() }
    private val safetyTask = Runnable {
        hideLoadingOverlay()
        web.alpha = 1f
    }
    private val tamperCheckTask = object : Runnable {
        override fun run() {
            if (checkTamper()) { tripKillSwitch() } else { handler.postDelayed(this, 4000) }
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun purpleGradient(orientation: GradientDrawable.Orientation): GradientDrawable =
        GradientDrawable(orientation, intArrayOf(PURPLE_LIGHT, PURPLE_MID, PURPLE_DARK))

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashHandler()

        val crash = pendingCrashLog()
        if (crash != null) {
            showDebugScreen(crash)
            return
        }

        if (isKillSwitchTripped() || checkTamper()) {
            if (!isKillSwitchTripped()) {
                getSharedPreferences("sg_sec", MODE_PRIVATE).edit().putBoolean("tripped", true).apply()
            }
            showBlockScreen()
            return
        }

        window.statusBarColor = PURPLE_DEEP

        root = FrameLayout(this)
        root.setBackgroundColor(PURPLE_DEEP)
        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(MATCH, MATCH))

        buildLoadingOverlay()
        buildFab()
        buildBanner()
        setContentView(root)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.textZoom = 100
        web.settings.setSupportZoom(true)
        web.settings.builtInZoomControls = true
        web.settings.displayZoomControls = false

        web.addJavascriptInterface(Bridge(), "AppLink")

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                val u = (url ?: "").lowercase()
                if (u.contains("logout") || u.contains("login") || u.contains("signin")) {
                    clearSession()
                }
                sheetOpen = false
                if (lastJson != null) showFab()
                view.evaluateJavascript(HOOK_JS, null)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(HOOK_JS, null)
                if (!firstLoadDone) {
                    firstLoadDone = true
                    // onPageFinished means the HTML document is loaded, not
                    // that this JS-heavy site has actually painted its real
                    // interface yet - that happens a bit later via its own
                    // scripts. This short pause covers that gap so we don't
                    // reveal a still-blank page out from under the spinner.
                    // The 15s safety timer (safetyTask) still applies on top
                    // of this, so a slow page never gets stuck hidden.
                    handler.postDelayed({
                        hideLoadingOverlay()
                        web.alpha = 1f
                    }, 700)
                }
            }
        }

        // Covered from the first frame, so the page's own blank-while-loading
        // state (and the brief network wait) is never visible as white.
        web.alpha = 0f
        showLoadingOverlay()
        if (!isSignatureTrusted()) {
            showBanner("Unofficial build", "This copy doesn't match the official app signature", "!")
        }
        web.loadUrl(SIAS_URL)
    }

    private fun buildLoadingOverlay() {
        loadingOverlay = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
        }
        spinner = IosSpinner(this)
        val size = dp(22)
        loadingOverlay.addView(spinner, FrameLayout.LayoutParams(size, size, Gravity.CENTER))
        ObjectAnimator.ofFloat(spinner, "rotation", 0f, 360f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = TimeInterpolator { (it * 12).toInt() / 12f }
            start()
        }
        root.addView(loadingOverlay, FrameLayout.LayoutParams(MATCH, MATCH))
    }

    private fun showLoadingOverlay() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        loadingOverlay.visibility = View.GONE
    }

    private fun buildFab() {
        fab = ImageView(this).apply {
            setImageResource(R.drawable.ic_logo_g)
            contentDescription = "Grades"
            setPadding(dp(13), dp(13), dp(13), dp(13))
            background = purpleGradient(GradientDrawable.Orientation.TL_BR).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(1), 0x66C4B5FD)
            }
            elevation = dp(10).toFloat()
            visibility = View.GONE
            setOnClickListener { openGrades() }
        }
        val lp = FrameLayout.LayoutParams(dp(56), dp(56), Gravity.BOTTOM or Gravity.END)
        lp.setMargins(0, 0, dp(16), dp(24))
        root.addView(fab, lp)
    }

    private fun showFab() {
        if (sheetOpen || fab.visibility == View.VISIBLE) return
        fab.alpha = 0f
        fab.scaleX = 0.4f
        fab.scaleY = 0.4f
        fab.visibility = View.VISIBLE
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(350).setInterpolator(OvershootInterpolator(1.4f)).start()
    }

    private fun hideFab() {
        fab.animate().cancel()
        fab.visibility = View.GONE
    }

    private fun clearSession() {
        val hadData = lastJson != null
        lastJson = null
        sheetOpen = false
        hideFab()
        web.alpha = 1f
        hideLoadingOverlay()
        web.evaluateJavascript("window.__hideGrades && window.__hideGrades()", null)
        if (hadData) showBanner("Logged out", "Your grades were cleared from the app")
    }

    private fun openGrades() {
        val data = lastJson ?: return
        web.evaluateJavascript(
            "window.__showGrades && window.__showGrades(" + JSONObject.quote(data) + ")",
            null
        )
    }

    private fun buildBanner() {
        bannerIcon = TextView(this).apply {
            text = "\u2713"
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x40FFFFFF)
            }
        }
        bannerTitle = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        bannerText = TextView(this).apply {
            setTextColor(0xFFEDE9FE.toInt())
            textSize = 12f
        }
        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(bannerTitle)
            addView(bannerText)
        }
        banner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(16), dp(12))
            background = purpleGradient(GradientDrawable.Orientation.LEFT_RIGHT).apply {
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), 0x40C4B5FD)
            }
            elevation = dp(12).toFloat()
            visibility = View.INVISIBLE
            setOnClickListener { dismissBanner() }
        }
        banner.addView(bannerIcon, LinearLayout.LayoutParams(dp(38), dp(38)))
        val textLp = LinearLayout.LayoutParams(0, WRAP, 1f)
        textLp.marginStart = dp(12)
        banner.addView(texts, textLp)

        val lp = FrameLayout.LayoutParams(MATCH, WRAP, Gravity.TOP)
        lp.setMargins(dp(12), dp(12), dp(12), 0)
        root.addView(banner, lp)
    }

    private fun showBanner(title: String, text: String, icon: String = "\u2713") {
        handler.removeCallbacks(hideBannerTask)
        bannerTitle.text = title
        bannerText.text = text
        bannerIcon.text = icon
        banner.alpha = 0f
        banner.translationY = -dp(120).toFloat()
        banner.visibility = View.VISIBLE
        banner.animate().translationY(0f).alpha(1f)
            .setDuration(380).setInterpolator(OvershootInterpolator(0.9f)).start()
        handler.postDelayed(hideBannerTask, 7000)
    }

    private fun dismissBanner() {
        handler.removeCallbacks(hideBannerTask)
        banner.animate().translationY(-dp(120).toFloat()).alpha(0f)
            .setDuration(250)
            .withEndAction { banner.visibility = View.INVISIBLE }
            .start()
    }

    inner class Bridge {
        @JavascriptInterface
        fun onGrades(json: String) {
            runOnUiThread {
                val first = lastJson == null
                lastJson = json
                showFab()
                if (first) showBanner("Grades ready", "Tap the G button to view them")
            }
        }

        @JavascriptInterface
        fun onLoginAttempt() {
            runOnUiThread {
                showBanner("Logging in\u2026", "Please wait")
            }
        }

        @JavascriptInterface
        fun onLoginFailed(kind: String) {
            runOnUiThread {
                handler.removeCallbacks(safetyTask)
                hideLoadingOverlay()
                web.alpha = 1f
                if (kind == "creds") {
                    showBanner("Wrong credentials", "Check your username and password and try again", "!")
                } else {
                    showBanner("Couldn't log in", "Check your connection and try again", "!")
                }
            }
        }

        @JavascriptInterface
        fun onLoggedIn() {
            runOnUiThread {
                showLoadingOverlay()
                web.alpha = 0f
                handler.removeCallbacks(safetyTask)
                handler.postDelayed(safetyTask, 15000)
                showBanner("You're logged in", "Fetching your grades\u2026")
            }
        }

        @JavascriptInterface
        fun onAutoFlowDone() {
            runOnUiThread {
                handler.removeCallbacks(safetyTask)
                hideLoadingOverlay()
                web.alpha = 1f
            }
        }

        @JavascriptInterface
        fun onLoginScreen() {
            runOnUiThread { clearSession() }
        }

        @JavascriptInterface
        fun onLogout() {
            runOnUiThread { clearSession() }
        }

        @JavascriptInterface
        fun onSheetState(open: Boolean) {
            runOnUiThread {
                sheetOpen = open
                if (open) hideFab() else if (lastJson != null) showFab()
            }
        }

        @JavascriptInterface
        fun onAutoClickFallback() {
            runOnUiThread {
                handler.removeCallbacks(safetyTask)
                hideLoadingOverlay()
                web.alpha = 1f
                if (lastJson == null) {
                    showBanner(
                        "Couldn't load automatically",
                        "Open your grades report once to load them"
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        web.evaluateJavascript("(window.__hideGrades ? window.__hideGrades() : false)") { result ->
            when {
                result == "true" -> Unit
                web.canGoBack() -> web.goBack()
                else -> {
                    val now = System.currentTimeMillis()
                    if (now - lastBackPress < 2000) {
                        finish()
                    } else {
                        lastBackPress = now
                        showBanner(
                            "Press back again to exit",
                            "There is no earlier page to go back to",
                            "!"
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::web.isInitialized) {
            handler.removeCallbacks(tamperCheckTask)
            handler.postDelayed(tamperCheckTask, 4000)
        }
    }

    override fun onPause() {
        handler.removeCallbacks(tamperCheckTask)
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacks(hideBannerTask)
        handler.removeCallbacks(safetyTask)
        handler.removeCallbacks(tamperCheckTask)
        super.onDestroy()
    }
}
