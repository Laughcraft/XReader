package com.laughcraft.android.myreader.permission

import android.Manifest
import android.app.Activity
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class Permissioner(private var permissionChecker: PermissionChecker,
                        private val permissionRequester: PermissionRequester) {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    else
        Manifest.permission.WRITE_EXTERNAL_STORAGE

    val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
    else
        Manifest.permission.WRITE_EXTERNAL_STORAGE


    fun isGranted(activity: Activity): Boolean = permissionChecker.isPermissionGranted(activity, permission)

    fun requestAllFilesPermissionIfNeeded(resultCaller: ActivityResultCaller,
                                          onRationaleRequested: (accepted: (Boolean)->Unit)-> Unit,
                                          onResult: (granted: Boolean) -> Unit): Boolean{

        val activity = extractActivity(resultCaller)

        val granted = isGranted(activity)

        if (!granted){
            requestPermissionNow(activity, resultCaller, onResult, onRationaleRequested)
        }
        return granted
    }

    fun requestPermissionLaterIfNeeded(resultCaller: ActivityResultCaller,
                               onRationaleRequested: (accepted: (Boolean)->Unit)-> Unit,
                               onResult: (granted: Boolean) -> Unit): ()-> Unit {

        val activity = extractActivity(resultCaller)

        return requestPermissionNow(activity, resultCaller, onResult, onRationaleRequested)
    }

    private fun extractActivity(resultCaller: ActivityResultCaller): Activity {
        return when (resultCaller){
            is AppCompatActivity -> resultCaller
            is Fragment -> resultCaller.requireActivity()
            else -> throw IllegalArgumentException("Unknown ActivityResultCaller Implementation")
        }
    }

    fun requestPermissionNow(activity: Activity,
                             resultCaller: ActivityResultCaller,
                             onResult: (granted: Boolean) -> Unit,
                             onRationaleRequested: (accepted: (Boolean) -> Unit) -> Unit): ()-> Unit {

        val callMeLater = permissionRequester.requestPermission(resultCaller, action, onResult)

        val showRationaleIfNeeded = {
            if (!isGranted(activity)){
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && activity.shouldShowRequestPermissionRationale(permission)){
                    onRationaleRequested.invoke { accepted ->
                        if (accepted){ callMeLater.invoke() }
                    }
                } else {
                    callMeLater.invoke()
                }
            }
        }

        return showRationaleIfNeeded
    }
}