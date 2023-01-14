import 'package:flutter/widgets.dart';
import 'filter_controller.dart';

class FilterPreview extends StatelessWidget {
  const FilterPreview(this.controller, {Key? key}) : super(key: key);

  final FilterController controller;

  @override
  Widget build(BuildContext context) {
    if (!controller.initialized) {
      return Container();
    }

    return AspectRatio(
      aspectRatio: controller.width / controller.height,
      // The flutter Texture widget draws a gpu texture using the texture id we got
      // from the filter native implementation
      child: Texture(
        textureId: controller.textureId,
      ),
    );
  }
}
