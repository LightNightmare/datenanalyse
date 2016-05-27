package sdsslogviewer.Util;

/**
 * A simple class to do time-related utilites.<p/>
 * @author JZhang
 */
public class timeUtil {

    /**
     * get running time between t1 and t2 in an int array
     * @return int array, first bit is the minute, and second bit is the seconds.
     */
    public static int[] getRunningTime(long t1, long t2){
        int[] runningtime = new int[]{0,0};

        long time = Math.abs(t1 - t2);

        runningtime[0] = (int) (time/(60 * 1000000));
        runningtime[1] = (int) ((time % (60 * 1000000))/1000000);

        return runningtime;
    }

}
