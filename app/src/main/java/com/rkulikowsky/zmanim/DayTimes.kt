package com.rkulikowsky.zmanim

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

data class DayTimes(val date: DateTime = DateTime.now(), val cityName: String,
               val latitude:String,val longitude :String) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - EEE", Locale.getDefault())


    val title: String
        get() = "$cityName - ${dateFormat.format(date.toDate())}"
    val alotHaShachar: String = ""
    val netz: String
    val hadlaka: String = ""
    val shkia: String
    val chatzot: String
        get() = (((shkia.toMinutes()) - (netz.toMinutes())) / 2 + (netz.toMinutes())).toStringFormat()
    val kSGra: String
        get() = ((netz.toMinutes())+(((shkia.toMinutes()) - (netz.toMinutes())) / 4)).toStringFormat()
    val kSMaguen: String = ""
    val tzet: String
        get() = ((shkia.toMinutes()).plus(18)).toStringFormat()
    val tzetShabat = ""
    val rabenuTam = ""

    init {
        val calendar = Calendar.getInstance()
        val calc = SunriseSunsetCalculator(
            Location(latitude, longitude),
            TimeZone.getDefault()
        )
        netz = calc.getOfficialSunriseForDate(
            calendar.apply { timeInMillis = date.millis })
        shkia = calc.getOfficialSunsetForDate(
            calendar.apply { timeInMillis = date.millis })

    }

    private fun Int.toStringFormat():String
        {
            val hrs = (this / 60)
            val min = (this % 60)
            return "%02d:%02d".format(hrs,min)
        }

    private fun String.toMinutes() =
        this.substringBefore(':').toInt().times(60) + this.substringAfter(':').toInt()

}