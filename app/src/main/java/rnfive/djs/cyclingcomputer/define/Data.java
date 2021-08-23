package rnfive.djs.cyclingcomputer.define;

import android.location.Location;

import lombok.Getter;
import lombok.Setter;

import static rnfive.djs.cyclingcomputer.define.StaticVariables.dGradeOffset;
import static rnfive.djs.cyclingcomputer.define.StaticVariables.speedMin;

@Getter
@Setter
public class Data {

    private static final String TAG = Data.class.getSimpleName();
    // Location
    private Altitude altitude = new Altitude();
    private double latitude;
    private double longitude;
    private Location locationPrev;
    private int gpsAccuracy;
    private float speedGpsPrev;
    private float speedGps;
    private double distanceGps;
    private double distanceGpsLap;
    private double distanceTot;
    private double distanceLap;
    private double distancePrev;
    private double angle;
    private Double[] angleArray = {0.0d,0.0d,0.0d};

    private float grade;
    private Float[][] gradeArray = new Float[2][20];

    // Time
    private long msLast;
    private int msElapsed;
    private int msElapsedLap;
    private int msTot;
    private int msLap;
    private int msTotM;
    private int msLapM;

    // Sensors
    private boolean antSpeedUsed;
    private float[] gravityArray = new float[3];
    private float[] magneticArray = new float[3];
    private double bearing;

    // Cadence
    private int cadence;
    private Integer[] cadArray = {0,0};
    private int cadBC;
    private Integer[] cadBCArray = {0,0,0};
    private int cadBP;
    private int cadenceMax;
    private int cadenceMaxLap;
    private int cadenceAvg;
    private Integer[] cadenceAvgArray = {0,0};
    private int cadenceAvgLap;
    private Integer[] cadenceAvgLapArray = {0,0};
    private Double[] rotTArray = {0.0d,0.0d,0.0d};
    private Double[] cadTArray = {0.0d,0.0d,0.0d};

    // Speed
    private float speed;
    private float speedMax;
    private float speedMaxLap;
    private float speedAvg;
    private float speedAvgLap;
    private float speedSenPrev;
    private float speedSen;
    private double distanceSen;
    private double distanceSenLap;

    // Power
    private int power;
    private Integer[] power3sArray = new Integer[3];
    private Integer[] power10sArray = new Integer[10];
    private Integer[] power30sArray = new Integer[30];
    private int powerMax;
    private int powerMaxLap;
    private int powerAvg;
    private int powerNZ;
    private Integer[] powerAvgArray = {0,0,0};
    private int powerAvgLap;
    private int powerNZLap;
    private Integer[] powerAvgLapArray = {0,0,0};
    private int smoothL;
    private int smoothR;
    private int torqueL;
    private int torqueR;
    private int balanceR;

    // Heart Rate
    private int iRRInterval;
    private int hr;
    private int hrMax;
    private int hrMaxLap;
    private int hrAvg;
    private int[] hrAvgArray = {0,0};
    private int hrAvgLap;
    private int[] hrAvgLapArray = {0,0};


    private int iGear;

    public Data() {}

    void updateAngle(double in) {
        in -= dGradeOffset;
        Arrays.updateArray(angleArray, in);
        angle = Arrays.getAvg(angleArray);
    }

    public void setAltitudeValue(double altitude) {
        this.altitude.setAltitude(altitude);
    }
    public double getAltitudeValue() {
        return altitude.getAltitude();
    }
    public double getAscentTot() {
        return altitude.getAscent();
    }
    public double getAscentLap() {
        return altitude.getAscentLap();
    }
    public double getDescentTot() {
        return altitude.getDescent();
    }
    public double getDescentLap() {
        return altitude.getDescentLap();
    }

    public void updatePower() {
        if (StaticVariables.bBPExists) {
            Arrays.updateArray(power3sArray, power);
            Arrays.updateArray(power10sArray, power);
            Arrays.updateArray(power30sArray, power);
            if (StaticVariables.bMoving && !StaticVariables.bPaused) {
                powerAvgArray[0] += power;
                powerAvgArray[2] ++;
                if (power > 0)
                    powerAvgArray[1] ++;
                if (powerAvgArray[1]>0)
                    powerNZ = powerAvgArray[0]/ powerAvgArray[1];
                if (powerAvgArray[2] > 0)
                    powerAvg = powerAvgArray[0]/powerAvgArray[2];

                powerAvgLapArray[0] += power;
                powerAvgLapArray[2] ++;
                if (power > 0)
                    powerAvgLapArray[1] ++;
                if (powerAvgLapArray[1]>0)
                    powerNZLap = powerAvgLapArray[0]/ powerAvgLapArray[1];
                if (powerAvgArray[2] > 0)
                    powerAvgLap = powerAvgLapArray[0]/powerAvgLapArray[2];
            }
        }
        powerMax = Math.max(power, powerMax);
        powerMaxLap = Math.max(power, powerMaxLap);
    }

    public void resetPower() {
        power = -1;
        cadBP = -1;
        balanceR = -1;
        smoothL = -1;
        smoothR = -1;
        torqueL = -1;
        torqueR = -1;
    }

    public void updateGPSDistance(double p2p) {
        if (StaticVariables.bStarted && !StaticVariables.bPaused) {
            distanceGpsLap += p2p;
            distanceGps += p2p;
        }
    }

    public void updateSenDistance(double p2p) {
        if (StaticVariables.bStarted && !StaticVariables.bPaused) {
            distanceSenLap += p2p;
            distanceSen += p2p;
        }
    }

    public void updateDistance() {
        boolean bUseAntDistance = (StaticVariables.bAntDistance && antSpeedUsed);

        distanceTot = (bUseAntDistance ? distanceSen : distanceGps);
        distanceLap = (bUseAntDistance ? distanceSenLap : distanceGpsLap);
    }

    public void updateSpeed() {
        antSpeedUsed = StaticVariables.bAntSpeed && StaticVariables.bBSExists;
        if (antSpeedUsed) {
            float speedRatio = 0.0f;
            if (speedSen > 0.0f)
                speedRatio = (speedGps - speedSen)/speedSen;
            else if (speedGps > 0.0f)
                speedRatio = (speedGps - speedSen)/speedGps;

            if (Math.abs(speedRatio) > 0.25f) {
                antSpeedUsed = false;
                // TODO - Notify user that GPS speed is being used
            }
        }

        float speedTmp = (antSpeedUsed ? speedSen : speedGps);
        speed = (speedTmp >= speedMin ? speedTmp : 0.0f);

        if (msTotM > 0)
            speedAvg = (float) distanceTot/(msTotM/1000.0f);
        if (msLapM > 0)
            speedAvgLap = (float) distanceLap/(msLapM/1000.0f);

        speedMax = Math.max(speed, speedMax);
        speedMaxLap = Math.max(speed, speedMaxLap);

        StaticVariables.bMoving = speed > 0.0f;
    }

    public void updateHeartRate() {
        if (StaticVariables.bHRExists) {
            if (StaticVariables.bStarted && !StaticVariables.bPaused) {
                hrAvgArray[0] += hr;
                hrAvgArray[1] ++;
                hrAvgLapArray[0] += hr;
                hrAvgLapArray[1] ++;
                hrAvg = hrAvgArray[0]/ hrAvgArray[1];
                hrAvgLap = hrAvgLapArray[0]/ hrAvgLapArray[1];

                hrMax = Math.max(hr, hrMax);
                hrMaxLap = Math.max(hr, hrMaxLap);
            }
        }
    }

    public void updateCadence() {
        if (StaticVariables.bBCExists || StaticVariables.bBPCadExists) {
            if (StaticVariables.bBCExists && !(cadBC == 0 && cadBP > 10))
                Arrays.updateArray(cadBCArray, cadBC);
            else
                Arrays.updateArray(cadBCArray, cadBP);
            cadence = (int) Math.round(Arrays.getAvg(cadBCArray));
            if (StaticVariables.bStarted && !StaticVariables.bPaused && cadence > 0 && StaticVariables.bMoving) {
                cadenceAvgArray[0] += cadence;
                cadenceAvgArray[1] ++;
                cadenceAvg = cadenceAvgArray[0]/cadenceAvgArray[1];
                cadenceAvgLapArray[0] += cadence;
                cadenceAvgLapArray[1] ++;
                cadenceAvgLap = cadenceAvgLapArray[0]/cadenceAvgLapArray[1];

                cadenceMax = Math.max(cadence, cadenceMax);
                cadenceMaxLap = Math.max(cadence, cadenceMaxLap);
            }
        }
    }

    void updatePressure(double p) {
        altitude.updatePressure(p);
    }
    public void updateAltitude() {
        altitude.updateAltitude();
    }

    public void addMS(long ms) {
        if (StaticVariables.bStarted) {
            msElapsed += ms;
            msElapsedLap += ms;
            if (!StaticVariables.bPaused) {
                msTot += ms;
                msLap += ms;
                if (StaticVariables.bMoving) {
                    msTotM += ms;
                    msLapM += ms;
                }
            }
        }
    }

    public void resetLapVariables() {
        msElapsedLap = 0;
        distanceLap = 0;
        msLap = 0;
        msLapM = 0;
        altitude.setAscentLap(0.0d);
        altitude.setDescentLap(0.0d);
        speedMaxLap = 0;
        speedAvgLap = 0;
        hrMaxLap = 0;
        hrAvgLap = -1;
        hrAvgLapArray = new int[] {0,0};
        cadenceMaxLap = 0;
        cadenceAvgLap = -1;
        cadenceAvgLapArray = new Integer[] {0,0};
        powerMaxLap = 0;
        powerAvgLap = -1;
        powerNZLap = -1;
        powerAvgLapArray = new Integer[] {0,0,0};
        distanceSenLap = 0;
        distanceGpsLap = 0;
    }
}
