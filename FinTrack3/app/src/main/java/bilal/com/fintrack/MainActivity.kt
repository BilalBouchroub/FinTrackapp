package bilal.com.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import bilal.com.fintrack.ui.navigation.FinTrackNavGraph
import bilal.com.fintrack.ui.theme.FinTrackTheme
import bilal.com.fintrack.utils.NotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Notification Channels
        NotificationService.createNotificationChannels(this)
        
        setContent {
            FinTrackTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        bilal.com.fintrack.ui.components.BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    FinTrackNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}