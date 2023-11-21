package io.barabuka

import BarabukaAppContent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BarabukaAppContent()
        }
    }
}

/*
@Preview
@Composable
fun AppAndroidPreview() {
    App(networker)
}*/
