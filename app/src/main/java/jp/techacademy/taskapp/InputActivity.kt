package jp.techacademy.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*

class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    //日付を設定するボタン
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            mYear = year
            mMonth = month
            mDay = dayOfMonth
            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            date_button.text = dateString //新しい日付に変更
        }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間を設定するボタン
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定ボタンを押したときに呼ばれる
    private val mOnDoneClickListener = View.OnClickListener{
        addTask() //Realmに保存/更新
        finish() //InputActivityを閉じて、前の画面（MainActivity）に戻る
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar) //setSupportActionBarメソッドにより、ツールバーをActionBarとして使えるように設定
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true) //setDisplayHomeAsUpEnabledメソッドで、ActionBarに戻るボタンを表示
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、 idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null){
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.data
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dataString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dataString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction() //Realmでデータを追加、削除など変更を行う場合

        if (mTask == null) {
            //新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.toString()
        val content = content_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.data = date

        realm.copyToRealmOrUpdate(mTask!!) //データの保存・更新 引数で与えたオブジェクトが存在していれば更新、なければ追加を行うメソッド
        realm.commitTransaction() //最後にcommitTransactionメソッドを呼び出す必要がある

        realm.close()
    }
}