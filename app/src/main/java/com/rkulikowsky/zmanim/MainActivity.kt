package com.rkulikowsky.zmanim

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.rkulikowsky.zmanim.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val requestCode = 101
    private val viewModel: MainActivityViewModel by lazy {ViewModelProvider(this).get(MainActivityViewModel::class.java)}
    private lateinit var binding: ActivityMainBinding
    private val shared by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener {shared,_->viewModel.onPreferencesChange(shared)} }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setPointer()
    }

    private fun setPointer() {

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setSupportActionBar(binding.topAppBar)
        shared.registerOnSharedPreferenceChangeListener(listener)
        viewModel.onPreferencesChange(shared)

        setAlarm()

        viewModel.currentDate.observe(this, {
            val city = viewModel.zmanim.value?.run { City(cityName,latitude,longitude) }
            if (city != null) {
                viewModel.refresh(it,city)
            }

        })


        //set witch times appear in screen
        viewModel.zmanim.observe(this, {
            Log.i("YOSSEF",it.toString())
            binding.hadlakaCard.visibility = if (it.date.dayOfWeek == 5) View.VISIBLE else View.GONE
            binding.tzetShabatCard.visibility =
                if (it.date.dayOfWeek == 6) View.VISIBLE else View.GONE
            binding.rTCard.visibility = binding.tzetShabatCard.visibility
            binding.tzetHacochavimCard.visibility =
                if (it.date.dayOfWeek == 6) View.GONE else View.VISIBLE
        })
    }

    private fun setAlarm() {
        val updateIntent = Intent(this,ZmanimAppWidget::class.java)
        val city = viewModel.zmanim.value?.run { City(cityName,latitude,longitude) }
        updateIntent.apply {
            action = "update_widget"
            putStringArrayListExtra("city", arrayListOf(city?.name,city?.latitude,city?.longitude))
        }
        val pi = PendingIntent.getBroadcast(this,0,updateIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), AlarmManager.INTERVAL_DAY,pi)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.location -> {
                val finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

                val shouldRequestRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)

                if (finePermission != PackageManager.PERMISSION_GRANTED && coarsePermission != PackageManager.PERMISSION_GRANTED) {
                    if (shouldRequestRationale) showRationale()
                    else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), requestCode)
                } else viewModel.locationClicked()
                true
            }

            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
                true
            }
        }
    }

    private fun showRationale() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Permission to access the location is required for this app to show the desired times.")
            .setTitle("Permission required")
        builder.setPositiveButton("OK") { _, _ ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                requestCode
            )
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            this.requestCode -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, resources.getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                else viewModel.locationClicked()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_bar, menu)
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        shared.unregisterOnSharedPreferenceChangeListener(listener)

    }
}