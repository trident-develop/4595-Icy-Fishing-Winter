package com.naturalmotion.customstreetrac.logic.anim

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class ParamProvider4 : PlayerParamProvider {

    override suspend fun getParam(context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        val value4 = getDeviceString()
        "hildx5tatho51t0rco" to value4
    }

    fun getDeviceString(): String {
        return try {
            buildString {
                val brand = Build.BRAND.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                append(brand).append(' ').append(Build.MODEL)
            }
        } catch (_: Throwable) {
            "unknown_device"
        }
    }
}