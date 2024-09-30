package com.mubbashir.androidble

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.mubbashir.androidble.ble.BLEListener
import com.mubbashir.androidble.ble.BLEServer
import com.mubbashir.androidble.theme.AndroidBLETheme

class ServerActivity : ComponentActivity(), BLEListener {

    private val bleServer = BLEServer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidBLETheme {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Start BLE Server",
                        modifier = Modifier
                            .background(Color.Red)
                            .padding(10.dp)
                            .clickable {
                                checkPermissions()
                            },
                        color = Color.White

                    )
                }
            }
        }

    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { isGranted -> isGranted.value }) {
                openBle()
            } else {
                // Permissions not granted, Handle accordingly
            }
        }

    private fun checkPermissions() {
        val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        bluetoothPermissions.filter {
            ActivityCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }.let {
            if (it.isNotEmpty()) {
                if (it.any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permission
                        )
                    }) {
                    // Take user to settings screen or check rationale
                } else {
                    permissionLauncher.launch(it.toTypedArray())
                }
            } else {
                openBle()
            }
        }
    }

    private fun openBle() {
        bleServer.addListener(this)
        bleServer.startServer()
        showToast("BLE Server started")
    }

    private val blueToothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                openBle()
            } else {
                showToast("Bluetooth not enabled")
                finish()
            }
        }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String) {
        showToast(error)
        finish()
    }

    override fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        blueToothEnableLauncher.launch(enableBtIntent)
    }

    override fun onSuccess(data: String) {

    }

    override fun scanResult(isConnected: Boolean) {
        super.scanResult(isConnected)

    }

    override fun onDestroy() {
        super.onDestroy()
        bleServer.stopServer()
    }

}

