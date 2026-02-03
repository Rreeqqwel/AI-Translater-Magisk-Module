package com.example.aitranslate

import android.widget.TextView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class XposedHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            TextView::class.java,
            "setText",
            CharSequence::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val text = param.args[0] as? CharSequence ?: return
                    XposedBridge.log("AI Translate Hook: $text")
                }
            }
        )
    }
}
