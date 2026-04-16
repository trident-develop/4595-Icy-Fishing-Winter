package com.naturalmotion.customstreetrac.logic.anim

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParamProvider2 : PlayerParamProvider {
    override suspend fun getParam(context: Context): Pair<String, String>? = withContext(Dispatchers.IO) {
        val value2 = getGadid(context)
        "1dlswmjzxqzr4" to value2
    }

    private val EMPTY_GADID = "00000000-0000-0000-0000-000000000000"

    private fun AdvertisingIdClient.Info?.safeId(): String {
        if (this == null) return EMPTY_GADID
        if (isLimitAdTrackingEnabled) return EMPTY_GADID
        return id?.takeIf { it.isNotBlank() } ?: EMPTY_GADID
    }

    suspend fun getGadid(context: Context): String =
        withContext(Dispatchers.IO) {
            runCatching {
                AdvertisingIdClient.getAdvertisingIdInfo(context)
            }.getOrNull().safeId()
        }
}