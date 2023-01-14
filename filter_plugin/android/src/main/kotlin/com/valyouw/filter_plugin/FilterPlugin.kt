package com.valyouw.filter_plugin

import android.graphics.Bitmap
import android.view.Surface
import androidx.annotation.NonNull
import com.valyouw.filter_plugin.filter.GaussianBlur

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.view.TextureRegistry
import java.nio.ByteBuffer

/** FilterPlugin */
class FilterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  private var gaussianBlur: GaussianBlur? = null
  private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
  private var flutterSurfaceTexture: TextureRegistry.SurfaceTextureEntry? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    // Create a communication channel between flutter land and android
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "vyw/filter_plugin")
    channel.setMethodCallHandler(this)
    this.pluginBinding = flutterPluginBinding
  }

  // This will be called whenever we get message from android land
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "create" -> {
        if (pluginBinding == null) {
          result.error("NOT_READY", "pluginBinding is null", null)
          return
        }

        createFilter(call, result)
      }
      "draw" -> {
        if (gaussianBlur != null) {
          // Get the radius param
          val radius: Double = call.argument("radius")!!

          gaussianBlur!!.draw(radius.toFloat(), true)
          result.success(null)
        } else {
          result.error("NOT_INITIALIZED", "Filter not initialized", null)
        }
      }
      "dispose" -> {
        gaussianBlur?.destroy()
        if (flutterSurfaceTexture != null) {
          flutterSurfaceTexture!!.release()
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    this.pluginBinding = null
  }

  private fun createFilter(@NonNull call: MethodCall, @NonNull result: Result) {
    // Get request params
    val width: Int = call.argument("width")!!
    val height: Int = call.argument("height")!!
    val srcImage = call.argument("img") as? ByteArray

    // our response will be a dictionary
    val reply: MutableMap<String, Any> = HashMap()

    if (srcImage != null) {
      // Convert input image to bitmap
      val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      bmp.copyPixelsFromBuffer(ByteBuffer.wrap(srcImage))

      // Create a Surface for our filter to draw on, it is backed by a texture we get from Flutter
      flutterSurfaceTexture = pluginBinding!!.textureRegistry.createSurfaceTexture()
      val nativeSurfaceTexture = flutterSurfaceTexture!!.surfaceTexture()
      nativeSurfaceTexture.setDefaultBufferSize(width, height)
      val nativeSurface = Surface(nativeSurfaceTexture)

      // create our filter and tell it to draw to the surface we just created (which is backed
      // by the flutter texture)
      gaussianBlur = GaussianBlur(nativeSurface, bmp)
    }

    // Return the flutter texture id to Flutter land, the "Texture" widget in our app will
    // display it
    reply["textureId"] = flutterSurfaceTexture?.id() ?: -1
    result.success(reply)
  }
}
