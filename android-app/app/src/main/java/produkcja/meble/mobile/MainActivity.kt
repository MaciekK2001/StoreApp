package produkcja.meble.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import produkcja.meble.mobile.ui.FurnitureApp
import produkcja.meble.mobile.ui.theme.MebleMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MebleMobileTheme {
                val viewModel: FurnitureViewModel = viewModel()
                FurnitureApp(viewModel)
            }
        }
    }
}
