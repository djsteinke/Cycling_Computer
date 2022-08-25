package rnfive.htfu.cyclingcomputer.define.listeners;

import rnfive.htfu.cyclingcomputer.define.enums.SensorState;

public interface IDeviceStateChangeReceiver {
    void onDeviceStateChange(int id, String sensorId, SensorState sensorState);
}
