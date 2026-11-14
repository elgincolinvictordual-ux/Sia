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
private val HOOK_KEY = intArrayOf(90, 59, 216, 228, 7, 194, 186, 125, 144, 107, 65, 130, 60, 247, 165, 72, 29, 119, 154, 51, 213, 60, 72, 151, 162, 0, 98, 52, 219, 66, 216, 167)
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
                    hideLoadingOverlay()
                    web.alpha = 1f
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
