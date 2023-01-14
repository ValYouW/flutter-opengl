
import 'filter_plugin_platform_interface.dart';

class FilterPlugin {
  Future<String?> getPlatformVersion() {
    return FilterPluginPlatform.instance.getPlatformVersion();
  }
}
