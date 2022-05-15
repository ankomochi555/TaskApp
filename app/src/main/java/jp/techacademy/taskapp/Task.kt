package jp.techacademy.taskapp

import java.io.Serializable
import java.util.*
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Task :RealmObject(), Serializable {
    var title: String = ""  //タイトル
    var contents: String = "" //内容
    var data: Date = Date() //日時

    //idをプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}