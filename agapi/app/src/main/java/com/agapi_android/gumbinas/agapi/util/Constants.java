package com.agapi_android.gumbinas.agapi.util;

import java.util.UUID;

public class Constants {

    public static String SERVICE_STRING = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);

    public static String CHARACTERISTIC_ECHO_STRING = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING);

    public static final long SCAN_PERIOD = 5000;
}