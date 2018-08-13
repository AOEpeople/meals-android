package com.aoe.mealsapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import com.aoe.mealsapp.settings.Language
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_web.*
import java.util.*
import java.util.regex.Pattern

class WebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // credentials used for last login
    private lateinit var lastUsername: String
    private lateinit var lastPassword: String

    // language returned from last load
    // defaults to GERMAN because the page loads in German by default
    private lateinit var lastLanguage: Language

    // true from onRestoreInstanceState() until first onPageFinished() call
    var restoring = false

    // remember an error in onReceivedError() so that the error page is left
    // untouched in onPageFinished(). Reset it in onPageStarted()
    var errorLoadingPage = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        setContentView(R.layout.activity_web)

        webActivity_toolbar_appBar.setLogo(R.drawable.logo)
        setSupportActionBar(webActivity_toolbar_appBar)

        /* SwipeRefreshLayout & WebView */

        swipeRefreshLayout = webActivity_swipeRefreshLayout_webView
        swipeRefreshLayout.setOnRefreshListener {
            refreshWebView()
        }

        webView = webActivity_webView_webApp
        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = HTTP_USER_AGENT
        webView.webViewClient = MyWebViewClient()

        /* */

        val settings = Settings.getInstance(this)

        lastUsername = settings.username
        lastPassword = settings.password
        lastLanguage = settings.language
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onSaveInstanceState() called with: outState = [$outState]")

        webView.saveState(outState)

        outState.putString(STATE_LAST_USERNAME, lastUsername)
        outState.putString(STATE_LAST_PASSWORD, lastPassword)
        outState.putSerializable(STATE_LAST_LANGUAGE, lastLanguage)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onRestoreInstanceState() called with: savedInstanceState = [$savedInstanceState]")

        restoring = true

        webView.restoreState(savedInstanceState)

        lastUsername = savedInstanceState.getString(STATE_LAST_USERNAME)
        lastPassword = savedInstanceState.getString(STATE_LAST_PASSWORD)
        lastLanguage = savedInstanceState.getSerializable(STATE_LAST_LANGUAGE) as Language
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onResume() called")

        /* after return from login / settings screen: check for changes */

        val settings = Settings.getInstance(this)

        val currentUsername = settings.username
        val currentPassword = settings.password
        val currentLanguage = settings.language

        if (lastUsername != currentUsername || lastPassword != currentPassword) {

            /* credentials changed ? save them, re-login */

            lastUsername = currentUsername
            lastPassword = currentPassword

            loadLoginPage(lastUsername!!, lastPassword!!)

        } else if (lastLanguage != currentLanguage) {

            /* language changed ? save it, switch language */

            lastLanguage = currentLanguage

            switchLanguage(webView.url)

        } else if (webView.url !in listOf(PAGE_MAIN, PAGE_TRANSACTIONS)) {

            /* invalid page ? re-login */

            loadLoginPage(lastUsername!!, lastPassword!!)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onBackPressed() called")

        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onOptionsItemSelected() called with: item = [$item]")

        when (item!!.itemId) {
            // debugging: manually trigger alarm
            R.id.mainMenu_alarm ->
                triggerAlarm()

            R.id.mainMenu_account ->
                webView.loadUrl(PAGE_TRANSACTIONS)

            R.id.mainMenu_settings ->
                startActivity(Intent(this, SettingsActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    // region ### load pages
    //

    private fun loadLoginPage(username: String, password: String) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "loadLoginPage() called with: username = [$username], password = [$password]")

        webView.clearHistory()

        val postData = ("_username=$username&_password=$password")
        webView.postUrl(PAGE_LOGIN, postData.toByteArray())
    }

    private fun switchLanguage(targetUrl: String) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "switchLanguage() called with: targetUrl = [$targetUrl]")

        webView.loadUrl(PAGE_LANGUAGE_SWITCH, mutableMapOf("Referer" to targetUrl))
    }

    private fun refreshWebView() {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "refreshWebView() called")

        webView.reload()
    }

    //
    // endregion

    // region ### debug
    //

    private fun triggerAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = PendingIntent.getBroadcast(this, 0,
                Intent(this, AlarmReceiver::class.java), 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().timeInMillis, alarmIntent)
    }

    //
    // endregion

    inner class MyWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onPageStarted() called with: view = [$view], url = [$url], favicon = [$favicon]")

            swipeRefreshLayout.isRefreshing = true

            errorLoadingPage = false
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onPageFinished() called with: view = [$view], url = [$url]")

            if (errorLoadingPage)
                return

            swipeRefreshLayout.isRefreshing = false

            if (restoring) {
                restoring = false
            } else {

                /* read language from cookies & save it */

                // TODO check
                var cookieString = CookieManager.getInstance().getCookie(PAGE_MAIN)
                if (cookieString != null) {
                    cookieString = cookieString.replace(" ", "")
                    val cookies = Pattern.compile(";").split(cookieString)
                    for (cookie in cookies) {
                        val cookieParts = cookie.split("=")
                        if (cookieParts[0] == "locale") {
                            lastLanguage = when (cookieParts[1]) {
                                "de" -> Language.GERMAN
                                "en" -> Language.ENGLISH
                                else -> Language.ENGLISH
                            }
                        }
                    }
                } else {
                    lastLanguage = Language.ENGLISH
                }

                /* wrong page (invalid credentials) ? show LoginActivity */

                if (webView.url !in listOf(PAGE_MAIN, PAGE_TRANSACTIONS)) {
                    startActivity(Intent(this@WebActivity, LoginActivity::class.java))
                } else {

                    /* valid page but wrong langauge ? switch language */

                    val currentLanguage = Settings.getInstance(this@WebActivity).language

                    if (lastLanguage != currentLanguage) {

                        /* language changed ? save it, switch language */

                        lastLanguage = currentLanguage

                        switchLanguage(webView.url)
                    }
                }
            }
        }


        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onReceivedError() called with: view = [$view], request = [$request], error = [$error]")

            swipeRefreshLayout.isRefreshing = false

            if (restoring) {
                restoring = false
            }

            errorLoadingPage = true
        }
    }

    private companion object {

        private const val TAG = "WebActivity"

        private const val PAGE_MAIN = BuildConfig.SERVER_URL + "/"
        private const val PAGE_LOGIN = BuildConfig.SERVER_URL + "/login"
        private const val PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions"
        private const val PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/language-switch"

        private const val HTTP_USER_AGENT = "MealsApp Android WebView"

        private const val STATE_LAST_USERNAME = "STATE_LAST_USERNAME"
        private const val STATE_LAST_PASSWORD = "STATE_LAST_PASSWORD"
        private const val STATE_LAST_LANGUAGE = "STATE_LAST_LANGUAGE"
    }
}
