package com.example.securelock.storage

import android.content.Context
import androidx.core.content.edit

// Helper SharedPreferences per il primo accesso
object SetupPrefs {
    private const val PREFS_NAME = "securelock_setup"
    private const val KEY_SETUP_DONE = "setup_completed"
    private const val KEY_BUILDING_ID = "building_id"
    private const val KEY_ADMIN_ID = "admin_user_id"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSetupCompleted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SETUP_DONE, false)

    fun setSetupCompleted(context: Context, completed: Boolean) {
        prefs(context).edit { putBoolean(KEY_SETUP_DONE, completed) }
    }

    fun saveBuildingId(context: Context, buildingId: Int) {
        prefs(context).edit { putInt(KEY_BUILDING_ID, buildingId) }
    }

    fun getBuildingId(context: Context): Int =
        prefs(context).getInt(KEY_BUILDING_ID, -1)

    fun saveAdminId(context: Context, adminId: Int) {
        prefs(context).edit { putInt(KEY_ADMIN_ID, adminId) }
    }

    fun getAdminId(context: Context): Int =
        prefs(context).getInt(KEY_ADMIN_ID, -1)

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }
}