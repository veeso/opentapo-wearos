package dev.veeso.opentapowearos

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.tapo.device.DeviceBuilder
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class DeviceActivity : Activity() {

    private lateinit var device: Device

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.device_activity)
    }

    override fun onResume() {
        super.onResume()

        val deviceData = intent.getParcelableExtra<DeviceData>(DEVICE_DATA_INTENT_NAME)
        val credentials = intent.getParcelableExtra<Credentials>(CREDENTIALS_INTENT_NAME)
        if (deviceData != null && credentials != null) {
            Log.d(TAG, String.format("Found device %s", deviceData.alias))
            setDeviceFromData(deviceData)
            login(credentials)
            populateView()
        } else {
            Log.d(TAG, "Intent device data was empty; terminating activity")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setDeviceFromData(deviceData: DeviceData) {
        this.device = DeviceBuilder.buildDevice(
            deviceData.alias,
            deviceData.id,
            deviceData.model,
            deviceData.endpoint
        )
    }

    private fun login(credentials: Credentials) {
        runBlocking {
            withContext(Dispatchers.IO) {
                device.login(credentials.username, credentials.password)
            }
        }
    }

    private fun populateView() {
        val aliasText: TextView = findViewById(R.id.device_activity_alias)
        aliasText.text = device.alias
        val modelText: TextView = findViewById(R.id.device_activity_model)
        modelText.text = device.model.toString()

        // switch
        val powerState: Switch = findViewById(R.id.device_activity_power)
        powerState.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, String.format("Changing power state for %s to %s", device.alias, isChecked))
            setPowerState(isChecked)
        }
    }

    private suspend fun fetchDeviceState() {
        Log.d(TAG, "Fetching device state...")
        TODO()
    }

    private fun setPowerState(state: Boolean) {
        runBlocking {
            withContext(Dispatchers.IO) {
                if (state) {
                    device.on()
                } else {
                    device.off()
                }
            }
        }
    }

    companion object {
        const val TAG = "DeviceActivity"
        const val DEVICE_DATA_INTENT_NAME = "DeviceData"
        const val CREDENTIALS_INTENT_NAME = "Credentials"
    }

}
