package com.example.smartee.ui.signup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadRegionData(context: Context): Map<String, List<String>> {
    val json = context.assets.open("region.json")
        .bufferedReader()
        .use { it.readText() }

    val type = object : TypeToken<Map<String, List<String>>>() {}.type
    return Gson().fromJson(json, type)
}
