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

/** Google account email + user supplied Indian number; no billed SMS/WhatsApp OTP. */
class StudentRegistrationRepository(private val context: Context) {
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
        onState(SignupState.Loading)
        firestore.collection("student_profiles").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val email = user.email.orEmpty()
                val name = user.displayName.orEmpty().take(100)
                val phone = doc.getString("phoneE164").orEmpty()
                val optIn = doc.getBoolean("whatsappOptIn") == true
                if (doc.exists() && IndianMobile.normalize(phone) == phone &&
                    doc.getString("email") == email) {
                    onState(SignupState.Ready(email, name, phone, optIn))
                } else onState(SignupState.NeedsPhone(email, name))
            }
            .addOnFailureListener {
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

    fun signInWithGoogle(activity: ComponentActivity, complete: (String?) -> Unit) {
        val firebaseAuth = auth ?: run {
            complete("Firebase is not configured.")
            return
        }
        val clientId = googleWebClientId()
        if (!clientId.endsWith(".apps.googleusercontent.com")) {
            complete("Google login setup is incomplete. Enable Google in Firebase, add the APK SHA-1, then refresh google-services.json.")
            return
        }
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

    fun savePhone(phoneInput: String, whatsappOptIn: Boolean,
                  onResult: (SignupState?, String?) -> Unit) {
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
        val name = user.displayName.orEmpty().take(100)
        val reference = firestore.collection("student_profiles").document(user.uid)
        reference.get().addOnSuccessListener { existing ->
            // createdAt is immutable; saved only on initial registration.
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
        auth?.signOut()
        activity.lifecycleScope.launch {
            try {
                CredentialManager.create(activity)
                    .clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) { /* Firebase sign-out already completed. */ }
        }
    }
}

/** Whole-screen registration gate; all app tabs stay behind it until registered. */
@Composable
fun StudentRegistrationGate(
    state: SignupState,
    hindi: Boolean,
    editing: Boolean,
    onGoogle: ((String?) -> Unit) -> Unit,
    onSave: (String, Boolean, (String?) -> Unit) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onPreview: () -> Unit
) {
    var phone by remember(state) {
        mutableStateOf((state as? SignupState.NeedsPhone)?.phone?.removePrefix("+91").orEmpty())
    }
    var optIn by remember(state) {
        mutableStateOf((state as? SignupState.NeedsPhone)?.whatsappOptIn ?: false)
    }
    var busy by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(R.drawable.bssc_bilingual_logo),
            "Approved OneShotGS BSSC CGL 4 Free Mock branding",
            Modifier.fillMaxWidth().height(155.dp),
            contentScale = ContentScale.Fit)
        Text(t(hindi, "Student registration", "छात्र पंजीकरण"),
            color = SoftWhite, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(t(hindi,
            "Free account · no paid SMS or WhatsApp OTP",
            "मुफ़्त खाता · कोई SMS या WhatsApp OTP शुल्क नहीं"),
            color = Gold, fontSize = 12.sp)
        when (state) {
            SignupState.Loading -> CircularProgressIndicator(color = Gold)
            SignupState.NeedsGoogle -> {
                Text(t(hindi,
                    "Continue with Google to use your verified account email.",
                    "अपनी सत्यापित ईमेल के लिए Google से लॉग इन करें।"),
                    color = SoftWhite, fontSize = 14.sp)
                Button(onClick = {
                    localError = null; busy = true
                    onGoogle { error -> busy = false; localError = error }
                }, enabled = !busy,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(t(hindi, "Continue with Google", "Google से जारी रखें"))
                }
            }
            is SignupState.NeedsPhone -> {
                Text(state.email, color = Gold, fontSize = 14.sp)
                Text(t(hindi,
                    "Enter your mobile number for your student account and important exam support. This number is NOT OTP-verified.",
                    "छात्र खाते और परीक्षा सहायता के लिए मोबाइल नंबर दें। यह नंबर OTP से सत्यापित नहीं है।"),
                    color = SoftWhite, fontSize = 13.sp)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { char -> char.isDigit() }.take(10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(t(hindi, "Indian mobile number", "भारतीय मोबाइल नंबर")) },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = optIn, onCheckedChange = { optIn = it })
                    Text(t(hindi,
                        "Optional: I agree to WhatsApp exam updates from OneShotGS. I can withdraw consent later.",
                        "वैकल्पिक: मुझे OneShotGS के WhatsApp परीक्षा अपडेट स्वीकार हैं। मैं सहमति वापस ले सकता/सकती हूँ।"),
                        modifier = Modifier.weight(1f), color = SoftWhite, fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        localError = null
                        busy = true
                        onSave(phone, optIn) { error -> busy = false; localError = error }
                    },
                    enabled = !busy && IndianMobile.normalize(phone) != null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(t(hindi, "Save and start learning", "सेव करें और तैयारी शुरू करें")) }
                if (editing) TextButton(onClick = onCancel) {
                    Text(t(hindi, "Cancel", "रद्द करें"), color = Gold)
                }
            }
            is SignupState.Unavailable -> {
                Text(state.reason, color = Color(0xFFFFCDD2), fontSize = 13.sp)
                OutlinedButton(onClick = onRetry) { Text(t(hindi, "Retry", "दोबारा कोशिश करें")) }
            }
            is SignupState.Ready -> Unit
        }
        localError?.let { Text(it, color = Color(0xFFFFCDD2), fontSize = 12.sp) }
        if (BuildConfig.DEBUG && !editing) {
            TextButton(onClick = onPreview) {
                Text(t(hindi, "Review other screens without registration (test only)",
                    "बिना पंजीकरण बाकी स्क्रीन देखें (सिर्फ परीक्षण)"),color=Gold)
            }
        }
        Text(t(hindi,
            "Your email and mobile are stored privately in Firebase. Only your account can view/edit its profile; administrators can manage registrations. We never send WhatsApp messages without separate consent. Delete your account from More.",
            "ईमेल और मोबाइल Firebase में निजी रूप से सुरक्षित रहेंगे। अपना डेटा आप और अधिकृत प्रशासक देख सकते हैं। बिना अलग सहमति के WhatsApp संदेश नहीं भेजे जाएंगे। खाता हटाने के लिए अन्य टैब खोलें।"),
            color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
    }
}
