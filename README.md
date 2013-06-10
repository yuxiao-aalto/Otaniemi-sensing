Otaniemi-sensing
================

Collect sensing information (e.g. video/audio, GPS, Wi-Fi, motion sensors) from Android phones, and forward information to remote storage

StreamingClient: An Android app that records video and then send the video data using UDP or RTP. In the latest version, the RTP based streams cannot be correctly decoded on the other side. Therefore, only the UDP streaming is working and has been tested with VLC player.

VMProxy: StreamingClient sends video data to VMProxy which then can forwards the data to more than one StreamVM.

We can consider StreamVM as a receiver or subscriber of the video streams.

StreamVM: Java code. It is listening on a UDP port.

To-do: the IP addresses are still hardcoded. They should be modified.
