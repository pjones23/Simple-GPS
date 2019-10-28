package com.vmw.simplegps.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocatorManager(context: Context) {
    private val locationManager: LocationManager = context.getSystemService(LocationManager::class.java) as LocationManager
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationManagerSingleRequestHandlerThread: HandlerThread
    private lateinit var locationManagerSingleRequestLocationListener: TypedLocationListener
    private lateinit var locationManagerMultipleRequestHandlerThread: HandlerThread
    private lateinit var locationManagerMultipleRequestLocationListener: TypedLocationListener
    private lateinit var fusionProviderMultipleRequestHandlerThread: HandlerThread
    private lateinit var fusionProviderMultipleRequestLocationCallback: TypedLocationCallback
    private lateinit var locationRequest: LocationRequest

    @SuppressLint("MissingPermission")
    fun requestLocation(locatorCallback: LocatorCallback, provider: String) {
        Log.i(TAG, "Requesting location with $provider provider")

        // Get last known location from LocationManager
        locationManager.getLastKnownLocation(provider)?.let{ locatorCallback.locationUpdated(LOCATION_MANAGER_LAST_KNOWN, it) }
            ?:run { locatorCallback.locationNotKnown(LOCATION_MANAGER_LAST_KNOWN) }

        if(!::locationManagerSingleRequestHandlerThread.isInitialized) {
            locationManagerSingleRequestHandlerThread = HandlerThread("lmsr", HandlerThread.MAX_PRIORITY)
            locationManagerSingleRequestHandlerThread.start()
        }

        if(!::locationManagerMultipleRequestHandlerThread.isInitialized) {
            locationManagerMultipleRequestHandlerThread = HandlerThread("lmmr", HandlerThread.MAX_PRIORITY)
            locationManagerMultipleRequestHandlerThread.start()
        }

        if(!::fusionProviderMultipleRequestHandlerThread.isInitialized) {
            fusionProviderMultipleRequestHandlerThread = HandlerThread("fpmr", HandlerThread.MAX_PRIORITY)
            fusionProviderMultipleRequestHandlerThread.start()
        }

        // Get single location update from LocationManager
        if(::locationManagerSingleRequestLocationListener.isInitialized) {
            locationManager.removeUpdates(locationManagerSingleRequestLocationListener)
        }
        locationManagerSingleRequestLocationListener = TypedLocationListener(LOCATION_MANAGER_SINGLE_UPDATE, locatorCallback)
        locationManager.requestSingleUpdate(provider, locationManagerSingleRequestLocationListener, locationManagerSingleRequestHandlerThread.looper)

        // Get multiple location updates from LocationManager
        if(::locationManagerMultipleRequestLocationListener.isInitialized) {
            locationManager.removeUpdates(locationManagerMultipleRequestLocationListener)
        }
        locationManagerMultipleRequestLocationListener = TypedLocationListener(LOCATION_MANAGER_MULTIPLE_UPDATE, locatorCallback)
        locationManager.requestLocationUpdates(provider, 3000L, 0f, locationManagerMultipleRequestLocationListener, locationManagerMultipleRequestHandlerThread.looper )

        // Get last known location from Fused Location Provider Client
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { it?.let { locatorCallback.locationUpdated(FUSION_CLIENT_LAST_KNOWN, it) }?:run {
            Log.e(TAG, "No location provided")
        } }

        fusedLocationProviderClient.lastLocation.addOnFailureListener { locatorCallback.locationNotKnown(FUSION_CLIENT_LAST_KNOWN) }

        fusedLocationProviderClient.lastLocation.addOnCanceledListener { locatorCallback.locationNotKnown(FUSION_CLIENT_LAST_KNOWN) }

        // Get multiple location updates from Fused Location Provider Client
        if(!::locationRequest.isInitialized) {
            locationRequest = LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000L).setFastestInterval(1000L)
        }
        if(::fusionProviderMultipleRequestLocationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(fusionProviderMultipleRequestLocationCallback)
        }
        fusionProviderMultipleRequestLocationCallback = TypedLocationCallback(FUSION_CLIENT_MULTIPLE_UPDATE, locatorCallback)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, fusionProviderMultipleRequestLocationCallback, fusionProviderMultipleRequestHandlerThread.looper)
    }

    fun stopLocationUpdates() {
        Log.i(TAG, "Stopping location updates")

        if(::locationManagerSingleRequestLocationListener.isInitialized) {
            locationManager.removeUpdates(locationManagerSingleRequestLocationListener)
        }

        if(::locationManagerMultipleRequestLocationListener.isInitialized) {
            locationManager.removeUpdates(locationManagerMultipleRequestLocationListener)
        }

        if(::fusionProviderMultipleRequestLocationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(fusionProviderMultipleRequestLocationCallback)
        }
    }

    fun isProviderEnabled(provider: String) : Boolean = if(LocationManager.GPS_PROVIDER == provider || LocationManager.NETWORK_PROVIDER == provider || LocationManager.PASSIVE_PROVIDER == provider)
            locationManager.isProviderEnabled(provider)
        else false

    companion object {

        private const val TAG = "LocatorManager"
        private var INSTANCE : LocatorManager? = null
        const val LOCATION_MANAGER_LAST_KNOWN = 0
        const val LOCATION_MANAGER_SINGLE_UPDATE = 1
        const val LOCATION_MANAGER_MULTIPLE_UPDATE = 2
        const val FUSION_CLIENT_LAST_KNOWN = 3
        const val FUSION_CLIENT_MULTIPLE_UPDATE = 4
        const val LOCATION_MANAGER_PROVIDER_STATUS_MODIFIED = 5

        const val INVALID_LOCATION = 400.0

        fun getInstance(context: Context) : LocatorManager = INSTANCE?:let {
            INSTANCE = LocatorManager(context)
            INSTANCE!!
        }
    }

    interface LocatorCallback {
        fun locationUpdated(type: Int, location: Location)
        fun locationNotKnown(type: Int)
        fun locationManagerProviderStatusModified()
    }
}