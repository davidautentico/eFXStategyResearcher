package com.drosa.efx.domain.model.finances;

import com.drosa.efx.domain.utils.DateUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class DataCleaning {

    public static void writeFileShort(ArrayList<QuoteShort> data,String fileName){
        try{
            //String fileName = path+"\\"+symbol+"_"+DateUtils.getAlways2digits(month+1)+"_"+year+suffix;
            PrintWriter writer;
            writer = new PrintWriter(fileName, "UTF-8");

            Calendar cal = Calendar.getInstance();
            for (int i=0;i<data.size();i++){
                QuoteShort q = data.get(i);
                QuoteShort.getCalendar(cal, q);
                //if (cal.get(Calendar.MONTH)==month
                //&& cal.get(Calendar.YEAR)==year){
                //String dateStr = DateUtils.getDukasFormat(cal);
                //String OHLC = PrintUtils.getOHLC(q);
                writer.println(q.toString());
                //}
            }
            writer.close();
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeFileShort(String symbol, ArrayList<QuoteShort> data, int month, int year, String path, String suffix) {
        try {
            String fileName = path + "\\" + symbol + "_" + DateUtils.getAlways2digits(month + 1) + "_" + year + suffix;
            PrintWriter writer;
            writer = new PrintWriter(fileName, "UTF-8");

            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < data.size(); i++) {
                QuoteShort q = data.get(i);
                QuoteShort.getCalendar(cal, q);
                //if (cal.get(Calendar.MONTH)==month
                //&& cal.get(Calendar.YEAR)==year){
                //String dateStr = DateUtils.getDukasFormat(cal);
                //String OHLC = PrintUtils.getOHLC(q);
                writer.println(q.toString());
                //}
            }
            writer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
