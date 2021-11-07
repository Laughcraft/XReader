package com.laughcraft.android.myreader.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PermissionChecker @Inject constructor(){

    open fun isPermissionGranted(fragment: Fragment, permission: String): Boolean {
        return isPermissionGranted(fragment.requireActivity(), permission)
    }

    open fun isPermissionGranted(activity: Activity, permission: String): Boolean {
        val status = ContextCompat.checkSelfPermission(activity, permission)
        return status == PackageManager.PERMISSION_GRANTED
    }
}