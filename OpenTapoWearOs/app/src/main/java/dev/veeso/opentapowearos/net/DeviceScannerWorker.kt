package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.Inet4Address

class DeviceScannerWorker(addressList: List<Inet4Address>, username: String, password: String) :
    Runnable {

    private val addressList: List<Inet4Address>
    private val username: String
    private val password: String
    private val tag: String

    var devices: MutableList<Device> = mutableListOf()

    init {
        this.addressList = addressList
        this.username = username
        this.password = password
        this.tag = String.format("DeviceScannerWorker[%s]", addressList.getOrElse(0) {
            Inet4Address.getByName("127.0.0.1")
        }.hostAddress)
    }

    override fun run() {
        runBlocking {
            withContext(Dispatchers.IO) {
                addressList.forEach { address ->
                    try {
                        if (address.isReachable(3000)) {
                            val client = TapoClient(address)

                            client.login(username, password)
                            Log.d(
                                tag,
                                "Successfully signed in to device; getting device info..."
                            )
                            devices.add(client.queryDevice())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e(tag, String.format("Discovery failed: %s", e))
                    }
                }

            }
        }
    }

}
