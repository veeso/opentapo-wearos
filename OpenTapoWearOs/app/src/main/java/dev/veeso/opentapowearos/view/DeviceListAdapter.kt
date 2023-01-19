package dev.veeso.opentapowearos.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.veeso.opentapowearos.R
import dev.veeso.opentapowearos.tapo.device.Device

internal class DeviceListAdapter(private val devices: List<Device>) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    var onItemClick: ((Device) -> Unit)? = null

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceAliasText: TextView = view.findViewById(R.id.deviceAlias)
        val deviceModelText: TextView = view.findViewById(R.id.deviceModel)

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
    }

    override fun getItemCount(): Int {
        return devices.size
    }

}
