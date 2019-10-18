package com.vmw.simplegps.mvvmi

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    val locationLiveData = MutableLiveData<LocationModel>()
    private val callback = object : LocationViewModelCallback {
        override fun locationUpdated(type: Int, location: Location) {
            locationLiveData.postValue(LocationModel(type, location.latitude, location.longitude, System.currentTimeMillis()))
        }

        override fun locationNotKnown(type: Int) {
            locationLiveData.postValue(LocationModel(type, 400.0, 400.0, System.currentTimeMillis()))
        }
    }

    private val interactor = LocationInteractor(callback)

    fun requestLocation() {
        interactor.requestLocations((getApplication() as Application).applicationContext)
    }

    interface LocationViewModelCallback {
        fun locationUpdated(type: Int, location: Location)
        fun locationNotKnown(type: Int)
    }

}