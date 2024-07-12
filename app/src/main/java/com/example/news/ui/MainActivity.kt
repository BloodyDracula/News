package com.example.news.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.news.databinding.ActivityMainBinding
import com.example.news.viewmodel.NewsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.news.observe(this, Observer { articles ->
            if (articles != null) {
                binding.recyclerView.adapter = NewsAdapter(articles)
            }
        })

        requestLocationPermission()

        // Запрашиваем разрешение на хранение данных
        requestStoragePermission()
    }

    private fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    getLastLocationAndFetchNews()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    getLastLocationAndFetchNews()
                }
                else -> {
                    // Разрешения не предоставлены, используем значения по умолчанию
                    viewModel.fetchTopHeadlines("ru", "ru", "8fb17db5f07b48fba8779e5944585c0d")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            getLastLocationAndFetchNews()
        }
    }

    private fun getLastLocationAndFetchNews() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val country = getCountryFromLocation(it)
                val language = Locale.getDefault().language
                if (country != null) {
                    viewModel.fetchTopHeadlines(country, language, "8fb17db5f07b48fba8779e5944585c0d")
                } else {
                    // Обработка случая, когда страна не определена
                    viewModel.fetchTopHeadlines("ru", "ru", "8fb17db5f07b48fba8779e5944585c0d")
                }
            }
        }
    }

    private fun getCountryFromLocation(location: Location): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return if (!addresses.isNullOrEmpty()) {
            addresses[0].countryCode
        } else {
            null
        }
    }

    private fun requestStoragePermission() {
        val storagePermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Разрешение получено, выполните действия, требующие разрешения на хранение данных
                // Например, загрузка изображений или других файлов
            } else {
                // Разрешение не предоставлено, выполните альтернативные действия или информируйте пользователя
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionRequest.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            // Разрешение уже предоставлено, выполните действия, требующие разрешения на хранение данных
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Обработка результатов запроса разрешения на местоположение
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocationAndFetchNews()
            } else {
                // Разрешение на местоположение не предоставлено, используем значения по умолчанию
                viewModel.fetchTopHeadlines("ru", "ru", "8fb17db5f07b48fba8779e5944585c0d")
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            // Обработка результатов запроса разрешения на хранение данных
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на хранение данных получено, выполните действия, требующие разрешения
                // Например, загрузка изображений или других файлов
            } else {
                // Разрешение на хранение данных не предоставлено, выполните альтернативные действия или информируйте пользователя
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 101
        private const val REQUEST_STORAGE_PERMISSION = 102
    }
}
