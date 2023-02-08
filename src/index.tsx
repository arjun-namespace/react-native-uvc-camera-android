import React from 'react';
import type { CameraApi } from './types';

import {
  findNodeHandle,
  NativeModules,
  requireNativeComponent,
} from 'react-native';
import { Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-uvc-camera-android' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const NativeCameraModule = NativeModules.UvcCameraAndroid
  ? NativeModules.UvcCameraAndroid
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
const NativeCameraView = requireNativeComponent('UvcCameraAndroidViewManager');

const Camera = React.forwardRef((props: any, ref) => {
  const nativeRef = React.useRef();

  React.useImperativeHandle<any, CameraApi>(ref, () => ({
    capture: async () => {
      return await NativeCameraModule.capture(
        findNodeHandle(nativeRef.current ?? null)
      );
    },
  }));

  return (
    <NativeCameraView
      ref={nativeRef}
      style={props.style}
      onCameraConnected={props.onCameraConnected}
      onCameraDisconnected={props.onCameraDisconnected}
      {...props}
    />
  );
});

export default Camera;
