package com.example.graphicstest

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Trace
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class CanvasTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val canvasView = CanvasRenderView(this)
        setContentView(canvasView)

        // Hiển thị nút quay lại trên ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Canvas Performance"

        // Đã xóa: Không còn finish() tự động nữa
        // canvasView.postDelayed({ finish() }, 3000)
    }

    // Xử lý sự kiện khi người dùng nhấn nút Back (mũi tên ActionBar)
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class CanvasRenderView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectangles = mutableListOf<Rect>()
    private var hasMeasured = false

    data class Rect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val color: Int,
        val rotation: Float
    )

    init {
        // Vẽ 20,000 hình ngẫu nhiên (cho test hiệu năng)
        for (i in 0 until 20000) {
            val left = Random.nextFloat() * 2000
            val top = Random.nextFloat() * 3000
            val width = 10f + Random.nextFloat() * 50
            val height = 10f + Random.nextFloat() * 50

            rectangles.add(
                Rect(
                    left = left,
                    top = top,
                    right = left + width,
                    bottom = top + height,
                    color = Color.argb(
                        150 + Random.nextInt(106),
                        Random.nextInt(256),
                        Random.nextInt(256),
                        Random.nextInt(256)
                    ),
                    rotation = Random.nextFloat() * 360f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Đo hiệu năng bằng Trace
        Trace.beginSection("Canvas_Render_20000_Shapes")

        val startRender = if (!hasMeasured) System.nanoTime() else 0L

        // Background gradient
        Trace.beginSection("Canvas_Background")
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            Color.BLACK, Color.DKGRAY,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        Trace.endSection()

        // Vẽ hình
        Trace.beginSection("Canvas_Draw_Rectangles")
        for (rect in rectangles) {
            canvas.save()
            canvas.rotate(
                rect.rotation,
                (rect.left + rect.right) / 2,
                (rect.top + rect.bottom) / 2
            )
            paint.color = rect.color
            paint.style = Paint.Style.FILL
            paint.setShadowLayer(5f, 2f, 2f, Color.BLACK)
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint)
            canvas.restore()
        }
        Trace.endSection()

        if (!hasMeasured) {
            val renderTime = (System.nanoTime() - startRender) / 1_000_000.0 // ms

            // Hiển thị kết quả
            Trace.beginSection("Canvas_Draw_Text")
            paint.clearShadowLayer()
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.style = Paint.Style.FILL
            canvas.drawText("Canvas (CPU)", 50f, 120f, paint)
            canvas.drawText("${String.format("%.2f", renderTime)} ms", 50f, 200f, paint)
            paint.textSize = 40f
            canvas.drawText("20,000 shapes + effects", 50f, 280f, paint)
            Trace.endSection()

            context.getSharedPreferences("results", Context.MODE_PRIVATE)
                .edit()
                .putFloat("canvas_render_time", renderTime.toFloat())
                .apply()
            Log.d("CanvasTest", "Canvas render time: $renderTime ms")

            Toast.makeText(
                context,
                "Canvas: ${String.format("%.2f", renderTime)} ms",
                Toast.LENGTH_LONG
            ).show()

            hasMeasured = true
        }

        Trace.endSection()
    }
}
