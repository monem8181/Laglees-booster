package com.lagless.booster.domain.usecase

import com.lagless.booster.data.model.StorageInfo
import com.lagless.booster.data.repository.StorageRepository

class GetStorageInfoUseCase(private val repository: StorageRepository) {
    suspend operator fun invoke(): StorageInfo = repository.getStorageInfo()
}
