package com.drosa.efx.domain.utils;


import com.drosa.efx.domain.model.finances.PositionCore;
import com.drosa.efx.domain.model.finances.PositionShort;
import com.drosa.efx.domain.model.finances.PositionStatus;
import com.drosa.efx.domain.model.finances.QuoteShort;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TradingUtils {

    public static ArrayList<QuoteShort> cleanWeekendDataS(ArrayList<QuoteShort> dataS) {
        // TODO Auto-generated method stub
        ArrayList<QuoteShort> data = new ArrayList<QuoteShort>();

        Calendar cal = Calendar.getInstance();
        for (int i=0;i<dataS.size();i++){
            QuoteShort q = dataS.get(i);
            QuoteShort.getCalendar(cal, q);
            int day = cal.get(Calendar.DAY_OF_YEAR);
            if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY
                    || day==1)
                continue;
            QuoteShort qNew = new QuoteShort();
            qNew.copy(q);
            data.add(q);
            //System.out.println(i);
        }
        return data;
    }

    public static ArrayList<Integer> calculateMaxMinByBarShortAbsoluteInt(
            ArrayList<QuoteShort> data) {

        int count5000 = 0;
        ArrayList<Integer> maxMins = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            QuoteShort q = data.get(i);
            int maxMin = 0;
            //maxMin.setExtra(0);
            int modeH = 0;
            int modeL = 0;
            int nbarsH = 0;
            int nbarsL = 0;
            if (i % 20000 == 0) {
                //System.out.println("calculados "+i);
            }
            //El i se toma high y Low
            //para backtest mirar siempre i-1
            for (int j = i - 1; j >= 0; j--) {
                QuoteShort qj = data.get(j);
                boolean isHigh = false;
                boolean isLow = false;

                if (q.getHigh5() > qj.getHigh5()) {
                    isHigh = true;
                }
                if (q.getLow5() < qj.getLow5()) {
                    isLow = true;
                }

                //System.out.println();

                if (modeH == 0 || modeH == 1) {
                    if (isHigh) {
                        modeH = 1;
                        nbarsH++;
                    } else {
                        modeH = -1;
                    }
                }
                if (modeL == 0 || modeL == 1) {
                    if (isLow) {
                        modeL = 1;
                        nbarsL++;
                    } else {
                        modeL = -1;
                    }
                }

                if (!isHigh) modeH = -1;
                if (!isLow) modeL = -1;

                if (!isHigh && !isLow) break;
                if (modeH == -1 && modeL == -1) break;
            }
            if (Math.abs(nbarsH) >= 5000 || Math.abs(nbarsL) >= 5000) {
                count5000++;
                //System.out.println("a�adiendo nbars: "+nbarsH+" "+nbarsL+" "+count5000);
            }
            if (nbarsH >= nbarsL) maxMin = nbarsH;
            if (nbarsH < nbarsL) maxMin = -nbarsL;
            maxMins.add(maxMin);
            //System.out.println("a�adidoMaxmin para "+i+" || "+q.toString()+" || "+maxMin);
        }
        return maxMins;
    }

    public static int getActualDay(int year, int day) {
        // TODO Auto-generated method stub
        return year * 367 + day;
    }

    public static long getOpenSize2(ArrayList<PositionCore> positions) {
        // TODO Auto-generated method stub

        long actualSize = 0;
        for (int i=0;i<positions.size();i++) {
            PositionCore p = positions.get(i);

            if (p.getPositionStatus()==PositionStatus.OPEN) {
                actualSize += p.getMicroLots();
            }
        }
        return actualSize;
    }

    public static String completeNumber5(String num){
        String com=num;

        if (com.length()==5)
            com+="0";
        if (com.length()==4)
            com+="00";
        if (com.length()==3)
            com+="000";
        if (com.length()==2)
            com+="0000";
        if (com.length()==1)
            com+="00000";
        return com;
    }

    public static String completeNumber(String num){
        String com=num;

        if (com.length()==4)
            com+="0";
        if (com.length()==3)
            com+="00";
        if (com.length()==2)
            com+="000";
        if (com.length()==1)
            com+="0000";
        return com;
    }

    public static int getPipsDiff(double val1,double val2){
        String val1Str = PrintUtils.Print3(val1);
        String val2Str = PrintUtils.Print3(val2);
        val1Str = completeNumber(val1Str.substring(0, 1)+val1Str.substring(2, val1Str.length()));
        val2Str = completeNumber(val2Str.substring(0, 1)+val2Str.substring(2, val2Str.length()));

        //System.out.println("[getPipsDiff] "+val1+" "+val2+" "+val1Str+" "+val2Str);
        int diff =Integer.valueOf(val1Str)-Integer.valueOf(val2Str);
        return diff;
    }

    public static int getDayIndexShort(ArrayList<QuoteShort> dataSource, Calendar actualCal, int index){
        int daySearch = actualCal.get(Calendar.DAY_OF_MONTH);
        Calendar sourceCal = Calendar.getInstance();
        for (int i=index;i<dataSource.size();i++){
            QuoteShort q = dataSource.get(i);
            q.getCalendar(sourceCal, q);
            int daySource = sourceCal.get(Calendar.DAY_OF_MONTH);
            if (DateUtils.isSameDay(sourceCal, actualCal)){
                return i;
            }
        }
        return -1;
    }

    public static int getPipsDiff5(double val1,double val2){
        String val1Str = PrintUtils.Print5dec(val1);
        String val2Str = PrintUtils.Print5dec(val2);
        val1Str = completeNumber5(val1Str.substring(0, 1)+val1Str.substring(2, val1Str.length()));
        val2Str = completeNumber5(val2Str.substring(0, 1)+val2Str.substring(2, val2Str.length()));


        int diff =Integer.valueOf(val1Str)-Integer.valueOf(val2Str);
        return diff;
    }

    public static HashMap<Integer, Integer> calculateATR(
            ArrayList<QuoteShort> data, int period) {

        HashMap<Integer, Integer> atrArr = new HashMap<Integer, Integer>();

        Calendar cal = Calendar.getInstance();
        int lastDay = -1;
        int range = 800;
        int high = -1;
        int low = -1;
        int year = -1;
        int day = -1;
        ArrayList<Integer> rangeArr = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            QuoteShort q = data.get(i);
            QuoteShort.getCalendar(cal, q);
            year = cal.get(Calendar.YEAR);
            day = cal.get(Calendar.DAY_OF_YEAR);
            int actualDay = TradingUtils.getActualDay(year, day);
            if (actualDay != lastDay) {
                if (lastDay != -1) {
                    range = high - low;
                    rangeArr.add(range);
                    int atr = (int) MathUtils.average(rangeArr, rangeArr.size() - period, rangeArr.size() - 1);
                    //System.out.println("poniendo dia: "+lastDay);
                    atrArr.put(lastDay, atr);
                }
                high = -1;
                low = -1;
                lastDay = actualDay;
            }

            if (high == -1 || q.getHigh5() > high) high = q.getHigh5();
            if (low == -1 || q.getLow5() < low) low = q.getLow5();
        }
        //ultimo dia
        int actualDay = TradingUtils.getActualDay(year, day);
        range = high - low;
        rangeArr.add(range);
        int atr = (int) MathUtils.average(rangeArr, rangeArr.size() - period, rangeArr.size() - 1);
        atrArr.put(actualDay, atr);

        return atrArr;
    }

    public static long getOpenSize(ArrayList<PositionShort> positions) {
        // TODO Auto-generated method stub

        long actualSize = 0;
        for (int i = 0; i < positions.size(); i++) {
            PositionShort p = positions.get(i);

            if (p.getPositionStatus() == PositionStatus.OPEN) {
                actualSize += p.getMicroLots();
            }
        }
        return actualSize;
    }

    /**
     * Estudiado a nivel de tick con dukascopy
     *
     * @param year
     * @param h
     * @return
     */
    public static int getTransactionCosts(int year, int h, int mode) {
        int spread = 20;

        int commisions = 6;//darwinex

        if (year == 2019) {
            if (h == 0) spread = 16;
            if (h == 1) spread = 6;
            if (h >= 2 && h <= 8) spread = 4;
            if (h >= 9 && h <= 22) spread = 3;
            if (h == 23) spread = 6;
        } else if (year == 2018) {
            if (h == 0) spread = 10;
            if (h == 1) spread = 5;
            if (h >= 2 && h <= 8) spread = 4;
            if (h >= 9 && h <= 22) spread = 4;
            if (h == 23) spread = 11;
        } else if (year == 2017) {
            if (h == 0) spread = 10;
            if (h == 1) spread = 7;
            if (h >= 2 && h <= 8) spread = 3;
            if (h >= 9 && h <= 22) spread = 3;
            if (h == 23) spread = 5;
        } else if (year == 2016) {
            if (h == 0) spread = 12;
            if (h == 1) spread = 7;
            if (h >= 2 && h <= 8) spread = 3;
            if (h >= 9 && h <= 22) spread = 2;
            if (h == 23) spread = 5;
        } else if (year == 2015) {
            if (h == 0) spread = 11;
            if (h == 1) spread = 6;
            if (h >= 2 && h <= 8) spread = 3;
            if (h >= 9 && h <= 22) spread = 3;
            if (h == 23) spread = 4;
        } else if (year == 2014) {
            if (h == 0) spread = 6;
            if (h == 1) spread = 4;
            if (h >= 2 && h <= 8) spread = 3;
            if (h >= 9 && h <= 22) spread = 2;
            if (h == 23) spread = 3;
        } else if (year == 2013) {
            if (h == 0) spread = 14;
            if (h == 1) spread = 8;
            if (h >= 2 && h <= 8) spread = 6;
            if (h >= 9 && h <= 22) spread = 4;
            if (h == 23) spread = 7;
        } else if (year == 2012) {
            if (h == 0) spread = 15;
            if (h == 1) spread = 11;
            if (h >= 2 && h <= 8) spread = 9;
            if (h >= 9 && h <= 22) spread = 8;
            if (h == 23) spread = 10;
        } else if (year == 2011) {
            if (h == 0) spread = 17;
            if (h == 1) spread = 14;
            if (h >= 2 && h <= 8) spread = 11;
            if (h >= 9 && h <= 22) spread = 9;
            if (h == 23) spread = 11;
        } else if (year == 2010) {
            if (h == 0) spread = 12;
            if (h == 1) spread = 12;
            if (h >= 2 && h <= 5) spread = 11;
            if (h >= 6 && h <= 9) spread = 10;
            if (h >= 10 && h <= 19) spread = 9;
            if (h >= 20 && h <= 22) spread = 10;
            if (h == 23) spread = 11;
        } else if (year == 2009) {
            if (h == 0) spread = 17;
            if (h == 1) spread = 16;
            if (h >= 2 && h <= 8) spread = 13;
            if (h >= 9 && h <= 22) spread = 10;
            if (h == 23) spread = 11;
        }

        if (mode == 2) return spread;
        if (mode == 3) return commisions;

        return spread + commisions;
    }

    public static int getTransactionCosts(
            HashMap<Integer, ArrayList<Double>> spreads,
            int year, int h, int mode
    ) {
        int spread = 20;

        int commisions = 7;//axi

        if (spreads != null)
            if (spreads != null && spreads.containsKey(year)) {
                spread = (int) Math.ceil(spreads.get(year).get(h) * 10.0);
            }

        if (mode == 2) return spread;
        if (mode == 3) return commisions;

        return spread + commisions;
    }

    public static int getTransactionCostsMinutes(
            HashMap<Integer, ArrayList<Double>> spreads,
            int year, int hmin, int mode
    ) {
        int spread = 20;

        int commisions = 6;//darwinex

        if (spreads.containsKey(year)) {
            spread = (int) Math.ceil(spreads.get(year).get(hmin) * 10.0);
        }

        if (mode == 2) return spread;
        if (mode == 3) return commisions;

        return spread + commisions;
    }
}
