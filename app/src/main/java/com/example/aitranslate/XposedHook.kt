package com.example.aitranslate

import android.widget.TextView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class XposedHook : IXposedHookLoadPackage {
    private val client = OkHttpClient()
    private val apiKey = "rYYp7sUr8uFom5y6ZZvWCRFdTOFTNgDM" // ВСТАВЬ СВОЙ КЛЮЧ ТУТ

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            TextView::class.java, "setText",
            CharSequence::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val text = param.args[0]?.toString() ?: return
                    if (text.length < 2 || text.contains("...")) return // Игнорим мусор

                    translateText(text) { translated ->
                        if (translated != null) {
                            param.args[0] = translated
                        }
                    }
                }
            }
        )
    }

    private fun translateText(text: String, callback: (String?) -> Unit) {
        val json = JSONObject().apply {
            put("model", "mistral-tiny")
            put("messages", JSONObject().apply { 
                put("role", "user")
                put("content", "Translate to Russian: $text") 
            })
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.mistral.ai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(null)
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                val translated = JSONObject(resBody).getJSONArray("choices")
                    .getJSONObject(0).getJSONObject("message").getString("content")
                callback(translated)
            }
        })
    }
}
