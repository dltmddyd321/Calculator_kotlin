package com.example.cal_ko

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cal_ko.dao.HistoryDao
import com.example.cal_ko.model.History

@Database(entities = [History::class], version = 1) //버전 관리
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}