package com.example.previsaodotempo

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.previsaodotempo.databinding.ActivityMainBinding
import com.example.previsodotempokotlin.constantes.Const
import com.example.previsodotempokotlin.model.Main
import com.example.previsodotempokotlin.model.Weather
import com.example.previsodotempokotlin.services.Api
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerPrincipal)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //fragmento que conecta o mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        switchTema()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Solicita a localização atual
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Use as coordenadas de latitude e longitude conforme necessário

                    var localizacao = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(localizacao).title("Localização atual"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacao, 18f))

                    previsaoTempo(latitude, longitude)
                }
            }
        } else {
            // Solicite permissão de localização se ainda não foi concedida
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        mMap.setOnMapClickListener {latlng ->

            mMap.clear() //limpeza do mapa

            var lat = latlng.latitude
            var long = latlng.longitude

            mMap.addMarker(MarkerOptions().position(latlng).title("Localização Escolhida"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))

            previsaoTempo(lat, long)
        }
    }


    private fun previsaoTempo(latitude: Double, longitude: Double){

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(Api::class.java)


        retrofit.weatherMap(latitude, longitude, Const.API_KEY, "pt_br")
            .enqueue(object : Callback<Main> {
                override fun onResponse(call: Call<Main>, response: Response<Main>) {

                    if (response.isSuccessful) {
                        respostaServidor(response)

                    } else {
                        Toast.makeText(applicationContext,"Cidade Inválida!",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Main>, t: Throwable) {
                    Toast.makeText(applicationContext,"Erro fatal de Servidor",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }


    @SuppressLint("SetTextI18n")
    private fun respostaServidor(response: Response<Main>) {

        val main: JsonObject? = response.body()?.main
        val temp = main?.get("temp").toString()
        val tempMin = main?.get("temp_min").toString()
        val tempMax = main?.get("temp_max").toString()
        val humidity = main?.get("humidity").toString()

        val sys: JsonObject? = response.body()?.sys
        val country = sys?.get("country")?.asString

        val weather: List<Weather>? = response.body()?.weather
        val id: Int? = weather?.get(0)?.id
        val description: String? = weather?.get(0)?.description

        val name: String? = response.body()?.name


        conversorKelvinCelsius(temp, tempMin, tempMax)

        if (id != null) {
            imagens(id)
        }

        if (country == null || name == null){
            binding.txtPaisCidade.setText("Região desconhecida")
        }else{
            binding.txtPaisCidade.setText("$country - $name")
        }

        binding.txtInformacoes1.setText("Clima \n $description \n\n Umidade \n $humidity")

    }


    private fun imagens(id: Int) {

        if (id < 300){
            binding.imgClima.setBackgroundResource(R.drawable.trunderstorm)
        }else if(id < 600){
            binding.imgClima.setBackgroundResource(R.drawable.rain)
        }else if(id < 700){
            binding.imgClima.setBackgroundResource(R.drawable.snow)
        }else if(id < 800){
            binding.imgClima.setBackgroundResource(R.drawable.clouds)
        }else if(id == 800){
            binding.imgClima.setBackgroundResource(R.drawable.clearsky)
        }else if(id > 801){
            binding.imgClima.setBackgroundResource(R.drawable.flewclouds)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun conversorKelvinCelsius(temp: String, tempMin: String, tempMax: String) {
        val temp_c = (temp.toDouble() - 273.15)
        val tempMin_c = (tempMin.toDouble() - 273.15)
        val tempMax_c = (tempMax.toDouble() - 273.15)
        val decimalFormat = DecimalFormat("00")

        binding.txtTemperatura.setText("${decimalFormat.format(temp_c)} ºC")
        binding.txtInformacoes2.setText("Temp. Min. \n ${decimalFormat.format(tempMin_c)} ºC \n\n" +
                "Temp. Max. \n ${decimalFormat.format(tempMax_c)} ºC")
    }


    private fun switchTema(){

        binding.switchTemas.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked){ //Tema escuro - Dark Mode
                binding.containerPrincipal.setBackgroundColor(Color.parseColor("#000000"))
                binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_escuro)
                binding.txtTituloInfo.setTextColor(Color.parseColor("#000000"))
                binding.txtInformacoes1.setTextColor(Color.parseColor("#000000"))
                binding.txtInformacoes2.setTextColor(Color.parseColor("#000000"))
                window.statusBarColor = Color.parseColor("#FFFFFFFF")
            }else{ //Tema claro - Light Mode
                binding.containerPrincipal.setBackgroundColor(Color.parseColor("#396BCB"))
                binding.containerInfo.setBackgroundResource(R.drawable.container_info_tema_claro)
                binding.txtTituloInfo.setTextColor(Color.parseColor("#FFFFFFFF"))
                binding.txtInformacoes1.setTextColor(Color.parseColor("#FFFFFFFF"))
                binding.txtInformacoes2.setTextColor(Color.parseColor("#FFFFFFFF"))
                window.statusBarColor = Color.parseColor("#396BCB")
            }
        }
    }
}