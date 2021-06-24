package com.example.cal_ko

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.cal_ko.model.History
import org.w3c.dom.Text
import java.lang.NumberFormatException
import java.util.function.BinaryOperator

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById<TextView>(R.id.expTxt)
    }

    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTxt)
    }

    private val historyLayout: View by lazy {
        findViewById<View>(R.id.hisLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.hisLinear)
    }

    lateinit var db: AppDatabase //DB 생성

    private var isOperator = false
    private var hasOperator = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
        //앱 데이터베이스가 생성 및 실행
    }

    fun buttonClicked(v: View) {
        when(v.id){
            R.id.btn0 -> numberButtonClicked("0")
            R.id.btn1 -> numberButtonClicked("1")
            R.id.btn2 -> numberButtonClicked("2")
            R.id.btn3 -> numberButtonClicked("3")
            R.id.btn4 -> numberButtonClicked("4")
            R.id.btn5 -> numberButtonClicked("5")
            R.id.btn6 -> numberButtonClicked("6")
            R.id.btn7 -> numberButtonClicked("7")
            R.id.btn8 -> numberButtonClicked("8")
            R.id.btn9 -> numberButtonClicked("9")
            R.id.btnM -> operatorButtonClicked("-")
            R.id.Modul -> operatorButtonClicked("%")
            R.id.btnP -> operatorButtonClicked("+")
            R.id.div -> operatorButtonClicked("/")
            R.id.btnX -> operatorButtonClicked("*")
        }
    }

    private fun numberButtonClicked(number: String) {

        if(isOperator) {
            expressionTextView.append(" ")
        }

        isOperator = false

        val expressionText= expressionTextView.text.split(" ") //띄어쓰기 입력

        if(expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this,"15자리까지만 허용 가능",Toast.LENGTH_SHORT).show()
            return //Text의 글자 수가 15자 이상 채워져있다면 메시지 알림 (예외처리)
        } else if (expressionText.last().isEmpty() && number == "0") {
            Toast.makeText(this,"0은 제일 앞에 올 수 없다.",Toast.LENGTH_SHORT).show()
            return //0이 먼저 앞에 입력될 경우 예외처리
        }
        expressionTextView.append(number) //입력값 추가
        resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        if(expressionTextView.text.isEmpty()) { //값이 비어있을 때
            return
        }

        when {
            isOperator -> {
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1) + operator
                //맨 끝에서부터 1자리만 지워준다.
            }
            hasOperator -> {
                Toast.makeText(this,"연산자는 1번만 사용 가능",Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                expressionTextView.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(expressionTextView.text)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ssb.setSpan(
                ForegroundColorSpan(getColor(R.color.green)),
                expressionTextView.text.length -1,
                expressionTextView.text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        expressionTextView.text = ssb

        isOperator = true
        hasOperator = true
    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTextView.text.split(" ")

        if(expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        if(expressionTexts.size != 3 && hasOperator) { //첫 번째 숫자와 연산자만 입력되면
            Toast.makeText(this,"아직 완성되지 않은 수식이다.",Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this,"오류 발생",Toast.LENGTH_SHORT).show()
            return //해당 오류에는 걸리는 일이 없어야 함
        }

        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        //TODO DB에 삽입하는 내용
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTextView.text = resultText

        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        val expressionTexts = expressionTextView.text.split(" ")

        if (hasOperator.not() || expressionTexts.size != 3) {
            return ""

        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
            //0번 인덱스 값과 2번 인덱스 값이 숫자가 아니라면
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when(op) { //연산
            "+" -> (exp1+exp2).toString()
                "-" -> (exp1 - exp2).toString()
                "*" -> (exp1 * exp2).toString()
                "/" -> (exp1 / exp2).toString()
                "%" -> (exp1 % exp2).toString()
                else -> ""
        }
    }

    fun historyButtonClicked(v: View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()
        //LineatLayout 하위에 있는 모든 View가 삭제

        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row,null,false)
                    //inflate를 통하여 xml 파일 간의 연결
                    historyView.findViewById<TextView>(R.id.expTxt).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTxt).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)
                    //위쪽부터 하나씩 연계되는 형식으로 추가된다.
                }
            }
            //최신 데이터를 위에 보여주기 위해 reversed 사용
            //데이터 한 개씩 순회하기 위해 forEach 사용
        }).start()

        //TODO DB에서 모든 기록 호출
        //TODO View에서 모든 기록 할당
    }

    fun historyClearButtonClicked(v: View) {
        historyLinearLayout.removeAllViews()
        //TODO DB에서 모든 기록 삭제

        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()
        //TODO View에서 모든 기록 삭제
    }

    fun closeHistoryButtonClicked(v: View) {
        historyLayout.isVisible = false
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

}

//객체 확장 함수
fun String.isNumber(): Boolean {
    return try {
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        false
    } //숫자가 아니면 오류 예외처리
}
