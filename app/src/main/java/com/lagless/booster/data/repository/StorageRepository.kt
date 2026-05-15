package com.lagless.booster.data.repository

import com.lagless.booster.data.model.FolderInfo
import com.lagless.booster.data.model.StorageInfo
import com.lagless.booster.utils.storage.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageRepository {

    suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        StorageUtils.getStorageInfo()
    }

    suspend fun getCommonFolders(): List<FolderInfo> = withContext(Dispatchers.IO) {
        StorageUtils.getCommonFolders()
    }
}
