package com.example.fingerprint_plugin

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.Executor

class FingerprintPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var activity: android.app.Activity? = null
    private var result: Result? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fingerprint_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        this.result = result
        when (call.method) {
            "checkBiometrics" -> checkBiometrics(result)
            "authenticate" -> authenticate()
            else -> result.notImplemented()
        }
    }

    private fun checkBiometrics(result: Result) {
        try {
            println("FingerprintPlugin: Starting biometric check")
            
            val context = activity?.applicationContext ?: run {
                val error = "Context is null - Activity is not attached"
                println("FingerprintPlugin: ERROR - $error")
                result.error("UNAVAILABLE", error, null)
                return
            }

            println("FingerprintPlugin: Context obtained successfully")
            
            val biometricManager = BiometricManager.from(context)
            
            println("FingerprintPlugin: BiometricManager obtained successfully")
            
            // Vérifier la disponibilité de la biométrie
            val canAuthenticate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                biometricManager.canAuthenticate(BIOMETRIC_STRONG)
            } else {
                @Suppress("DEPRECATION")
                biometricManager.canAuthenticate()
            }
            
            println("FingerprintPlugin: canAuthenticate result: $canAuthenticate")
            
            when (canAuthenticate) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    println("FingerprintPlugin: BIOMETRIC_SUCCESS - Biometric authentication available")
                    result.success(true)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    println("FingerprintPlugin: BIOMETRIC_ERROR_NO_HARDWARE - No biometric hardware available")
                    result.success(false)
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    println("FingerprintPlugin: BIOMETRIC_ERROR_HW_UNAVAILABLE - Biometric hardware unavailable")
                    result.success(false)
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    println("FingerprintPlugin: BIOMETRIC_ERROR_NONE_ENROLLED - No biometrics enrolled")
                    result.success(false)
                }
                else -> {
                    println("FingerprintPlugin: UNKNOWN_RESULT - Result code: $canAuthenticate")
                    result.success(false)
                }
            }
        } catch (e: Exception) {
            println("FingerprintPlugin: Exception in checkBiometrics: ${e.message}")
            result.error("ERROR", e.message, null)
        }
    }

    // Variables pour gérer l'état de l'authentification
    private var isFingerDetected = false
    private var lastAuthResult: Boolean = false
    private var biometricPrompt: BiometricPrompt? = null
    private var currentResult: Result? = null

    private fun authenticate() {
        println("FingerprintPlugin: Starting authentication process...")
        currentResult = result
        val activity = this.activity as? FragmentActivity
        if (activity == null) {
            val error = "Activity is not attached or not a FragmentActivity"
            println("FingerprintPlugin: ERROR - $error")
            currentResult?.error("UNAVAILABLE", error, null)
            currentResult = null
            return
        }
        println("FingerprintPlugin: Activity is valid")

        // Vérifier d'abord si les biométries sont disponibles
        val context = activity.applicationContext
        val biometricManager = BiometricManager.from(context)
        
        // Vérifier la disponibilité de la biométrie
        val canAuthenticate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            biometricManager.canAuthenticate()
        }
        
        println("FingerprintPlugin: canAuthenticate result: $canAuthenticate")
        
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val errorMsg = when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Aucune empreinte digitale enregistrée"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Aucun capteur biométrique disponible"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Le capteur biométrique n'est pas disponible"
                else -> "Erreur inconnue: $canAuthenticate"
            }
            println("FingerprintPlugin: Cannot authenticate - $errorMsg")
            currentResult?.error("AUTH_ERROR", errorMsg, canAuthenticate)
            currentResult = null
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        
        // Créer le BiometricPrompt une seule fois
        if (biometricPrompt == null) {
            biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        val errorMessage = "Authentication error: $errorCode - $errString"
                        println("FingerprintPlugin: $errorMessage")
                        
                        when (errorCode) {
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_USER_CANCELED -> {
                                println("FingerprintPlugin: Authentication cancelled by user")
                                isFingerDetected = false
                                currentResult?.success(false)
                            }
                            BiometricPrompt.ERROR_LOCKOUT -> {
                                println("FingerprintPlugin: Too many failed attempts. Biometric locked.")
                                isFingerDetected = false
                                currentResult?.error("AUTH_ERROR", "Trop de tentatives échouées. Veuillez réessayer plus tard.", errorCode)
                            }
                            else -> {
                                isFingerDetected = false
                                currentResult?.error("AUTH_ERROR", errorMessage, errorCode)
                            }
                        }
                        currentResult = null
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        println("FingerprintPlugin: Fingerprint recognized")
                        isFingerDetected = true
                        lastAuthResult = true
                        currentResult?.success(true)
                        currentResult = null
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        println("FingerprintPlugin: Authentication failed - Biometric not recognized")
                        isFingerDetected = false
                        lastAuthResult = false
                        // Ne pas envoyer de résultat ici, attendre une nouvelle tentative ou une annulation
                    }
                })
        }

        try {
            println("FingerprintPlugin: Building prompt info...")
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentification par empreinte digitale")
                .setSubtitle("Placez votre doigt sur le capteur")
                .setNegativeButtonText("Annuler")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build()

            // Démarrer la détection d'empreinte digitale
            println("FingerprintPlugin: Starting fingerprint detection...")
            biometricPrompt?.authenticate(promptInfo)
            
        } catch (e: Exception) {
            val errorMessage = "Exception in authenticate: ${e.javaClass.name} - ${e.message}"
            println("FingerprintPlugin: $errorMessage")
            e.printStackTrace()
            currentResult?.error("AUTH_ERROR", "Erreur lors de la détection d'empreinte: ${e.message}", null)
            currentResult = null
        }
    }
}
