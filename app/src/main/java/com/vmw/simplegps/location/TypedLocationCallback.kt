package com.vmw.simplegps.location

import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class TypedLocationCallback(private val type: Int, private val locatorCallback: LocatorManager.LocatorCallback) : LocationCallback() {

    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult?.lastLocation?.let { locatorCallback.locationUpdated(type, it) } ?: run { locatorCallback.locationNotKnown(type) }
    }

    override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
        locationAvailability?.let {
            if(!it.isLocationAvailable) {
                locatorCallback.locationNotKnown(type)
            }
        }?:run {locatorCallback.locationNotKnown(type)}
    }
}