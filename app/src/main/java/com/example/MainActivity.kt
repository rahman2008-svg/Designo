package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DesignViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: DesignViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

