import 'package:flutter_test/flutter_test.dart';
import 'package:filter_plugin/filter_plugin.dart';
import 'package:filter_plugin/filter_plugin_platform_interface.dart';
import 'package:filter_plugin/filter_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFilterPluginPlatform
    with MockPlatformInterfaceMixin
    implements FilterPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FilterPluginPlatform initialPlatform = FilterPluginPlatform.instance;

  test('$MethodChannelFilterPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFilterPlugin>());
  });

  test('getPlatformVersion', () async {
    FilterPlugin filterPlugin = FilterPlugin();
    MockFilterPluginPlatform fakePlatform = MockFilterPluginPlatform();
    FilterPluginPlatform.instance = fakePlatform;

    expect(await filterPlugin.getPlatformVersion(), '42');
  });
}
