/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.example.androidthings.peripherals;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";
    private static final String DEVICE_RPI = "rpi3";
    private static final String BUTTON_PIN_NAME = Build.DEVICE.equals(DEVICE_RPI)
            ? "BCM21" : "GPIO6_IO14";
    private static final String LED_PIN_NAME = Build.DEVICE.equals(DEVICE_RPI)
            ? "BCM6" : "GPIO2_IO02";

    // GPIO connection to button input
    private Gpio mButtonGpio;
    // GPIO connection to LED output
    private Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager pioManager = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + pioManager.getGpioList());

        try {
            // Create GPIO connection.
            mButtonGpio = pioManager.openGpio(BUTTON_PIN_NAME);

            // Configure as an input, trigger events on every change.
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // Value is true when the pin is LOW
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);
            // Register the event callback.
            mButtonGpio.registerGpioCallback(mCallback);

            mLedGpio = pioManager.openGpio(LED_PIN_NAME);
            // Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.w(TAG, "Error opening GPIO", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the button
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing GPIO", e);
            }
        }

        // Close the LED.
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing GPIO", e);
            }
        }
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                boolean buttonValue = gpio.getValue();
                mLedGpio.setValue(buttonValue);
            } catch (IOException e) {
                Log.w(TAG, "Error reading GPIO");
            }

            // Return true to keep callback active.
            return true;
        }
    };
}
