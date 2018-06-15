package com.aoe.mealsapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import com.aoe.mealsapp.settings.Language
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.settings.SettingsActivity
import java.util.*

/**
 * Must be attached to an AppCompatActivity.
 *
 * Reads settings (username, password, language) and checks for updates when resuming.
 */
class WebFragment : Fragment(), OnBackPressedListener {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // copy of relevant settings
    // necessary to recognize updates happening to settings between onPause() and onResume()
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var language: Language

    // region ### init UI widgets
    //

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onCreateView() called with: inflater = [$inflater], container = [$container]"
                + ", savedInstanceState = [$savedInstanceState]")

        val rootView = inflater.inflate(R.layout.fragment_web, container, false)

        setHasOptionsMenu(true)

        /* set up app bar */

        val toolbar = rootView.findViewById<Toolbar>(R.id.webFragment_toolbar_appBar)
        toolbar.setLogo(R.drawable.logo)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        /* init widgets */

        webView = rootView.findViewById(R.id.webFragment_webView_webApp)
        progressBar = rootView.findViewById(R.id.webFragment_progressBar_webApp)

        swipeRefreshLayout = rootView.findViewById(R.id.webFragment_swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onCreateView() called")

            webView.reload()
        }

        initWebView()

        /* read settings */

        // Fragment has been attached when onCreateView() is called
        val settings = Settings.getInstance(context!!)

        username = settings.username
        password = settings.password
        language = settings.language

        /* */

        loadLoginPage(username, password)

        return rootView
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

                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                Log.d(TAG, Thread.currentThread().name + " ### "
                        + "onPageFinished() called with: view = [$view], url = [$url]")

                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false

                /* unexpected page (e.g. bad credentials) ? notify Activity */

                // ignore if activity has been destroyed
                if (onFragmentInteractionListener != null) {

                    // remove trailing slash from URL
                    val normalizedUrl = url.replace("/$".toRegex(), "")

                    if (normalizedUrl !in listOf(PAGE_MAIN, PAGE_TRANSACTIONS)) {
                        onFragmentInteractionListener!!.onLoginFailed()
                    }
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.d(TAG, Thread.currentThread().name + " ### " +
                        "onReceivedError() called with: view = [$view], request = [$request], error = [$error]")

                progressBar.visibility = View.GONE

                Toast.makeText(context, "ERROR", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadLoginPage(username: String, password: String) {

        val postData = ("_username=$username&_password=$password")
        webView.postUrl(PAGE_LOGIN, postData.toByteArray())
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onCreateOptionsMenu() called with: menu = [$menu], inflater = [$inflater]")

        // should not be null (?)
        inflater!!.inflate(R.menu.menu_main, menu)
    }

    //
    // endregion

    // region ### fragment -> activity communication
    //

    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onAttach() called with: context = [$context]")

        if (context is OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onDetach() called")

        onFragmentInteractionListener = null
    }

    /**
     * To be implemented by the containing activity so that the fragment can communicate with it.
     */
    internal interface OnFragmentInteractionListener {
        fun onLoginFailed()
    }

    //
    // endregion

    // region ### app bar menu
    //

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onOptionsItemSelected() called with: item = [$item]")

        // should not be null (?)
        val optionsItemId = item!!.itemId

        when (optionsItemId) {
            R.id.mainMenu_alarm -> {

                /* for debugging: trigger immediate alarm */

                // Fragment has been attached when onOptionsItemSelected() is called
                val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val alarmIntent = PendingIntent.getBroadcast(context, 0,
                        Intent(context, AlarmReceiver::class.java), 0)
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        Calendar.getInstance().timeInMillis, alarmIntent)
            }

            R.id.mainMenu_account -> {
                webView.loadUrl(PAGE_TRANSACTIONS)
                return true
            }

            R.id.mainMenu_settings -> {
                startActivity(Intent(context, SettingsActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //
    // endregion

    // region ### onResume(): check settings
    //

    /**
     * Check for any changes made to the settings while this fragment was paused.
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onResume() called")

        // Fragment has been attached to Activity when onResume() is called
        val settings = Settings.getInstance(context!!)

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

    // region ### onBackPressed(): back in browser history / close Fragment
    //

    override fun onBackPressed(): Boolean {
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onBackPressed() called")

        return if (webView.canGoBack()) {
            webView.goBack()

            true
        } else {
            false
        }
    }

    //
    // endregion

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