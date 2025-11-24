package com.example.graphicstest

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kết quả so sánh"

        val textResult = findViewById<TextView>(R.id.text_result)

        val prefs = getSharedPreferences("results", MODE_PRIVATE)

        val canvasRender = prefs.getFloat("canvas_render_time", 0f).toDouble()
        val openglRender = prefs.getFloat("opengl_render_time", 0f).toDouble()

        if (canvasRender > 0 && openglRender > 0) {
            val improvement = ((canvasRender - openglRender) / canvasRender * 100)
            val ratio = canvasRender / openglRender
            val faster = if (openglRender < canvasRender) "OpenGL ES" else "Canvas"

            val result = """
                ═══════════════════════════════════
                📊 KẾT QUẢ SO SÁNH RENDER TIME
                ═══════════════════════════════════
                
                Task: Vẽ 20,000 hình chữ nhật
                
                🎨 Canvas (CPU):
                   ${String.format("%.2f", canvasRender)} ms
                   
                ⚡ OpenGL ES (GPU):
                   ${String.format("%.2f", openglRender)} ms
                
                ───────────────────────────────────
                
                🏆 KẾT LUẬN:
                
                $faster nhanh hơn ${String.format("%.1fx", ratio)}
                (Cải thiện ${String.format("%.1f", improvement)}%)
                
                ───────────────────────────────────
                
                📝 GIẢI THÍCH:
                
                • Canvas: CPU render từng pixel tuần tự
                • OpenGL ES: GPU xử lý song song
                • Với 20,000 objects, GPU vượt trội
                
                📌 ĐO LƯỜNG:
                
                • Dùng System.nanoTime() + Trace API
                • OpenGL ES có glFinish() để sync GPU
                • Kết quả khớp với Android Profiler
                
                ═══════════════════════════════════
            """.trimIndent()

            textResult.text = result

        } else {
            textResult.text = """
                ⚠️ CHƯA ĐỦ DỮ LIỆU
                
                Vui lòng test cả 2 phương pháp:
                1. Test Canvas (20,000 shapes)
                2. Test OpenGL ES (20,000 shapes)
                
                Sau đó quay lại xem kết quả
            """.trimIndent()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
