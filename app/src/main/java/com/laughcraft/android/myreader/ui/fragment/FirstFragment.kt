package com.laughcraft.android.myreader.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.laughcraft.android.myreader.R
import com.laughcraft.android.myreader.XReader
import com.laughcraft.android.myreader.databinding.FragmentFirstBinding
import com.laughcraft.android.myreader.permission.Permissioner
import javax.inject.Inject

class FirstFragment: Fragment(R.layout.fragment_first) {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var permissionCallback: ()-> Unit

    @Inject lateinit var permissioner: Permissioner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XReader.dagger.inject(this)
        permissionCallback = permissioner.requestPermissionLaterIfNeeded(this, {}){ if (it) gotoMainFragment() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentFirstBinding.bind(view)
        binding.btnPrivacyPolicy.setOnClickListener { openPrivacyPolicy() }
        binding.btnAskPermission.setOnClickListener { permissionCallback.invoke() }
    }

    override fun onResume() {
        super.onResume()
        if (permissioner.isGranted(requireActivity())) gotoMainFragment()
    }

    private fun gotoMainFragment(){
        findNavController().navigate(FirstFragmentDirections.actionFirstFragmentToMainFragment())
    }

    private fun openPrivacyPolicy() {
        val url = "https://xreader.flycricket.io/privacy.html"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }
}