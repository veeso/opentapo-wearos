package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.Inet4Address

class DeviceScannerWorker(address: Inet4Address, username: String, password: String) : Runnable {

    private val address: Inet4Address
    private val username: String
    private val password: String
    private val tag: String

    var device: Device? = null

    init {
        this.address = address
        this.username = username
        this.password = password
        this.tag = String.format("DeviceScannerWorker[%s]", address.hostAddress!!)
    }

    override fun run() {
        runBlocking {
            withContext(Dispatchers.IO) {
                if (address.isReachable(10000)) {
                    val client = TapoClient(address)
                    try {
                        client.login(username, password)
                        Log.d(
                            tag,
                            "Successfully signed in to device; getting device info..."
                        )
                        device = client.queryDevice()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(tag, String.format("Discovery failed: %s", e))
                    }
                }
            }
        }
    }

}
