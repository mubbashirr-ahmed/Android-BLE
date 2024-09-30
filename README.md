This repository demonstrates the implementation of Bluetooth Low Energy (BLE) communication in an Android app. It consists of two activities: ClientActivity and ServerActivity, which simulate a BLE client and a BLE server, respectively.

**Features**

ClientActivity: Scans for BLE devices, connects to the BLE server, and requests data.
ServerActivity: Acts as a BLE server, advertising services that the BLE client can interact with.

**Project Structure**

ClientActivity: The BLE client functionality is implemented using BLEClient. The client scans for devices with a specific service UUID, connects to the server, and requests data.
ServerActivity: The BLE server functionality is implemented using BLEServer. The server advertises a specific service and handles incoming connections from BLE clients.

**Package**: com.mubbashir.androidble

ClientActivity: Responsible for starting the BLE client, scanning for available servers, and requesting data.
ServerActivity: Responsible for starting the BLE server and managing the interaction with connected BLE clients.
BLEClient: Contains all the BLE client-related logic such as scanning, connecting, and reading data from the BLE server.
BLEServer: Manages BLE server functions like advertising and handling client connections.
BLEListener: Interface used for callback communication between BLE components and the activities.

**Permissions**

Bluetooth permissions: The app requires Bluetooth and location permissions for scanning BLE devices.
Add the following permissions in AndroidManifest.xml:

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />


**How to Run**

1. Clone the repository: git clone https://github.com/yourusername/android-ble-example.git
2. Open the project in Android Studio.
2. Build and run the project on a physical device (BLE functionality is not available on emulators).

**Usage** 

ClientActivity
Start the BLE client on Device1 by clicking the "Start BLE Client" button.
The client scans for BLE devices advertising a specific UUID (SERVICE_UUID).
Once connected, it will request data from the BLE server.

ServerActivity
Start the BLE server on Device2 by clicking the "Start BLE Server" button.
The server begins advertising a service that the client can connect to.

**Notes**
This project uses Jetpack Compose for the UI.
Make sure to test this on a physical device, as BLE features are not supported on Android emulators.
