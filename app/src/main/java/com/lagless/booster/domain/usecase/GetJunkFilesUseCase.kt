package com.lagless.booster.domain.usecase

import com.lagless.booster.data.model.JunkFileItem
import com.lagless.booster.data.repository.CleanerRepository

class GetJunkFilesUseCase(private val repository: CleanerRepository) {
    suspend operator fun invoke(): List<JunkFileItem> = repository.scanJunkFiles()
}
