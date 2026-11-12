package com.theunknown.siasgrades

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
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
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

private const val SIAS_URL = "https://icloudph.com/prmsu-main/sias/"
private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

// Purple palette
private const val PURPLE_LIGHT = 0xFFA78BFA.toInt()
private const val PURPLE_MID = 0xFF7C3AED.toInt()
private const val PURPLE_DARK = 0xFF5B21B6.toInt()

// Injected on every page load.
// 1. Hooks fetch/XHR and sends the "curreval" response to the app (SiasBridge.onGrades).
// 2. Defines window.__showGrades(text) which draws the grades sheet.
// 3. Watches for the login form disappearing to tell the app the user logged in.
// Real grade = Previous[last].Grade / 100, units = Units / 100 (top-level Grade is scrambled).
private val HOOK_JS = """
(function () {
  if (window.__curevalHooked) return;
  window.__curevalHooked = true;

  function esc(s) {
    return String(s == null ? '' : s).replace(/&/g, '&amp;').replace(/</g, '&lt;');
  }

  function parse(text) {
    var data = JSON.parse(text);
    var list = data && data.Data;
    if (!Array.isArray(list)) return null;
    var pts = 0, units = 0, graded = 0, terms = [], map = {};
    list.forEach(function (s) {
      var u = (s.Units || 0) / 100;
      var prev = (s.Previous && s.Previous.length) ? s.Previous[s.Previous.length - 1] : null;
      var g = prev ? prev.Grade / 100 : null;
      if (g !== null && g > 0) { pts += g * u; units += u; graded++; } else { g = null; }
      var key = 'Year ' + s.Year + ' - Term ' + s.Term;
      if (!map[key]) { map[key] = []; terms.push(key); }
      map[key].push({ code: s.SubjCode, name: s.SubjName, u: u, g: g });
    });
    return { terms: terms, map: map, gwa: units > 0 ? pts / units : null, units: units, graded: graded, total: list.length };
  }

  function cls(g) {
    if (g === null) return '__g_na';
    return g > 3.0 ? '__g_d' : '__g_a';
  }

  function build(model, showAll) {
    var h = '';
    model.terms.forEach(function (t) {
      var rows = model.map[t].filter(function (r) { return showAll || r.g !== null; });
      if (!rows.length) return;
      h += '<div class="__g_term"><span>' + esc(t) + '</span><i></i></div>';
      rows.forEach(function (r) {
        h += '<div class="__g_row"><div class="__g_info"><div class="__g_name">' + esc(r.name) +
          '</div><div class="__g_meta">' + esc(r.code) + ' &middot; ' + r.u.toFixed(1) + ' units</div></div>' +
          '<div class="__g_pill ' + cls(r.g) + '">' + (r.g !== null ? r.g.toFixed(2) : '-') + '</div></div>';
      });
    });
    return h || '<div class="__g_empty">No grades yet</div>';
  }

  function stat(label, value) {
    return '<div class="__g_stat"><div class="__g_sv">' + value + '</div><div class="__g_sl">' + label + '</div></div>';
  }

  function injectStyle() {
    if (document.getElementById('__g_style')) return;
    var st = document.createElement('style');
    st.id = '__g_style';
    st.textContent =
      '#__g_back{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(30,10,60,.55);z-index:2147483646;opacity:0;transition:opacity .25s}' +
      '#__g_sheet{position:fixed;left:0;right:0;bottom:0;max-height:88%;display:flex;flex-direction:column;overflow:hidden;background:#faf8ff;color:#1e1b4b;font-family:Roboto,sans-serif;font-size:3.1vw;line-height:1.3;border-radius:1.6em 1.6em 0 0;z-index:2147483647;transform:translateY(100%);transition:transform .32s cubic-bezier(.2,.8,.2,1);box-shadow:0 -8px 32px rgba(76,29,149,.45)}' +
      '#__g_sheet *{box-sizing:border-box}' +
      '.__g_hero{flex:none;background:linear-gradient(135deg,#a78bfa 0%,#7c3aed 55%,#5b21b6 100%);color:#fff;padding:.7em 1.3em 1.2em}' +
      '.__g_handle{width:3em;height:.3em;background:rgba(255,255,255,.5);border-radius:1em;margin:0 auto .9em}' +
      '.__g_head{display:flex;align-items:center;justify-content:space-between}' +
      '.__g_title{font-size:1.6em;font-weight:700}' +
      '.__g_sub{font-size:.85em;opacity:.8;margin-top:.1em}' +
      '.__g_x{width:2.4em;height:2.4em;border-radius:50%;background:rgba(255,255,255,.2);display:flex;align-items:center;justify-content:center;font-size:.9em}' +
      '.__g_stats{display:flex;gap:.6em;margin-top:1em}' +
      '.__g_stat{flex:1;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.22);border-radius:1em;padding:.7em .4em;text-align:center}' +
      '.__g_sv{font-size:1.5em;font-weight:700}' +
      '.__g_sl{font-size:.7em;opacity:.8;margin-top:.1em;letter-spacing:.06em;text-transform:uppercase}' +
      '.__g_tabs{flex:none;padding:.9em 1.3em .3em}' +
      '.__g_seg{display:flex;background:#ede9fe;border-radius:2em;padding:.25em}' +
      '.__g_tab{flex:1;text-align:center;padding:.5em 0;border-radius:2em;color:#6d28d9;font-weight:500}' +
      '.__g_tab.on{background:linear-gradient(135deg,#8b5cf6,#6d28d9);color:#fff;box-shadow:0 2px 8px rgba(109,40,217,.35)}' +
      '.__g_list{flex:1;min-height:0;overflow-y:auto;padding:.2em 1.3em 1.6em}' +
      '.__g_term{display:flex;align-items:center;gap:.6em;font-size:.75em;font-weight:700;color:#7c3aed;text-transform:uppercase;letter-spacing:.08em;margin:1.2em 0 .5em}' +
      '.__g_term i{flex:1;height:1px;background:#ddd6fe}' +
      '.__g_row{display:flex;align-items:center;gap:.8em;background:#fff;border:1px solid #efeaff;border-radius:1em;padding:.65em .8em;margin-bottom:.4em;box-shadow:0 1px 3px rgba(91,33,182,.06)}' +
      '.__g_info{flex:1;min-width:0}' +
      '.__g_name{font-size:1em;font-weight:500;color:#1e1b4b}' +
      '.__g_meta{font-size:.78em;color:#8b83b3;margin-top:.15em}' +
      '.__g_pill{min-width:3.4em;text-align:center;padding:.4em .7em;border-radius:.8em;font-weight:700;color:#fff}' +
      '.__g_a{background:linear-gradient(135deg,#8b5cf6,#6d28d9)}' +
      '.__g_d{background:linear-gradient(135deg,#f87171,#dc2626)}' +
      '.__g_na{background:#f1f0f7;color:#a5a0c4}' +
      '.__g_empty{text-align:center;color:#8b83b3;padding:2em}';
    document.head.appendChild(st);
  }

  function removeNow() {
    ['__g_back', '__g_sheet'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.remove();
    });
  }

  function closeSheet() {
    var back = document.getElementById('__g_back');
    var sheet = document.getElementById('__g_sheet');
    if (!sheet) return;
    try { SiasBridge.onSheetState(false); } catch (e) {}
    sheet.style.transform = 'translateY(100%)';
    if (back) back.style.opacity = '0';
    setTimeout(removeNow, 320);
  }

  window.__hideGrades = function () {
    var open = !!document.getElementById('__g_sheet');
    if (open) closeSheet();
    return open;
  };

  window.__showGrades = function (text) {
    var model;
    try { model = parse(text); } catch (e) { return; }
    if (!model) return;
    removeNow();
    injectStyle();

    var back = document.createElement('div');
    back.id = '__g_back';
    var sheet = document.createElement('div');
    sheet.id = '__g_sheet';
    sheet.innerHTML =
      '<div class="__g_hero">' +
        '<div class="__g_handle"></div>' +
        '<div class="__g_head"><div><div class="__g_title">My Grades</div><div class="__g_sub">Curriculum Evaluation</div></div>' +
        '<div class="__g_x" id="__g_close">&#10005;</div></div>' +
        '<div class="__g_stats">' +
          stat('GWA', model.gwa === null ? '-' : model.gwa.toFixed(2)) +
          stat('Units', model.units.toFixed(1)) +
          stat('Graded', model.graded + '/' + model.total) +
        '</div>' +
      '</div>' +
      '<div class="__g_tabs"><div class="__g_seg"><div class="__g_tab on" id="__g_t1">Graded</div><div class="__g_tab" id="__g_t2">All subjects</div></div></div>' +
      '<div class="__g_list" id="__g_list"></div>';
    document.body.appendChild(back);
    document.body.appendChild(sheet);

    var list = sheet.querySelector('#__g_list');
    var t1 = sheet.querySelector('#__g_t1');
    var t2 = sheet.querySelector('#__g_t2');
    list.innerHTML = build(model, false);
    t1.onclick = function () { t1.className = '__g_tab on'; t2.className = '__g_tab'; list.innerHTML = build(model, false); };
    t2.onclick = function () { t2.className = '__g_tab on'; t1.className = '__g_tab'; list.innerHTML = build(model, true); };
    back.onclick = closeSheet;
    sheet.querySelector('#__g_close').onclick = closeSheet;

    try { SiasBridge.onSheetState(true); } catch (e) {}
    setTimeout(function () {
      back.style.opacity = '1';
      sheet.style.transform = 'translateY(0)';
    }, 30);
  };

  // ---- capture the curreval response ----
  function capture(text) {
    try {
      var d = JSON.parse(text);
      if (d && Array.isArray(d.Data)) SiasBridge.onGrades(text);
    } catch (e) {}
  }

  var origFetch = window.fetch;
  window.fetch = function () {
    var url = (arguments[0] && arguments[0].url) || String(arguments[0]);
    if (String(url).toLowerCase().indexOf('logout') !== -1) {
      try { SiasBridge.onLogout(); } catch (e) {}
    }
    var p = origFetch.apply(this, arguments);
    if (url.indexOf('curreval') !== -1) {
      p.then(function (res) { return res.clone().text(); }).then(capture).catch(function () {});
    }
    return p;
  };

  var origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    if (String(url).toLowerCase().indexOf('logout') !== -1) {
      try { SiasBridge.onLogout(); } catch (e) {}
    }
    if (String(url).indexOf('curreval') !== -1) {
      this.addEventListener('load', function () { capture(this.responseText); });
    }
    return origOpen.apply(this, arguments);
  };

  // ---- logout detection: tapping an element labelled Logout / Sign out ----
  document.addEventListener('click', function (e) {
    var el = e.target;
    for (var i = 0; el && i < 4; i++, el = el.parentElement) {
      var t = (el.textContent || '').trim().toLowerCase().replace(/\s/g, '');
      if (t === 'logout' || t === 'signout') {
        try { SiasBridge.onLogout(); } catch (err) {}
        return;
      }
    }
  }, true);

  // ---- login detection: the password field was on screen, then went away ----
  var PW = 'input[type=password],input[name*=pass i],input[id*=pass i],input[placeholder*=pass i]';
  var seenLogin = false, misses = 0;
  function findPw() {
    var list = document.querySelectorAll(PW);
    for (var i = 0; i < list.length; i++) {
      if (list[i].isConnected && list[i].getClientRects().length > 0) return list[i];
    }
    return null;
  }
  setInterval(function () {
    if (!document.body) return;
    if (findPw()) {
      misses = 0;
      if (!seenLogin) {
        seenLogin = true;
        try { SiasBridge.onLoginScreen(); } catch (e) {}
      }
    } else if (seenLogin) {
      misses++;
      if (misses >= 2) {
        seenLogin = false;
        misses = 0;
        try { SiasBridge.onLoggedIn(); } catch (e) {}
      }
    }
  }, 800);
})();
""".trimIndent()

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var web: WebView
    private lateinit var fab: ImageView
    private lateinit var banner: LinearLayout
    private lateinit var bannerIcon: TextView
    private lateinit var bannerTitle: TextView
    private lateinit var bannerText: TextView

    private var lastJson: String? = null
    private var sheetOpen = false
    private var lastBackPress = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val hideBannerTask = Runnable { dismissBanner() }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun purpleGradient(orientation: GradientDrawable.Orientation): GradientDrawable =
        GradientDrawable(orientation, intArrayOf(PURPLE_LIGHT, PURPLE_MID, PURPLE_DARK))

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = PURPLE_DARK

        root = FrameLayout(this)
        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(MATCH, MATCH))
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

        web.addJavascriptInterface(Bridge(), "SiasBridge")

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                // Landing on a logout/login page means the session ended
                val u = (url ?: "").lowercase()
                if (u.contains("logout") || u.contains("login") || u.contains("signin")) {
                    clearSession()
                }
                // A fresh page means any open sheet is gone
                sheetOpen = false
                if (lastJson != null) showFab()
                view.evaluateJavascript(HOOK_JS, null)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(HOOK_JS, null)
            }
        }

        web.loadUrl(SIAS_URL)
    }

    // ---------- floating atom button ----------
    private fun buildFab() {
        fab = ImageView(this).apply {
            setImageResource(R.drawable.ic_atom)
            contentDescription = "Grades"
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = purpleGradient(GradientDrawable.Orientation.TL_BR).apply {
                shape = GradientDrawable.OVAL
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

    // Forget everything about the current user so the next login starts clean
    private fun clearSession() {
        val hadData = lastJson != null
        lastJson = null
        sheetOpen = false
        hideFab()
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

    // ---------- sliding banner from the top ----------
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

    // ---------- called from the page's JavaScript ----------
    inner class Bridge {
        @JavascriptInterface
        fun onGrades(json: String) {
            runOnUiThread {
                val first = lastJson == null
                lastJson = json
                showFab()
                if (first) showBanner("Grades ready", "Tap the atom button to view them")
            }
        }

        @JavascriptInterface
        fun onLoggedIn() {
            runOnUiThread {
                showBanner("You're logged in", "Go to Reports \u203A Curriculum Evaluation to see your grades")
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
    }

    // Back order: close grades sheet -> go back in the site -> press twice to exit
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

    override fun onDestroy() {
        handler.removeCallbacks(hideBannerTask)
        super.onDestroy()
    }
}
