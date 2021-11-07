package com.laughcraft.android.myreader.permission

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

class SettingPermissionRequester: PermissionRequester() {
    override fun requestPermission(resultCaller: ActivityResultCaller,
                                   action: String,
                                   onResult: (granted: Boolean) -> Unit): ()->Unit {

        val resultLauncher = resultCaller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            onResult.invoke(it.resultCode == Activity.RESULT_OK)
        }

        return { resultLauncher.launch(Intent(action)) }
    }
}