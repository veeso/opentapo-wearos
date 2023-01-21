package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.databinding.ActivityMainBinding
import dev.veeso.opentapowearos.net.IpFinder
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceData
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
                        val client = TpLinkCloudClient(TpLinkCloudClient.BASE_URL, credentials.token)
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
            Log.d(TAG, String.format("Clicked on device %s; starting DeviceActivity", it.alias))
            val intent = Intent(this, DeviceActivity::class.java)
            intent.putExtra(
                DeviceActivity.INTENT_NAME, DeviceData(
                    it.alias, it.id, it.model, it.macAddress, it.ipAddr!!.hostName
                )
            )
            startActivity(intent)
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
            val fallbackIntent = Intent(this, LoginActivity::class.java)
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        login(username, password)
                    } catch (e: Exception) {
                        runOnUiThread {
                            startActivity(fallbackIntent)
                        }
                    }
                }
            }
        }
    }

    private suspend fun login(username: String, password: String) {
        Log.d(TAG, String.format("Signing in as %s", username))
        val client = TpLinkCloudClient()
        client.login(username, password)
        this.credentials = Credentials(client.token!!)
        discoverDevices(client)
    }

    private suspend fun discoverDevices(client: TpLinkCloudClient) {
        this.devices.clear()
        client.discoverDevices().forEach {
            Log.d(
                TAG,
                String.format("Found a new device of type %s with alias %s", it.model, it.alias)
            )
            this.devices.add(it)
        }
        discoverDevicesIpAddress()
    }

    private suspend fun discoverDevicesIpAddress() {
        val ipDiscoveryService = IpFinder(
            this.devices.map {
                it.macAddress
            }
        )
        Log.d(TAG, "Getting local address")
        val networkAddress = getNetworkAddress()
        Log.d(TAG, String.format("Found local device address %s/%s", networkAddress.first, networkAddress.second))
        ipDiscoveryService.scanNetwork(networkAddress.first, networkAddress.second)
        Log.d(TAG, "Running ip discovery service")
        // assign addresses
        this.devices.forEach {
            val ip = ipDiscoveryService.hosts[it.macAddress]
            if (ip != null) {
                it.setIpAddress(ip)
            }
        }
        // remove devices without ip address (offline)
        this.devices = this.devices.filter {
            it.ipAddr != null
        }.toMutableList()
        Log.d(TAG, String.format("Found IP address for %d devices", this.devices.size))
    }

    private fun getNetworkAddress(): Pair<String, String> {
        TODO()
    }



    companion object {
        const val TAG = "MainActivity"
        const val SHARED_PREFS = "OpenTapoWearOs"
        const val SHARED_PREFS_USERNAME = "username"
        const val SHARED_PREFS_PASSWORD = "password"
    }

}
