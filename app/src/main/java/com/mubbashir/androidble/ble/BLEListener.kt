package com.mubbashir.androidble.ble

interface BLEListener {
    fun onError(error: String)
    fun enableBluetooth()
    fun onSuccess(data: String)
    fun scanResult(isConnected: Boolean) {}
}