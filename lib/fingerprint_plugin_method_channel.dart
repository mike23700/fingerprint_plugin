import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'fingerprint_plugin_platform_interface.dart';

/// An implementation of [FingerprintPluginPlatform] that uses method channels.
class MethodChannelFingerprintPlugin extends FingerprintPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('fingerprint_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
