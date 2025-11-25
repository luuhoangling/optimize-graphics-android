package com.example.graphicstest

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.Trace
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random

class OpenGLTestActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(RectanglesRenderer(this))
        setContentView(glSurfaceView)

        glSurfaceView.postDelayed({ finish() }, 3000)
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}

class RectanglesRenderer(private val activity: OpenGLTestActivity) : GLSurfaceView.Renderer {

    private val rectangles = mutableListOf<GLRect>()
    private var program = 0
    private var hasMeasured = false
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    data class GLRect(
        val vertexBuffer: FloatBuffer,
        val color: FloatArray,
        val rotation: Float,
        val centerX: Float,
        val centerY: Float
    )

    init {
        // ✅ TĂNG LÊN 20,000 HÌNH
        for (i in 0 until 20000) {
            val x = Random.nextFloat() * 2 - 1
            val y = Random.nextFloat() * 2 - 1
            val width = 0.005f + Random.nextFloat() * 0.03f
            val height = 0.005f + Random.nextFloat() * 0.03f

            val vertices = floatArrayOf(
                x, y, 0f,
                x + width, y, 0f,
                x, y + height, 0f,
                x + width, y + height, 0f
            )

            val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .apply {
                    put(vertices)
                    position(0)
                }

            val color = floatArrayOf(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat(),
                0.6f + Random.nextFloat() * 0.4f
            )

            rectangles.add(
                GLRect(
                    buffer,
                    color,
                    Random.nextFloat() * 360f,
                    x + width / 2,
                    y + height / 2
                )
            )
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {

        // ✅ TRACE MARKER - Bắt đầu đo toàn bộ render
        Trace.beginSection("OpenGL_Render_20000_Shapes")

        val startRender = if (!hasMeasured) System.nanoTime() else 0L

        // ✅ TRACE MARKER - Clear screen
        Trace.beginSection("OpenGL_Clear")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        Trace.endSection()

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        // ✅ TRACE MARKER - Vẽ 20,000 hình
        Trace.beginSection("OpenGL_Draw_Rectangles")
        for (rect in rectangles) {
            val modelMatrix = FloatArray(16)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.rotateM(modelMatrix, 0, rect.rotation, 0f, 0f, 1f)

            val tempMatrix = FloatArray(16)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, rect.vertexBuffer)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glUniform4fv(colorHandle, 1, rect.color, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
        GLES20.glDisableVertexAttribArray(positionHandle)
        Trace.endSection() // End Draw_Rectangles

        // ✅ QUAN TRỌNG: Đồng bộ GPU để đo chính xác
        Trace.beginSection("OpenGL_Finish")
        GLES20.glFinish()
        Trace.endSection()

        if (!hasMeasured) {
            val renderTime = (System.nanoTime() - startRender) / 1_000_000.0

            activity.runOnUiThread {
                activity.getSharedPreferences("results", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putFloat("opengl_render_time", renderTime.toFloat())
                    .apply()

                Log.d("OpenGLTest", "OpenGL ES render time: $renderTime ms")

                Toast.makeText(
                    activity,
                    "OpenGL ES: ${String.format("%.2f", renderTime)} ms",
                    Toast.LENGTH_LONG
                ).show()
            }

            hasMeasured = true
        }

        // ✅ TRACE MARKER - Kết thúc đo toàn bộ
        Trace.endSection() // End OpenGL_Render_20000_Shapes
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
