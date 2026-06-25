package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Design
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {
    @Query("SELECT * FROM designs ORDER BY updatedAt DESC")
    fun getAllDesigns(): Flow<List<Design>>

    @Query("SELECT * FROM designs WHERE id = :id")
    fun getDesignById(id: Int): Flow<Design?>

    @Query("SELECT * FROM designs WHERE id = :id")
    suspend fun getDesignByIdSuspended(id: Int): Design?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesign(design: Design): Long

    @Update
    suspend fun updateDesign(design: Design)

    @Delete
    suspend fun deleteDesign(design: Design)

    @Query("DELETE FROM designs WHERE id = :id")
    suspend fun deleteDesignById(id: Int)
}
