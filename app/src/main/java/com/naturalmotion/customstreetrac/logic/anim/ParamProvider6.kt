package com.naturalmotion.customstreetrac.logic.anim

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParamProvider6 : PlayerParamProvider {

    override suspend fun getParam(context: Context): Pair<String, String> = withContext(Dispatchers.IO) {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        val value6 = pi.firstInstallTime.toString()
        "df3d72wqqybe" to value6
    }
}