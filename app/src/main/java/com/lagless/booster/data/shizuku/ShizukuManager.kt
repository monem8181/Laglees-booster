package com.lagless.booster.data.shizuku

import android.content.Context
import android.content.pm.PackageManager
import com.lagless.booster.data.model.ShizukuStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

object ShizukuManager {

    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val SHIZUKU_PERMISSION_CODE = 1001

    private val _status = MutableStateFlow(ShizukuStatus.NOT_INSTALLED)
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    // ── Status detection ──────────────────────────────────────

    fun computeStatus(context: Context): ShizukuStatus {
        val installed = try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) { false }

        if (!installed) return ShizukuStatus.NOT_INSTALLED

        val running = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
        if (!running) return ShizukuStatus.NOT_RUNNING

        val granted = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) { false }

        return if (granted) ShizukuStatus.CONNECTED else ShizukuStatus.PERMISSION_DENIED
    }

    fun refreshStatus(context: Context) {
        _status.value = computeStatus(context)
    }

    fun isConnected(): Boolean = _status.value == ShizukuStatus.CONNECTED

    // ── Permission request ────────────────────────────────────

    fun requestPermission() {
        try {
            val version = Shizuku.getVersion()
            if (version >= 11) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE)
            }
            // Pre-v11 Shizuku used a manifest permission; nothing to request at runtime
        } catch (_: Throwable) { /* Shizuku not available or not running */ }
    }

    // ── Shell command execution ───────────────────────────────

    suspend fun runShellCommand(vararg args: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, args, null, null) as Process
            val stdout  = process.inputStream.bufferedReader().readText()
            val stderr  = process.errorStream.bufferedReader().readText()
            val exit    = process.waitFor()
            if (exit == 0) Result.success(stdout.trim())
            else Result.failure(ShizukuCommandException("Exit $exit: ${stderr.trim()}"))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // ── Advanced actions ──────────────────────────────────────

    /**
     * Force-stops an app using ADB shell `am force-stop`.
     * Requires Shizuku to be CONNECTED and permission granted.
     */
    suspend fun forceStopApp(packageName: String): Result<Unit> =
        runShellCommand("am", "force-stop", packageName).map { }

    /**
     * Returns a quick info dump for the given package (Advanced Mode only).
     */
    suspend fun getPackageDump(packageName: String): Result<String> =
        runShellCommand("dumpsys", "package", packageName)

    // ── Human-readable status description ────────────────────

    fun ShizukuStatus.displayTitle(): String = when (this) {
        ShizukuStatus.NOT_INSTALLED    -> "Shizuku Not Installed"
        ShizukuStatus.NOT_RUNNING      -> "Shizuku Not Running"
        ShizukuStatus.PERMISSION_DENIED -> "Permission Denied"
        ShizukuStatus.CONNECTED        -> "Advanced Mode Active"
    }

    fun ShizukuStatus.displaySubtitle(): String = when (this) {
        ShizukuStatus.NOT_INSTALLED    -> "Install Shizuku from Play Store to unlock Advanced Mode"
        ShizukuStatus.NOT_RUNNING      -> "Open Shizuku and start the service via wireless/USB debugging"
        ShizukuStatus.PERMISSION_DENIED -> "Tap to grant permission in the Shizuku app"
        ShizukuStatus.CONNECTED        -> "Force-stop and deeper analysis features are unlocked"
    }
}

class ShizukuCommandException(message: String) : Exception(message)
