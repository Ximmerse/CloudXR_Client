# CloudXR Client

- **This software contains source code provided by NVIDIA Corporation**.
- The client decodes and renders content that is streamed from the CloudXR server and collects motion and controller data from the VR headset that is sent to the CloudXR server.The VR headset must be capable of decoding 4k HEVC video @ 60fps. 

## Feature
- The Project integrates with [CloudXR v3.2](https://developer.nvidia.com/nvidia-cloudxr-sdk-get-started) 

### development environment required
- Android Studio 2020.3.1 or later, can be downloaded from https://developer.android.com/studio.
- Android SDK 10 (API level 29) or higher.
- Android build tools 29.0.3
- Android NDK 21.4.7075529
- OpenJDK 1.8
- Android Debug Bridge (ADB) to install the client application without needing full developer tools.
- An MR headset: RhinoX Pro 



## Building the  CloudXR Client
1. Navigate to `cloudxr_client` folder and open the `cloudxr_client`.
2. Select **Build > Make Project**.

This process should generate an `.apk` file in the `cloudxr_client\app\build\outputs\apk\debug` directory that can be used to debug or be installed manually. You can also automatically generate an `.apk` file by running Android Studio. See **Running the CloudXR Client** for more information.


## Installing the CloudXR Client

> 💡 You do not need these steps if you are running directly from Android Studio, it will install the `.apk` for you.
1. Place the MR device in developer mode and allow a USB connection in debug mode on the device.
2. Use a USB cable to connect the MR device to the development system.
3. If prompted on the device to allow connections, select **Allow**.
4. In a Command Prompt window, navigate to the folder that contains the `.apk` file that was created by building the sample application.
5. Use ADB to install the application from the release `.apk` file.
```
    adb.exe install -r <APK name>.apk
```
> 💡 By default, the ADB.exe program is installed by Android Studio in `C:\Users\{username}\AppData\Local\Android\Sdk\platform-tools`

When the installation is complete, ADB responds with a `Success` message.

## Running the CloudXR Client
1. (**Optional**) Pre-specify the command-line per device:
   1. Create a plain-text file named `CloudXRLaunchOptions.txt` that contains `-s <IP address NVIDIA CloudXR server>`.
  For example, for a server with `IP = 1.1.1.1`, the file should contain `-s 1.1.1.1`.
   2. Copy the `CloudXRLaunchOptions.txt` file to the base device folder as shown in Windows Explorer, or if using ADB, to the `/sdcard/` directory of the device using the following command:
      ```
      adb.exe push CloudXRLaunchOptions.txt /sdcard/CloudXRLaunchOptions.txt
      ```
      See [Command-Line Options](https://docs.nvidia.com/cloudxr-sdk/usr_guide/cmd_line_options.html#command-line-options) for more information about using launch options and a full list of all available options.

2. Start **SteamVR** on the server system.
3. Start the **CloudXR Client** app on MR device.
  This process can be completed in one of the following ways:
  - If installed from ADB with a launch options file, launch from VR on the device:
     - Open the main menu.
     - Select **Library**.
     - Select the **CloudXR Client** app.
  - If building in Android Studio, deploy/run directly.
    - You can also set launch options inside the Configuration. See [Command-Line Options]((https://docs.nvidia.com/cloudxr-sdk/usr_guide/cmd_line_options.html#command-line-options)) for more information about how to set launch options from Android Studio.
    > 💡 If prompted, grant the requested permissions on the device.

4. Start the OpenVR application on the server that will be streamed to the client.
This process can be completed in one of the following ways:
  - Launch it directly on the server.
  > 💡 Launch the OpenVR application only after the client has connected to the server unless the client has been pre-configured on the server. Otherwise, the application will report that there is no connected headset. When a client first connects, it reports its specifications, such as resolution and refresh rate, to the server and then the server creates a virtual headset device
