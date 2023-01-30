package dev.veeso.opentapowearos.view.main_activity

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.DeviceActivity
import dev.veeso.opentapowearos.R
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.intent_data.Credentials
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
internal class DeviceListAdapter(private val devices: List<Device>) :
    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    var onItemClick: ((Device) -> Unit)? = null
    var onItemLongClick: ((Device) -> Unit)? = null
    var selected: Boolean = false
    lateinit var credentials: Credentials

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceAliasText: TextView = view.findViewById(R.id.device_list_item_alias)
        val deviceModelText: TextView = view.findViewById(R.id.device_list_item_model)
        val devicePowerSwitch: Switch = view.findViewById(R.id.device_list_item_power)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(devices[bindingAdapterPosition])
            }
            itemView.setOnLongClickListener {
                onLongClick(it, bindingAdapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceAliasText.text = device.alias
        holder.deviceModelText.text = device.model.toString()
        // power switch
        holder.devicePowerSwitch.isChecked = device.status.deviceOn
        holder.devicePowerSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(
                DeviceActivity.TAG,
                String.format("Changing power state for %s to %s", device.alias, isChecked)
            )
            setPowerState(device, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    private fun onLongClick(view: View, adapterPosition: Int) {
        Log.d(TAG, "OnLongClick")
        selected = !selected
        val backgroundColor = if (selected) {
            SELECTED_COLOR
        } else {
            UNSELECTED_COLOR
        }
        view.setBackgroundColor(Color.parseColor(backgroundColor))
        onItemLongClick?.invoke(devices[adapterPosition])
    }

    private fun setPowerState(device: Device, powerState: Boolean) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if (!device.authenticated) {
                        Log.d(TAG, String.format("Device %s is not authenticated yet; signing in", device.alias))
                        device.login(credentials.username, credentials.password)
                    }
                    if (powerState) {
                        device.on()
                    } else {
                        device.off()
                    }
                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        String.format("Failed to set power state for %s: %s", device.alias, e)
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "DeviceListAdapter"
        const val SELECTED_COLOR = "#AB2196F3"
        const val UNSELECTED_COLOR = "#00000000"
    }

}
