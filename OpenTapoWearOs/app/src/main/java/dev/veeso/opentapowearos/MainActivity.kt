package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.databinding.ActivityMainBinding
import dev.veeso.opentapowearos.net.DeviceScanner
import dev.veeso.opentapowearos.net.NetworkUtils
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceData
import dev.veeso.opentapowearos.view.DeviceListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.Inet4Address

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

        Log.d(TAG, "onResume")

        // read credentials from intent
        if (this.credentials == null) {
            val credentials = intent.getParcelableExtra<Credentials>(LoginActivity.INTENT_OUTPUT)
            if (credentials != null) {
                Log.d(TAG, "Got credentials from login activity")
                this.credentials = credentials
                // discover devices
                runBlocking {
                    withContext(Dispatchers.IO) {
                        discoverDevices() // TODO: handle error
                    }
                }
            } else {
                Log.d(
                    TAG,
                    "Credentials not in intent and not in preferences. Starting LoginActivity"
                )
                startActivity(Intent(this, LoginActivity::class.java))
            }
        } else {
            runBlocking {
                withContext(Dispatchers.IO) {
                    discoverDevices() // TODO: handle error
                }
            }
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
            Log.d(TAG, String.format("Found credentials for %s", username))
            val fallbackIntent = Intent(this, LoginActivity::class.java)
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        login(username, password)
                        Log.d(TAG, "Login successful")
                    } catch (e: Exception) {
                        Log.e(TAG, String.format("Login failed: %s; going to login activity", e))
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
        this.credentials = Credentials(username, password)
        discoverDevices()
    }

    private suspend fun discoverDevices() {
        this.devices.clear()
        discoverDevicesOnLocalNetwork()
        populateDeviceList()
    }

    private suspend fun discoverDevicesOnLocalNetwork() {
        val deviceScanner = DeviceScanner(
            credentials!!.username,
            credentials!!.password
        )
        Log.d(TAG, "Getting local address")
        val networkAddress = getDeviceNetworkAddresses()
        Log.d(
            TAG,
            String.format(
                "Found local device address %s/%s",
                networkAddress.first,
                networkAddress.second
            )
        )
        deviceScanner.scanNetwork(networkAddress.first, networkAddress.second)
        Log.d(TAG, "Running ip discovery service")
        // assign devices
        this.devices = deviceScanner.devices
        Log.d(TAG, String.format("Found IP address for %d devices", this.devices.size))
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
                    it.alias, it.id, it.model, "DUMMY"
                )
            )
            startActivity(intent)
        }
    }

    private fun getDeviceNetworkAddresses(): Pair<String, String> {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        if (connectivityManager is ConnectivityManager) {
            val networks =
                connectivityManager.allNetworks.map { (connectivityManager.getLinkProperties(it) as LinkProperties) }
            val link: LinkProperties = networks[networks.size - 1]
            Log.d(TAG, link.linkAddresses.toString())
            val ipAddress = link.linkAddresses[1].address as Inet4Address
            val netmask = NetworkUtils.cidrToNetmask(link.linkAddresses[1].prefixLength)
            Log.d(
                TAG,
                String.format(
                    "Device IP address is %s and netmask is %s",
                    ipAddress.hostName,
                    netmask
                )
            )

            return Pair("192.168.178.23", "255.255.255.0")
            return Pair(ipAddress.hostName, netmask)
        }

        throw Exception("No link")
    }


    companion object {
        const val TAG = "MainActivity"
        const val SHARED_PREFS = "OpenTapoWearOs"
        const val SHARED_PREFS_USERNAME = "username"
        const val SHARED_PREFS_PASSWORD = "password"
    }

}
