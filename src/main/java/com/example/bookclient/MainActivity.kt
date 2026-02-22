package com.example.bookclient

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnFetch.setOnClickListener {
            tvResult.text = "서버 데이터 분석 중..."

            thread {
                try {
                    val socket = Socket("10.0.2.2", 8080)
                    socket.soTimeout = 5000

                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    var line: String?
                    var jsonResponse = ""
                    var isJsonStarted = false

                    // 핵심: HTTP 헤더는 무시하고 '{'가 나오는 시점부터 읽기
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line?.trim() ?: ""
                        if (currentLine.startsWith("{")) isJsonStarted = true
                        if (isJsonStarted) jsonResponse += currentLine
                    }
                    socket.close()

                    if (jsonResponse.isNotEmpty()) {
                        val jsonObject = JSONObject(jsonResponse)
                        val booksArray = jsonObject.getJSONArray("books")

                        val displayText = StringBuilder()
                        displayText.append("📚 도서 대출 현황\n━━━━━━━━━━\n\n")

                        for (i in 0 until booksArray.length()) {
                            val book = booksArray.getJSONObject(i)
                            val title = book.getString("title")
                            val author = book.getString("author")
                            val available = if (book.getInt("available") == 1) "대출 가능" else "대출 중"

                            displayText.append("${i+1}. $title\n   - 저자: $author\n   - 상태: [$available]\n\n")
                        }

                        runOnUiThread { tvResult.text = displayText.toString() }
                    }
                } catch (e: Exception) {
                    runOnUiThread { tvResult.text = "오류 발생: ${e.localizedMessage}" }
                }
            }
        }
    }
}