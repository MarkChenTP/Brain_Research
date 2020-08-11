/**
 *  TimeManager.java
 *  Created by: Mark Chen
 *  Last Modified: 08/14/2020
 */

package ucla.erlab.brainresearch;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeManager {

    private Context mContext;
    private String startDate;
    private String startTime;


    public TimeManager(Context context) {
        mContext = context;
        startDate = getDateString();
        startTime = getTimeString();
    }

    public String getStartDate(){
        return this.startDate;
    }
    public String getStartTime(){
        return this.startTime;
    }


    /**
     * Return a string containing today's date in format (MM-dd-yyyy)
     */
    public static String getDateString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        return formatter.format(time);
    }

    /**
     * Return a string containing a timestamp in format (HH:mm:ss)
     */
    public static String getTimeString(){
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return formatter.format(time);
    }

    /**
     * Return a string containing a timestamp in format (HH:mm:ss.SSS)
     */
    public static String getFineTimeString() {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        return formatter.format(time);
    }
}
