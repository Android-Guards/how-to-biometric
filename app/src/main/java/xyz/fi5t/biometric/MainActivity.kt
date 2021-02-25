package xyz.fi5t.biometric

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import xyz.fi5t.biometric.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.weakBiometryButton.setOnClickListener {
            val success = BiometricManager.from(this)
                .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS

            if (success) {
                val authPrompt = Class2BiometricAuthPrompt.Builder("Input biometry", "dismiss").apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }.build()

                lifecycleScope.launch {
                    try {
                        authPrompt.authenticate(AuthPromptHost(this@MainActivity))

                        Log.d("It works", "Hello from biometry")
                    } catch (e: AuthPromptErrorException) {
                        Log.e("AuthPromptError", e.message ?: "no message")
                    } catch (e: AuthPromptFailureException) {
                        Log.e("AuthPromptFailure", e.message ?: "no message")
                    }
                }
            }
        }

        binding.strongBiometryButton.setOnClickListener {
            val success = BiometricManager.from(this)
                .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS

            if (success) {
                val biometricCipher = BiometricCipher(this.applicationContext)
                val encryptor = biometricCipher.getEncryptor()

                val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                    setSubtitle("Input your biometry")
                    setDescription("We need your finger")
                    setConfirmationRequired(true)
                }.build()

                lifecycleScope.launch {
                    try {
                        val authResult = authPrompt.authenticate(AuthPromptHost(this@MainActivity), encryptor)

                        val encryptedEntity = authResult.cryptoObject?.cipher?.let { cipher ->
                            biometricCipher.encrypt(
                                "Secret data",
                                cipher
                            )
                        }

                        Log.d(MainActivity::class.simpleName, String(encryptedEntity!!.ciphertext))
                    } catch (e: AuthPromptErrorException) {
                        Log.e("AuthPromptError", e.message ?: "no message")
                    } catch (e: AuthPromptFailureException) {
                        Log.e("AuthPromptFailure", e.message ?: "no message")
                    }
                }
            }
        }
    }
}
