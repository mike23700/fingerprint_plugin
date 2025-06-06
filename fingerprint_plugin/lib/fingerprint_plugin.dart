import 'package:flutter/services.dart';

class FingerprintPlugin {
  static const MethodChannel _channel = MethodChannel('fingerprint_plugin');

  /// Vérifie si la biométrie est disponible sur l'appareil
  static Future<bool> checkBiometrics() async {
    try {
      final bool isAvailable = await _channel.invokeMethod('checkBiometrics');
      return isAvailable;
    } catch (e) {
      print('Error checking biometrics: $e');
      return false;
    }
  }

  /// Lance l'authentification biométrique
  /// Retourne true si l'authentification a réussi, false sinon
  static Future<bool> authenticate() async {
    try {
      final bool isAuthenticated = await _channel.invokeMethod('authenticate');
      return isAuthenticated;
    } catch (e) {
      print('Error during biometric authentication: $e');
      return false;
    }
  }
}
