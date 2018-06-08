package com.aoe.mealsapp.rest

import com.squareup.moshi.Json

data class LoginResponse (@Json(name = "access_token") val accessToken: String?)