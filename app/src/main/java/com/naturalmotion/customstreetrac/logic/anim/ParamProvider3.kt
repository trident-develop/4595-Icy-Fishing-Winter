package com.naturalmotion.customstreetrac.logic.anim

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParamProvider3 : PlayerParamProvider {

    override suspend fun getParam(context: Context): Pair<String, String>? = withContext(Dispatchers.IO) {
        val value3 = runProbe(context).toString()
//        val value3 = "0"
        "hxitf8dlfwjkmq18hk2" to value3
    }

    fun runProbe(context: Context): Int {
        fun res(): ContentResolver = context.contentResolver
        fun key(): String = Settings.Global.ADB_ENABLED
        fun defaultValue(): Int = 0
        fun read(resolver: ContentResolver, key: String, def: Int): Int {
            return Settings.Global.getInt(resolver, key, def)
        }
        return try {
            val raw = read(res(), key(), defaultValue())

            if (raw == 0) {
                0
            } else {
                throw IllegalStateException("Probe enabled")
            }

        } catch (e: Exception) {
            1
        }
    }
}