package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.PickRepository
import com.example.ui.PickApp
import com.example.ui.PickViewModel
import com.example.ui.PickViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup database and repository instances
        val database = AppDatabase.getDatabase(this)
        val repository = PickRepository(database.pickDao())
        
        // Instantiate the ViewModel using the standard Factory pattern
        val viewModel: PickViewModel by viewModels {
            PickViewModelFactory(repository)
        }
        
        // Enable full drawing edge-to-edge
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                PickApp(viewModel = viewModel)
            }
        }
    }
}
