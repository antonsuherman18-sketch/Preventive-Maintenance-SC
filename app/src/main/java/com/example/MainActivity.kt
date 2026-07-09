package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.CentrifugeApp
import com.example.ui.CentrifugeViewModel
import com.example.ui.CentrifugeViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = Repository(database)

        // Instantiate ViewModel with factory
        val viewModel: CentrifugeViewModel by viewModels {
            CentrifugeViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                CentrifugeApp(viewModel = viewModel)
            }
        }
    }
}
