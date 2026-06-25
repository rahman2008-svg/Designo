package com.example.data.repository

import com.example.data.database.DesignDao
import com.example.data.model.Design
import kotlinx.coroutines.flow.Flow

class DesignRepository(private val designDao: DesignDao) {
    val allDesigns: Flow<List<Design>> = designDao.getAllDesigns()

    fun getDesignById(id: Int): Flow<Design?> {
        return designDao.getDesignById(id)
    }

    suspend fun getDesignByIdSuspended(id: Int): Design? {
        return designDao.getDesignByIdSuspended(id)
    }

    suspend fun insertDesign(design: Design): Long {
        return designDao.insertDesign(design)
    }

    suspend fun updateDesign(design: Design) {
        designDao.updateDesign(design)
    }

    suspend fun deleteDesign(design: Design) {
        designDao.deleteDesign(design)
    }

    suspend fun deleteDesignById(id: Int) {
        designDao.deleteDesignById(id)
    }
}
