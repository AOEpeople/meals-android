package com.aoe.mealsapp.rest

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.aoe.mealsapp.BuildConfig
import com.aoe.mealsapp.util.SingletonHolder

class MealsApiImpl private constructor(context: Context)
    : MealsApi {

    private val volleyRequestQueue = Volley.newRequestQueue(context)

    override fun requestLogin(
            username: String,
            password: String,
            callback: (loginResponse: LoginResponse?) -> Unit
    ) {
        volleyRequestQueue.add(object : MoshiRequest<LoginResponse>(
                Method.POST,
                BuildConfig.SERVER_URL + "/oauth/v2/token",
                LoginResponse::class.java,
                null,

                Response.Listener {
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestLogin() called")

                    callback.invoke(it)
                },

                Response.ErrorListener {
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestLogin() called")

                    callback.invoke(null)
                }
        ) {
            override fun getParams() = mapOf(
                    "grant_type" to "password",
                    "client_id" to OAUTH_CLIENT_ID,
                    "client_secret" to OAUTH_CLIENT_SECRET,
                    "username" to username,
                    "password" to password)
        })
    }

    override fun requestCurrentWeek(
            token: String,
            callback: (currentWeekResponse: CurrentWeekResponse?) -> Unit
    ) {
        volleyRequestQueue.add(object: MoshiRequest<CurrentWeekResponse>(
                Method.GET,
                BuildConfig.SERVER_URL + "/rest/v1/week/active",
                CurrentWeekResponse::class.java,
                null,

                Response.Listener {
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestCurrentWeek() called")

                    callback(it)
                },

                Response.ErrorListener {
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestCurrentWeek() called")

                    callback(null)
                }
        ) {
            override fun getHeaders() = mutableMapOf("Authorization" to "Bearer $token")
        })
    }

    companion object : SingletonHolder<MealsApiImpl, Context>(::MealsApiImpl) {

        private const val TAG = "MealsApiImpl"

        private const val OAUTH_CLIENT_ID = BuildConfig.OAUTH_CLIENT_ID
        private const val OAUTH_CLIENT_SECRET = BuildConfig.OAUTH_CLIENT_SECRET
    }
}