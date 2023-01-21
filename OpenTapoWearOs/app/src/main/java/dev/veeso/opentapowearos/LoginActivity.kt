package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import dev.veeso.opentapowearos.view.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_activity)

        val passwordText: EditText = findViewById(R.id.signin_password)
        passwordText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSignIn(v)
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // load credentials
        readCredentialsFromPrefs()
    }

    override fun onPause() {
        super.onPause()

        val usernameText: EditText = findViewById(R.id.signin_username)
        val passwordText: EditText = findViewById(R.id.signin_password)
        val errorLabel: TextView = findViewById(R.id.error_label)
        usernameText.setText("")
        passwordText.setText("")
        errorLabel.visibility = View.INVISIBLE
    }

    fun onSignIn(view: View) {
        val usernameText: EditText = findViewById(R.id.signin_username)
        val passwordText: EditText = findViewById(R.id.signin_password)
        val savePasswordToggle: Switch = findViewById(R.id.signin_save_password)

        val username = usernameText.text.toString()
        val password = passwordText.text.toString()
        val savePassword = savePasswordToggle.isChecked

        Log.d(
            TAG,
            String.format(
                "Signing in with username %s and password %d long",
                username,
                password.length
            )
        )

        if (username.isNotEmpty() && password.isNotEmpty()) {
            doSignIn(username, password, savePassword)
        } else {
            setError(getString(R.string.activity_login_input_field_empty))
        }
    }

    private fun setError(message: String) {
        val errorLabel: TextView = findViewById(R.id.error_label)
        errorLabel.text = message
        errorLabel.visibility = View.VISIBLE
    }

    private fun saveCredentialsToPrefs(username: String, password: String, savePassword: Boolean) {
        Log.d(TAG, "Saving credentials to preferences")
        val sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        editor.putString(MainActivity.SHARED_PREFS_USERNAME, username)
        if (savePassword) {
            editor.putString(MainActivity.SHARED_PREFS_PASSWORD, password)
        }
        editor.apply()
    }

    private fun readCredentialsFromPrefs() {
        Log.d(TAG, "Trying to retrieve credentials from shared preferences")
        val sharedPrefs = getSharedPreferences(MainActivity.SHARED_PREFS, Context.MODE_PRIVATE)

        if (sharedPrefs.contains(MainActivity.SHARED_PREFS_USERNAME)) {
            val username = sharedPrefs.getString(MainActivity.SHARED_PREFS_USERNAME, "")
            val usernameText: EditText = findViewById(R.id.signin_username)
            usernameText.setText(username)
        }
        if (sharedPrefs.contains(MainActivity.SHARED_PREFS_PASSWORD)) {
            val password = sharedPrefs.getString(MainActivity.SHARED_PREFS_PASSWORD, "")
            val passwordText: EditText = findViewById(R.id.signin_password)
            passwordText.setText(password)
        }
    }

    private fun doSignIn(username: String, password: String, savePassword: Boolean) {
        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val credentials = login(username, password)
                    Log.d(TAG, "Login successful. Saving credentials to shared preferences")
                    saveCredentialsToPrefs(username, password, savePassword)
                    // return to main activity
                    val intent = Intent()
                    intent.putExtra(INTENT_OUTPUT, credentials)
                    setResult(RESULT_OK, intent)
                    finish()
                } catch (e: Exception) {
                    Log.d(TAG, String.format("Login failed: %s", e))
                    runOnUiThread {
                        setError(getString(R.string.activity_login_signin_error))
                    }
                }
            }
        }
    }

    private suspend fun login(username: String, password: String): Credentials {
        val client = TpLinkCloudClient()
        client.login(username, password)

        return Credentials(client.token!!)
    }

    companion object {
        const val TAG = "LoginActivity"
        const val INTENT_OUTPUT = "Credentials"
    }

}