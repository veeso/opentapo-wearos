package dev.veeso.opentapowearos

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import dev.veeso.opentapowearos.tapo.device.*
import dev.veeso.opentapowearos.view.Color
import dev.veeso.opentapowearos.view.Color.Companion.COLOR_LIST
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceData
import kotlinx.coroutines.*


@OptIn(DelicateCoroutinesApi::class)
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
            deviceData.endpoint,
            deviceData.ipAddress,
            deviceData.status
        )
    }

    private fun login(credentials: Credentials) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                if (!device.authenticated) {
                    device.login(credentials.username, credentials.password)
                }
                fetchDeviceState()
                setLoading(false)
            }
        }
    }

    private fun populateView() {
        val aliasText: TextView = findViewById(R.id.device_activity_alias)
        aliasText.text = device.alias
        val modelText: TextView = findViewById(R.id.device_activity_model)
        modelText.text = device.model.toString()

        // switch ON/OFF
        val powerState: Switch = findViewById(R.id.device_activity_power)
        setPowerView(device.status.deviceOn)
        powerState.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, String.format("Changing power state for %s to %s", device.alias, isChecked))
            setPowerView(isChecked)
            setPowerState(isChecked)
        }
        // brightness
        val brightnessSeekBar: SeekBar = findViewById(R.id.device_activity_brightness)
        if (device.type == DeviceType.LIGHT_BULB || device.type == DeviceType.RGB_LIGHT_BULB) {
            brightnessSeekBar.visibility = View.VISIBLE
            setBrightnessView(device.status.brightness ?: 1)
            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        setBrightnessView(progress)
                        setBrightness(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        } else {
            brightnessSeekBar.visibility = View.GONE
        }
        // color
        val colorList: Spinner = findViewById(R.id.device_activity_color)
        if (device.type == DeviceType.RGB_LIGHT_BULB) {
            colorList.visibility = View.VISIBLE
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, COLOR_LIST)
            colorList.adapter = adapter
            colorList.setSelection(0, false)
            colorList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    // your code here
                    val color = Color.fromString(COLOR_LIST[position])
                    Log.d(TAG, String.format("Setting lamp color to %s", color))
                    setColor(color)
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    // your code here
                }
            }
        } else {
            colorList.visibility = View.GONE
        }
    }

    private fun fetchDeviceState() {
        Log.d(TAG, "Fetching device state...")
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val deviceInfo = device.getDeviceStatus()
                setPowerView(deviceInfo.deviceOn)
                if (deviceInfo.brightness != null) {
                    setBrightnessView(deviceInfo.brightness)
                }
                setLoading(false)
            }
        }
    }

    private fun setPowerView(state: Boolean) {
        runOnUiThread {
            val powerState: Switch = findViewById(R.id.device_activity_power)
            powerState.isChecked = state
            val powerLabel: TextView = findViewById(R.id.device_activity_power_label)
            powerLabel.text = String.format(
                resources.getString(R.string.device_activity_power), if (state) {
                    resources.getString(R.string.device_activity_power_on)
                } else {
                    resources.getString(R.string.device_activity_power_off)
                }
            )
        }
    }

    private fun setBrightnessView(brightness: Int) {
        runOnUiThread {
            val brightnessLabel: TextView =
                findViewById(R.id.device_activity_brightness_label)
            brightnessLabel.text =
                String.format(
                    resources.getString(R.string.device_activity_brightness),
                    brightness
                )
            val brightnessSeekBar: SeekBar = findViewById(R.id.device_activity_brightness)
            brightnessSeekBar.progress = brightness
        }
    }

    private fun setPowerState(state: Boolean) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                if (state) {
                    device.on()
                } else {
                    device.off()
                }
                fetchDeviceState()
                setLoading(false)
            }
        }
    }

    private fun setBrightness(brightness: Int) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                when (device) {
                    is L510 -> {
                        (device as L510).setBrightness(brightness)
                    }
                    is L520 -> {
                        (device as L520).setBrightness(brightness)
                    }
                    is L530 -> {
                        (device as L530).setBrightness(brightness)
                    }
                    is L610 -> {
                        (device as L610).setBrightness(brightness)
                    }
                    is L630 -> {
                        (device as L630).setBrightness(brightness)
                    }
                }
                fetchDeviceState()
                setLoading(false)
            }
        }
    }

    private fun setColor(color: Color) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                when (device) {
                    is L530 -> {
                        (device as L530).setColor(color)
                    }
                    is L630 -> {
                        (device as L630).setColor(color)
                    }
                }
                fetchDeviceState()
                setLoading(false)
            }
        }

    }

    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            val dataLayout: LinearLayout = findViewById(R.id.device_activity_data)
            val loadingLayout: LinearLayout = findViewById(R.id.device_activity_wait)
            if (loading) {
                dataLayout.visibility = View.GONE
                loadingLayout.visibility = View.VISIBLE
            } else {
                dataLayout.visibility = View.VISIBLE
                loadingLayout.visibility = View.GONE
            }
        }
    }

    companion object {
        const val TAG = "DeviceActivity"
        const val DEVICE_DATA_INTENT_NAME = "DeviceData"
        const val CREDENTIALS_INTENT_NAME = "Credentials"
    }

}
