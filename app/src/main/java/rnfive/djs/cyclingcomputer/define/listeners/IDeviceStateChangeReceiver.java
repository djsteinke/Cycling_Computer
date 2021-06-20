package rnfive.djs.cyclingcomputer.define.listeners;

import rnfive.djs.cyclingcomputer.define.enums.SensorState;

public interface IDeviceStateChangeReceiver {
    void onDeviceStateChange(int id, String sensorId, SensorState sensorState);
}
