import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'filter_plugin_method_channel.dart';

abstract class FilterPluginPlatform extends PlatformInterface {
  /// Constructs a FilterPluginPlatform.
  FilterPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static FilterPluginPlatform _instance = MethodChannelFilterPlugin();

  /// The default instance of [FilterPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelFilterPlugin].
  static FilterPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FilterPluginPlatform] when
  /// they register themselves.
  static set instance(FilterPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
