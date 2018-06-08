package com.aoe.mealsapp.rest

interface MealsApi {

    fun requestLogin(
            username: String,
            password: String,
            callback: (loginResponse: LoginResponse?) -> Unit)

    fun requestCurrentWeek(
            token: String,
            callback: (currentWeekResponse: CurrentWeekResponse?) -> Unit)
}