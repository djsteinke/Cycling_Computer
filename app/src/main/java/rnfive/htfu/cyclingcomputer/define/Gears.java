package rnfive.htfu.cyclingcomputer.define;

import static rnfive.htfu.cyclingcomputer.service.Service_Recording.data;

public class Gears {
    private Gears() {}

    private static int[] cassette = new int[] {11,12,13,14,15,17,19,21,23,25,28};
    private static int[] chainring = new int[] {34,50};
    private static int[] iaRatios;
    public static String[] sRatios;

    static {
        iaRatios = new int[cassette.length* chainring.length];
        sRatios = new String[iaRatios.length];
        int r = 0;
        for (int cr: chainring) {
            for (int cs: cassette) {
                iaRatios[r] = (int)((double)cr/(double)cs*10);
                sRatios[r] = cr+"|"+cs;
                r++;
            }
        }
    }

    public static void determineGearing () {
        int nGear = -1;
        boolean bCadIncrease = data.getCadBCArray()[0]>data.getCadBCArray()[2];
        int iRatio = (int)(Arrays.getAvg(data.getCadTArray())/ Arrays.getAvg(data.getRotTArray())*10);
        int iCnt = 0;
        int iCurr = data.getIGear();
        if (bCadIncrease) {
            while (iCnt < iaRatios.length) {
                int d = iaRatios[iCurr];
                if (iRatio == d) {
                    nGear = iCurr;
                    break;
                }
                if (iCurr == 0)
                    iCurr = iaRatios.length - 1;
                else
                    iCurr--;
                iCnt++;
            }
        }
        else {
            while (iCnt < iaRatios.length) {
                int d = iaRatios[iCurr];
                if (iRatio == d) {
                    nGear = iCurr;
                    break;
                }
                if (iCurr == iaRatios.length-1)
                    iCurr = 0;
                else
                    iCurr++;
                iCnt++;
            }
        }
        if (nGear > -1) {
            data.setIGear(nGear);
        }
    }
}
