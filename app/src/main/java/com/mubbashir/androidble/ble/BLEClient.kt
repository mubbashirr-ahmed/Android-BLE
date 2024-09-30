package com.mubbashir.androidble.ble


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

class BLEClient(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var listener: BLEListener? = null

    companion object {
        const val SERVICE_UUID = "0000180D-0000-1000-8000-00805f9b34fb"
        const val CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
    }

    fun addListener(listener: BLEListener) {
        this.listener = listener
    }

    fun startClient() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            listener?.onError("Bluetooth is not available")
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            listener?.enableBluetooth()
            return
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter?.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        logger("BLE Client started")

    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            logger("onScanResult: $result")
            result?.device?.let { device ->
                if (result.scanRecord?.deviceName == "MyAdapter") {
                    logger("Found device: ${device.address}")
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
                    connectToDevice(device)

                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            logger("Scan Failed: $errorCode")
            listener?.scanResult(false)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            logger("onBatchScanResults: $results")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        logger("Connecting to device: ${device.address}")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            logger("onConnectionStateChange: $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logger("Connected to server")
                listener?.scanResult(true)
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logger("Disconnected from server")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            logger("onServicesDiscovered status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                requestData(gatt)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int
        ) {
            logger("onCharacteristicRead status: $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val receivedData = characteristic?.getStringValue(0) ?: ""
                logger("onCharacteristicRead: $receivedData")
                listener?.onSuccess(receivedData)
                gatt?.close()
            }
        }
    }

    private fun requestData(gatt: BluetoothGatt?) {
        val characteristic = gatt?.getService(UUID.fromString(SERVICE_UUID))
            ?.getCharacteristic(UUID.fromString(CHAR_UUID))
        gatt?.readCharacteristic(characteristic)
    }


    private fun logger(string: String) {
        Log.d("BLEClientTag", string)
    }

    fun stopClient() {
        try {
            bluetoothGatt?.close()
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {

        }
    }
}