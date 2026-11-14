package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityManager(private val context: Context) {

    private val keyAlias = "uk_virtual_phone_aes_key"
    private val vaultFile: File get() = File(context.filesDir, "uk_secure_vault.enc")

    private fun getOrCreateAesKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(keyAlias, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encryptAndSaveSecureData(plainText: String): Boolean {
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encodedData = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
            vaultFile.writeText("$encodedIv:$encodedData")
            true
        }.getOrDefault(false)
    }

    fun readEncryptedSecureData(): String? {
        if (!vaultFile.exists()) return null
        return runCatching {
            val parts = vaultFile.readText().split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        }.getOrNull()
    }

    fun savePin(pin: String): Boolean {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hashPin(pin, saltHex)
        return encryptAndSaveSecureData("PIN_V1|$saltHex|$hashHex")
    }

    fun verifyPin(pin: String): Boolean {
        val stored = readEncryptedSecureData() ?: return false
        val parts = stored.split("|")
        if (parts.size != 3 || parts[0] != "PIN_V1") return false
        val saltHex = parts[1]
        val expectedHash = parts[2]
        return hashPin(pin, saltHex) == expectedHash
    }

    fun hasPinConfigured(): Boolean {
        return readEncryptedSecureData()?.startsWith("PIN_V1|") == true
    }

    fun clearPinAndVault() {
        if (vaultFile.exists()) {
            vaultFile.delete()
        }
    }

    fun isKeystoreBacked(): Boolean {
        return runCatching {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.containsAlias(keyAlias) || getOrCreateAesKey().algorithm == "AES"
        }.getOrDefault(false)
    }

    private fun hashPin(pin: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$saltHex:$pin:uk_vphone_salt".toByteArray(Charsets.UTF_8)
        return digest.digest(input).joinToString("") { "%02x".format(it) }
    }

    fun getBiometricCapability(): BiometricCapability {
        val manager = BiometricManager.from(context)
        val result = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.UNAVAILABLE
            else -> BiometricCapability.UNAVAILABLE
        }
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Unlock UK Virtual Phone",
        subtitle: String = "Verify your identity using device biometrics",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric not recognised. Try again or use PIN.")
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .build()
        prompt.authenticate(promptInfo)
    }
}

enum class BiometricCapability(val label: String) {
    AVAILABLE("Biometric hardware ready"),
    NOT_ENROLLED("Supported, no biometrics enrolled on device"),
    UNAVAILABLE("No biometric sensor available on this device")
}
