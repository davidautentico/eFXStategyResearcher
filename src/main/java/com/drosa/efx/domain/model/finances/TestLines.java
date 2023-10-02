package com.drosa.efx.domain.model.finances;

import com.drosa.efx.domain.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TestLines {

    public static ArrayList<Quote> calculateCalendarAdjusted(ArrayList<Quote> data) {

        ArrayList<Quote> transformed = new ArrayList<Quote>();

        if (data == null) return null;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < data.size(); i++) {
            Quote q = data.get(i);
            Quote qNew = new Quote();
            qNew.copy(q);
            Date d = data.get(i).getDate();

            cal.setTimeInMillis(d.getTime());
            int offset = DateUtils.calculatePepperGMTOffset(cal);

            //if (cal.get(Calendar.YEAR)==2009)
            //System.out.println(DateUtils.datePrint(d)+" "+offset);

            cal.add(Calendar.HOUR_OF_DAY, offset);
            qNew.getDate().setTime(cal.getTimeInMillis());

            //f (cal.get(Calendar.YEAR)==2009)
            //System.out.println(PrintUtils.Print(qNew));

            transformed.add(qNew);
        }

        return transformed;
    }

    public static ArrayList<QuoteShort> calculateCalendarAdjustedS(ArrayList<QuoteShort> data) {

        ArrayList<QuoteShort> transformed = new ArrayList<QuoteShort>();

        if (data == null) return null;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < data.size(); i++) {
            QuoteShort q = data.get(i);
            QuoteShort qNew = new QuoteShort();
            qNew.copy(q);
            QuoteShort.getCalendar(cal, q);
            int offset = DateUtils.calculatePepperGMTOffset(cal);
            //System.out.println("cal antes: "+DateUtils.datePrint(cal));
            cal.add(Calendar.HOUR_OF_DAY, offset);
            //System.out.println("cal despues: "+DateUtils.datePrint(cal)+' '+offset);
            qNew.setCal(cal);

            transformed.add(qNew);
        }

        return transformed;
    }

    public static void calculateCalendarAdjustedSinside(ArrayList<QuoteShort> data) {

        //ArrayList<QuoteShort> transformed = new ArrayList<QuoteShort>();

        if (data == null) return;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < data.size(); i++) {
            QuoteShort q = data.get(i);
            //QuoteShort qNew = new QuoteShort();
            //qNew.copy(q);
            QuoteShort.getCalendar(cal, q);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int offset = DateUtils.calculatePepperGMTOffset(cal);

	           /* if (offset>0
	            		&& cal.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY

	            		) {
	            	offset += 48;//para pasar el fin de semana
	            }*/
            //System.out.println("cal antes: "+DateUtils.datePrint(cal));
            cal.add(Calendar.HOUR_OF_DAY, offset);
            //System.out.println("cal despues: "+DateUtils.datePrint(cal)+' '+offset);
            q.setCal(cal);

            //transformed.add(qNew);
        }
    }


    public static double getAverageRange(ArrayList<Quote> dailyData, int bar) {
        int i;
        int longADRPeriod = 100;
        int shortADRPeriod = 3;
        double localHigh = 0;
        double localLow = 0;
        double highMALong = 0;
        double lowMALong = 0;
        double highMAShort = 0;
        double lowMAShort = 0;
        i = 0;
        Calendar cal = Calendar.getInstance();
        int total = 0;
        //System.out.println("[getAverageRange] bar "+bar);
        while (total < longADRPeriod) {
            if ((bar - i) >= 0) {
                Quote q = dailyData.get(bar - i);
                cal.setTime(q.getDate());
                if (cal.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY
                        && cal.get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY) {
                    localLow = dailyData.get(bar - i).getLow();
                    localHigh = dailyData.get(bar - i).getHigh();
                    lowMALong += localLow;
                    highMALong += localHigh;
                    if (i < shortADRPeriod) {
                        lowMAShort += localLow;
                        highMAShort += localHigh;
                    }
                    total++;
	                   /* System.out.println("lowMAShort highMAShort localLow localHigh:"
	                            +PrintUtils.Print(lowMAShort)
	                            +" "+PrintUtils.Print(highMAShort)
	                            +" "+PrintUtils.Print(localLow)
	                            +" "+PrintUtils.Print(localHigh)
	                            );
	                      */
                }
            } else {
                break;
            }
            i++;
        }
        lowMALong /= longADRPeriod;
        highMALong /= longADRPeriod;
        lowMAShort /= shortADRPeriod;
        highMAShort /= shortADRPeriod;

        if ((highMALong - lowMALong) <= (highMAShort - lowMAShort)) {
            return (highMALong - lowMALong);
        } else {
            return (highMAShort - lowMAShort);
        }
    }


}
