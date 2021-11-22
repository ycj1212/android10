package com.thequietz.travelog.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.thequietz.travelog.record.model.RecordImage

@Dao
abstract class RecordImageDao : BaseDao<RecordImage> {
    @Query("SELECT * FROM RecordImage")
    abstract fun loadAllRecordImages(): List<RecordImage>

    @Query("SELECT * FROM RecordImage WHERE title =:title")
    abstract fun loadRecordImageByTitle(title: String): List<RecordImage>

    @Query("SELECT * FROM RecordImage WHERE id =:id")
    abstract fun loadRecordImageById(id: Int): RecordImage

    @Query("UPDATE RecordImage SET comment =:comment WHERE id =:id")
    abstract fun updateRecordImageCommentById(comment: String, id: Int)
}
