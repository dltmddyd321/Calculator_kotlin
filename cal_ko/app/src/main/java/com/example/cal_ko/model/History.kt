package com.example.cal_ko.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    @PrimaryKey val uid:Int?,
    @ColumnInfo(name = "expression") val expression: String?,
    @ColumnInfo(name = "result") val result: String?
)
//Room을 이용하여 로컬 데이터베이스에 데이터를 저장
//로컬에 저장되는 것이라 앱을 재시작해도 기록이 남아있음