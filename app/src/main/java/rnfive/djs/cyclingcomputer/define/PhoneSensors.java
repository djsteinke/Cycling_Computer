package rnfive.djs.cyclingcomputer.define;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import static rnfive.djs.cyclingcomputer.service.Service_Recording.data;

@Getter
@Setter
public class PhoneSensors implements SensorEventListener {

    private static final String TAG = PhoneSensors.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorPressure;
    private Sensor sensorMagneticField;
    private Sensor sensorStepDetector;
    private boolean running;
    private double pressure = 0f;
    private double pressurePrev = 0f;
    private double pressureCurr = 0f;

    private List<Float> pressureList = new ArrayList<>();
    private static final int pressureListSize = 15;

    public static double absPressure;

    public PhoneSensors() {}

    public void setSensorManager(@Nullable SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        if (this.sensorManager != null) {
            this.sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            this.sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            this.sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            this.sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }
    }

    public void startUpdates() {
        if (sensorManager != null) {
            if (!running) {
                if (sensorPressure != null)
                    sensorManager.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
                if (sensorAccelerometer != null && sensorMagneticField != null) {
                    sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
                }
                if (sensorStepDetector != null) {
                    sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
                }
                Log.d("ACTION", "Sensors registered.");
            }
            running = true;
        }
    }

    public void stopUpdates() {
        if (!StaticVariables.bStarted) {
            if (running) {
                if (sensorPressure != null)
                    sensorManager.unregisterListener(this, sensorPressure);
                if (sensorAccelerometer != null && sensorMagneticField != null) {
                    sensorManager.unregisterListener(this, sensorAccelerometer);
                    sensorManager.unregisterListener(this, sensorMagneticField);
                }
                if (sensorStepDetector != null) {
                    sensorManager.unregisterListener(this, sensorStepDetector);
                }
                Log.d("ACTION", "Sensors unregistered.");
            }
            running = false;
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if device accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        boolean bAccOrMag = false;
        if (data != null) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_STEP_DETECTOR:
                    // TODO steps

                    //if (event.values[0]==1) {
                        //a_l_steps = updateLongArray(a_l_steps, System.currentTimeMillis());
                    //}

                    break;
                case Sensor.TYPE_PRESSURE :
                    Lists.addValue(pressureList, event.values[0], pressureListSize);
                    pressure = Lists.getAvg(pressureList);
                    //Log.d(TAG, "Pressure[" + event.values[0] + "]");
                    if (pressureCurr == 0)
                        pressureCurr = event.values[0];
                    pressureCurr = StaticVariables.lowPassFilter( (double) event.values[0], pressureCurr);
                    break;
                case Sensor.TYPE_ACCELEROMETER :
                    data.setGravityArray(Filters.lowPassFilter(event.values.clone(), data.getGravityArray()));
                    bAccOrMag = true;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD :
                    data.setMagneticArray(Filters.lowPassFilter(event.values.clone(), data.getMagneticArray()));
                    bAccOrMag = true;
                    break;
                default :
                    break;
            }

            if (bAccOrMag) {
                if (data.getGravityArray() != null && data.getMagneticArray() != null) {
                    float[] R = new float[9];
                    float[] I = new float[9];
                    if (SensorManager.getRotationMatrix(R, I, data.getGravityArray(), data.getMagneticArray())) {

                        // orientation contains azimut, pitch and roll
                        //SensorManager.getOrientation(R, orientation);
                        //float azimut = orientation[0];
                        //fBearing = azimutToBearing(azimut);

                        // get bearing to target
                        float[] orientation = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        // east degrees of true North
                        data.setBearing(Bearing.determineDirection(Math.toDegrees(orientation[0]), StaticVariables.geomagneticField));
                    }
                }
            }
        }
    }
}
