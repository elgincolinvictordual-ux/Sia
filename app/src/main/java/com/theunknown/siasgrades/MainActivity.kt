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

private const val PURPLE_LIGHT = 0xFF6D28D9.toInt()
private const val PURPLE_MID = 0xFF4C1D95.toInt()
private const val PURPLE_DARK = 0xFF2E1065.toInt()
private const val PURPLE_DEEP = 0xFF12062B.toInt()

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
    return g > 3.0 ? '__g_d' : '';
  }

  function build(model, showAll) {
    var h = '';
    model.terms.forEach(function (t) {
      var rows = model.map[t].filter(function (r) { return showAll || r.g !== null; });
      if (!rows.length) return;
      h += '<div class="__g_term"><span>' + esc(t) + '</span><i></i></div>';
      rows.forEach(function (r) {
        h += '<div class="__g_row' + (r.g === null ? ' __g_dim' : '') + '"><span class="__g_acc"></span>' +
          '<div class="__g_info"><div class="__g_name">' + esc(r.name) + '</div>' +
          '<div class="__g_meta">' + esc(r.code) + ' &middot; ' + r.u.toFixed(1) + ' units</div></div>' +
          '<div class="__g_g ' + cls(r.g) + '">' + (r.g !== null ? r.g.toFixed(2) : '-') + '</div></div>';
      });
    });
    return h || '<div class="__g_empty">No grades yet</div>';
  }

  function chip(label, value) {
    return '<div class="__g_chip"><em>' + label + '</em><b>' + value + '</b></div>';
  }

  function injectStyle() {
    if (document.getElementById('__g_style')) return;
    var st = document.createElement('style');
    st.id = '__g_style';
    st.textContent =
      '#__g_back{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(6,0,18,.72);z-index:2147483646;opacity:0;transition:opacity .25s}' +
      '#__g_sheet{position:fixed;left:0;right:0;bottom:0;max-height:88%;display:flex;flex-direction:column;overflow:hidden;background:linear-gradient(180deg,#1d0b3f 0%,#12062b 100%);color:#f3e8ff;font-family:Roboto,sans-serif;font-size:3.1vw;line-height:1.3;border-radius:1.8em 1.8em 0 0;border-top:1px solid rgba(167,139,250,.35);z-index:2147483647;transform:translateY(100%);transition:transform .32s cubic-bezier(.2,.8,.2,1);box-shadow:0 -10px 40px rgba(124,58,237,.35)}' +
      '#__g_sheet *{box-sizing:border-box}' +
      '.__g_hero{flex:none;padding:.7em 1.4em 1em}' +
      '.__g_handle{width:3em;height:.3em;background:rgba(196,181,253,.35);border-radius:1em;margin:0 auto .9em}' +
      '.__g_top{display:flex;align-items:center;justify-content:space-between}' +
      '.__g_ttl{font-size:1.5em;font-weight:700}' +
      '.__g_ttl span{display:block;font-size:.5em;font-weight:400;color:#a78bfa;margin-top:.2em;letter-spacing:.1em;text-transform:uppercase}' +
      '.__g_x{width:2.3em;height:2.3em;border-radius:50%;background:rgba(124,58,237,.25);border:1px solid rgba(167,139,250,.3);display:flex;align-items:center;justify-content:center;font-size:.85em;color:#ddd6fe}' +
      '.__g_score{display:flex;align-items:center;justify-content:space-between;margin-top:1.1em}' +
      '.__g_big{font-size:3.4em;font-weight:800;line-height:1;background:linear-gradient(135deg,#c4b5fd,#8b5cf6 55%,#c026d3);-webkit-background-clip:text;background-clip:text;color:transparent;-webkit-text-fill-color:transparent}' +
      '.__g_lbl{font-size:.7em;letter-spacing:.14em;color:#8b7bb5;margin-top:.5em;text-transform:uppercase}' +
      '.__g_chips{display:flex;flex-direction:column;gap:.5em}' +
      '.__g_chip{display:flex;align-items:center;justify-content:space-between;gap:1.2em;min-width:10em;background:rgba(124,58,237,.18);border:1px solid rgba(167,139,250,.2);border-radius:.9em;padding:.5em 1em}' +
      '.__g_chip b{font-size:1.2em;color:#f3e8ff}' +
      '.__g_chip em{font-style:normal;font-size:.72em;color:#a78bfa;letter-spacing:.1em;text-transform:uppercase}' +
      '.__g_prog{height:.4em;background:rgba(124,58,237,.25);border-radius:1em;margin-top:1.2em;overflow:hidden}' +
      '.__g_prog b{display:block;height:100%;background:linear-gradient(90deg,#7c3aed,#c026d3);border-radius:1em}' +
      '.__g_progt{font-size:.72em;color:#8b7bb5;margin-top:.5em}' +
      '.__g_note{display:flex;gap:.7em;align-items:flex-start;margin-top:.9em;padding:.7em .8em;background:rgba(124,58,237,.14);border:1px solid rgba(167,139,250,.2);border-radius:.9em;font-size:.72em;line-height:1.35;color:#b9a8e6}' +
      '.__g_note span{flex:none;width:1.6em;height:1.6em;border-radius:50%;background:rgba(167,139,250,.3);color:#ede9fe;font-weight:700;font-style:italic;text-align:center;line-height:1.6em;font-size:.95em}' +
      '.__g_tabs{flex:none;display:flex;gap:1.8em;padding:.2em 1.4em 0;border-bottom:1px solid rgba(167,139,250,.15)}' +
      '.__g_tab{padding:.7em 0;color:#8b7bb5;font-weight:500;border-bottom:2px solid transparent;margin-bottom:-1px}' +
      '.__g_tab.on{color:#f3e8ff;border-bottom-color:#8b5cf6}' +
      '.__g_list{flex:1;min-height:0;overflow-y:auto;padding:.2em 1.4em 1.8em}' +
      '.__g_term{display:flex;align-items:center;gap:.7em;font-size:.72em;font-weight:700;color:#a78bfa;text-transform:uppercase;letter-spacing:.12em;margin:1.4em 0 .6em}' +
      '.__g_term i{flex:1;height:1px;background:rgba(167,139,250,.18)}' +
      '.__g_row{display:flex;align-items:center;gap:.9em;background:rgba(124,58,237,.12);border:1px solid rgba(167,139,250,.14);border-radius:1em;padding:.7em .9em .7em .7em;margin-bottom:.45em}' +
      '.__g_acc{align-self:stretch;width:.25em;border-radius:1em;background:linear-gradient(180deg,#8b5cf6,#c026d3)}' +
      '.__g_info{flex:1;min-width:0}' +
      '.__g_name{font-size:1em;font-weight:500;color:#f5f3ff}' +
      '.__g_meta{font-size:.76em;color:#8b7bb5;margin-top:.2em}' +
      '.__g_g{font-size:1.5em;font-weight:800;min-width:2.4em;text-align:right;color:#ddd6fe}' +
      '.__g_d{color:#fb7185}' +
      '.__g_na{color:#4c3a78;font-weight:600}' +
      '.__g_dim .__g_name{color:#9d8cc9}' +
      '.__g_dim .__g_acc{background:#2e1c52}' +
      '.__g_empty{text-align:center;color:#8b7bb5;padding:2em}';
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
    try { AppLink.onSheetState(false); } catch (e) {}
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
    var pct = model.total ? Math.round(model.graded * 100 / model.total) : 0;
    sheet.innerHTML =
      '<div class="__g_hero">' +
        '<div class="__g_handle"></div>' +
        '<div class="__g_top"><div class="__g_ttl">My Grades<span>Curriculum Evaluation</span></div>' +
        '<div class="__g_x" id="__g_close">&#10005;</div></div>' +
        '<div class="__g_score">' +
          '<div><div class="__g_big">' + (model.gwa === null ? '-' : model.gwa.toFixed(2)) + '</div><div class="__g_lbl">GWA (approx.)</div></div>' +
          '<div class="__g_chips">' + chip('Units', model.units.toFixed(1)) + chip('Graded', model.graded + '/' + model.total) + '</div>' +
        '</div>' +
        '<div class="__g_prog"><b style="width:' + pct + '%"></b></div>' +
        '<div class="__g_progt">' + pct + '% of your curriculum is graded</div>' +
        '<div class="__g_note"><span>i</span><div>This GWA is computed from grades rounded to 2 decimals, the same rounded values the site sends. The official GWA on the page uses the exact unrounded grades, so it can differ slightly. Use the page for the official number.</div></div>' +
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

    try { AppLink.onSheetState(true); } catch (e) {}
    setTimeout(function () {
      back.style.opacity = '1';
      sheet.style.transform = 'translateY(0)';
    }, 30);
  };

  var TARGET = String.fromCharCode(99,117,114,114,101,118,97,108);

  function capture(text) {
    try {
      var d = JSON.parse(text);
      if (d && Array.isArray(d.Data)) AppLink.onGrades(text);
    } catch (e) {}
  }

  var origFetch = window.fetch;
  window.fetch = function () {
    var url = (arguments[0] && arguments[0].url) || String(arguments[0]);
    if (String(url).toLowerCase().indexOf('logout') !== -1) {
      try { AppLink.onLogout(); } catch (e) {}
    }
    var p = origFetch.apply(this, arguments);
    if (url.indexOf(TARGET) !== -1) {
      p.then(function (res) { return res.clone().text(); }).then(capture).catch(function () {});
    }
    return p;
  };

  var origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    if (String(url).toLowerCase().indexOf('logout') !== -1) {
      try { AppLink.onLogout(); } catch (e) {}
    }
    if (String(url).indexOf(TARGET) !== -1) {
      this.addEventListener('load', function () { capture(this.responseText); });
    }
    return origOpen.apply(this, arguments);
  };

  document.addEventListener('click', function (e) {
    var el = e.target;
    for (var i = 0; el && i < 4; i++, el = el.parentElement) {
      var t = (el.textContent || '').trim().toLowerCase().replace(/\s/g, '');
      if (t === 'logout' || t === 'signout') {
        try { AppLink.onLogout(); } catch (err) {}
        return;
      }
    }
  }, true);

  function findLeafByText(text) {
    var all = document.querySelectorAll('div,span');
    for (var i = 0; i < all.length; i++) {
      var el = all[i];
      if (el.children.length === 0 && el.textContent.trim() === text) return el;
    }
    return null;
  }

  function simulateClick(el) {
    var r = el.getBoundingClientRect();
    var x = r.left + r.width / 2, y = r.top + r.height / 2;
    ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click'].forEach(function (type) {
      var isPointer = type.indexOf('pointer') === 0;
      var opts = { bubbles: true, cancelable: true, clientX: x, clientY: y };
      var ev;
      try {
        ev = isPointer ? new PointerEvent(type, Object.assign({ pointerId: 1, isPrimary: true }, opts))
                        : new MouseEvent(type, opts);
      } catch (e) {
        ev = new MouseEvent(type, opts);
      }
      el.dispatchEvent(ev);
    });
  }

  function waitFor(text, timeoutMs, cb) {
    var start = Date.now();
    (function poll() {
      var el = findLeafByText(text);
      if (el) return cb(el);
      if (Date.now() - start > timeoutMs) return cb(null);
      setTimeout(poll, 150);
    })();
  }

  function autoOpenReport() {
    waitFor('Reports', 10000, function (reportsEl) {
      if (!reportsEl) {
        try { AppLink.onAutoClickFallback(); } catch (e) {}
        return;
      }
      simulateClick(reportsEl);
      setTimeout(function () {
        waitFor('Curriculum Evaluation', 8000, function (ceEl) {
          if (!ceEl) {
            try { AppLink.onAutoClickFallback(); } catch (e) {}
            return;
          }
          simulateClick(ceEl);
        });
      }, 500);
    });
  }

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
        try { AppLink.onLoginScreen(); } catch (e) {}
      }
    } else if (seenLogin) {
      misses++;
      if (misses >= 2) {
        seenLogin = false;
        misses = 0;
        try { AppLink.onLoggedIn(); } catch (e) {}
        setTimeout(autoOpenReport, 1200);
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

        window.statusBarColor = PURPLE_DEEP

        root = FrameLayout(this)
        root.setBackgroundColor(PURPLE_DEEP)
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
            }
        }

        web.loadUrl(SIAS_URL)
    }

    private fun buildFab() {
        fab = ImageView(this).apply {
            setImageResource(R.drawable.ic_atom)
            contentDescription = "Grades"
            setPadding(dp(14), dp(14), dp(14), dp(14))
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
                if (first) showBanner("Grades ready", "Tap the atom button to view them")
            }
        }

        @JavascriptInterface
        fun onLoggedIn() {
            runOnUiThread {
                showBanner("You're logged in", "Fetching your grades\u2026")
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
                if (lastJson == null) {
                    showBanner(
                        "Couldn't load automatically",
                        "Open Reports \u203A Curriculum Evaluation once to load your grades"
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

    override fun onDestroy() {
        handler.removeCallbacks(hideBannerTask)
        super.onDestroy()
    }
}
