package com.laughcraft.android.myreader.permission

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import javax.inject.Inject

open class PermissionRequester @Inject constructor(){

    open fun requestPermission(resultCaller: ActivityResultCaller,
                               permission: String,
                               onResult: (granted: Boolean) -> Unit): ()->Unit {
        val resultLauncher = resultCaller.registerForActivityResult(ActivityResultContracts.RequestPermission()){
            onResult.invoke(it)
        }

        return { resultLauncher.launch(permission) }
    }
}
