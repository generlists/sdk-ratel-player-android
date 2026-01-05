package com.sean.ratel.player.demo.ui.download

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccompanistBrowserScreen(initialUrl: String) {
    val context = LocalContext.current

    // 1. 웹뷰 상태 관리 (Accompanist 제공)
    val state = rememberWebViewState(url = initialUrl)
    val navigator = rememberWebViewNavigator()

    // 2. 브릿지 설정 (익스텐션 파싱 결과 수신용)
    val chromeClient = object : AccompanistWebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Video Downloader", fontSize = 16.sp) },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 주소창이나 로딩 바 등을 여기에 추가 가능
            if (state.isLoading) {
                LinearProgressIndicator(
                    progress = state.loadingState.let { if (it is LoadingState.Loading) it.progress else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Accompanist WebView 컴포넌트
            WebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
                onCreated = { v ->
                    // 웹뷰 초기 설정
                    setupWebViewSettings(v, context)
                },
                chromeClient = chromeClient,
                client = object : AccompanistWebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        // 페이지 로딩 후 우리가 만든 FB 감지 스크립트 주입
                        view.let { injectFacebookLogic(it) }
                    }
                }
            )
        }
    }

    // 시스템 백버튼 처리
    BackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }
}

private fun setupWebViewSettings(webView: WebView, context: Context) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    // 네이티브 브릿지 등록
    webView.addJavascriptInterface(object {
        @JavascriptInterface
        fun onVideoDetected(videoId: String, postUrl: String, sdUrl: String, hdUrl: String, audioUrl: String) {
            // 네이티브 영역: 다운로드 로직 호출
            Log.d("FB_DEBUG", "--------------------------")
            Log.d("FB_DEBUG", "감지된 영상 ID: $videoId")
            Log.d("FB_DEBUG", "분석 페이지 URL: $postUrl")
            Log.d("FB_DEBUG", "✅ SD 재생 주소: $sdUrl")
            Log.d("FB_DEBUG", "✅ HD 재생 주소: $hdUrl")
            Log.d("FB_DEBUG", "✅ 오디오 주소: $audioUrl")
            Log.d("FB_DEBUG", "--------------------------")
        }
    }, "AndroidBridge")
}

private fun injectFacebookLogic(webView: WebView) {
    val jsCode = """
    (function () {

      /* =====================
         Utils
      ===================== */
      function decode(str) {
        if (!str) return "";
        return str
          .replace(/\\u0026/g, "&")
          .replace(/\\\//g, "/")
          .replace(/&amp;/g, "&");
      }

      /* =====================
         VIDEO ID 추출 (feed)
         ✔ innerHTML 금지
         ✔ link 기반
      ===================== */
      function extractVideoId(container) {
        const links = container.querySelectorAll("a[href]");
        for (const a of links) {
          const h = a.href;

          // watch?v=
          let m = h.match(/[?&]v=(\d+)/);
          if (m) return m[1];

          // /videos/{id}
          m = h.match(/\/videos\/(\d+)/);
          if (m) return m[1];

          // /reel/{id}
          m = h.match(/\/reel\/(\d+)/);
          if (m) return m[1];
        }
        return null;
      }

      /* =====================
         WATCH PAGE 파싱
         (yt-dlp 동일 개념)
      ===================== */
      function parseWatchPage() {
        const scripts = Array.from(document.querySelectorAll("script"))
          .map(s => s.textContent || "")
          .join("\\n");

        const urls = [];
        const re = /"base_url":"(https:[^"]+)"/g;
        let m;
        while ((m = re.exec(scripts)) !== null) {
          urls.push(decode(m[1]));
        }

        let audio = "";
        let sd = "";
        let hd = "";

        urls.forEach(u => {
          if (u.includes("audio") && !audio) {
            audio = u;
          } else if (u.includes("mp4")) {
            if ((u.includes("720") || u.includes("1080")) && !hd) {
              hd = u;
            } else if (!sd) {
              sd = u;
            }
          }
        });

        const idMatch = location.href.match(/[?&]v=(\d+)/);
        const videoId = idMatch ? idMatch[1] : null;

        window.AndroidBridge.onVideoDetected(
          "FB_" + (videoId || "unknown"),
          location.href,
          sd,
          hd,
          audio
        );
      }

      /* =====================
         FEED 버튼 삽입
      ===================== */
      function attachButtons() {
        const videos = document.querySelectorAll("video:not(.fb-btn-attached)");
        videos.forEach(v => {
          v.classList.add("fb-btn-attached");

          const container =
            v.closest('[role="article"]') || v.parentElement;
          if (!container) return;

          container.style.position = "relative";

          const btn = document.createElement("div");
          btn.innerHTML = "🎬";
          btn.style.cssText = `
            position:absolute;
            top:10px; right:10px;
            z-index:9999;
            width:44px; height:44px;
            background:white;
            border-radius:50%;
            border:2px solid #1877f2;
            display:flex;
            align-items:center;
            justify-content:center;
            font-size:22px;
            cursor:pointer;
            box-shadow:0 2px 6px rgba(0,0,0,.4);
          `;

          btn.onclick = e => {
            e.preventDefault();
            e.stopPropagation();

            const videoId = extractVideoId(container);
            if (!videoId) {
              alert("video id not found");
              return;
            }

            // ✅ 반드시 watch 페이지로 이동
            location.href = "https://www.facebook.com/watch/?v=" + videoId;
          };

          container.appendChild(btn);
        });
      }

      /* =====================
         MODE 분기
      ===================== */
      if (location.href.includes("/watch")) {
        setTimeout(parseWatchPage, 1500);
      } else {
        const observer = new MutationObserver(attachButtons);
        observer.observe(document.body, { childList: true, subtree: true });
        attachButtons();
      }

    })();
    """.trimIndent()

    webView.evaluateJavascript(jsCode, null)
}

