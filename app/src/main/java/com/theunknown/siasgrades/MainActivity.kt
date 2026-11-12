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
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject

private const val SIAS_URL = "https://icloudph.com/prmsu-main/sias/"
private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

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
    if (g <= 1.5) return '__g_a';
    if (g <= 2.0) return '__g_b';
    if (g <= 3.0) return '__g_c';
    return '__g_d';
  }

  function build(model, showAll) {
    var h = '';
    model.terms.forEach(function (t) {
      var rows = model.map[t].filter(function (r) { return showAll || r.g !== null; });
      if (!rows.length) return;
      h += '<div class="__g_term">' + esc(t) + '</div>';
      rows.forEach(function (r) {
        h += '<div class="__g_row"><div class="__g_info"><div class="__g_code">' + esc(r.code) +
          '</div><div class="__g_name">' + esc(r.name) + '</div></div>' +
          '<div class="__g_units">' + r.u.toFixed(1) + ' u</div>' +
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
      '#__g_back{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(15,23,42,.5);z-index:2147483646;opacity:0;transition:opacity .25s}' +
      '#__g_sheet{position:fixed;left:0;right:0;bottom:0;max-height:85%;display:flex;flex-direction:column;background:#f8fafc;color:#0f172a;font-family:Roboto,sans-serif;font-size:3.7vw;line-height:1.3;border-radius:1.6em 1.6em 0 0;z-index:2147483647;transform:translateY(100%);transition:transform .32s cubic-bezier(.2,.8,.2,1);box-shadow:0 -6px 30px rgba(0,0,0,.3)}' +
      '#__g_sheet *{box-sizing:border-box;margin:0;padding:0}' +
      '.__g_handle{width:3em;height:.35em;background:#cbd5e1;border-radius:1em;margin:.7em auto .2em}' +
      '.__g_head{display:flex;align-items:center;justify-content:space-between;padding:.4em 1.2em}' +
      '.__g_title{font-size:1.45em;font-weight:700}' +
      '.__g_x{width:2.2em;height:2.2em;border-radius:50%;background:#e2e8f0;display:flex;align-items:center;justify-content:center;font-size:.9em;color:#475569}' +
      '.__g_stats{display:flex;gap:.7em;padding:.4em 1.2em .8em}' +
      '.__g_stat{flex:1;background:#fff;border-radius:1em;padding:.8em;text-align:center;box-shadow:0 1px 3px rgba(0,0,0,.08)}' +
      '.__g_sv{font-size:1.4em;font-weight:700;color:#2563eb}' +
      '.__g_sl{font-size:.8em;color:#64748b;margin-top:.15em}' +
      '.__g_tabs{display:flex;gap:.5em;padding:0 1.2em .6em}' +
      '.__g_tab{padding:.45em 1em;border-radius:2em;background:#e2e8f0;color:#475569;font-size:.9em}' +
      '.__g_tab.on{background:#2563eb;color:#fff}' +
      '.__g_list{overflow-y:auto;padding:0 1.2em 1.5em;flex:1;min-height:0}' +
      '.__g_term{font-size:.8em;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:.05em;margin:1em 0 .4em}' +
      '.__g_row{display:flex;align-items:center;gap:.7em;background:#fff;border-radius:1em;padding:.7em .9em;margin-bottom:.45em;box-shadow:0 1px 2px rgba(0,0,0,.06)}' +
      '.__g_info{flex:1;min-width:0}' +
      '.__g_code{font-size:.75em;font-weight:700;color:#2563eb}' +
      '.__g_name{font-size:.9em}' +
      '.__g_units{font-size:.8em;color:#94a3b8}' +
      '.__g_pill{min-width:3.2em;text-align:center;padding:.4em .6em;border-radius:.8em;font-weight:700}' +
      '.__g_a{background:#dcfce7;color:#15803d}.__g_b{background:#dbeafe;color:#1d4ed8}.__g_c{background:#fef3c7;color:#b45309}.__g_d{background:#fee2e2;color:#b91c1c}.__g_na{background:#f1f5f9;color:#94a3b8}' +
      '.__g_empty{text-align:center;color:#94a3b8;padding:2em}';
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
    if (sheet) sheet.style.transform = 'translateY(100%)';
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
      '<div class="__g_handle"></div>' +
      '<div class="__g_head"><div class="__g_title">My Grades</div><div class="__g_x" id="__g_close">&#10005;</div></div>' +
      '<div class="__g_stats">' +
        stat('GWA', model.gwa === null ? '-' : model.gwa.toFixed(2)) +
        stat('Units', model.units.toFixed(1)) +
        stat('Graded', model.graded + '/' + model.total) +
      '</div>' +
      '<div class="__g_tabs"><div class="__g_tab on" id="__g_t1">Graded</div><div class="__g_tab" id="__g_t2">All subjects</div></div>' +
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
    var p = origFetch.apply(this, arguments);
    if (url.indexOf('curreval') !== -1) {
      p.then(function (res) { return res.clone().text(); }).then(capture).catch(function () {});
    }
    return p;
  };

  var origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    if (String(url).indexOf('curreval') !== -1) {
      this.addEventListener('load', function () { capture(this.responseText); });
    }
    return origOpen.apply(this, arguments);
  };

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
    private lateinit var fab: TextView
    private lateinit var banner: LinearLayout
    private lateinit var bannerIcon: TextView
    private lateinit var bannerTitle: TextView
    private lateinit var bannerText: TextView

    private var lastJson: String? = null
    private var lastBackPress = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val hideBannerTask = Runnable { dismissBanner() }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                view.evaluateJavascript(HOOK_JS, null)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(HOOK_JS, null)
            }
        }

        web.loadUrl(SIAS_URL)
    }

    // ---------- floating "Grades" button ----------
    private fun buildFab() {
        fab = TextView(this).apply {
            text = "\uD83C\uDF93  Grades"
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(20), dp(14), dp(22), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(30).toFloat()
                setColor(0xFF2563EB.toInt())
            }
            elevation = dp(8).toFloat()
            visibility = View.GONE
            setOnClickListener { openGrades() }
        }
        val lp = FrameLayout.LayoutParams(WRAP, WRAP, Gravity.BOTTOM or Gravity.END)
        lp.setMargins(0, 0, dp(16), dp(24))
        root.addView(fab, lp)
    }

    private fun showFab() {
        if (fab.visibility == View.VISIBLE) return
        fab.alpha = 0f
        fab.scaleX = 0.4f
        fab.scaleY = 0.4f
        fab.visibility = View.VISIBLE
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(350).setInterpolator(OvershootInterpolator(1.4f)).start()
    }

    private fun hideFab() {
        fab.visibility = View.GONE
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
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF22C55E.toInt())
            }
        }
        bannerTitle = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }
        bannerText = TextView(this).apply {
            setTextColor(0xFFCBD5E1.toInt())
            textSize = 13f
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
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(0xF2111827.toInt())
            }
            elevation = dp(12).toFloat()
            visibility = View.INVISIBLE
            setOnClickListener { dismissBanner() }
        }
        banner.addView(bannerIcon, LinearLayout.LayoutParams(dp(40), dp(40)))
        val textLp = LinearLayout.LayoutParams(0, WRAP, 1f)
        textLp.marginStart = dp(12)
        banner.addView(texts, textLp)

        val lp = FrameLayout.LayoutParams(MATCH, WRAP, Gravity.TOP)
        lp.setMargins(dp(12), dp(12), dp(12), 0)
        root.addView(banner, lp)
    }

    private fun showBanner(
        title: String,
        text: String,
        icon: String = "\u2713",
        color: Int = 0xFF22C55E.toInt()
    ) {
        handler.removeCallbacks(hideBannerTask)
        bannerTitle.text = title
        bannerText.text = text
        bannerIcon.text = icon
        (bannerIcon.background as GradientDrawable).setColor(color)
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
                if (first) showBanner("Grades ready", "Tap the Grades button to view them")
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
            runOnUiThread {
                lastJson = null
                hideFab()
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
                            "!",
                            0xFFF59E0B.toInt()
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
