package com.example.graphicstest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.graphicstest.CanvasTestActivity
import com.example.graphicstest.OpenGLTestActivity
import com.example.graphicstest.ResultActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnCanvas: Button
    private lateinit var btnOpenGL: Button
    private lateinit var btnTexture: Button
    private lateinit var btnResult: Button
    private lateinit var textResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCanvas = findViewById(R.id.btn_canvas)
        btnOpenGL = findViewById(R.id.btn_opengl)
        btnResult = findViewById(R.id.btn_result)
        textResult = findViewById(R.id.text_result)

        btnCanvas.setOnClickListener {
            startActivity(Intent(this, CanvasTestActivity::class.java))
        }

        btnOpenGL.setOnClickListener {
            startActivity(Intent(this, OpenGLTestActivity::class.java))
        }

        btnResult.setOnClickListener {
            startActivity(Intent(this, ResultActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showQuickResult()
    }

    private fun showQuickResult() {
        val prefs = getSharedPreferences("results", MODE_PRIVATE)

        val canvasTime = prefs.getFloat("canvas_render_time", 0f).toDouble()
        val openglTime = prefs.getFloat("opengl_render_time", 0f).toDouble()

        if (canvasTime > 0 && openglTime > 0) {
            val ratio = canvasTime / openglTime
            val faster = if (openglTime < canvasTime) "OpenGL ES" else "Canvas"

            textResult.text = """
                Canvas: ${String.format("%.2f", canvasTime)} ms
                OpenGL ES: ${String.format("%.2f", openglTime)} ms
                
                $faster nhanh hơn ${String.format("%.2fx", ratio)}
            """.trimIndent()
        } else {
            textResult.text = "Chưa test đủ 2 phương pháp."
        }
    }
}
