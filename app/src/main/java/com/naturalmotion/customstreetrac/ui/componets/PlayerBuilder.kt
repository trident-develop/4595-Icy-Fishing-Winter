package com.naturalmotion.customstreetrac.ui.componets

import android.content.Context
import androidx.core.net.toUri
import com.naturalmotion.customstreetrac.logic.anim.PlayerParamProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerBuilder(
    private val baseName: String,
    private val providers: List<PlayerParamProvider>,
    private val context: Context
) {
    suspend fun build(): String = withContext(Dispatchers.IO) {
        var uri = baseName.toUri()

        providers.forEachIndexed { index, provider ->

            val pair = provider.getParam(context)

//            Log.d("MYTAG", "Step $index → ${provider::class.java.simpleName}")

            if (pair != null) {

//                Log.d("MYTAG", "Adding: ${pair.first} = ${pair.second}")

                uri = uri.buildUpon()
                    .appendQueryParameter(pair.first, pair.second)
                    .build()

//                Log.d("MYTAG", "Result URL: $uri")
            } else {
//                Log.d("MYTAG", "Skipped")
            }
        }

        uri.toString()
    }
}