package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.DesignElement
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class DesignTypeConverters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val listType = Types.newParameterizedType(List::class.java, DesignElement::class.java)
    private val adapter = moshi.adapter<List<DesignElement>>(listType)

    @TypeConverter
    fun fromElementsList(elements: List<DesignElement>?): String {
        return adapter.toJson(elements ?: emptyList())
    }

    @TypeConverter
    fun toElementsList(json: String?): List<DesignElement> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
