package com.naturalmotion.customstreetrac.logic.anim

import android.content.Context

interface PlayerParamProvider {
    suspend fun getParam(context: Context): Pair<String, String>?
}