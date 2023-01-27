package dev.veeso.opentapowearos.view.main_activity

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class DeviceListAdapter(private val devices: List<Device>) :
    RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    var onItemClick: ((Device) -> Unit)? = null

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceAliasText: TextView = view.findViewById(R.id.device_list_item_alias)
        val deviceModelText: TextView = view.findViewById(R.id.device_list_item_model)
        val devicePowerSwitch: Switch = view.findViewById(R.id.device_list_item_power)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(devices[bindingAdapterPosition])
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

    private fun setPowerState(device: Device, powerState: Boolean) {
        runBlocking {
            withContext(Dispatchers.IO) {
                if (powerState) {
                    device.on()
                } else {
                    device.off()
                }
            }
        }
    }

}