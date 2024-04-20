package com.example.previsodotempokotlin.model

import com.google.gson.JsonObject

data class Main (
    val main: JsonObject?,
    val weather: List<Weather>?,
    val name: String?,
    val sys: JsonObject?
    )