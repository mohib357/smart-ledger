package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)

enum class AuthState { UNKNOWN, SIGNED_IN, SIGNED_OUT }

class FirebaseAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseAuthManager"
        private const val PREFS = "app_settings"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(AuthState.UNKNOWN)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        // Restore from prefs on startup
        val savedUid = prefs.getString(KEY_USER_UID, "") ?: ""
        if (savedUid.isNotEmpty()) {
            _authState.value = AuthState.SIGNED_IN
            _userProfile.value = UserProfile(
                uid = savedUid,
                name = prefs.getString(KEY_USER_NAME, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                phone = prefs.getString(KEY_USER_PHONE, "") ?: ""
            )
        } else {
            _authState.value = AuthState.SIGNED_OUT
        }
        // Try to sync with Firebase Auth current user
        tryRestoreFirebaseSession()
    }

    private fun tryRestoreFirebaseSession() {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _authState.value = AuthState.SIGNED_IN
                _userProfile.value = UserProfile(
                    uid = currentUser.uid,
                    name = prefs.getString(KEY_USER_NAME, currentUser.displayName ?: "") ?: "",
                    email = currentUser.email ?: "",
                    phone = currentUser.phoneNumber ?: ""
                )
                saveToPrefs(_userProfile.value)
            } else if (_authState.value != AuthState.SIGNED_IN) {
                _authState.value = AuthState.SIGNED_OUT
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not available, using local auth state", e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign in failed"))
            val profile = UserProfile(
                uid = user.uid,
                name = prefs.getString(KEY_USER_NAME, user.displayName ?: "") ?: "",
                email = user.email ?: email,
                phone = user.phoneNumber ?: ""
            )
            _authState.value = AuthState.SIGNED_IN
            _userProfile.value = profile
            saveToPrefs(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signIn failed", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<UserProfile> {
        return try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign up failed"))
            // Update display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            // Send verification email
            try { user.sendEmailVerification().await() } catch (e: Exception) { Log.w(TAG, "Verification email failed", e) }
            val profile = UserProfile(uid = user.uid, name = name, email = email, phone = "")
            _authState.value = AuthState.SIGNED_IN
            _userProfile.value = profile
            saveToPrefs(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signUp failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordReset failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() } catch (e: Exception) { Log.w(TAG, "Firebase signOut error", e) }
        prefs.edit()
            .remove(KEY_USER_UID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_PHONE)
            .apply()
        _authState.value = AuthState.SIGNED_OUT
        _userProfile.value = UserProfile()
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()?.await()
            signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteAccount failed", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, phone: String): Result<Unit> {
        return try {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val updates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(updates).await()
            }
            val updated = _userProfile.value.copy(name = name, phone = phone)
            _userProfile.value = updated
            saveToPrefs(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateProfile failed", e)
            Result.failure(e)
        }
    }

    private fun saveToPrefs(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_UID, profile.uid)
            .putString(KEY_USER_NAME, profile.name)
            .putString(KEY_USER_EMAIL, profile.email)
            .putString(KEY_USER_PHONE, profile.phone)
            .apply()
    }

    fun getCurrentUid(): String = prefs.getString(KEY_USER_UID, "") ?: ""
    fun isSignedIn(): Boolean = _authState.value == AuthState.SIGNED_IN
}
