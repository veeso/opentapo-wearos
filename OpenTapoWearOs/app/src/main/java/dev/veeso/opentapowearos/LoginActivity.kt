package dev.veeso.opentapowearos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import dev.veeso.opentapowearos.view.intent_data.Credentials
import dev.veeso.opentapowearos.view.login_activity.LoginActivityState
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
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
        runOnUiThread {
            val errorLabel: TextView = findViewById(R.id.error_label)
            errorLabel.text = message
            errorLabel.visibility = View.VISIBLE
        }
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
        setViewState(LoginActivityState.SIGNING_IN)
        val intent = Intent()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val credentials = login(username, password)
                    Log.d(TAG, "Login successful. Saving credentials to shared preferences")
                    saveCredentialsToPrefs(username, password, savePassword)
                    // return to main activity
                    intent.putExtra(INTENT_OUTPUT, credentials)
                    setViewState(LoginActivityState.LOGIN_FORM)
                    setResult(RESULT_OK, intent)
                    finish()
                } catch (e: Exception) {
                    Log.d(TAG, String.format("Login failed: %s", e))
                    setError(getString(R.string.activity_login_signin_error))
                    setViewState(LoginActivityState.LOGIN_FORM)
                }
            }
        }
    }

    private suspend fun login(username: String, password: String): Credentials {
        val client = TpLinkCloudClient()
        client.login(username, password)

        return Credentials(username, password)
    }

    private fun setViewState(state: LoginActivityState) {
        Log.d(TAG, String.format("Entering new view state: %s", state))
        when (state) {
            LoginActivityState.LOGIN_FORM -> enterLoginForm()
            LoginActivityState.SIGNING_IN -> enterSigningIn()
        }
    }

    private fun enterLoginForm() {
        runOnUiThread {
            Log.d(TAG, "Entering login form state")
            val loginLayout: LinearLayout = findViewById(R.id.signin_login_layout)
            val loadingLayout: LinearLayout = findViewById(R.id.signin_loading_layout)
            loginLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
        }
    }

    private fun enterSigningIn() {
        runOnUiThread {
            Log.d(TAG, "Entering signing-in")
            val loginLayout: LinearLayout = findViewById(R.id.signin_login_layout)
            val loadingLayout: LinearLayout = findViewById(R.id.signin_loading_layout)
            loginLayout.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "LoginActivity"
        const val INTENT_OUTPUT = "Credentials"
    }

}
