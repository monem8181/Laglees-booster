package com.lagless.booster.domain.usecase

import com.lagless.booster.data.model.AppUsageInfo
import com.lagless.booster.data.repository.AppRepository

class GetUnusedAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(thresholdDays: Int = 30): List<AppUsageInfo> =
        repository.getUnusedApps(thresholdDays)
}
