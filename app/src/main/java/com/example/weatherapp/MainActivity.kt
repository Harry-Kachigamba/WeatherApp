package com.example.weatherapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.WeatherData
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherApp()
        }
    }
}

@Composable
fun WeatherApp() {
    val weatherRepository = WeatherRepository()
    val coroutineScope = rememberCoroutineScope()
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var cityName by remember { mutableStateOf("") }
    var permissionGranted by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (!permissionGranted) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Location permission is required to fetch weather data.")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = cityName,
                onValueChange = { cityName = it },
                label = { Text("Enter City") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            showError = false
                            val data = weatherRepository.getWeather(cityName)
                            weatherData = data
                        } catch (e: Exception) {
                            showError = true
                        }
                    }
                }
            ) {
                Text("Get Weather")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if(weatherData != null) {
                WeatherScreen(weatherData!!)
            } else if(showError) {
                Text(
                    text = "Failed to retrieve weather. Please check the city name",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun WeatherScreen(weatherData: WeatherData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(100.dp)
    ) {
        Text("Weather in ${weatherData.name}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/${weatherData.weather[0].icon}@2x.png"),
            contentDescription = "Weather Icon"
        )
        Text(
            "${weatherData.main.temp}°C",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            weatherData.weather[0].description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
