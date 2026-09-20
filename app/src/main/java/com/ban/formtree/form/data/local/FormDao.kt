package com.ban.formtree.form.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ban.formtree.form.data.entity.FormItemEntity
import com.ban.formtree.form.data.entity.ResponseEntity
import com.ban.formtree.form.data.entity.ResponseSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {

    @Transaction
    @Query("SELECT * FROM ${FormItemEntity.TABLE_NAME}")
    fun observeFormItems(): Flow<List<FormItemWithResponses>>

    @Insert
    suspend fun insertItems(items: List<FormItemEntity>)

    @Insert
    suspend fun insertResponseSets(responseSets: List<ResponseSetEntity>)

    @Insert
    suspend fun insertResponses(responses: List<ResponseEntity>)

    @Query("DELETE FROM ${ResponseEntity.TABLE_NAME}")
    suspend fun deleteAllResponses()

    @Query("DELETE FROM ${ResponseSetEntity.TABLE_NAME}")
    suspend fun deleteAllResponseSets()

    @Query("DELETE FROM ${FormItemEntity.TABLE_NAME}")
    suspend fun deleteAllItems()

    @Transaction
    suspend fun replaceAll(bundle: FormEntityBundle) {
        deleteAllResponses()
        deleteAllResponseSets()
        deleteAllItems()
        insertItems(bundle.items)
        insertResponseSets(bundle.responseSets)
        insertResponses(bundle.responses)
    }
}
