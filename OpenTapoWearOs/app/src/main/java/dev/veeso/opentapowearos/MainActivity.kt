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
import dev.veeso.opentapowearos.view.intent_data.Credentials
import dev.veeso.opentapowearos.view.app_data.DeviceCache
import dev.veeso.opentapowearos.view.intent_data.DeviceData
import dev.veeso.opentapowearos.view.main_activity.ActivityState
import dev.veeso.opentapowearos.view.main_activity.DeviceListAdapter
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

    // state
    private var state: ActivityState = ActivityState.LOADING_DEVICE_LIST

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
        setActivityState(ActivityState.LOADING_DEVICE_LIST)
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
                setActivityState(ActivityState.NO_DEVICE_FOUND)
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
                if (devices.isNotEmpty()) {
                    setActivityState(ActivityState.DEVICE_LIST)
                } else {
                    setActivityState(ActivityState.NO_DEVICE_FOUND)
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

    private fun discoverDevices() {

        val connectivityManager: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
                        if (devices.isNotEmpty()) {
                            setActivityState(ActivityState.DEVICE_LIST)
                        } else {
                            setActivityState(ActivityState.NO_DEVICE_FOUND)
                        }
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
                setActivityState(ActivityState.NO_LINK)
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

    // device states

    private fun setActivityState(state: ActivityState) {
        this.state = state
        when (this.state) {
            ActivityState.DEVICE_LIST -> enterDeviceListState()
            ActivityState.LOADING_DEVICE_LIST -> enterLoadingDeviceListState()
            ActivityState.NO_DEVICE_FOUND -> enterNoDeviceFoundState()
            ActivityState.NO_LINK -> enterNoLinkState()
        }
    }

    private fun enterDeviceListState() {
        Log.d(TAG, "Entering no device found state")
        toggleDeviceList(visible = true)
        toggleReloadIcon(visible = true)
        toggleMessageBox(visible = false)
        toggleLoading(loading = false)
        toggleAlert(visible = false)
    }

    private fun enterLoadingDeviceListState() {
        Log.d(TAG, "Entering no device found state")
        toggleDeviceList(visible = false)
        toggleReloadIcon(visible = false)
        toggleMessageBox(visible = true)
        toggleLoading(loading = true)
        toggleAlert(visible = false)
    }

    private fun enterNoDeviceFoundState() {
        Log.d(TAG, "Entering no device found state")
        toggleDeviceList(visible = false)
        toggleReloadIcon(visible = true)
        toggleMessageBox(visible = true)
        toggleLoading(loading = false)
        toggleAlert(visible = true, R.string.main_activity_not_found)
    }

    private fun enterNoLinkState() {
        Log.d(TAG, "Entering no link state")
        toggleDeviceList(visible = false)
        toggleReloadIcon(visible = true)
        toggleMessageBox(visible = true)
        toggleLoading(loading = false)
        toggleAlert(visible = true, R.string.main_activity_no_network)

    }

    private fun populateDeviceList() {
        runOnUiThread {
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

    private fun toggleDeviceList(visible: Boolean) {
        runOnUiThread {
            Log.d(TAG, "Entering no device found state")
            val deviceList: RecyclerView = findViewById(R.id.activity_main_device_list)

            if (visible) {
                deviceList.visibility = View.VISIBLE
                populateDeviceList()
            } else {
                deviceList.visibility = View.GONE
            }
        }
    }

    private fun toggleMessageBox(visible: Boolean) {
        runOnUiThread {
            val messageBox: LinearLayout = findViewById(R.id.activity_main_message_box)
            if (visible) {
                messageBox.visibility = View.VISIBLE
            } else {
                messageBox.visibility = View.GONE
            }
        }
    }

    private fun toggleLoading(loading: Boolean) {
        runOnUiThread {
            val alertIcon: ImageView = findViewById(R.id.activity_main_device_not_found)
            val progress: ProgressBar = findViewById(R.id.activity_main_progressbar)
            val message: TextView = findViewById(R.id.activity_main_message)

            if (loading) {
                alertIcon.visibility = View.GONE
                progress.visibility = View.VISIBLE
                message.visibility = View.VISIBLE
                message.text = resources.getString(R.string.main_activity_loading)
            } else {
                progress.visibility = View.GONE
            }
        }
    }

    private fun toggleAlert(visible: Boolean, message: Int? = null) {
        runOnUiThread {
            val alertIcon: ImageView = findViewById(R.id.activity_main_device_not_found)

            if (visible) {
                alertIcon.visibility = View.VISIBLE
                val messageView: TextView = findViewById(R.id.activity_main_message)
                if (message != null) {
                    messageView.visibility = View.VISIBLE
                    messageView.text = resources.getString(message)
                } else {
                    messageView.visibility = View.GONE
                }
            } else {
                alertIcon.visibility = View.GONE
            }
        }
    }

    private fun toggleReloadIcon(visible: Boolean) {
        runOnUiThread {
            val reloadIcon: ImageView = findViewById(R.id.activity_main_reload)
            if (visible) {
                reloadIcon.visibility = View.VISIBLE
            } else {
                reloadIcon.visibility = View.GONE
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
