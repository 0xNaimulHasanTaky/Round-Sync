package de.felixnuesse.extract.updates

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.R
import de.felixnuesse.extract.updates.workmanager.UpdateManager


class UpdateChecker(private var mContext: Context) {


    private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(mContext)

    private var checkForUpdates = preferenceManager.getBoolean(mContext.getString(R.string.pref_key_app_updates), true)

    private var updateManager = UpdateManager(mContext)


    init {
        if(checkForUpdates) {
            checkForUpdates = isUpdateableInstall()
        }
    }

    fun schedule() {
        if(!checkForUpdates) {
            updateManager.cancelAll()
            return
        }
        updateManager.queueRepeating()
    }

    private fun isUpdateableInstall(): Boolean {
        val storeApps = mutableListOf(
            "com.android.vending",
            "com.google.android.feedback",
            "org.fdroid.basic",
            "org.fdroid.fdroid",
            "com.aurora.store"
        )

        val sourcePackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mContext.packageManager.getInstallSourceInfo(mContext.packageName).installingPackageName
        } else {
            mContext.packageManager.getInstallerPackageName(mContext.packageName)
        }

        return !storeApps.contains(sourcePackage)
    }

}