package com.vmw.simplegps.mvvmi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.vmw.simplegps.R
import com.vmw.simplegps.location.LocatorManager
import kotlinx.coroutines.*
import java.text.DateFormat

private const val TAG = "LocationActivity"

class LocationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var getLocationBtn: FloatingActionButton
    private lateinit var locationManagerLastKnownStatus: TextView
    private lateinit var locationManagerLastKnownLocationTxt: TextView
    private lateinit var locationManagerSingleUpdateStatus: TextView
    private lateinit var locationManagerSingleUpdateLocationTxt: TextView
    private lateinit var locationManagerMultipleUpdateStatus: TextView
    private lateinit var locationManagerMultipleUpdateLocationTxt: TextView
    private lateinit var fusionProviderLastKnownStatus: TextView
    private lateinit var fusionProviderLastKnownLocationTxt: TextView
    private lateinit var fusionProviderMultipleUpdateStatus: TextView
    private lateinit var fusionProviderMultipleUpdateLocationTxt: TextView
    private lateinit var viewModel: LocationViewModel
    private val observer = getObserver()
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val suppressedException = exception.suppressed?.let { "with suppressed exception(s) ${it.contentToString()}" } ?: run { "" }
        @Suppress("NO_REFLECTION_IN_CLASS_PATH")
        Log.e(TAG, "${exception::class.simpleName} occurred in background $suppressedException", exception)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getLocationBtn = findViewById(R.id.get_location_btn)
        getLocationBtn.setOnClickListener(this)

        locationManagerLastKnownStatus = findViewById(R.id.location_manager_last_known_card_status)
        locationManagerLastKnownLocationTxt = findViewById(R.id.location_manager_last_known_card_location)

        locationManagerSingleUpdateStatus = findViewById(R.id.location_manager_single_update_card_status)
        locationManagerSingleUpdateLocationTxt = findViewById(R.id.location_manager_single_update_card_location)

        locationManagerMultipleUpdateStatus = findViewById(R.id.location_manager_multiple_updates_card_status)
        locationManagerMultipleUpdateLocationTxt = findViewById(R.id.location_manager_multiple_updates_card_location)

        fusionProviderLastKnownStatus = findViewById(R.id.fused_location_provider_last_known_card_status)
        fusionProviderLastKnownLocationTxt = findViewById(R.id.fused_location_provider_last_known_card_location)

        fusionProviderMultipleUpdateStatus = findViewById(R.id.fused_location_provider_multiple_updates_card_status)
        fusionProviderMultipleUpdateLocationTxt = findViewById(R.id.fused_location_provider_multiple_updates_card_location)

        viewModel = LocationViewModel(application)
        viewModel.locationLiveData.observe(this, observer)
    }


    override fun onResume() {
        super.onResume()

        // Update status of location providers
        coroutineScope.launch(coroutineExceptionHandler) {
            updateLocationProviderStatuses()
        }
    }

    override fun onDestroy() {
        viewModel.locationLiveData.removeObserver(observer)
        super.onDestroy()
    }

    private fun getObserver() : Observer<LocationModel> = Observer {
        it?.let {
            Log.d(TAG, "${it.type}, ${it.latitude}, ${it.longitude}")
            when(it.type) {
                LocatorManager.LOCATION_MANAGER_LAST_KNOWN -> {
                    locationManagerLastKnownStatus.text = DateFormat.getTimeInstance().format(it.time)
                    locationManagerLastKnownLocationTxt.text = if(it.latitude == 400.0) applicationContext.getString(R.string.location_not_found) else
                        "${Location.convert(it.latitude, Location.FORMAT_DEGREES)} ${Location.convert(it.longitude, Location.FORMAT_DEGREES)}"
                }
                LocatorManager.LOCATION_MANAGER_SINGLE_UPDATE -> {
                    locationManagerSingleUpdateStatus.text = DateFormat.getTimeInstance().format(it.time)
                    locationManagerSingleUpdateLocationTxt.text = if(it.latitude == 400.0) applicationContext.getString(R.string.location_not_found) else
                        "${Location.convert(it.latitude, Location.FORMAT_DEGREES)} ${Location.convert(it.longitude, Location.FORMAT_DEGREES)}"
                }
                LocatorManager.LOCATION_MANAGER_MULTIPLE_UPDATE -> {
                    locationManagerMultipleUpdateStatus.text = DateFormat.getTimeInstance().format(it.time)
                    locationManagerMultipleUpdateLocationTxt.text = if(it.latitude == 400.0) applicationContext.getString(R.string.location_not_found) else
                        "${Location.convert(it.latitude, Location.FORMAT_DEGREES)} ${Location.convert(it.longitude, Location.FORMAT_DEGREES)}"
                }
                LocatorManager.FUSION_CLIENT_LAST_KNOWN -> {
                    fusionProviderLastKnownStatus.text = DateFormat.getTimeInstance().format(it.time)
                    fusionProviderLastKnownLocationTxt.text = if(it.latitude == 400.0) applicationContext.getString(R.string.location_not_found) else
                        "${Location.convert(it.latitude, Location.FORMAT_DEGREES)} ${Location.convert(it.longitude, Location.FORMAT_DEGREES)}"
                }
                LocatorManager.FUSION_CLIENT_MULTIPLE_UPDATE -> {
                    fusionProviderMultipleUpdateStatus.text = DateFormat.getTimeInstance().format(it.time)
                    fusionProviderMultipleUpdateLocationTxt.text = if(it.latitude == 400.0) applicationContext.getString(R.string.location_not_found) else
                        "${Location.convert(it.latitude, Location.FORMAT_DEGREES)} ${Location.convert(it.longitude, Location.FORMAT_DEGREES)}"
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.get_location_btn -> {
                Log.i(TAG, "Getting location")
                updateLocationStatus()
                coroutineScope.launch(coroutineExceptionHandler) {
                    getLocation()
                    updateLocationProviderStatuses()
                }
            }
        }
    }

    private fun updateLocationStatus() {
        locationManagerLastKnownStatus.text = getString(R.string.waiting_for_location)
        locationManagerSingleUpdateStatus.text = getString(R.string.waiting_for_location)
        locationManagerMultipleUpdateStatus.text = getString(R.string.waiting_for_location)
        fusionProviderLastKnownStatus.text = getString(R.string.waiting_for_location)
        fusionProviderMultipleUpdateStatus.text = getString(R.string.waiting_for_location)
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            coroutineScope.launch(coroutineExceptionHandler) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(R.id.mainConstraintLayout), R.string.no_location_permission, Snackbar.LENGTH_SHORT).show()
                }
                return@launch
            }
        } else {
            viewModel.requestLocation()
        }
    }

    private fun updateLocationProviderStatuses() {
        val isGpsProviderEnabled = viewModel.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkProviderEnabled = viewModel.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isPassiveProviderEnabled = viewModel.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
        Log.i(TAG, "GPS: $isGpsProviderEnabled Network: $isNetworkProviderEnabled Passive: $isPassiveProviderEnabled")
        // TODO Show Status in UI
        /*coroutineScope.launch(coroutineExceptionHandler) {
            withContext(Dispatchers.Main) {
            }
        }*/
    }
}
