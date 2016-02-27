# L05.04_Sockets
Lecture 05 - 04 Sockets, DISCA - UPV, Development of apps for mobile devices.

A TabHost displays a user interface (UI) for two different devices to act as Server and Client, and transfer an image file between them. The image is transferred using Sockets.
- The Server UI just displays a single ToggleButton to start/stop a ServerSocket listening on port 9999. The IP address of the Server is also displayed, as it will be required by the Client to establish a connection. The ServerSocket operates on background in an asynchronous task. Upong receiving the image file, it will be stored on internal storage and the image displayed on screen.
- The user must introduce the Server IP address on the Client UI. The center icon lets the user select an image from those available on the device. By clicking the bottom Button, an asynchronous task will connect to the ServerSocket (via a Socket) to send the image file.

Both the Server and Client devices should be in the same network to be able to communicate.

In case of using Android emulators, then follow the instructions at http://developer.android.com/intl/es/tools/devices/emulator.html#connecting to enable the communication between them (the Server will always get the IP adress 10.0.2.15, but the Client should send the image to the IP address 10.0.2.2).
