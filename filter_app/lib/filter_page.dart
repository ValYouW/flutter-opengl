import 'dart:async';
import 'dart:ui';
import 'package:filter_plugin/filter_preview.dart';
import 'package:flutter/material.dart';
import 'package:filter_plugin/filter_controller.dart';

class FilterPage extends StatefulWidget {
  const FilterPage({Key? key}) : super(key: key);

  @override
  State<FilterPage> createState() => _FilterPageState();
}

class _FilterPageState extends State<FilterPage> {
  double _radius = 0;
  FilterController? _controller;

  @override
  void initState() {
    super.initState();

    init();
  }

  @override
  dispose() async {
    super.dispose();
    await _controller?.dispose();
  }

  init() async {
    // Load the image from the embedded assets
    const imageProvider = AssetImage('assets/drawable/matterhorn.jpg');
    var stream = imageProvider.resolve(ImageConfiguration.empty);

    // create a promise that will be resolved once the image is loaded
    final Completer<ImageInfo> completer = Completer<ImageInfo>();
    var listener = ImageStreamListener((ImageInfo info, bool _) {
      completer.complete(info);
    });

    // listen to the image loaded event
    stream.addListener(listener);

    // wait for the image to be loaded
    final imageInfo = await completer.future;

    // create the filter controller
    await initFilterController(imageInfo);

    // This is important to release memory within the image stream
    stream.removeListener(listener);
  }

  initFilterController(ImageInfo imageInfo) async {
    // Convert the image bytes to raw rgba
    final rgba = await imageInfo.image.toByteData(format: ImageByteFormat.rawRgba);

    // Initialize the filter controller
    _controller = FilterController();
    await _controller!.initialize(rgba!, imageInfo.image.width, imageInfo.image.height);

    await _controller!.draw(_radius);

    // update ui
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    if (_controller == null) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    return Material(
      child: Container(
        color: Colors.white,
        child: Column(
          children: [
            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  FilterPreview(_controller!),
                ],
              ),
            ),
            Row(
              children: [
                const SizedBox(width: 20),
                const Text(
                  'Blur',
                  style: TextStyle(color: Colors.black, fontSize: 20),
                ),
                Expanded(
                  child: Slider(
                    value: _radius,
                    min: 0,
                    max: 20,
                    onChanged: (val) {
                      setState(() {
                        _radius = val;
                        _controller!.draw(_radius);
                      });
                    },
                  ),
                )
              ],
            )
          ],
        ),
      ),
    );
  }
}
