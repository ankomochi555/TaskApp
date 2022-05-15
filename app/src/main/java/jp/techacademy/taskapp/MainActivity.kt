package jp.techacademy.taskapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

const val EXTRA_TASK = "jp.techacademy.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    //Realmクラスを保持するmRealmを定義
    private lateinit var mRealm: Realm

    //RealmChangeListenerクラスのmRealmListenerはRealmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm>{
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }

    //TaskAdapterを保持するプロパティを定義する
    private lateinit var mTaskAdapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }


        //Realmの設定
        mRealm = Realm.getDefaultInstance() //オブジェクト取得
        mRealm.addChangeListener(mRealmListener) //mRealmListenerを設定

        //ListViewの設定 TaskAdapterを生成する
        mTaskAdapter = TaskAdapter(this)

        //ListViewをタップしたときの処理
        listView1.setOnItemClickListener{ parent, _, position, _ ->
            //入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }


        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener{ parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            //ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }


    //TaskAdapterにデータを設定、ListViewにTaskAdapterを設定、再描画するreloadListViewメソッドを追加する
    private fun reloadListView() {
        // Realmデータベースから、「すべてのデータを取得して新しい日時順に並べた結果」を取得
        //findAll ですべてのTaskデータを取得して、sortで"date"（日時）を Sort.DESCENDING（降順）で並べ替えた結果を返す
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("data", Sort.DESCENDING)

        // 上記の結果を、TaskListとしてセットする
        //次にその結果を、 mRealm.copyFromRealm(taskRealmResults) でコピーしてアダプターに渡す
        //Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は、
        // 直接渡すのではなく、このようにコピーして渡す必要があるため。
        mTaskAdapter.mTaskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }


    //getDefaultInstanceメソッドで取得したRealmクラスのオブジェクトはcloseメソッドで終了させる必要がある。
    // onDestroyメソッドはActivityが破棄されるときに呼び出されるメソッドなので、最後にRealmクラスのオブジェクトを破棄することになる。
    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }

}