export type CameraApi = {
  capture: () => Promise<{ uri: string }>;
};
