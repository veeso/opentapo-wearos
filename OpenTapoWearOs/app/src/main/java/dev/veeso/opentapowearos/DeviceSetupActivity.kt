package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.view.device_setup.ActivityState
import dev.veeso.opentapowearos.view.intent_data.Credentials
import dev.veeso.opentapowearos.view.intent_data.DeviceData
import kotlinx.coroutines.*
import java.net.Inet4Address

@OptIn(DelicateCoroutinesApi::class)
class DeviceSetupActivity : Activity() {

    private lateinit var credentials: Credentials

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_device_setup)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        val credentials = intent.getParcelableExtra<Credentials>(INTENT_INPUT)
        if (credentials != null) {
            this.credentials = credentials
            val ipAddressText: EditText = findViewById(R.id.device_setup_ip_address)
            ipAddressText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onIpAddressDone(v as EditText)
                    true
                } else {
                    false
                }
            }
            setActivityState(ActivityState.FORM)
        } else {
            Log.d(TAG, "Intent credentials was empty; terminating activity")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun onIpAddressDone(v: EditText) {
        val addressText = v.text.toString()
        Log.d(TAG, String.format("Ip address chosen for the device: %s", addressText))

        try {
            val address = Inet4Address.getByName(addressText)
            setupDeviceByAddress(address as Inet4Address)
        } catch (e: Exception) {
            setActivityState(ActivityState.FORM)
            setError(R.string.device_setup_activity_error_bad_address)
        }
    }

    private fun setupDeviceByAddress(address: Inet4Address) {
        Log.d(TAG, String.format("setupDeviceByAddress: %s", address))
        setActivityState(ActivityState.CONFIGURATION)
        setLoadingMessage(R.string.device_setup_activity_loading_reachable)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                if (address.isReachable(3000)) {
                    doDeviceHandshake(address)
                } else {
                    setError(R.string.device_setup_activity_error_unreachable)
                    setActivityState(ActivityState.FORM)
                }
            }
        }
    }

    private suspend fun doDeviceHandshake(address: Inet4Address) {
        Log.d(TAG, String.format("Do device handshake with: %s", address))
        setLoadingMessage(R.string.device_setup_activity_loading_setup)
        val client = TapoClient(address)
        try {
            Log.d(TAG, "Signing in to device")
            client.login(credentials.username, credentials.password)
            Log.d(
                TAG,
                "Successfully signed in to device; getting device info..."
            )
            val device = client.queryDevice()
            Log.d(
                TAG,
                String.format("Found device of type %s with name %s", device.type, device.alias)
            )

            val intent = Intent()
            intent.putExtra(
                INTENT_OUTPUT, DeviceData(
                    alias = device.alias,
                    id = device.id,
                    model = device.model,
                    endpoint = device.endpoint,
                    ipAddress = device.ipAddress,
                    status = device.status
                )
            )
            setResult(RESULT_OK, intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, String.format("Failed to fetch device: %s", e))
            setError(R.string.device_setup_activity_error_handhshake_failed)
            setActivityState(ActivityState.FORM)
        }
    }

    // view functions

    private fun setActivityState(state: ActivityState) {
        when (state) {
            ActivityState.FORM -> enterFormState()
            ActivityState.CONFIGURATION -> enterConfigurationState()
        }
    }

    private fun enterConfigurationState() {
        runOnUiThread {
            val formLayout: LinearLayout = findViewById(R.id.device_setup_form)
            val loadingLayout: LinearLayout = findViewById(R.id.device_setup_loading)
            formLayout.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun enterFormState() {
        runOnUiThread {
            val formLayout: LinearLayout = findViewById(R.id.device_setup_form)
            val loadingLayout: LinearLayout = findViewById(R.id.device_setup_loading)
            formLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
        }
    }

    private fun setLoadingMessage(id: Int) {
        runOnUiThread {
            val message = resources.getString(id)
            Log.d(TAG, String.format("Set loading message: %s", message))
            val loadingText: TextView = findViewById(R.id.device_setup_loading_message)
            loadingText.text = message
        }
    }

    private fun setError(id: Int) {
        runOnUiThread {
            val error = resources.getString(id)
            Log.d(TAG, String.format("Set error: %s", error))
            val errorText: TextView = findViewById(R.id.device_setup_error)
            errorText.text = error
            errorText.visibility = View.VISIBLE
        }
    }

    companion object {
        const val INTENT_INPUT = "DeviceSetupInput"
        const val INTENT_OUTPUT = "DeviceSetupOutput"
        const val TAG = "DeviceSetupActivity"
    }

}
