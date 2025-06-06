import 'package:flutter_test/flutter_test.dart';
import 'package:fingerprint_plugin/fingerprint_plugin.dart';
import 'package:fingerprint_plugin/fingerprint_plugin_platform_interface.dart';
import 'package:fingerprint_plugin/fingerprint_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFingerprintPluginPlatform
    with MockPlatformInterfaceMixin
    implements FingerprintPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FingerprintPluginPlatform initialPlatform = FingerprintPluginPlatform.instance;

  test('$MethodChannelFingerprintPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFingerprintPlugin>());
  });

  test('getPlatformVersion', () async {
    FingerprintPlugin fingerprintPlugin = FingerprintPlugin();
    MockFingerprintPluginPlatform fakePlatform = MockFingerprintPluginPlatform();
    FingerprintPluginPlatform.instance = fakePlatform;

    expect(await fingerprintPlugin.getPlatformVersion(), '42');
  });
}
