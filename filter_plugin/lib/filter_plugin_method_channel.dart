import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'filter_plugin_platform_interface.dart';

/// An implementation of [FilterPluginPlatform] that uses method channels.
class MethodChannelFilterPlugin extends FilterPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('filter_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
