package com.example.cal_ko.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.cal_ko.model.History

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun getAll(): List<History>
    //History를 모두 가져온다.

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history")
    fun deleteAll()

    //@Delete
    //fun delete(history: History)
    //특정 자료만 delete

    //@Query("SELECT * FROM history WHERE result LIKE :result")
    //fun findByResult(result:String): List<History>
    //특정 조건에 따른 SELECT
}