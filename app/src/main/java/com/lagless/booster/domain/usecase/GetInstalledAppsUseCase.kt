package com.lagless.booster.domain.usecase

import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.repository.AppRepository

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(userOnly: Boolean = false): List<InstalledAppInfo> =
        if (userOnly) repository.getUserInstalledApps() else repository.getInstalledApps()
}
