package rnfive.htfu.cyclingcomputer.define;

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
import rnfive.htfu.cyclingcomputer.MainActivity;

import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;

@Getter
@Setter
public class PhoneSensors implements SensorEventListener {

    private static final String TAG = PhoneSensors.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorPressure;
    private Sensor sensorMagneticField;
    private Sensor sensorStepDetector;
    private Sensor sensorRotationVector;
    private boolean running;
    private double pressure;
    private double pressurePrev;
    private double pressureCurr;

    private List<Float> pressureList = new ArrayList<>();
    private static final int pressureListSize = 15;
    private static final int READING_RATE_1000 = 1000000;
    private static final int READING_RATE_4000 = 100000;

    public static double absPressure;

    private int iAccMag;

    public PhoneSensors() {}

    public void setSensorManager(@Nullable SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        if (this.sensorManager != null) {
            sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
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
                if (sensorRotationVector != null) {
                    sensorManager.registerListener(this,sensorRotationVector,  SensorManager.SENSOR_DELAY_NORMAL);
                }
                if (sensorStepDetector != null && MainActivity.bActivityRecognitionGranted) {
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
                if (sensorRotationVector != null)
                    sensorManager.unregisterListener(this, sensorRotationVector);
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

    private boolean bAcc;
    private boolean bMag;
    private boolean bRot;
    private float[] R = new float[9];
    //private float[] prevR = new float[9];
    private float[] I = new float[9];
    private float[] orientation = new float[3];
    private float[] rotationOrientation = new float[3];
    private float[] rotationMatrix = new float[16];
    private double angle;
    private float angleF;
    private int cnt = 0;
    private float[] gravityValues;
    private float[] geomagneticValues;
    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (data != null) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_STEP_DETECTOR:
                    // TODO steps
                    //if (event.values[0]==1) {
                        //a_l_steps = updateLongArray(a_l_steps, System.currentTimeMillis());
                    //}
                    break;
                case Sensor.TYPE_PRESSURE :
                    pressure = event.values[0];
                    data.updatePressure(pressure);
                    break;
                case Sensor.TYPE_ACCELEROMETER :
                    data.setGravityArray(event.values.clone());
                    gravityValues = event.values.clone();
                    bAcc = true;
                    iAccMag ++;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD :
                    data.setMagneticArray(event.values.clone());
                    geomagneticValues = event.values.clone();
                    bMag = true;
                    iAccMag ++;
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                    SensorManager.getOrientation(rotationMatrix, rotationOrientation);
                    cnt += 1;
                    if (cnt > 5) {
                        Log.d(TAG, Math.toDegrees(rotationOrientation[0]) + " : " + Math.toDegrees(rotationOrientation[1]) + " : " + Math.toDegrees(rotationOrientation[2]) + " : " +
                                event.accuracy);
                        cnt = 0;
                    }
                    //angle = Filters.doubleULPFilter(angle, rotationOrientation[1]);
                    //data.updateAngleRad(-angle);
                    //data.updateAngle(angleDeg);
                    break;
                default :
                    break;
            }

            if (bAcc && bMag) {
                if (SensorManager.getRotationMatrix(R, I, data.getGravityArray(), data.getMagneticArray())) {

                    boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues);

                    if (success) {
                        float[] orient = new float[3];
                        SensorManager.getOrientation(rotationMatrix, orient);

                        angleF = Filters.floatUltraLPFilter(angleF, orient[1]);
                        data.updateAngleRad(-angleF);

                        //float pitch = (float) Math.toDegrees(orient[1]);

                        //Log.d(TAG, "Pitch (Top to Bottom): " + pitch + "°");

                    }

                    // get bearing to target
                    SensorManager.getOrientation(R, orientation);

                    //float[] angleDelta = new float[3];
                    //SensorManager.getAngleChange(angleDelta, R, prevR);

                    //angleRad = Filters.floatUltraLPFilter(angleRad, orientation[1]);
                    //Log.d(TAG, String.valueOf(-Math.toDegrees(orientation[1])));
                    //data.updateAngle(-Math.toDegrees(orientation[1]));



                    //float incl = SensorManager.getInclination(I);
                    //Log.d(TAG, "Angle Delta: " + Math.toDegrees(angleRad));
                    // east degrees of true North
                    data.setBearing(Bearing.determineDirection(Math.toDegrees(orientation[0]), StaticVariables.geomagneticField));
                    iAccMag = 0;
                    bAcc = false;
                    bMag = false;
                    //prevR = R;
                }
            }
        }
    }
}
