package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.databinding.ActivityMainBinding
import dev.veeso.opentapowearos.net.DeviceScanner
import dev.veeso.opentapowearos.net.NetworkUtils
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.Credentials
import dev.veeso.opentapowearos.view.DeviceCache
import dev.veeso.opentapowearos.view.DeviceData
import dev.veeso.opentapowearos.view.DeviceListAdapter
import kotlinx.coroutines.*
import java.net.Inet4Address

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding

    // credentials
    private var credentials: Credentials? = null

    // devices
    private var devices: MutableList<Device> = mutableListOf()

    // network
    private var deviceNetwork: Pair<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        // get credentials
        if (this.credentials == null) {
            Log.d(TAG, "Credentials are null; handling null credentials")
            onNullCredentials()
        } else {
            if (devices.isEmpty()) {
                Log.d(TAG, "Device list is empty; discover devices")
                discoverDevices()
            }
            // reload device state
            Log.d(TAG, "Credentials are set; reloading device state...")
            reloadDeviceState()
        }

        // add reload listener
        val reloadIcon: ImageView = findViewById(R.id.activity_main_reload)
        reloadIcon.setOnClickListener {
            onReloadDeviceList()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(
            TAG,
            String.format(
                "OnActivityResult; credentials is %s and result code is %d",
                credentials,
                resultCode
            )
        )
        if (this.credentials == null && resultCode == RESULT_OK && data != null) {
            val credentials = data.getParcelableExtra<Credentials>(LoginActivity.INTENT_OUTPUT)
            Log.d(TAG, String.format("Credentials from activity: %s", credentials))
            if (credentials != null) {
                Log.d(TAG, "Got credentials from login activity")
                this.credentials = credentials
            }
        }
    }

    private fun onReloadDeviceList() {
        deleteCachedDeviceAddressList()
        setMessageBox(visible = true, searching = true)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                discoverDevices()
            }
        }
    }

    private fun onNullCredentials() {
        // try to read from prefs
        readCredentialsFromPrefs()

        // if still NULL; try to read from intent
        if (this.credentials != null) {
            try {
                discoverDevices()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, String.format("Failed to discover devices: %s", e))
                setMessageBox(visible = true, searching = false)
            }
        } else {
            Log.d(
                TAG,
                "Credentials not in intent and not in preferences. Starting LoginActivity"
            )
            startActivityForResult(Intent(this, LoginActivity::class.java), 1)
        }
    }

    private fun reloadDeviceState() {
        Log.d(TAG, "Reloading device state...")
        this.devices.forEach {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    if (!it.authenticated) {
                        Log.d(
                            TAG,
                            String.format("Device %s is not authenticated yet; signin in", it.alias)
                        )
                        try {
                            it.login(credentials!!.username, credentials!!.password)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, String.format("Login failed on %s: %s", it.alias, e))
                        }
                    }
                    try {
                        Log.d(TAG, String.format("Getting device state for %s", it.alias))
                        it.getDeviceStatus()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(
                            TAG,
                            String.format("Failed to get device state for %s: %s", it.alias, e)
                        )
                    }
                }
                populateDeviceList()
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

    private fun discoverDevices() {

        val connectivityManager: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Wifi available")
                // The Wi-Fi network has been acquired, bind it to use this network by default
                connectivityManager.bindProcessToNetwork(network)
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        devices.clear()
                        discoverDevicesOnLocalNetwork()
                        populateDeviceList()
                        // reload device state
                        reloadDeviceState()
                    }
                }
            }

        }
        connectivityManager.requestNetwork(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
            callback
        )
    }

    private fun discoverDevicesOnLocalNetwork() {
        val cachedDeviceList = getCachedDeviceAddressList()
        val deviceScanner = if (cachedDeviceList != null && cachedDeviceList.isNotEmpty()) {
            Log.d(TAG, String.format("Found %d cached devices for scanner", cachedDeviceList.size))
            DeviceScanner(
                credentials!!.username,
                credentials!!.password,
                cachedDeviceList
            )
        } else {
            DeviceScanner(
                credentials!!.username,
                credentials!!.password
            )
        }
        Log.d(TAG, "Getting local address")
        if (deviceNetwork == null) {
            try {
                this.deviceNetwork = getDeviceNetworkAddresses()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, String.format("Failed to get device network address: %s", e))
                setMessageBox(visible = false, searching = false, R.string.main_activity_no_network)
                return
            }
        }
        deviceScanner.scanNetwork(deviceNetwork!!.first, deviceNetwork!!.second)
        Log.d(TAG, "Running ip discovery service")
        // assign devices
        this.devices = deviceScanner.devices
        Log.d(TAG, String.format("Found %d devices", this.devices.size))
        // cache devices
        setCachedDeviceAddressList(devices.map { it.ipAddress })
    }

    private fun populateDeviceList() {
        runOnUiThread {
            if (devices.isEmpty()) {
                setMessageBox(visible = true, searching = false)
            } else {
                setMessageBox(visible = false, searching = false)
                val deviceList: RecyclerView = findViewById(R.id.activity_main_device_list)
                val devicesAdapter = DeviceListAdapter(devices)
                deviceList.adapter = devicesAdapter
                deviceList.layoutManager = LinearLayoutManager(this)

                devicesAdapter.onItemClick = {
                    Log.d(
                        TAG,
                        String.format("Clicked on device %s; starting DeviceActivity", it.alias)
                    )
                    val intent = Intent(this, DeviceActivity::class.java)
                    intent.putExtra(
                        DeviceActivity.DEVICE_DATA_INTENT_NAME, DeviceData(
                            it.alias, it.id, it.model, it.endpoint, it.ipAddress, it.status
                        )
                    )
                    intent.putExtra(DeviceActivity.CREDENTIALS_INTENT_NAME, credentials)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setMessageBox(visible: Boolean, searching: Boolean, messageId: Int = R.string.main_activity_not_found) {
        runOnUiThread {
            val deviceList: RecyclerView = findViewById(R.id.activity_main_device_list)
            val messageBox: LinearLayout = findViewById(R.id.activity_main_message_box)
            val reloadIcon: ImageView = findViewById(R.id.activity_main_reload)
            if (visible) {
                deviceList.visibility = View.GONE
                messageBox.visibility = View.VISIBLE
                reloadIcon.visibility = View.GONE
                if (searching) {
                    val progress: ProgressBar = findViewById(R.id.activity_main_progressbar)
                    progress.visibility = View.VISIBLE
                    val alertIcon: ImageView = findViewById(R.id.activity_main_device_not_found)
                    alertIcon.visibility = View.GONE
                } else {
                    val alertIcon: ImageView = findViewById(R.id.activity_main_device_not_found)
                    alertIcon.visibility = View.VISIBLE
                    val progress: ProgressBar = findViewById(R.id.activity_main_progressbar)
                    progress.visibility = View.GONE
                    val message: TextView = findViewById(R.id.activity_main_message)
                    message.text = resources.getString(messageId)
                }
            } else {
                messageBox.visibility = View.GONE
                deviceList.visibility = View.VISIBLE
                reloadIcon.visibility = View.VISIBLE
            }
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
                    "Found local device address %s and netmask %s",
                    ipAddress.hostAddress,
                    netmask
                )
            )
            // return Pair("192.168.178.23", "255.255.255.0")
            return Pair(ipAddress.hostAddress!!, netmask)
        }

        throw Exception("No link")
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

    private fun getCachedDeviceAddressList(): List<String>? {
        Log.d(TAG, "Trying to retrieve cached device list from shared preferences")
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        if (sharedPrefs.contains(SHARED_PREFS_DEVICE_ADDRESS)) {
            val cachedDevices = sharedPrefs.getString(SHARED_PREFS_DEVICE_ADDRESS, "")
            Log.d(TAG, String.format("Found device list: %s", cachedDevices))
            return DeviceCache.deserialize(cachedDevices!!)
        }
        return null
    }

    private fun setCachedDeviceAddressList(deviceList: List<String>) {
        Log.d(TAG, "Writing ip list to preferences")
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val payload = DeviceCache.serialize(deviceList)
        editor.putString(SHARED_PREFS_DEVICE_ADDRESS, payload)
        editor.apply()
        Log.d(TAG, String.format("Device list written as %s", payload))
    }

    private fun deleteCachedDeviceAddressList() {
        Log.d(TAG, "Deleting ip list from preferences")
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove(SHARED_PREFS_DEVICE_ADDRESS)
        editor.commit()
        Log.d(TAG, "Device address cleared")
    }

    companion object {
        const val TAG = "MainActivity"
        const val SHARED_PREFS = "OpenTapoWearOs"
        const val SHARED_PREFS_USERNAME = "username"
        const val SHARED_PREFS_PASSWORD = "password"
        const val SHARED_PREFS_DEVICE_ADDRESS = "deviceAddressList"
    }

}
