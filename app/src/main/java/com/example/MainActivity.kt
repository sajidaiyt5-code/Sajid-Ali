package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.VirtualPhoneRoot
import com.example.viewmodel.VirtualPhoneViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val virtualPhoneViewModel: VirtualPhoneViewModel = viewModel()
            VirtualPhoneRoot(viewModel = virtualPhoneViewModel)
        }
    }
}
