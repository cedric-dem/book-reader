package com.fluffycactus.wordperwordreader

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {

    private val prefs = context.getSharedPreferences("my_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val jsonKey = "map_data"

    private fun loadMap(): HashMap<String, BookLocation> {
        val json = prefs.getString(jsonKey, null) ?: return HashMap()

        val type = object : TypeToken<HashMap<String, BookLocation>>() {}.type
        return gson.fromJson(json, type) ?: HashMap()
    }

    private fun saveMap(map: HashMap<String, BookLocation>) {
        val json = gson.toJson(map)
        prefs.edit().putString(jsonKey, json).apply()
    }

    fun saveValue(key: String, v1: Int, v2: Int) {
        val map = loadMap()
        map[key] = BookLocation(v1, v2)
        saveMap(map)
    }

    fun getValue(key: String): BookLocation? {
        val map = loadMap()
        return map[key]
    }
}