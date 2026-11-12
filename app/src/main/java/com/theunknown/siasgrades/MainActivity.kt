package com.theunknown.siasgrades

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

private const val SIAS_URL = "https://icloudph.com/prmsu-main/sias/"

// Runs on every page load. Hooks fetch + XHR, waits for a URL containing "curreval",
// then draws the grades on top of the page.
// Real grade = Previous[last].Grade / 100, units = Units / 100.
// The top-level "Grade" field is a scrambled number, so it is ignored.
private val HOOK_JS = """
(function () {
  if (window.__curevalHooked) return;
  window.__curevalHooked = true;

  function esc(s) {
    return String(s == null ? '' : s).replace(/&/g, '&amp;').replace(/</g, '&lt;');
  }

  function show(text) {
    var data;
    try { data = JSON.parse(text); } catch (e) { return; }
    var list = data && data.Data;
    if (!Array.isArray(list)) return;

    var pts = 0, units = 0, lastKey = '';
    var body = list.map(function (s) {
      var u = (s.Units || 0) / 100;
      var prev = (s.Previous && s.Previous.length) ? s.Previous[s.Previous.length - 1] : null;
      var g = prev ? prev.Grade / 100 : null;
      if (g !== null && g > 0) { pts += g * u; units += u; }

      var key = 'Year ' + s.Year + ' - Term ' + s.Term;
      var head = '';
      if (key !== lastKey) {
        lastKey = key;
        head = '<tr><td colspan="4" style="background:#eee;font-weight:bold;padding:4px">' + key + '</td></tr>';
      }
      return head + '<tr>' +
        '<td style="border:1px solid #999;padding:3px">' + esc(s.SubjCode) + '</td>' +
        '<td style="border:1px solid #999;padding:3px">' + esc(s.SubjName) + '</td>' +
        '<td style="border:1px solid #999;padding:3px;text-align:center">' + u.toFixed(1) + '</td>' +
        '<td style="border:1px solid #999;padding:3px;text-align:center;font-weight:bold">' +
          (g !== null ? g.toFixed(2) : '-') + '</td></tr>';
    }).join('');

    var gwa = units > 0 ? (pts / units).toFixed(2) : '-';

    var html = '<div style="font-weight:bold;font-size:15px;margin-bottom:6px">My Grades</div>' +
      '<table style="border-collapse:collapse;font-size:12px;width:100%">' +
      '<tr><th style="border:1px solid #999;padding:3px">Code</th>' +
      '<th style="border:1px solid #999;padding:3px">Subject</th>' +
      '<th style="border:1px solid #999;padding:3px">Units</th>' +
      '<th style="border:1px solid #999;padding:3px">Grade</th></tr>' + body + '</table>' +
      '<div style="margin-top:8px;font-weight:bold">GWA (graded subjects): ' + gwa + '</div>' +
      '<button style="margin-top:8px;padding:6px 14px" onclick="this.parentNode.remove()">Close</button>';

    var box = document.getElementById('__grades_box');
    if (!box) {
      box = document.createElement('div');
      box.id = '__grades_box';
      box.style.cssText = 'position:fixed;top:0;left:0;right:0;max-height:85%;overflow:auto;' +
        'background:#fff;color:#000;z-index:2147483647;padding:10px;box-shadow:0 2px 10px #0006';
      document.body.appendChild(box);
    }
    box.innerHTML = html;
  }

  var origFetch = window.fetch;
  window.fetch = function () {
    var url = (arguments[0] && arguments[0].url) || String(arguments[0]);
    var p = origFetch.apply(this, arguments);
    if (url.indexOf('curreval') !== -1) {
      p.then(function (res) { return res.clone().text(); }).then(show).catch(function () {});
    }
    return p;
  };

  var origOpen = XMLHttpRequest.prototype.open;
  XMLHttpRequest.prototype.open = function (method, url) {
    if (String(url).indexOf('curreval') !== -1) {
      this.addEventListener('load', function () { show(this.responseText); });
    }
    return origOpen.apply(this, arguments);
  };
})();
""".trimIndent()

class MainActivity : Activity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
