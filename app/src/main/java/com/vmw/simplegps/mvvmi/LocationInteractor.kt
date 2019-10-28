package com.vmw.simplegps.mvvmi

import android.content.Context
import android.location.Location
import com.vmw.simplegps.location.LocatorManager

class LocationInteractor(val callback: LocationViewModel.LocationViewModelCallback) {

    fun requestLocations(context: Context, provider: String) {
        LocatorManager.getInstance(context).requestLocation(object : LocatorManager.LocatorCallback{
            override fun locationUpdated(type: Int, location: Location) {
                callback.locationUpdated(type, location)
            }

            override fun locationNotKnown(type: Int) {
                callback.locationNotKnown(type)
            }

            override fun locationManagerProviderStatusModified() {
                callback.locationManagerProviderStatusModified()
            }
        }, provider)
    }

    fun isProviderEnabled(context: Context, provider: String) : Boolean = LocatorManager.getInstance(context).isProviderEnabled(provider)

    fun stopLocationUpdates(context: Context) = LocatorManager.getInstance(context).stopLocationUpdates()

}