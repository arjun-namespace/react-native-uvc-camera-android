package com.uvccameraandroid

import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.jiangdg.ausbc.CameraClient
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.callback.IDeviceStatusCallBack
import com.jiangdg.ausbc.camera.CameraUvcStrategy
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.IAspectRatio

class UvcCameraView(context: ThemedReactContext) : FrameLayout(context), IDeviceStatusCallBack {
  private val mContext: ThemedReactContext
  private val mCameraClient: CameraClient?

  init {
    LayoutInflater.from(context).inflate(R.layout.fragment_uvc_camera, this, true)
    mContext = context
    mCameraClient = cameraClient
    val cameraView = AspectRatioTextureView(context)
    val cameraViewContainer: ViewGroup = findViewById(R.id.fragment_container)
    cameraViewContainer.removeAllViews()
    cameraViewContainer.addView(
      cameraView, LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT,
        Gravity.CENTER
      )
    )
    handleTextureView(cameraView)
  }

  private fun handleTextureView(textureView: AspectRatioTextureView) {
    textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
      override fun onSurfaceTextureAvailable(
        surface: SurfaceTexture,
        width: Int,
        height: Int
      ) {
        Log.i(TAG, "handleTextureView onSurfaceTextureAvailable")
        openCamera(textureView)
      }

      override fun onSurfaceTextureSizeChanged(
        surface: SurfaceTexture,
        width: Int,
        height: Int
      ) {
        Log.i(TAG, "handleTextureView onSurfaceTextureSizeChanged")
        surfaceSizeChanged(width, height)
      }

      override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.i(TAG, "handleTextureView onSurfaceTextureDestroyed")
        closeCamera()
        return false
      }

      override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
  }

  private fun surfaceSizeChanged(surfaceWidth: Int, surfaceHeight: Int) {
    mCameraClient?.setRenderSize(surfaceWidth, surfaceHeight)
  }

  private fun openCamera(st: IAspectRatio) {
    Log.d(TAG, "openCamera: CONNECTED")
    invokeOnCameraConnected()
    mCameraClient?.openCamera(st, false)
  }

  private fun closeCamera() {
    Log.d(TAG, "closeCamera: DISCONNECTED")
    invokeOnCameraDisconnected()
    mCameraClient?.closeCamera()
  }

  private val cameraClient: CameraClient?
    get() = if (mContext.currentActivity != null) CameraClient.newBuilder(mContext.currentActivity!!)
      .setEnableGLES(true)
      .setRawImage(true)
      .setCameraStrategy(CameraUvcStrategy(mContext.currentActivity!!, this))
      .setCameraRequest(cameraRequest)
      .openDebug(true)
      .build() else null

  private val cameraRequest: CameraRequest
    get() = CameraRequest.Builder()
      .setPreviewWidth(640)
      .setPreviewHeight(480)
      .create()

  fun captureImage(mPromise: Promise) {
    if (java.lang.Boolean.FALSE == mCameraClient?.isCameraOpened()) {
      mPromise.reject("Camera is not opened")
    }
    mCameraClient?.captureImage(object : ICaptureCallBack {
      override fun onBegin() {
        Log.d(TAG, "onBegin: ")
      }

      override fun onError(error: String?) {
        Log.e(TAG, "onError: $error")
        mPromise.reject(error)
      }

      override fun onComplete(path: String?) {
        Log.d(TAG, "onComplete: $path")
        mPromise.resolve(path)
      }
    }, null)
  }

  private fun invokeOnCameraConnected() {
    val event: WritableMap = Arguments.createMap()
    mContext.getJSModule(RCTEventEmitter::class.java)
      .receiveEvent(this.id, "onCameraConnected", event)
  }

  private fun invokeOnCameraDisconnected() {
    val event: WritableMap = Arguments.createMap()
    mContext.getJSModule(RCTEventEmitter::class.java)
      .receiveEvent(this.id, "onCameraDisconnected", event)
  }

  override fun onConnectDev(device: UsbDevice?) {
    if (device != null) Log.d(TAG, "onConnectDev: " + device.deviceName)
    invokeOnCameraConnected()
  }

  override fun onDisConnectDev(device: UsbDevice?) {
    if (device != null) Log.d(TAG, "onDisConnectDev: " + device.deviceName)
    invokeOnCameraDisconnected()
  }

  companion object {
    private const val TAG = "CameraView"
  }
}
