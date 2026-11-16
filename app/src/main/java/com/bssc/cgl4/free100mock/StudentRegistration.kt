package com.bssc.cgl4.free100mock

import androidx.activity.ComponentActivity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

/** A student chooses the name once at registration (also shown on leaderboards). */
object StudentName {
    fun normalize(input: String): String? {
        if (input.any { Character.isISOControl(it) }) return null
        val name = input.trim().replace(Regex("\\s+"), " ")
        return name.takeIf {
            it.length in 2..28 && it.none { char -> Character.isISOControl(char) }
        }
    }
}

/** Phone capture is free and NOT phone ownership verification. */
object IndianMobile {
    private val tenDigits = Regex("^[6-9][0-9]{9}$")
    fun normalize(value: String): String? {
        val digits = value.filter { it.isDigit() }
        val local = if (digits.length == 12 && digits.startsWith("91")) digits.drop(2) else digits
        return if (tenDigits.matches(local)) "+91" + local else null
    }
}

sealed interface SignupState {
    data object Loading : SignupState
    data object NeedsGoogle : SignupState
    data class NeedsPhone(
        val email: String, val displayName: String,
        val phone: String = "", val whatsappOptIn: Boolean = false
    ) : SignupState
    data class Ready(
        val email: String, val displayName: String,
        val phone: String, val whatsappOptIn: Boolean
    ) : SignupState
    data class Unavailable(val reason: String) : SignupState
}

/** A persisted profile is complete only when email, chosen name and mobile exist. */
object RegistrationFlowPolicy {
    fun profileComplete(exists:Boolean, savedEmail:String?, authenticatedEmail:String,
                        savedName:String?, phone:String) =
        exists && savedEmail == authenticatedEmail && savedName != null &&
            IndianMobile.normalize(phone) == phone
}

/** Google account email + user supplied Indian number; no billed SMS/WhatsApp OTP. */
class StudentRegistrationRepository(private val context: Context) {
    private var requirePhoneConfirmation = false
    private var enteredName: String? = null
    private var authEpoch: Int = 0
    private val app = CloudBackend.app(context)
    private val auth: FirebaseAuth? = app?.let { FirebaseAuth.getInstance(it) }
    private val db: FirebaseFirestore? = app?.let { FirebaseFirestore.getInstance(it) }

    private fun validGoogleUser(): FirebaseUser? =
        auth?.currentUser?.takeIf { user ->
            !user.isAnonymous && user.providerData.any {
                it.providerId == GoogleAuthProvider.PROVIDER_ID
            } && !user.email.isNullOrBlank()
        }

    fun observe(onState: (SignupState) -> Unit): () -> Unit {
        val firebaseAuth = auth ?: run {
            onState(SignupState.Unavailable("Firebase is not configured on this device."))
            return {}
        }
        val listener = FirebaseAuth.AuthStateListener {
            authEpoch += 1
            if (validGoogleUser() == null) onState(SignupState.NeedsGoogle)
            else loadProfile(onState)
        }
        firebaseAuth.addAuthStateListener(listener)
        return { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun loadProfile(onState: (SignupState) -> Unit) {
        val user = validGoogleUser() ?: run {
            onState(SignupState.NeedsGoogle)
            return
        }
        val firestore = db ?: run {
            onState(SignupState.Unavailable("Firestore is not configured."))
            return
        }
        val epoch = ++authEpoch
        val uid = user.uid
        onState(SignupState.Loading)
        firestore.collection("student_profiles").document(uid).get()
            .addOnSuccessListener { doc ->
                if (epoch != authEpoch || validGoogleUser()?.uid != uid) return@addOnSuccessListener
                val email = user.email.orEmpty()
                val storedName = StudentName.normalize(doc.getString("displayName").orEmpty())
                val name = enteredName ?: storedName ?: ""
                val phone = doc.getString("phoneE164").orEmpty()
                val optIn = doc.getBoolean("whatsappOptIn") == true
                val complete = RegistrationFlowPolicy.profileComplete(
                    doc.exists(), doc.getString("email"), email, storedName, phone)
                if (complete && !requirePhoneConfirmation)
                    onState(SignupState.Ready(email, storedName!!, phone, optIn))
                else
                    onState(SignupState.NeedsPhone(email, name, phone, optIn))
            }
            .addOnFailureListener {
                if (epoch != authEpoch || validGoogleUser()?.uid != uid) return@addOnFailureListener
                onState(SignupState.Unavailable(
                    "Unable to read your registration. Check internet and deployed Firestore rules."))
            }
    }

    private fun googleWebClientId(): String {
        val explicit = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (explicit.isNotEmpty()) return explicit
        // google-services plugin creates default_web_client_id after enabling Google
        // sign-in in Firebase and downloading the refreshed google-services.json.
        val id = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName)
        return if (id != 0) context.getString(id) else ""
    }

    fun signInWithGoogle(activity: ComponentActivity, nameInput: String, complete: (String?) -> Unit) {
        val chosenName = StudentName.normalize(nameInput) ?: run {
            complete("Enter your name (2–28 characters).")
            return
        }
        val firebaseAuth = auth ?: run {
            complete("Firebase is not configured.")
            return
        }
        enteredName = chosenName
        requirePhoneConfirmation = true
        val clientId = googleWebClientId()
        if (clientId.endsWith(".apps.googleusercontent.com")) {
            launchGoogleFlow(activity, firebaseAuth, clientId, complete)
            return
        }
        // Public configuration can be enabled in Firebase after APK distribution.
        // No secret or downloaded credentials are stored in this document.
        val firestore = db ?: run {
            complete("Firestore unavailable. Check Firebase setup.")
            return
        }
        firestore.collection("auth_public").document("google").get()
            .addOnSuccessListener { snapshot ->
                val remoteId = snapshot.getString("webClientId").orEmpty().trim()
                if (snapshot.getBoolean("enabled") == true &&
                    remoteId.endsWith(".apps.googleusercontent.com")) {
                    launchGoogleFlow(activity, firebaseAuth, remoteId, complete)
                } else complete("Enable Google login and set auth_public/google webClientId in Firebase Console.")
            }
            .addOnFailureListener {
                complete("Google login needs the Firestore auth_public/google config and deployed security rules.")
            }
    }

    private fun launchGoogleFlow(
        activity: ComponentActivity, firebaseAuth: FirebaseAuth,
        clientId: String, complete: (String?) -> Unit
    ) {
        activity.lifecycleScope.launch {
            try {
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option).build()
                val credential = CredentialManager.create(activity).getCredential(
                    context = activity, request = request).credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    complete("Google did not return a sign-in token.")
                    return@launch
                }
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val current = firebaseAuth.currentUser
                val attempt = if (current?.isAnonymous == true) {
                    current.linkWithCredential(firebaseCredential)
                } else firebaseAuth.signInWithCredential(firebaseCredential)
                attempt.addOnCompleteListener { result ->
                    if (result.isSuccessful) complete(null)
                    else if (current?.isAnonymous == true &&
                        result.exception is FirebaseAuthUserCollisionException) {
                        firebaseAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { retry ->
                                complete(if (retry.isSuccessful) null
                                    else retry.exception?.localizedMessage ?: "Google sign-in failed.")
                            }
                    } else complete(result.exception?.localizedMessage ?: "Google sign-in failed.")
                }
            } catch (exc: Exception) {
                complete(exc.localizedMessage ?: "Google sign-in was cancelled.")
            }
        }
    }

    fun savePhone(nameInput: String, phoneInput: String, whatsappOptIn: Boolean,
                  onResult: (SignupState?, String?) -> Unit) {
        val name = StudentName.normalize(nameInput) ?: run {
            onResult(null, "Please enter your name (2–28 characters).")
            return
        }
        val phone = IndianMobile.normalize(phoneInput) ?: run {
            onResult(null, "Enter a valid Indian 10-digit mobile number.")
            return
        }
        val user = validGoogleUser() ?: run {
            onResult(null, "Please sign in with Google first.")
            return
        }
        val firestore = db ?: run {
            onResult(null, "Firestore is unavailable.")
            return
        }
        val email = user.email.orEmpty()
        val reference = firestore.collection("student_profiles").document(user.uid)
        val epoch = authEpoch
        reference.get().addOnSuccessListener { existing ->
            if (epoch != authEpoch || validGoogleUser()?.uid != user.uid) {
                onResult(null, "Your login changed. Please sign in again.")
                return@addOnSuccessListener
            }
            // Use the student-entered name, not an older Google display name.
            val values = mutableMapOf<String, Any>(
                "email" to email,
                "displayName" to name,
                "phoneE164" to phone,
                "phoneVerified" to false,
                "whatsappOptIn" to whatsappOptIn,
                "consentVersion" to 1,
                "provider" to "google.com",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!existing.exists()) values["createdAt"] = FieldValue.serverTimestamp()
            reference.set(values, SetOptions.merge())
                .addOnSuccessListener {
                    if (epoch != authEpoch || validGoogleUser()?.uid != user.uid) {
                        onResult(null, "Your login changed. Please sign in again.")
                        return@addOnSuccessListener
                    }
                    requirePhoneConfirmation = false
                    enteredName = null
                    onResult(SignupState.Ready(email, name, phone, whatsappOptIn), null)
                }
                .addOnFailureListener {
                    onResult(null, "Could not save registration. Verify Firebase rules and internet.")
                }
        }.addOnFailureListener {
            onResult(null, "Could not reach Firebase. Check your connection.")
        }
    }

    fun signOut(activity: ComponentActivity) {
        authEpoch += 1
        requirePhoneConfirmation = false
        enteredName = null
        auth?.signOut()
        activity.lifecycleScope.launch {
            try {
                CredentialManager.create(activity)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) { /* Firebase sign-out already completed. */ }
        }
    }
}

