package com.campionpumps.webservices.qrcode

import java.util.*

object Constants {
    private const val SERVICE_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb"
    @JvmField
    var SERVICE_UUID = UUID.fromString(SERVICE_STRING)
    private const val CHARACTERISTIC_STRING = "0000ffe2-0000-1000-8000-00805f9b34fb"
    var CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_STRING)
    private const val DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb"
    var DESCRIPTOR_UUID = UUID.fromString(DESCRIPTOR_STRING)
    const val SCAN_PERIOD: Long = 5000
    const val LOGIN_URL = "https://myscadacloud.com/ScadaCloudAPI/api/auth/login"
    const val defaultUserName = "qrlogin"
    const val defaultPassword = "qrpassword"
}