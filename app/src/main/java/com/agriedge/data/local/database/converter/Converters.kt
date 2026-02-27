package com.agriedge.data.local.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Type converters for Room database to handle complex types
 * Requirements: 5.1, 34.3
 */
class Converters {
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // String List Converter
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(value)
    }
    
    // Map<String, String> Converter
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        val adapter = moshi.adapter<Map<String, String>>(type)
        return adapter.toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        val adapter = moshi.adapter<Map<String, String>>(type)
        return adapter.fromJson(value)
    }
    
    // Map<Int, Int> Converter (for rating distribution)
    @TypeConverter
    fun fromIntMap(value: Map<Int, Int>?): String? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            Int::class.javaObjectType,
            Int::class.javaObjectType
        )
        val adapter = moshi.adapter<Map<Int, Int>>(type)
        return adapter.toJson(value)
    }
    
    @TypeConverter
    fun toIntMap(value: String?): Map<Int, Int>? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            Int::class.javaObjectType,
            Int::class.javaObjectType
        )
        val adapter = moshi.adapter<Map<Int, Int>>(type)
        return adapter.fromJson(value)
    }
}
