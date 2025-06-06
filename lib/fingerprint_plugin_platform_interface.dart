import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'fingerprint_plugin_method_channel.dart';

abstract class FingerprintPluginPlatform extends PlatformInterface {
  /// Constructs a FingerprintPluginPlatform.
  FingerprintPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FingerprintPluginPlatform _instance = MethodChannelFingerprintPlugin();

  /// The default instance of [FingerprintPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFingerprintPlugin].
  static FingerprintPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FingerprintPluginPlatform] when
  /// they register themselves.
  static set instance(FingerprintPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
