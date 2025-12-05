import { NativeModules } from 'react-native';
const { LockTask } = NativeModules;

export default {
  isDeviceAdminEnabled: () => LockTask.isDeviceAdminEnabled(),
  requestDeviceAdmin: () => LockTask.requestDeviceAdmin(),
  removeDeviceAdmin: () => LockTask.removeDeviceAdmin(),
  setLockTaskPackages: () => LockTask.setLockTaskPackages(),
  startLockTask: () => LockTask.startLockTask(),
  stopLockTask: () => LockTask.stopLockTask(),
  enableImmersiveMode: () => LockTask.enableImmersiveMode(),
  startKiosk: async () => {
    const enabled = await LockTask.isDeviceAdminEnabled();
    if (!enabled) {
      LockTask.requestDeviceAdmin();
      return;
    }
    LockTask.setLockTaskPackages();
    LockTask.enableImmersiveMode();
    LockTask.startLockTask();
  },
};
