package com.mubbashir.androidble.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BLEServer(private val context: Context) {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var listener: BLEListener? = null
    private var data = "Hello from Server"

    companion object {
        private const val SERVICE_UUID =
            "0000180D-0000-1000-8000-00805f9b34fb" // Example UUID for service
        private const val CHAR_UUID =
            "00002a37-0000-1000-8000-00805f9b34fb"   // Example UUID for characteristic
    }

    fun startServer() {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null) {
            listener?.onError("Bluetooth is not available")
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            listener?.enableBluetooth()
            return
        }

        bluetoothAdapter?.let {
            it.name = "MyAdapter"
            advertiser = it.bluetoothLeAdvertiser
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .build()

            advertiser?.startAdvertising(settings, data, advertiseCallback)
        }

        initGattServer()
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logger("Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            logger("Advertising failed with error: $errorCode")
        }
    }

    private fun initGattServer() {
        bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)

        val service = BluetoothGattService(
            UUID.fromString(SERVICE_UUID),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val characteristic = BluetoothGattCharacteristic(
            UUID.fromString(CHAR_UUID),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(characteristic)
        bluetoothGattServer?.addService(service)
        logger("Gatt server initialized")

    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logger("Device connected: ${device?.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logger("Device disconnected")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            logger("onCharacteristicReadRequest $requestId")
            if (characteristic?.uuid == UUID.fromString(CHAR_UUID)) {

                val responseData = data.toByteArray()
                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    responseData
                )

            }
        }


    }

    fun stopServer() {
        advertiser?.stopAdvertising(advertiseCallback)
        bluetoothGattServer?.close()
    }

    private fun logger(string: String) {
        Log.d("BLEClientTag", string)
    }

    fun addListener(listener: BLEListener) {
        this.listener = listener
    }

    fun setData(data: String) {
        this.data = data
    }

}