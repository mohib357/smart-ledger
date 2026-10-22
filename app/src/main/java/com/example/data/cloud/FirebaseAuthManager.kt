package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import com.example.BuildConfig

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val fatherName: String = "",
    val address: String = "",
    val businessName: String = "",
    val profileUpdatedAt: Long = 0L
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
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
        private const val KEY_USER_FATHER_NAME = "user_father_name"
        private const val KEY_USER_ADDRESS = "user_address"
        private const val KEY_USER_BUSINESS_NAME = "user_business_name"
        private const val KEY_PROFILE_UPDATED_AT = "profile_updated_at"
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
                phone = prefs.getString(KEY_USER_PHONE, "") ?: "",
                photoUrl = prefs.getString(KEY_USER_PHOTO_URL, "") ?: "",
                fatherName = prefs.getString(KEY_USER_FATHER_NAME, "") ?: "",
                address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
                businessName = prefs.getString(KEY_USER_BUSINESS_NAME, "") ?: "",
                profileUpdatedAt = prefs.getLong(KEY_PROFILE_UPDATED_AT, 0L)
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
                    phone = currentUser.phoneNumber ?: "",
                    photoUrl = prefs.getString(KEY_USER_PHOTO_URL, currentUser.photoUrl?.toString() ?: "") ?: "",
                    fatherName = prefs.getString(KEY_USER_FATHER_NAME, "") ?: "",
                    address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
                    businessName = prefs.getString(KEY_USER_BUSINESS_NAME, "") ?: "",
                    profileUpdatedAt = prefs.getLong(KEY_PROFILE_UPDATED_AT, 0L)
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
                phone = user.phoneNumber ?: "",
                photoUrl = prefs.getString(KEY_USER_PHOTO_URL, user.photoUrl?.toString() ?: "") ?: "",
                fatherName = prefs.getString(KEY_USER_FATHER_NAME, "") ?: "",
                address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
                businessName = prefs.getString(KEY_USER_BUSINESS_NAME, "") ?: "",
                profileUpdatedAt = prefs.getLong(KEY_PROFILE_UPDATED_AT, 0L)
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
            .remove(KEY_USER_PHOTO_URL)
            .remove(KEY_USER_FATHER_NAME)
            .remove(KEY_USER_ADDRESS)
            .remove(KEY_USER_BUSINESS_NAME)
            .remove(KEY_PROFILE_UPDATED_AT)
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

    // ── Profile sync methods ───────────────────────────────────────────────

    suspend fun saveProfileLocally(profile: UserProfile) {
        saveToPrefs(profile)
        _userProfile.value = profile
    }

    suspend fun syncProfileToFirestore(profile: UserProfile) {
        try {
            val uid = profile.uid.ifEmpty { getCurrentUid() }
            if (uid.isEmpty()) return
            val data = mapOf(
                "uid" to uid,
                "name" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "photoUrl" to profile.photoUrl,
                "fatherName" to profile.fatherName,
                "address" to profile.address,
                "businessName" to profile.businessName,
                "profileUpdatedAt" to profile.profileUpdatedAt
            )
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("profile").document("main")
                .set(data)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "syncProfileToFirestore failed", e)
        }
    }

    suspend fun fetchProfileFromFirestore(): Result<UserProfile> {
        return try {
            val uid = getCurrentUid()
            if (uid.isEmpty()) return Result.failure(Exception("Not signed in"))
            val doc = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("profile").document("main")
                .get().await()
            if (!doc.exists()) return Result.failure(Exception("No remote profile"))
            val profile = UserProfile(
                uid = uid,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                photoUrl = doc.getString("photoUrl") ?: "",
                fatherName = doc.getString("fatherName") ?: "",
                address = doc.getString("address") ?: "",
                businessName = doc.getString("businessName") ?: "",
                profileUpdatedAt = doc.getLong("profileUpdatedAt") ?: 0L
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.w(TAG, "fetchProfileFromFirestore failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAndMergeFirestoreProfile() {
        val remote = fetchProfileFromFirestore().getOrNull() ?: return
        val local = _userProfile.value
        // Take whichever is newer by profileUpdatedAt; if equal, prefer remote
        val merged = if (remote.profileUpdatedAt >= local.profileUpdatedAt) {
            remote.copy(uid = local.uid.ifEmpty { remote.uid })
        } else {
            local
        }
        saveProfileLocally(merged)
    }

    // ── Google Sign-In via Credential Manager ─────────────────────────────

    suspend fun signInWithGoogle(context: Context): Result<UserProfile> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credentialResponse = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithCredential(authCredential).await()
            val user = result.user ?: return Result.failure(Exception("Google sign-in failed"))
            val profile = UserProfile(
                uid = user.uid,
                name = prefs.getString(KEY_USER_NAME, user.displayName ?: "") ?: (user.displayName ?: ""),
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                photoUrl = prefs.getString(KEY_USER_PHOTO_URL, user.photoUrl?.toString() ?: "") ?: (user.photoUrl?.toString() ?: ""),
                fatherName = prefs.getString(KEY_USER_FATHER_NAME, "") ?: "",
                address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
                businessName = prefs.getString(KEY_USER_BUSINESS_NAME, "") ?: "",
                profileUpdatedAt = prefs.getLong(KEY_PROFILE_UPDATED_AT, 0L)
            )
            _authState.value = AuthState.SIGNED_IN
            _userProfile.value = profile
            saveToPrefs(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogle failed", e)
            Result.failure(e)
        }
    }

    // ── Phone OTP ─────────────────────────────────────────────────────────

    suspend fun sendOtp(phone: String, activity: android.app.Activity): Result<String> {
        return suspendCancellableCoroutine { cont ->
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        if (cont.isActive) cont.resume(Result.success(credential.smsCode ?: ""))
                    }
                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        if (cont.isActive) cont.resume(Result.failure(e))
                    }
                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        if (cont.isActive) cont.resume(Result.success(verificationId))
                    }
                }
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                if (cont.isActive) cont.resume(Result.failure(e))
            }
        }
    }

    suspend fun verifyOtp(verificationId: String, otp: String): Result<UserProfile> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("OTP verification failed"))
            val profile = UserProfile(
                uid = user.uid,
                name = prefs.getString(KEY_USER_NAME, user.displayName ?: "") ?: "",
                email = user.email ?: "",
                phone = user.phoneNumber ?: "",
                photoUrl = prefs.getString(KEY_USER_PHOTO_URL, "") ?: "",
                fatherName = prefs.getString(KEY_USER_FATHER_NAME, "") ?: "",
                address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
                businessName = prefs.getString(KEY_USER_BUSINESS_NAME, "") ?: "",
                profileUpdatedAt = prefs.getLong(KEY_PROFILE_UPDATED_AT, 0L)
            )
            _authState.value = AuthState.SIGNED_IN
            _userProfile.value = profile
            saveToPrefs(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp failed", e)
            Result.failure(e)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun saveToPrefs(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_UID, profile.uid)
            .putString(KEY_USER_NAME, profile.name)
            .putString(KEY_USER_EMAIL, profile.email)
            .putString(KEY_USER_PHONE, profile.phone)
            .putString(KEY_USER_PHOTO_URL, profile.photoUrl)
            .putString(KEY_USER_FATHER_NAME, profile.fatherName)
            .putString(KEY_USER_ADDRESS, profile.address)
            .putString(KEY_USER_BUSINESS_NAME, profile.businessName)
            .putLong(KEY_PROFILE_UPDATED_AT, profile.profileUpdatedAt)
            .apply()
    }

    fun getCurrentUid(): String = prefs.getString(KEY_USER_UID, "") ?: ""
    fun isSignedIn(): Boolean = _authState.value == AuthState.SIGNED_IN
}
