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
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
internal class GroupListAdapter(private val groups: List<Pair<String, List<Device>>>) :
    RecyclerView.Adapter<GroupListAdapter.ViewHolder>() {

    var onItemClick: ((String) -> Unit)? = null
    var onItemLongClick: ((String) -> Unit)? = null
    var selected: Boolean = false

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupNameText: TextView = view.findViewById(R.id.group_list_item_name)
        val groupPowerSwitch: Switch = view.findViewById(R.id.group_list_power)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(groups[bindingAdapterPosition].first)
            }
            itemView.setOnLongClickListener {
                onLongClick(it, bindingAdapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pair = groups[position]
        val groupName = pair.first
        val devices = pair.second
        val powerState = devices.all {
            it.status.deviceOn
        }
        Log.d(TAG, String.format("Group %s power state is %s", groupName, powerState))
        holder.groupNameText.text = groupName
        // power switch
        holder.groupPowerSwitch.isChecked = powerState
        holder.groupPowerSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(
                DeviceActivity.TAG,
                String.format("Changing power state for %s to %s", groupName, isChecked)
            )
            setPowerState(devices, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return groups.size
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
        onItemLongClick?.invoke(groups[adapterPosition].first)
    }

    private fun setPowerState(devices: List<Device>, powerState: Boolean) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                devices.forEach {
                    Log.d(
                        TAG,
                        String.format(
                            "Setting power state for %s in group to %s",
                            it.alias,
                            powerState
                        )
                    )
                    if (powerState) {
                        it.on()
                    } else {
                        it.off()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "GroupListAdapter"
        const val SELECTED_COLOR = "#AB2196F3"
        const val UNSELECTED_COLOR = "#00000000"
    }

}
