package com.vmw.simplegps.mvvmi

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vmw.simplegps.location.LocatorManager

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    val locationLiveData = MutableLiveData<LocationModel>()
    private val callback = object : LocationViewModelCallback {
        override fun locationUpdated(type: Int, location: Location) {
            locationLiveData.postValue(LocationModel(type, location.latitude, location.longitude, System.currentTimeMillis()))
        }

        override fun locationNotKnown(type: Int) {
            locationLiveData.postValue(LocationModel(type, LocatorManager.INVALID_LOCATION, LocatorManager.INVALID_LOCATION, System.currentTimeMillis()))
        }

        override fun locationManagerProviderStatusModified() {
            locationLiveData.postValue(LocationModel(LocatorManager.LOCATION_MANAGER_PROVIDER_STATUS_MODIFIED, LocatorManager.INVALID_LOCATION, LocatorManager.INVALID_LOCATION, System.currentTimeMillis()))
        }
    }

    private val interactor = LocationInteractor(callback)

    fun requestLocation(provider: String) {
        interactor.requestLocations((getApplication() as Application).applicationContext, provider)
    }

    fun isProviderEnabled(provider: String) : Boolean = interactor.isProviderEnabled((getApplication() as Application).applicationContext, provider)

    fun stopLocationUpdates() = interactor.stopLocationUpdates((getApplication() as Application).applicationContext)

    interface LocationViewModelCallback {
        fun locationUpdated(type: Int, location: Location)
        fun locationNotKnown(type: Int)
        fun locationManagerProviderStatusModified()
    }

}