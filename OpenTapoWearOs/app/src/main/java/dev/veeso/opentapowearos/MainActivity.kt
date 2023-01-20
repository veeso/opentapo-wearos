package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.databinding.ActivityMainBinding
import dev.veeso.opentapowearos.tapo.api.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    // credentials
    private var credentials: Credentials? = null

    // devices
    private var devices: MutableList<Device> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        readCredentialsFromPrefs()

    }

    override fun onResume() {
        super.onResume()

        // read credentials from intent
        if (this.credentials == null) {
            val credentials = intent.getParcelableExtra<Credentials>(LoginActivity.INTENT_OUTPUT)
            if (credentials != null) {
                this.credentials = credentials
                // discover devices
                runBlocking {
                    withContext(Dispatchers.IO) {
                        val client = TapoClient(TapoClient.BASE_URL, credentials.token)
                        discoverDevices(client) // TODO: handle error
                    }
                }
            } else {
                Log.d(
                    TAG,
                    "Credentials not in intent and not in preferences. Starting LoginActivity"
                )
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        // populate list
        populateDeviceList()
    }

    private fun populateDeviceList() {
        val deviceList: RecyclerView = findViewById(R.id.deviceList)
        val devicesAdapter = DeviceListAdapter(devices)
        deviceList.adapter = devicesAdapter
        deviceList.layoutManager = LinearLayoutManager(this)

        devicesAdapter.onItemClick = {
            Log.d(TAG, String.format("Clicked on device %s", it.alias))
        }
    }

    private fun readCredentialsFromPrefs() {
        Log.d(TAG, "Trying to retrieve credentials from shared preferences")
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)

        val username = if (sharedPrefs.contains(SHARED_PREFS_USERNAME)) {
            sharedPrefs.getString(SHARED_PREFS_USERNAME, "")
        } else {
            null
        }
        val password = if (sharedPrefs.contains(SHARED_PREFS_PASSWORD)) {
            sharedPrefs.getString(SHARED_PREFS_PASSWORD, "")
        } else {
            null
        }
        if (username != null && password != null) {
            // login and discover devices
            runBlocking {
                withContext(Dispatchers.IO) {
                    login(username, password)
                    // TODO: handle error
                }
            }

        }
    }

    private suspend fun login(username: String, password: String) {
        Log.d(TAG, String.format("Signing in as %s", username))
        val client = TapoClient()
        client.login(username, password)
        this.credentials = Credentials(client.token!!)
        discoverDevices(client)
    }

    private suspend fun discoverDevices(client: TapoClient) {
        client.discoverDevices().forEach {
            Log.d(
                TAG,
                String.format("Found a new device of type %s with alias %s", it.model, it.alias)
            )
            this.devices.add(it)
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val SHARED_PREFS = "OpenTapoWearOs"
        const val SHARED_PREFS_USERNAME = "username"
        const val SHARED_PREFS_PASSWORD = "password"
    }

}
