package com.example.youome.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.youome.R

class SettingsFragment : PreferenceFragmentCompat(), 
    SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up click listeners for specific preferences
        setupPreferenceClickListeners()

    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun setupPreferenceClickListeners() {
        // Profile settings preference
        findPreference<Preference>("profile_settings")?.onPreferenceClickListener = this
        
        // Action preferences
        findPreference<Preference>("clear_cache")?.onPreferenceClickListener = this
        findPreference<Preference>("logout")?.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference.key) {
            "profile_settings" -> {
                launchProfileActivity()
                true
            }
            "clear_cache" -> {
                handleClearCache()
                true
            }
            "logout" -> {
                handleLogout()
                true
            }
            else -> false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "default_currency" -> {
                val newCurrency = sharedPreferences?.getString(key, "USD")
                // TODO: Update user preferences in database
                Toast.makeText(context, "Default currency changed to $newCurrency", Toast.LENGTH_SHORT).show()
            }
            "enable_notifications" -> {
                val isEnabled = sharedPreferences?.getBoolean(key, true) ?: true
                // TODO: Update notification settings
                Toast.makeText(context, if (isEnabled) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
            "theme_switch" -> {
                val isDarkMode = sharedPreferences?.getBoolean(key, false) ?: false
                // TODO: Update theme settings
                Toast.makeText(context, if (isDarkMode) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchProfileActivity() {
        // TODO: Replace with actual ProfileActivity later

        // val intent = Intent(context, com.example.youome.profile.ProfileActivity::class.java)
        // startActivity(intent)
        try {
            val intent = Intent().apply {
                setClassName(requireContext(), "com.example.youome.profile.ProfileActivity")
                // Add any extras you want to pass to the activity here
                // putExtra("user_id", userId)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback if activity doesn't exist yet
            Toast.makeText(context, "Profile activity not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleClearCache() {
        // TODO: Implement cache clearing
        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
    }

    private fun handleLogout() {
        // TODO: Implement logout functionality
        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
    }
}
