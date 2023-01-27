package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import dev.veeso.opentapowearos.view.intent_data.NewGroupInput
import dev.veeso.opentapowearos.view.intent_data.NewGroupOutput

class NewGroupActivity : Activity() {

    private var existingGroups: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_group)
    }

    override fun onResume() {
        super.onResume()

        val groups = intent.getParcelableExtra<NewGroupInput>(GROUP_INTENT_INPUT)
        if (groups != null) {
            Log.d(TAG, String.format("Found device for groups %s", groups.aliasList))
            // set existing groups
            Log.d(TAG, String.format("Existing groups: %s", groups.existingGroups))
            this.existingGroups = groups.existingGroups
            // set title
            val title: TextView = findViewById(R.id.new_group_title)
            title.text = String.format(
                resources.getString(R.string.new_group_activity_title),
                groups.aliasList.size
            )

            val groupNameText: EditText = findViewById(R.id.activity_login_password)
            groupNameText.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onGroupNameDone(v as EditText)
                    true
                } else {
                    false
                }
            }
        } else {
            Log.d(DeviceActivity.TAG, "Intent device data was empty; terminating activity")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun onGroupNameDone(v: EditText) {
        val name = v.text.toString()
        Log.d(TAG, String.format("Name chosen for the group: %s", name))

        if (name.isEmpty()) {
            setError(R.string.new_group_activity_group_name_error_empty_name)
        } else if (!groupExists(name)) {
            val intent = Intent()
            intent.putExtra(GROUP_INTENT_OUTPUT, NewGroupOutput(name))
            setResult(RESULT_OK, intent)
            finish()
        } else {
            setError(R.string.new_group_activity_group_name_error_duped)
        }
    }

    private fun groupExists(groupName: String): Boolean {
        return this.existingGroups.contains(groupName)
    }

    private fun setError(id: Int) {
        val error = resources.getString(id)
        Log.d(TAG, String.format("Set error: %s", error))
        val errorText: TextView = findViewById(R.id.new_group_error)
        errorText.text = error
        errorText.visibility = View.VISIBLE
    }

    companion object {
        const val GROUP_INTENT_INPUT = "NewGroupInput"
        const val GROUP_INTENT_OUTPUT = "NewGroupOutput"
        const val TAG = "NewGroupActivity"
    }
}
