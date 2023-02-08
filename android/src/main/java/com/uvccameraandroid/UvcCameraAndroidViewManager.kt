package com.uvccameraandroid

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager


class UvcCameraAndroidViewManager : ViewGroupManager<UvcCameraView?>() {
  override fun getName(): String {
    return TAG
  }

  override fun createViewInstance(reactContext: ThemedReactContext): UvcCameraView {
    return UvcCameraView(reactContext)
  }

  override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any>? {
    val builder = MapBuilder.builder<String, Any>()
    builder.put("onCameraConnected", MapBuilder.of("registrationName", "onCameraConnected"))
    builder.put("onCameraDisconnected", MapBuilder.of("registrationName", "onCameraDisconnected"))
    return builder.build()
  }

  companion object {
    private const val TAG = "UvcCameraAndroidViewManager"
  }
}
