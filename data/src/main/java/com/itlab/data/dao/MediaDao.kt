package com.itlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.itlab.data.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media WHERE noteId = :noteId")
    fun getMediaForNoteFlow(noteId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE noteId = :noteId")
    fun getMediaForNote(noteId: String): List<MediaEntity>

    @Insert
    suspend fun insert(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaEntity>)

    @Delete
    suspend fun delete(media: MediaEntity)
}
