package com.campionpumps.webservices.qrcode;

import java.util.UUID;

public class Constants {

    private static String SERVICE_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    private static String CHARACTERISTIC_STRING = "0000ffe2-0000-1000-8000-00805f9b34fb";
    public static UUID CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_STRING);

    private static String DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb";
    public static UUID DESCRIPTOR_UUID = UUID.fromString(DESCRIPTOR_STRING);

    public static final long SCAN_PERIOD = 5000;

    public static final String LOGIN_URL = "https://myscadacloud.com/ScadaCloudAPI/api/auth/login";

    public static final String defaultUserName = "qrlogin";
    public static final String defaultPassword = "qrpassword";

}
