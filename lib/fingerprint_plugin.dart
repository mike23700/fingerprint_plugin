
import 'fingerprint_plugin_platform_interface.dart';

class FingerprintPlugin {
  Future<String?> getPlatformVersion() {
    return FingerprintPluginPlatform.instance.getPlatformVersion();
  }
}
