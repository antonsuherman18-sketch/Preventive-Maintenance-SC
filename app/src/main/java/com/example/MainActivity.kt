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

import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Tetapkan Zona Waktu Default Aplikasi ke WIB (Waktu Indonesia Barat / UTC+7 / Asia/Jakarta)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"))

        // Explicitly initialize FirebaseApp to ensure cloud sync runs immediately
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "FirebaseApp init error", e)
        }

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
