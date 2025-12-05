# react-native-lock-task

A React Native Library to support lock task mode for Android platform

## Installation

```
npm install react-native-lock-task --save
```

Make the following changes to your main `AndroidManifest.xml`
```
  <application>
      <activity
          android:excludeFromRecents="true"
          android:launchMode="singleTask"
          android:lockTaskMode="if_whitelisted"
          ... />

      <receiver
          android:name="com.darkpos.locktask.LockTaskReceiver"
          android:exported="true"
          android:label="LockTask"
          android:description="Allow LockTask to lock the screen and run in an immersive mode"
          android:permission="android.permission.BIND_DEVICE_ADMIN" >
          <meta-data
              android:name="android.app.device_admin"
              android:resource="@xml/device_admin_receiver" />

          <intent-filter>
              <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
          </intent-filter>
      </receiver>
  ...
  </application>
```

Run the following command to assign the device owner
```
adb shell dpm set-device-owner "com.darkpos.kiosk/com.darkpos.locktask.LockTaskReceiver"
```

## Integrate module

To integrate `react-native-lock-task` with the rest of your react app just execute:
```
react-native link react-native-lock-task
```

## Usage

```javascript
import LockTask from "react-native-lock-task";

LockTask.startKiosk();
LockTask.stopLockTask();
```
