package dev.veeso.opentapowearos

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import dev.veeso.opentapowearos.tapo.device.*
import dev.veeso.opentapowearos.view.device_activity.Color
import dev.veeso.opentapowearos.view.device_activity.Color.Companion.COLOR_LIST
import dev.veeso.opentapowearos.view.intent_data.Credentials
import dev.veeso.opentapowearos.view.intent_data.GroupData
import kotlinx.coroutines.*


@OptIn(DelicateCoroutinesApi::class)
class GroupManagementActivity : Activity() {

    private lateinit var groupName: String
    private lateinit var devices: List<Device>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_management)
    }

    override fun onResume() {
        super.onResume()

        val groupData = intent.getParcelableExtra<GroupData>(GROUP_DATA_INTENT_NAME)
        val credentials = intent.getParcelableExtra<Credentials>(CREDENTIALS_INTENT_NAME)
        if (groupData != null && credentials != null) {
            this.groupName = groupData.groupName
            Log.d(TAG, String.format("Found groups %s", groupData))
            setDevicesFromData(groupData)
            login(credentials)
            populateView()
        } else {
            Log.d(TAG, "Intent group data was empty; terminating activity")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setDevicesFromData(groupData: GroupData) {
        this.devices = groupData.devices.map {
            DeviceBuilder.buildDevice(
                it.alias,
                it.id,
                it.model,
                it.endpoint,
                it.ipAddress,
                it.status
            )
        }
    }

    private fun login(credentials: Credentials) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                devices.forEach {
                    if (!it.authenticated) {
                        it.login(credentials.username, credentials.password)
                    }
                }
                fetchDevicesState()
                setLoading(false)
            }
        }
    }

    private fun populateView() {
        val nameText: TextView = findViewById(R.id.group_management_name)
        nameText.text = groupName

        // switch ON/OFF
        val powerStateSwitch: Switch = findViewById(R.id.group_management_power)
        val powerState = devices.all { it.status.deviceOn }
        setPowerView(powerState)
        powerStateSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, String.format("Changing power state for %s to %s", groupName, isChecked))
            setPowerView(isChecked)
            setPowerState(isChecked)
        }
        // brightness
        val brightnessSeekBar: SeekBar = findViewById(R.id.group_management_brightness)
        val hasBrightness = this.devices.any {
            it.type == DeviceType.LIGHT_BULB || it.type == DeviceType.RGB_LIGHT_BULB
        }
        if (hasBrightness) {
            brightnessSeekBar.visibility = View.VISIBLE
            val brightnessValue = this.devices.maxOf { it.status.brightness ?: 1 }
            setBrightnessView(brightnessValue)
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
        val colorList: Spinner = findViewById(R.id.group_management_color)
        val hasColor = this.devices.any {
            it.type == DeviceType.RGB_LIGHT_BULB
        }
        if (hasColor) {
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

    private fun fetchDevicesState() {
        Log.d(TAG, "Fetching device state...")
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                devices.forEach {
                    try {
                        val deviceInfo = it.getDeviceStatus()
                        setPowerView(deviceInfo.deviceOn)
                        if (deviceInfo.brightness != null) {
                            setBrightnessView(deviceInfo.brightness)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, String.format("failed to collect device state: %s", e))
                    }
                }
                setLoading(false)
            }
        }
    }

    private fun setPowerView(state: Boolean) {
        runOnUiThread {
            val powerState: Switch = findViewById(R.id.group_management_power)
            powerState.isChecked = state
            val powerLabel: TextView = findViewById(R.id.group_management_power_label)
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
                findViewById(R.id.group_management_brightness_label)
            brightnessLabel.text =
                String.format(
                    resources.getString(R.string.device_activity_brightness),
                    brightness
                )
            val brightnessSeekBar: SeekBar = findViewById(R.id.group_management_brightness)
            brightnessSeekBar.progress = brightness
        }
    }

    private fun setPowerState(state: Boolean) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                devices.forEach {
                    try {
                        if (state) {
                            it.on()
                        } else {
                            it.off()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, String.format("Failed to set power state: %s", e))
                        // revert state
                        setPowerView(!state)
                    }
                }
                fetchDevicesState()
                setLoading(false)
            }
        }
    }

    private fun setBrightness(brightness: Int) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                devices.forEach {
                    try {
                        when (it) {
                            is L510 -> {
                                it.setBrightness(brightness)
                            }
                            is L520 -> {
                                it.setBrightness(brightness)
                            }
                            is L530 -> {
                                it.setBrightness(brightness)
                            }
                            is L610 -> {
                                it.setBrightness(brightness)
                            }
                            is L630 -> {
                                it.setBrightness(brightness)
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, String.format("Failed to set brightness: %s", e))
                    }
                    fetchDevicesState()
                    setPowerView(true)
                }
                setLoading(false)
            }
        }
    }

    private fun setColor(color: Color) {
        setLoading(true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    devices.forEach {
                        when (it) {
                            is L530 -> {
                                (it).setColor(color)
                            }
                            is L630 -> {
                                (it).setColor(color)
                            }
                            else -> {}
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, String.format("Failed to set color: %s", e))
                }
                fetchDevicesState()
                setPowerView(true)
                setLoading(false)
            }
        }

    }

    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            val dataLayout: LinearLayout = findViewById(R.id.group_management_data)
            val loadingLayout: LinearLayout = findViewById(R.id.group_management_activity_wait)
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
        const val GROUP_DATA_INTENT_NAME = "GroupData"
        const val CREDENTIALS_INTENT_NAME = "Credentials"
    }

}
