package com.valyouw.filter_plugin.filter

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer

object GLUtils {
	var VertexShaderSource = """#version 300 es
	// vertex value between 0-1
	in vec2 a_texCoord;

	uniform float u_flipY;

	// Used to pass the texture coordinates to the fragment shader
	out vec2 v_texCoord;

	// all shaders have a main function
	void main() {
		// convert from 0->1 to 0->2
		vec2 zeroToTwo = a_texCoord * 2.0;

		// convert from 0->2 to -1->+1 (clipspace)
		vec2 clipSpace = zeroToTwo - 1.0;

		gl_Position = vec4(clipSpace * vec2(1, u_flipY), 0, 1);

		// pass the texCoord to the fragment shader
		// The GPU will interpolate this value between points.
		v_texCoord = a_texCoord;
	}	"""

	fun createProgram(vertexSource: String, fragmentSource: String): Int {
		val vertexShader = buildShader(GLES30.GL_VERTEX_SHADER, vertexSource)
		if (vertexShader == 0) {
			return 0
		}

		val fragmentShader = buildShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
		if (fragmentShader == 0) {
			return 0
		}

		val program = GLES30.glCreateProgram()
		if (program == 0) {
			return 0
		}

		GLES30.glAttachShader(program, vertexShader)
		GLES30.glAttachShader(program, fragmentShader)
		GLES30.glLinkProgram(program)

		return program
	}

	fun createTexture(data: Bitmap?, width: Int, height: Int, internalFormat: Int = GLES30.GL_RGBA, format: Int = GLES30.GL_RGBA, type: Int = GLES30.GL_UNSIGNED_BYTE): Int {
		val texture = IntArray(1)
		GLES30.glGenTextures(1, texture, 0)
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])

		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)

		// Upload the image into the texture.
		val mipLevel = 0 // the largest mip
		val border = 0

		if (data != null) {
			val buffer = ByteBuffer.allocate(data.byteCount)
			data.copyPixelsToBuffer(buffer)
			buffer.position(0)
			GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, mipLevel, internalFormat, width, height, border, format, type, buffer)
		} else {
			GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, mipLevel, internalFormat, width, height, border, format, type, null)
			GLES30.glGetError()
		}

		return texture[0]
	}

	fun checkEglError(msg: String) {
		val error = EGL14.eglGetError()
		if (error != EGL14.EGL_SUCCESS) {
			throw RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
		}
	}

	private fun buildShader(type: Int, shaderSource: String): Int {
		val shader = GLES30.glCreateShader(type)
		if (shader == 0) {
			return 0
		}

		GLES30.glShaderSource(shader, shaderSource)
		GLES30.glCompileShader(shader)

		val status = IntArray(1)
		GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
		if (status[0] == 0) {
			Log.e("CPXGLUtils", GLES30.glGetShaderInfoLog(shader))
			GLES30.glDeleteShader(shader)
			return 0
		}

		return shader
	}
}
