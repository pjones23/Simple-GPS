package com.vmw.simplegps.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     * update.
     */
    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}