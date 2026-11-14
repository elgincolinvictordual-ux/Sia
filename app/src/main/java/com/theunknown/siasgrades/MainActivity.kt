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
private val HOOK_KEY = intArrayOf(56, 181, 72, 160, 81, 233, 96, 239, 74, 122, 187, 227, 109, 239, 36, 36, 223, 139, 51, 234, 244, 151, 238, 254, 151, 254, 145, 50, 148, 175, 176, 250)
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
