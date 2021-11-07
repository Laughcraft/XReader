package com.laughcraft.android.myreader.permission

import android.app.Activity
import android.app.AppOpsManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
open class Api30PermissionChecker: PermissionChecker() {

    override fun isPermissionGranted(activity: Activity, permission: String): Boolean {
        val appOps = activity.getSystemService(AppOpsManager::class.java)
        val mode = appOps.unsafeCheckOpNoThrow(
            createAppOpsPermissionFromManifestPermission(permission),
            activity.applicationInfo.uid,
            activity.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    open fun createAppOpsPermissionFromManifestPermission(permission: String): String{
        return permission.replace(".permission.", ":").lowercase()
    }
}