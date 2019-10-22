package com.vmw.simplegps.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log

private const val TAG = "TypedLocationListener"

class TypedLocationListener(private val type: Int, private val locatorCallback: LocatorManager.LocatorCallback) : LocationListener {
    /**
     * Called when the location has changed.
     *
     *
     *  There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    override fun onLocationChanged(location: Location?) {
        location?.let { locatorCallback.locationUpdated(type, it) } ?: run { locatorCallback.locationNotKnown(type) }
    }

    /**
     * This callback will never be invoked and providers can be considers as always in the
     * [LocationProvider.AVAILABLE] state.
     *
     */
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderEnabled(provider: String?) {
        Log.i(TAG, "Provider enabled: $provider")
    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderDisabled(provider: String?) {
        Log.i(TAG, "Provider enabled: $provider")
    }
}