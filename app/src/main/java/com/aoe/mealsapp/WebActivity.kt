package com.aoe.mealsapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import com.aoe.mealsapp.settings.Language
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_web.*
import java.util.*

class WebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // copy of relevant settings necessary to recognize updates happening to settings between
    // onPause() and onResume()
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var language: Language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        initUi()
        readSettings()
        loadLoginPage(username, password)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onResume() called")

        checkAndHandleSettingsChange()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onBackPressed() called")

        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onOptionsItemSelected() called with: item = [$item]")

        handleMenuClick(item!!.itemId)

        return super.onOptionsItemSelected(item)
    }

    // region ### init UI widgets
    //

    private fun initUi() {

        setContentView(R.layout.activity_web)

        /* app bar */

        webFragment_toolbar_appBar.setLogo(R.drawable.logo)
        setSupportActionBar(webFragment_toolbar_appBar)

        /* widgets */

        webView = webFragment_webView_webApp

        swipeRefreshLayout = webFragment_swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onCreateView() called")

            refreshWebView()
        }
    }

    private fun refreshWebView() {
        webView.reload()
    }

    //
    // endregion

    // region ### settings
    //

    private fun readSettings() {
        val settings = Settings.getInstance(this)

        username = settings.username
        password = settings.password
        language = settings.language
    }

    private fun checkAndHandleSettingsChange() {
        val settings = Settings.getInstance(this)

        val usernameChanged = username != settings.username
        val passwordChanged = password != settings.password
        val languageChanged = language != settings.language

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
    // endregion

    private fun loadLoginPage(username: String, password: String) {

        val postData = ("_username=$username&_password=$password")
        webView.postUrl(PAGE_LOGIN, postData.toByteArray())
    }

    private fun handleMenuClick(itemId: Int) {
        when (itemId) {
        // for debugging: trigger immediate alarm
            R.id.mainMenu_alarm -> {
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
    }

    companion object {

        private const val TAG = "WebFragment"

        private const val PAGE_MAIN = BuildConfig.SERVER_URL
        private const val PAGE_LOGIN = BuildConfig.SERVER_URL + "/login"
        private const val PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions"
        private const val PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/language-switch"

        private const val HTTP_USER_AGENT = "MealsApp Android WebView"

        fun newInstance() = WebFragment()
    }
}
