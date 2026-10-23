package com.hisabkhata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hisabkhata.app.ui.navigation.AppNavGraph
import com.hisabkhata.app.ui.theme.BackgroundLight
import com.hisabkhata.app.ui.theme.HisabKhataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HisabKhataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
