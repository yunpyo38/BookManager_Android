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
            tvResult.text = "서버와 연결 중..."

            thread {
                try {
                    // 1. 서버 연결 (에뮬레이터에서 로컬 서버 접속 주소는 10.0.2.2)
                    val socket = Socket("10.0.2.2", 8080)
                    socket.soTimeout = 5000 // 5초 동안 응답 없으면 타임아웃

                    // 2. 데이터 읽기 (서버의 \n을 기다림)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val serverData = reader.readLine()
                    socket.close()

                    if (!serverData.isNullOrBlank()) {
                        // 3. JSON 파싱 (데이터 보따리 풀기)
                        val jsonObject = JSONObject(serverData.trim())
                        val booksArray = jsonObject.getJSONArray("books")

                        val displayText = StringBuilder()
                        displayText.append("📚 현재 도서 대출 목록\n")
                        displayText.append("━━━━━━━━━━━━━━━━━━\n\n")

                        for (i in 0 until booksArray.length()) {
                            val book = booksArray.getJSONObject(i)
                            val id = book.getInt("id")
                            val title = book.getString("title")
                            val author = book.getString("author")
                            val available = if (book.getInt("available") == 1) "대출 가능" else "대출 중"

                            displayText.append("$id. $title\n")
                            displayText.append("   - 저자: $author\n")
                            displayText.append("   - 상태: [$available]\n\n")
                        }

                        // 4. 화면 업데이트 (UI 스레드에서 실행)
                        runOnUiThread {
                            tvResult.text = displayText.toString()
                        }
                    } else {
                        runOnUiThread {
                            tvResult.text = "오류: 서버로부터 빈 데이터를 받았습니다."
                        }
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        // 에러 발생 시 상세 메시지 출력
                        tvResult.text = "연결 실패: ${e.localizedMessage}\n(서버가 켜져 있는지 확인하세요!)"
                    }
                    e.printStackTrace()
                }
            }
        }
    }
}