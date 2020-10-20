package com.rkulikowsky.zmanim

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationServices
import org.joda.time.DateTime
import java.io.IOException
import java.util.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(application) }

    private val shared = PreferenceManager.getDefaultSharedPreferences(app)

    private val _currentDate = MutableLiveData(DateTime.now())
    val currentDate: LiveData<DateTime>
        get() = _currentDate

    private val _zmanim = MutableLiveData<DayTimes>()
    val zmanim: LiveData<DayTimes>
        get() = _zmanim




    @SuppressLint("MissingPermission")

    fun locationClicked() {

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    try{
                        val geocoder = Geocoder(app, Locale.getDefault())
                        val adresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val city = adresses[0].locality

                        shared.edit().putString("city", "$city,${location.latitude},${location.longitude}").apply()
                    }catch (ioException: IOException){
                        Toast.makeText(app, ioException.message, Toast.LENGTH_SHORT).show()
                    }
                }
                else Toast.makeText(app, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
            }
    }

    fun onPreferencesChange(shared: SharedPreferences){
        val updateIntent = Intent(app,ZmanimAppWidget::class.java)



        val cityRaw = shared.getString("city", null)?:"jerusalem,31.768318,35.213711"
        val cityPref = cityRaw.split(',')
        val city = City(cityPref[0].getStringFromResources(),cityPref[1],cityPref[2])

        Log.i("YOSSEF", cityPref.toString())
        updateIntent.apply {
            action = "update_widget"
            putStringArrayListExtra("city",cityPref as ArrayList<String>)
        }
        PendingIntent.getBroadcast(app,0,updateIntent,PendingIntent.FLAG_CANCEL_CURRENT)    .send()
        refresh(currentDate.value!!,city)
    }

    fun refresh(currentDate:DateTime, city: City) {
        Log.i("YOSSEF", city.toString())
        _zmanim.value = DayTimes(currentDate, city.name, city.latitude, city.longitude)

    }
    fun rightClicked() {_currentDate.value = _currentDate.value!!.plusDays(1)}
    fun leftClicked() { _currentDate.value=_currentDate.value!!.minusDays(1)}

    private fun String.getStringFromResources(): String {
        val test = app.resources.getIdentifier(this,"string",app.packageName)
        return if (test==0) this else app.resources.getString(test)

        }
}


