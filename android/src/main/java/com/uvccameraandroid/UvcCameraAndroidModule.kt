package com.uvccameraandroid

import com.facebook.react.bridge.*
import com.facebook.react.uimanager.UIManagerModule


class UvcCameraAndroidModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun capture(viewTag: Int, promise: Promise?) {
    val context: ReactContext = reactApplicationContext
    val uiManager = context.getNativeModule(UIManagerModule::class.java)
    val runnable = Runnable {
      val view = uiManager!!.resolveView(viewTag) as UvcCameraView
      view.captureImage(promise!!)
    }
    context.runOnUiQueueThread(runnable)
  }

  companion object {
    const val NAME = "UvcCameraAndroid"
  }
}
