package com.aoe.mealsapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.aoe.mealsapp.settings.Language
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_web.*
import java.util.*

class WebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // lastly used settings to load page
    // used to detect settings change between onPause() and onResume()
    // e.g. because settings were changed in SettingsActivity
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var language: Language

    //
    // onCreate(), onCreateOptionsMenu()
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        initUi()
        readSettings()

        if (savedInstanceState == null) {
            loadLoginPage(username, password)
        }
    }

    private fun initUi() {

        setContentView(R.layout.activity_web)

        /* app bar */

        webActivity_toolbar_appBar.setLogo(R.drawable.logo)
        setSupportActionBar(webActivity_toolbar_appBar)

        /* widgets */

        webView = webActivity_webView_webApp

        swipeRefreshLayout = webActivity_swipeRefreshLayout_webView
        swipeRefreshLayout.setOnRefreshListener {
            refreshWebView()
        }

        /* */

        initWebView()
    }

    /**
     * - enable JavaScript
     * - set custom HTTP user agent
     * - set onPageFinished handler that notifies the parent activity if the credentials are wrong
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {

        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = HTTP_USER_AGENT

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, Thread.currentThread().name + " ### " +
                        "onPageStarted() called with: view = [$view], url = [$url], favicon = [$favicon]")

                swipeRefreshLayout.isRefreshing = true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                Log.d(TAG, Thread.currentThread().name + " ### " +
                        "onPageFinished() called with: view = [$view], url = [$url]")

                swipeRefreshLayout.isRefreshing = false

                /* unexpected page (e.g. bad credentials) ? notify Activity */

                // remove trailing slash from URL
                val normalizedUrl = url.replace("/$".toRegex(), "")

                if (normalizedUrl !in listOf(PAGE_MAIN, PAGE_TRANSACTIONS)) {
                    startActivity(Intent(this@WebActivity, LoginActivity::class.java))
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.d(TAG, Thread.currentThread().name + " ### " +
                        "onReceivedError() called with: view = [$view], request = [$request], error = [$error]")

                swipeRefreshLayout.isRefreshing = false

                Toast.makeText(this@WebActivity, "ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readSettings() {
        val settings = Settings.getInstance(this)

        username = settings.username
        password = settings.password
        language = settings.language
    }

    private fun loadLoginPage(username: String, password: String) {

        val postData = ("_username=$username&_password=$password")
        webView.postUrl(PAGE_LOGIN, postData.toByteArray())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreateOptionsMenu() called with: menu = [$menu]")

        menuInflater.inflate(R.menu.menu_main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    //
    // onSaveInstanceState(), onRestoreInstanceState()
    //

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onSaveInstanceState() called with: outState = [$outState], outPersistentState = [$outPersistentState]")

        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onRestoreInstanceState() called with: savedInstanceState = [$savedInstanceState]")

        webView.restoreState(savedInstanceState)
    }

    //
    // onResume(): check for settings change
    //

    override fun onResume() {
        super.onResume()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onResume() called")

        /* check and handle settings change */

        val settings = Settings.getInstance(this)

        val usernameChanged = username != settings.username
        val passwordChanged = password != settings.password
        val languageChanged = language != settings.language

        username = settings.username
        password = settings.password
        language = settings.language

        if ((usernameChanged || passwordChanged) && languageChanged) {
            switchLanguage(targetUrl = PAGE_LOGIN)

        } else if (usernameChanged || passwordChanged) {
            loadLoginPage(username, password)

        } else if (languageChanged) {
            switchLanguage(webView.url)
        }
    }

    private fun switchLanguage(targetUrl: String) {
        webView.loadUrl(PAGE_LANGUAGE_SWITCH, mapOf("Referer" to targetUrl))
    }

    //
    // onBackPressed()
    //

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onBackPressed() called")

        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    //
    // onOptionsItemSelected()
    //

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onOptionsItemSelected() called with: item = [$item]")

        when (item!!.itemId) {
            R.id.mainMenu_alarm -> {
                /* for debugging: trigger immediate alarm */

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val alarmIntent = PendingIntent.getBroadcast(this, 0,
                        Intent(this, AlarmReceiver::class.java), 0)
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        Calendar.getInstance().timeInMillis, alarmIntent)
            }

            R.id.mainMenu_account -> {
                webView.loadUrl(PAGE_TRANSACTIONS)
            }

            R.id.mainMenu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }

            R.id.mainMenu_refresh -> {
                swipeRefreshLayout.isRefreshing = true
                refreshWebView()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //
    // common
    //

    private fun refreshWebView() {
        webView.reload()
    }

    //
    //
    //

    private companion object {

        private const val TAG = "WebActivity"

        private const val PAGE_MAIN = BuildConfig.SERVER_URL
        private const val PAGE_LOGIN = BuildConfig.SERVER_URL + "/login"
        private const val PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions"
        private const val PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/language-switch"

        private const val HTTP_USER_AGENT = "MealsApp Android WebView"
    }
}
