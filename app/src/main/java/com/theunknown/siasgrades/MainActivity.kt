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
private val HOOK_KEY = intArrayOf(183, 212, 71, 179, 145, 164, 193, 222, 84, 71, 138, 199, 73, 70, 32, 150, 158, 41, 204, 241, 4, 167, 140, 230, 171, 50, 144, 20, 220, 172, 86, 93)
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
