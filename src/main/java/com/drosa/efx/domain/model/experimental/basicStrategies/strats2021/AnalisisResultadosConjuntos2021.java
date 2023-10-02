package com.drosa.efx.domain.model.experimental.basicStrategies.strats2021;

import com.drosa.efx.domain.model.experimental.basicStrategies.base.Bollinger;
import com.drosa.efx.domain.model.experimental.basicStrategies.base.BollingerStrat;
import com.drosa.efx.domain.model.experimental.basicStrategies.base.StratPerformance;
import com.drosa.efx.domain.model.finances.*;
import com.drosa.efx.domain.utils.DateUtils;
import com.drosa.efx.domain.utils.MathUtils;
import com.drosa.efx.domain.utils.PrintUtils;
import com.drosa.efx.domain.utils.TradingUtils;
import com.drosa.efx.infrastructure.DAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class AnalisisResultadosConjuntos2021 {


    public static StratPerformance getBollingueResults(
            String path0,
            String curr,
            double initialBalance,
            int nbars, double dt, double sl, double risk,
            int year1, int year2,
            int h1, int h2, int m1, int m2,
            String periodBidStr,
            String periodAskStr,
            String outFileName,
            int minAtr,
            int printOptions
    ) throws IOException {
        //System.out.println("[getBollingueResults] out: "+outFileName);
        ArrayList<String> currList = new ArrayList<String>();
        currList.add(curr);
        Calendar calFrom = Calendar.getInstance();
        Calendar calTo = Calendar.getInstance();
        calFrom.set(year1, m1, 0, 0, 0);
        calTo.set(year2, m2, 31, 23, 59);


        int maxPositions = 80;
        int delay = 00;
        int thr = -1;
        int ah1 = 0;
        int ah2 = -1;
        int minDiff = 0;
        int maxSpread = 99999;

        ArrayList<QuoteShort> dataI = null;
        ArrayList<QuoteShort> dataS = null;
        //obtenci�n de datos
        HashMap<String, ArrayList<QuoteShort>> dataAskHash = new HashMap<String, ArrayList<QuoteShort>>();
        HashMap<String, ArrayList<QuoteShort>> dataBidHash = new HashMap<String, ArrayList<QuoteShort>>();
        HashMap<String, ArrayList<Integer>> maxMinsHash = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, HashMap<Integer, Integer>> atrHashHash = new HashMap<String, HashMap<Integer, Integer>>();

        ArrayList<Tick> ticks = new ArrayList<Tick>();
        int minSize = -1;
        String pathBid = path0 + curr + periodBidStr;
        dataI = new ArrayList<QuoteShort>();
        dataI = DAO.retrieveDataShort5m(pathBid, DataProvider.DUKASCOPY_FOREX4);
        TestLines.calculateCalendarAdjustedSinside(dataI);
        dataS = TradingUtils.cleanWeekendDataS(dataI);
        ArrayList<QuoteShort> dataBid = dataS;

        String pathAsk = path0 + curr + periodAskStr;
        dataI = new ArrayList<QuoteShort>();
        dataI = DAO.retrieveDataShort5m(pathAsk, DataProvider.DUKASCOPY_FOREX4);
        TestLines.calculateCalendarAdjustedSinside(dataI);
        dataS = TradingUtils.cleanWeekendDataS(dataI);
        ArrayList<QuoteShort> dataAsk = dataS;

        //System.out.println("curr= "+curr+" dataBid/Ask: "+pathBid+" "+pathAsk+" "+dataBid.size()+" "+dataAsk.size());
        if (minSize == -1 || dataBid.size() < minSize) minSize = dataBid.size();
        if (minSize == -1 || dataAsk.size() < minSize) minSize = dataAsk.size();

        ArrayList<Integer> maxMins = TradingUtils.calculateMaxMinByBarShortAbsoluteInt(dataBid);
        HashMap<Integer, Integer> atrHash = TradingUtils.calculateATR(dataBid, 20);

        dataAskHash.put(curr, dataAsk);
        dataBidHash.put(curr, dataBid);
        maxMinsHash.put(curr, maxMins);
        atrHashHash.put(curr, atrHash);

        HashMap<Integer, ArrayList<BollingerStrat>> strats = new HashMap<Integer, ArrayList<BollingerStrat>>();
        for (int w = 0; w <= 23; w++) {
            BollingerStrat bs1 = new BollingerStrat(curr, 0, 39, 0.12, 0.8, 0.3, false);
            ArrayList<BollingerStrat> list1 = new ArrayList<BollingerStrat>();
            list1.add(bs1);
            strats.put(w, list1);
        }

        //seteamos las estrategias
        for (int w = h1; w <= h2; w++) {
            int ah = w;
            if (w > 23) ah -= 24;
            BollingerStrat bs = strats.get(ah).get(0);
            bs.setNbars(nbars);
            bs.setDt(dt);
            bs.setRisk(risk);
            bs.setSl(sl);
            bs.setEnabled(true);
        }


        Bollinger mm = new Bollinger();
        StratPerformance sp = new StratPerformance();
        sp.reset();
        sp.setInitialBalance(initialBalance);
        sp.setActualBalance(initialBalance);

        String strParam =
                nbars
                        + " " + dt
                        + " " + sl
                        + " " + maxPositions
                        + " " + delay
                        + " " + thr
                        + " " + ah1
                        + " " + ah2
                        + " " + risk
                        + " " + minDiff
                        + " " + minAtr
                        + " " + maxSpread;
        //System.out.println(strParam);

        mm.setParameters(nbars, dt, 0, sl, maxPositions, delay, thr, ah1, ah2, false, risk, minDiff, minAtr, maxSpread);
        mm.setStrats(strats);
        mm.setPrintDayProfit(outFileName, true);
        mm.setPrintMonthProfit(outFileName + ".month.csv", true);

        mm.doTest(h1 + " " + h2 + " " + nbars + " " + (int) (dt * 100) + " " + (int) (sl * 10) + " " + (int) (100 * risk),
                currList, dataBidHash, dataAskHash, maxMinsHash, atrHashHash, minSize, calFrom, calTo, sp,
                1, 0, printOptions);

        return sp;

    }

    private static int doCalculateResultadoAgregado(String header,
                                                    double initialBalance,
                                                    HashMap<Integer, Integer> atrHash,
                                                    HashMap<Long, Boolean> excludedDays,
                                                    ArrayList<String> files,
                                                    int offset,
                                                    int offset2,
                                                    boolean isAlways,
                                                    int minAtr,
                                                    StratPerformance sp
    ) throws IOException {

        boolean printDayInfo = false;
        int isEndInt = 0;
        int maxSize = 0;

        HashMap<String, ArrayList> dayResults = new HashMap<String, ArrayList>();

        HashMap<Integer, Double> dayMonthPerformace = new HashMap<Integer, Double>();

        HashMap<Integer, Double> dayLeverage = new HashMap<Integer, Double>();

        ArrayList<HashMap<Long, String>> valuesArray = new ArrayList<HashMap<Long, String>>();
        ArrayList<HashMap<Long, Double>> leveArray = new ArrayList<HashMap<Long, Double>>();
        Calendar cali = Calendar.getInstance();
        Calendar cald = Calendar.getInstance();
        ArrayList<Long> keys = new ArrayList<Long>();
        HashMap<Long, String> keyHash = new HashMap<Long, String>();
        for (int i = 0; i < files.size(); i++) {
            String fileName = files.get(i);

            List<String> lines = Files.readAllLines(Paths.get(fileName));
            HashMap<Long, String> currHash = new HashMap<Long, String>();
            HashMap<Long, Double> leveHash = new HashMap<Long, Double>();
            for (int j = 0; j < lines.size(); j++) {

                try {
                    String dateTimeStr = lines.get(j).split(";")[0].trim();
                    String timeStr = dateTimeStr.split(" ")[1].trim();
                    String dateStr = dateTimeStr.split(" ")[0].trim();
                    int year = 2000;
                    int day = 0;
                    int month = 0;
                    int hh = 0;
                    int mm = 0;
                    int ss = 0;

                    year = Short.valueOf(dateStr.split("-")[2].trim());
                    day = Byte.valueOf(dateStr.split("-")[0].trim());
                    month = Byte.valueOf(dateStr.split("-")[1].trim());
                    hh = Byte.valueOf(timeStr.substring(0, 2));
                    mm = Byte.valueOf(timeStr.substring(3, 5));
                    ss = Byte.valueOf(timeStr.substring(6, 8));
                    cali.set(year, month - 1, day, hh, mm, ss);
                    cald.set(year, month - 1, day, 0, 0, 0);
                    cald.set(Calendar.MILLISECOND, 0);

                    //System.out.println(DateUtils.datePrint(cald)+" "+cald.getTimeInMillis()+" "+excludedDays.size());
                    if (excludedDays.containsKey(cald.getTimeInMillis())) {
                        //System.out.println("excluido...");
                        continue;
                    }

                    //si est� en la lista de excluidos no se tiene en cuenta

                    if (!keyHash.containsKey(cali.getTimeInMillis())) {
                        keys.add(cali.getTimeInMillis());
                        keyHash.put(cali.getTimeInMillis(), "");
                    }
                    double bal = Float.valueOf(lines.get(j).split(";")[6].trim());
                    int microLots = Integer.valueOf(lines.get(j).split(";")[3].trim());
                    double leve = microLots * 1000 / bal;
                    currHash.put(cali.getTimeInMillis(), lines.get(j).split(";")[1].trim());
                    leveHash.put(cali.getTimeInMillis(), leve);
                } catch (Exception e) {
                    System.out.println("ERROR: " + fileName + " " + lines.get(j) + " " + e.getMessage());
                }
            }
            valuesArray.add(currHash);
            leveArray.add(leveHash);
        }

        int totalYears = 0;
        int totalYearsPositive = 0;
        ArrayList<Double> monthChange = new ArrayList<Double>();
        ArrayList<Double> maxMonthDDArr = new ArrayList<Double>();
        double actualBalance = initialBalance;
        double maxBalance = actualBalance;
        double maxDD = 0;
        double maxDD2 = 0;
        ArrayList<Double> dayChangeList = new ArrayList<Double>();
        ArrayList<Integer> recoverList = new ArrayList<Integer>();
        Calendar cal = Calendar.getInstance();

        //extraigo las keys del primero
        int lastYear = -1;
        int lastMonth = -1;
        int totalm = 0;
        double var95 = 10.0;
        double var95_2 = 2.0;
        int winm = 0;
        double maxMonthDD = 0.0;
        double maxMonthPeak = actualBalance;
        int maxlossm95 = 0;
        int maxlossm95_2 = 0;
        int lossm95 = 0;
        int lossm95_2 = 0;
        double lastBalanceMonth = initialBalance;
        int lastDDDay = -1;
        int actualDay = -1;
        int totalDays975 = 0;
        int totalDays = 0;
        int month = -2;
        int maxTimePeak = 0;
        double maxDayBalance = initialBalance;
        double maxDayDD = 0;

        //ultima fecha
        Calendar calEnd3m = Calendar.getInstance();
        Calendar calBegin3m = Calendar.getInstance();
        Calendar calEnd6m = Calendar.getInstance();
        Calendar calBegin6m = Calendar.getInstance();
        Calendar calEnd9m = Calendar.getInstance();
        Calendar calBegin9m = Calendar.getInstance();
        Collections.sort(keys);
        long key = keys.get(keys.size() - 1);
        calEnd3m.setTimeInMillis(key);
        calEnd6m.setTimeInMillis(key);
        calEnd9m.setTimeInMillis(key);
        calBegin3m.setTimeInMillis(key);
        calBegin3m.add(Calendar.MONTH, -3);
        calBegin6m.setTimeInMillis(key);
        calBegin6m.add(Calendar.MONTH, -6);
        calBegin9m.setTimeInMillis(key);
        calBegin9m.add(Calendar.MONTH, -9);

        //System.out.println(DateUtils.datePrint(calEnd3m));

        //delimitacion �ltimos 3 mesaes
        double initialBalance3m = 0;
        double finalBalance3m = 0;
        boolean isInit = false;
        //delimitacion �ltimos 6 meses
        double initialBalance6m = 0;
        double finalBalance6m = 0;
        boolean isInit6 = false;
        //delimitacion �ltimos 9 mesaes
        double initialBalance9m = 0;
        double finalBalance9m = 0;
        boolean isInit9 = false;

        //
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        Collections.sort(keys);
        key = keys.get(0);
        calStart.setTimeInMillis(key);
        calStart.add(Calendar.MONTH, offset);
        calEnd.setTimeInMillis(calStart.getTimeInMillis());
        if (offset2 >= 0) {
            calEnd.add(Calendar.MONTH, offset2);
        } else {
            calEnd.setTimeInMillis(calEnd3m.getTimeInMillis());
        }

        if (calEnd.getTimeInMillis() > calEnd3m.getTimeInMillis()) {
            isEndInt = 1;
        }
        int lastDay = -1;
        int totalPossibleDays = 0;
        double dayBalance = initialBalance;
        double maxDayChange = 0;
        double maxDayChangeDD = 0;
        int lastLevDay = -2;
        double maxDayLeverage = 0;
        boolean maxBalanceUpdated = false;
        long lastKey = 0;
        for (int i = 0; i < keys.size(); i++) {//para cada unidad de tiempo..
            key = keys.get(i);
            double dayChange = 0;
            double totalLeve = 0;
            cal.setTimeInMillis(key);
            cali.setTimeInMillis(key);
            cali.add(Calendar.DAY_OF_MONTH, -1);
            if (i > 0) {
                cali.setTimeInMillis(keys.get(i - 1));
            }
            int dayMonth = cal.get(Calendar.DAY_OF_MONTH);
            int day = cal.get(Calendar.DAY_OF_YEAR);
            int year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            actualDay = TradingUtils.getActualDay(year, day);
            int atr = 800;
            if (atrHash != null) {
                //System.out.println(year +" "+day+" "+actualDay+" || "+DateUtils.datePrint(cal));
                atr = atrHash.get(actualDay);
            }

            if (atr < minAtr) continue;

            //System.out.println("llegado "+DateUtils.datePrint(cal)+" "+DateUtils.datePrint(calStart)+" "+DateUtils.datePrint(calEnd));
            if (cal.getTimeInMillis() < calStart.getTimeInMillis()) continue;
            if (cal.getTimeInMillis() > calEnd.getTimeInMillis()) break;// continue;
            if (lastDDDay == -1) lastDDDay = actualDay;

            if (cal.getTimeInMillis() >= calBegin3m.getTimeInMillis()) {
                if (!isInit) {
                    isInit = true;
                    initialBalance3m = actualBalance;
                    //System.out.println("balance 3 mese "+DateUtils.datePrint(cal)+" "+PrintUtils.Print2dec(initialBalance3m,false));
                }
                if (cal.getTimeInMillis() <= calEnd3m.getTimeInMillis()) {
                    finalBalance3m = actualBalance;
                }
            }

            if (cal.getTimeInMillis() >= calBegin6m.getTimeInMillis()) {
                if (!isInit6) {
                    isInit6 = true;
                    initialBalance6m = actualBalance;
                }
                if (cal.getTimeInMillis() <= calEnd6m.getTimeInMillis()) {
                    finalBalance6m = actualBalance;
                }
            }

            if (cal.getTimeInMillis() >= calBegin9m.getTimeInMillis()) {
                if (!isInit9) {
                    isInit9 = true;
                    initialBalance9m = actualBalance;
                }
                if (cal.getTimeInMillis() <= calEnd9m.getTimeInMillis()) {
                    finalBalance9m = actualBalance;
                }
            }
            int dec = 0;
            int valInt = 0;
            String valStr = "";
            for (int j = 0; j < files.size(); j++) {
                if (valuesArray.get(j).containsKey(key)) {
                    String str = valuesArray.get(j).get(key);
                    //System.out.println(str);
                    String[] values = str.split(("\\."));
                    dec = values[1].length();
                    valStr = str.replace(".", "");
                    valInt = Integer.valueOf(valStr);
					
					/*if (valInt!=0){
						System.out.println("[INSIDE] "+DateUtils.datePrint(cal)
								+" "+valInt
						);
					}*/

                    if (dec == 2)
                        dayChange += valInt / 100.0;
                    else if (dec == 3) dayChange += valInt / 1000.0;

                    double leve = leveArray.get(j).get(key);
                    if (leve > 0) {
                        totalLeve += leve;
                    }
                }
            }
            if (!dayMonthPerformace.containsKey(dayMonth)) {
                dayMonthPerformace.put(dayMonth, 0.0);
            }
            double ap = dayMonthPerformace.get(dayMonth);
            dayMonthPerformace.put(dayMonth, ap + dayChange);
            dayChangeList.add(dayChange);
            actualBalance = actualBalance * (1 + dayChange / 100.0);
			
			/*if (dayChange!=0.00)
				System.out.println("[Change] "+DateUtils.datePrint(cal)
						+" "+PrintUtils.Print3dec(dayChange, false)
						+" "+PrintUtils.Print2dec(actualBalance, false)
			);*/

            double dayPer = 100.0 - actualBalance * 100.0 / dayBalance;

            if (dayPer >= maxDayChange) {
                maxDayChange = dayPer;
            } else {
                double ddDiff = maxDayChange - dayPer;
                if (ddDiff > maxDayChangeDD) {
                    maxDayChangeDD = ddDiff;
                }
            }

            if (Math.abs(dayChange) > 30000.0)
                System.out.println(DateUtils.datePrint(cal)
                        + " " + PrintUtils.Print2dec(actualBalance, false)
                        + " " + dec + " " + valStr + " " + valInt
                        + " " + PrintUtils.Print2dec(dayChange, false)
                );

            double actualLeverage = totalLeve;
            if (actualLeverage >= maxDayLeverage) maxDayLeverage = actualLeverage;
            //if (actualLeverage>=9.75){
            if (actualLeverage >= 9.75) {
                int daykey = year * 365 + cal.get(Calendar.DAY_OF_YEAR);

                if (!dayLeverage.containsKey(daykey)) {
                    dayLeverage.put(daykey, actualLeverage);
                    //System.out.println("[Leverage above 9.75] "+DateUtils.datePrint(cal)
                    //+";"+PrintUtils.Print2dec(actualLeverage, false));
                    totalDays975++;
                }
                double dayl = dayLeverage.get(daykey);
                if (actualLeverage >= dayl) dayLeverage.put(daykey, actualLeverage);
            }


            if (day != lastDay) {
                lastKey = key;
                dayPer = 100.0 - actualBalance * 100.0 / dayBalance;
                sp.addDayReturn(-dayPer);
				/*if (dayPer>=-3000.0){
					System.out.println(DateUtils.datePrint(cal)
							+";"+PrintUtils.Print2dec(-dayPer, false));
				}*/
                if (maxDayLeverage > 0) {
                    totalDays++;
                }
                //recover Day
                if (maxBalanceUpdated) {
                    //System.out.println("[Max balance updated] "+DateUtils.datePrint(cal)+" "+PrintUtils.Print2dec(actualBalance, false));
                    int diffRec = actualDay - lastDDDay;
                    if (diffRec > maxTimePeak) {
                        maxTimePeak = diffRec;
                        //System.out.println(DateUtils.datePrint(cal)+" "+lastDDDay);
                    }
                    if (diffRec > 0)
                        recoverList.add(diffRec);
                    lastDDDay = actualDay;
                }

                //mandamos a salida
                if (printDayInfo || maxDayChangeDD > 500.0) {
                    System.out.println(DateUtils.datePrint(cali) + ";" + PrintUtils.Print2dec(-dayPer, false)
                                    + ";" + PrintUtils.Print2dec(maxDayChangeDD, false)
                            //+";"+PrintUtils.Print2dec(dayBalance, false)
                            //+";"+PrintUtils.Print2dec(actualBalance, false)
                    );
                }

                maxDayChange = 0;
                maxDayChangeDD = 0;
                maxBalanceUpdated = false;
                maxDayLeverage = 0;
                dayBalance = actualBalance;
                lastDay = day;
                totalPossibleDays++;

                //solo evaluamos con el cambio de dia..
                if (actualBalance >= maxMonthPeak) {
                    maxMonthPeak = actualBalance;
                } else {
                    double dd = 100.0 - actualBalance * 100.0 / maxMonthPeak;
                    if (dd >= maxMonthDD) maxMonthDD = dd;
                }
            }

            if (actualBalance > maxBalance) {
                //System.out.println("[Max balance updated] "+DateUtils.datePrint(cal)+" "+PrintUtils.Print2dec(actualBalance, false));
                maxBalanceUpdated = true;
                maxBalance = actualBalance;
            } else {
                double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                if (dd > maxDD) {
                    maxDD = dd;
                    if (dd >= 1000.0) {
                        System.out.println("[DD UPDATE] " + DateUtils.datePrint(cal)
                                + ";" + PrintUtils.Print2dec(dd, false) + ";" + PrintUtils.Print2dec(maxBalance, false) + ";" + PrintUtils.Print2dec(actualBalance, false));
                    }
                }
            }

            if (actualBalance < initialBalance) {
                double dd = 100.0 - actualBalance * 100.0 / initialBalance;
                if (dd > maxDD2) {
                    maxDD2 = dd;
                }
            }


            if (month != lastMonth) {
                if (lastMonth != -1) {
                    double diffm = actualBalance - lastBalanceMonth;
                    double per = actualBalance * 100.0 / lastBalanceMonth - 100.0;
                    monthChange.add(per);
					/*System.out.println(
							"mes "
							+PrintUtils.Print2dec(per, false)	
							+" "+PrintUtils.Print2dec(actualBalance, false)
							//+" "+PrintUtils.Print2dec(per, false)
					);*/
                    if (per >= 0) {
                        winm++;
                    } else {
                        if (per <= -var95) {
                            lossm95++;
                        }
                        if (per <= -var95_2) {
                            lossm95_2++;
                        }
                    }
                    totalm++;

                    if (maxMonthDD >= var95) {
                        maxlossm95++;
                    }
                    if (maxMonthDD >= var95_2) {
                        maxlossm95_2++;
                    }
                    maxMonthDDArr.add(maxMonthDD);
                }
                maxMonthPeak = actualBalance;
                maxMonthDD = 0.0;
                lastBalanceMonth = actualBalance;
                lastMonth = month;
            }
        }
        if (lastMonth != -1) {
            double diffm = actualBalance - lastBalanceMonth;
            double per = actualBalance * 100.0 / lastBalanceMonth - 100.0;
            monthChange.add(per);
			/*System.out.println(
					"mes "
					+PrintUtils.Print2dec(per, false)	
					+" "+PrintUtils.Print2dec(actualBalance, false)
					//+" "+PrintUtils.Print2dec(per, false)
			);*/
            if (per >= 0) {
                winm++;
            } else {
                if (per <= -var95) {
                    lossm95++;
                }
                if (per <= -var95_2) {
                    lossm95_2++;
                }
            }
            totalm++;

            if (maxMonthDD >= var95) {
                maxlossm95++;
            }
            if (maxMonthDD >= var95_2) {
                maxlossm95_2++;
            }
            maxMonthDDArr.add(maxMonthDD);
        }
        //mandamos a salida
        double dayPer = 100.0 - actualBalance * 100.0 / dayBalance;
        sp.addDayReturn(-dayPer);
        if (printDayInfo) {
            System.out.println(DateUtils.datePrint(cal) + ";" + PrintUtils.Print2dec(dayPer, false) + ";" + PrintUtils.Print2dec(maxDayChangeDD, false)
            );
        }

        lastBalanceMonth = actualBalance;
        lastMonth = month;

        int diffRec = actualDay - lastDDDay;
        if (diffRec > 0) {
            recoverList.add(diffRec);
            if (diffRec > maxTimePeak) maxTimePeak = diffRec;
        }

        double avgChange = MathUtils.average(dayChangeList);
        double dt = Math.sqrt(MathUtils.variance(dayChangeList));

        double avgChangem = MathUtils.average(monthChange);
        double dtm = Math.sqrt(MathUtils.variance(monthChange));
        double sharperatio = (avgChangem / dtm) * Math.sqrt(12);
        double sharperatioDays = (avgChange / dt) * Math.sqrt(totalDays);

        double avgRecoverem = MathUtils.average(recoverList);
        double dtr = Math.sqrt(MathUtils.variance(recoverList));

        double diff3m = actualBalance * 100.0 / initialBalance3m - 100.0;
        double diff6m = actualBalance * 100.0 / initialBalance6m - 100.0;
        double diff9m = actualBalance * 100.0 / initialBalance9m - 100.0;
        double tradinDaysPer = totalDays * 100.0 / totalPossibleDays;

        if (sp != null) {
            sp.setActualBalance(actualBalance);
            sp.setMaxDD(maxDD2);
        }

        double avgMaxMonthDD = MathUtils.average(maxMonthDDArr);
        if (isAlways || diff3m >= 0)//s�lo si los 3 ultimos meses son positivos
            System.out.println(PrintUtils.leftpad(header, 50)
                    + " " + DateUtils.datePrintDDMMYYYYNoSep(calStart)
                    + " " + DateUtils.datePrintDDMMYYYYNoSep(calEnd)
                    + " || "
                    + " sr= " + PrintUtils.Print2dec(sharperatio, false)
                    + " maxDD= " + PrintUtils.leftpad(PrintUtils.Print2dec(maxDD, false), 6)
                    + " M3=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff3m, false), 6)
                    + " M6=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff6m, false), 6)
                    + " M9=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff9m, false), 6)
                    + " maxtime= " + PrintUtils.leftpad(PrintUtils.Print2dec(maxTimePeak, false), 6)
                    + " timePeak= " + PrintUtils.leftpad(PrintUtils.Print2dec(diffRec, false), 6)
                    + " maxDD2= " + PrintUtils.Print2dec(maxDD2, false)
                    + " avgMonthDD=  " + PrintUtils.Print2dec(avgMaxMonthDD, false)
                    + " profit%= " + PrintUtils.Print2dec(actualBalance * 100.0 / initialBalance - 100.0, false)
                    + " rtime= " + PrintUtils.Print2dec(avgRecoverem, false)
                    + " days%= " + PrintUtils.Print2dec(tradinDaysPer, false)
                    + " || balance=" + PrintUtils.Print2dec(actualBalance, false)
                    + " || "
                    + " days%>=9.75= " + PrintUtils.Print2dec(totalDays975 * 100.0 / totalDays, false)
                    + " || "
                    + " " + PrintUtils.Print2dec(winm * 100.0 / totalm, false)
                    + " " + PrintUtils.Print2dec(avgChangem, false)
                    + " " + PrintUtils.Print2dec(dtm, false)
                    + " || maxMontDD10= " + PrintUtils.Print2dec(maxlossm95 * 100.0 / totalm, false)
                    + " || maxMontDD65= " + PrintUtils.Print2dec(maxlossm95_2 * 100.0 / totalm, false)
                    + " || monthVar [" + PrintUtils.Print2dec(var95, false) + "] " + PrintUtils.Print2dec(lossm95 * 100.0 / totalm, false)
                    + " || monthVar [" + PrintUtils.Print2dec(var95_2, false) + "] " + PrintUtils.Print2dec(lossm95_2 * 100.0 / totalm, false)
            );

        //dayMonthPerformance
		
		/*List<Integer> keysMP = new ArrayList<>(dayMonthPerformace.keySet());
		Collections.sort(keysMP);
		for (int i=0;i<keysMP.size();i++){
			double per = dayMonthPerformace.get(keysMP.get(i));
			System.out.println("D�a "+keysMP.get(i)+" : "+PrintUtils.Print2dec(per, false));
		}*/

        return isEndInt;
    }

    /**
     * Probamos sistema en producci�n
     *
     * @throws IOException
     */
    public static int doTestProductionSystem(String headStr, String path,
                                             ArrayList<String> files,
                                             double balance,
                                             HashMap<Integer, Integer> atrHash,
                                             int offset, int offset2, boolean isAlways, boolean printComponents,
                                             int minATR,
                                             StratPerformance sp,
                                             int excludeFomcDays
    ) throws IOException {
        if (!headStr.equalsIgnoreCase(""))
            System.out.println(headStr);
        //String path = "c:\\fxdata\\";

        //String eurusd_2_3="eurusd_50_25_4_2_2_3.csv";
        //String eurusd_5="eurusd_50_10_5_1_5_5.csv";
        //String eurusd_6="eurusd_55_14_9_3_6_6.csv";
        //String eurusd_7_8="eurusd_65_20_8_3_7_8.csv";
        String eurusd_23_1_axi = "eurusd_42_10_7_1_23_25.csv";
        String eurusd_23_26 = "eurusd_96_10_8_1_23_26.csv";
        String eurusd_23_1 = "eurusd_42_10_7_2_23_25.csv";
        String eurusd_2_2 = "eurusd_24_6_7_3_2_2.csv";//eurusd_24_15_6_4_2_2.csv";
        String eurusd_5_8 = "eurusd_36_15_8_1_5_8.csv";
        String eurusd_5_8_darwinex = "eurusd_36_15_8_1_5_8.csv";
        String eurusd_5_5 = "eurusd_36_10_6_2_5_5.csv";
        String eurusd_6_8 = "eurusd_36_10_9_2_6_8.csv";
        String eurusd_9_9 = "eurusd_10_6_7_4_9_9.csv";
        String eurusd_14_14 = "eurusd_15_10_6_2_14_14.csv";
        String eurusd_18_18 = "eurusd_6_11_10_4_18_18.csv";
        String eurusd_22_22 = "eurusd_84_15_9_2_22_22.csv";

        //de 11 a 22
        String eurusd_11_11 = "eurusd_24_15_2_1_11_11.csv";
        String eurusd_17_17 = "eurusd_96_35_4_1_17_17.csv";
        String eurusd_23_1_1min = "eurusd_180_10_5_1_23_25_1min.csv";
        //String eurusd_23_1_1min="eurusd_105_20_5_1_23_25_1min.csv";
        String eurusd_5_8_1min = "eurusd_280_20_5_1_5_8_1min.csv";
        String eurusd_9_9_1min = "eurusd_42_10_2_1_9_9_1min.csv";
        String eurusd_2_2_1min = "eurusd_24_10_6_4_2_2.csv";//eurusd_55_20_5_2_2_2_1min.csv";
        String eurusd_18_18_1min = "eurusd_30_20_5_1_18_18_1min.csv";


        String audusd_23_1 = "audusd_24_10_4_1_23_25.csv";
        String usdjpy_23_1 = "usdjpy_24_10_2_1_23_25.csv";
        String gbpjpy_23_1 = "gbpjpy_30_30_10_2_23_25.csv";

        String eurcad_23_1 = "eurcad_35_15_6_2_23_25.csv";
        String euraud_23_1 = "euraud_30_15_4_1_23_25.csv";
        String eurgbp_23_1 = "eurgbp_35_15_6_2_23_25.csv";
        String gbpusd_23_1 = "gbpusd_35_15_6_2_23_25.csv";

        String usdjpy_23_23 = "usdjpy_36_10_10_2_23_23.csv";
        String usdjpy_0_0 = "usdjpy_36_10_10_1_0_0.csv";
        String usdjpy_1_1 = "usdjpy_36_10_10_2_1_1.csv";
        String usdjpy_2_2 = "usdjpy_60_15_6_2_2_2.csv";

        String eurjpy_23_1 = "eurjpy_36_10_7_2_23_25.csv";
        String eurjpy_2_2 = "eurjpy_48_10_8_1_2_2.csv";
        String usdjpy_12_12 = "usdjpy_36_20_9_3_12_12.csv";
        String usdjpy_14_14 = "usdjpy_12_15_9_5_14_14.csv";
        String usdjpy_21_21 = "usdjpy_39_10_9_2_21_21.csv";
        String usdjpy_22_22 = "usdjpy_42_10_10_2_21_21.csv";

        //estrategia buuffer
        String euBuffer = "tbglobal_400_10_70_3.csv";
        String euBuffer_darwinex = "tb_global_prod_20200805_darwinex.csv";
        String euBuffer_axi = "tb_global_prod_20201106_axiRisk.csv";
        String eurusd_23_1_15min = "eurusd_13_10_6_3_23_25_15min.csv";
        String eurusd_5_8_15min = "eurusd_15_10_6_3_5_8_15min.csv";
        String eurusd_18_18_15min = "eurusd_3_10_9_10_18_18_15min.csv";
        String eurusd_0_1_15min = "eurusd_15_10_8_6_0_1_15min.csv";
        String eurusd_0_1_darwinex_15min = "eurusd_15_10_8_5_0_1_15min.csv";
        String eurusd_0_15min = "eurusd_15_10_8_6_0_0_15min.csv";
        String eurusd_1_15min = "eurusd_15_10_7_6_1_1_15min.csv";

        String eurusd_6_15min = "eurusd_18_10_9_6_6_6_15min.csv";
        String eurusd_7_15min = "eurusd_17_10_9_7_7_7_15min.csv";
        String eurusd_8_15min = "eurusd_22_10_10_5_8_8_15min.csv";

        String eurusd_19_15min = "eurusd_4_10_7_5_19_19_15min.csv";

        String eurusd_22_15min = "eurusd_14_10_8_4_22_22_15min.csv";
        String eurusd_23_15min = "eurusd_16_15_9_6_23_23_15min.csv";

        //15 min darwinex
        String eurusd_23_25_15min = "eurusd_14_10_6_4_23_25_15min.csv";
        String eurusd_2_15min = "eurusd_18_10_7_5_2_2_15min.csv";
        String eurusd_6_8_15min = "eurusd_18_11_10_4_6_8_15min.csv";
        String eurusd_18_15min = "eurusd_3_10_10_7_18_18_15min.csv";
        String eurusd_21_15min = "eurusd_13_15_10_6_21_21_15min.csv";

        String usdjpy_23_25_15min = "usdjpy_14_10_6_4_23_25_15min.csv";

        //COMPROBAMOS EL RESULTADO AGREGADO

        //getexcludeddays
        HashMap<Long, Boolean> excludedDays = new HashMap<Long, Boolean>();
        if (excludeFomcDays != 99)
            getExcludeDays(path + "FOMC.csv", excludedDays, excludeFomcDays);//excluir dia de antes dia y dia despues

        //el resultado mas exacto sale de los bollinger
        int isEndInt = doCalculateResultadoAgregado("SPC", balance, atrHash, excludedDays, files, offset, offset2, isAlways, minATR, sp);

        if (printComponents) {
            ArrayList<String> test = new ArrayList<String>();
            for (int i = 0; i < files.size(); i++) {
                test.clear();
                test.add(files.get(i));
                balance = 7000;
                doCalculateResultadoAgregado("SPI: " + files.get(i), balance, atrHash, excludedDays, test, offset, offset2, true, 0, sp);
            }
        }

        return isEndInt;
    }

    private static void getExcludeDays(String fileName, HashMap<Long, Boolean> excludedDaysHash, int boths) throws IOException {
        excludedDaysHash.clear();
        Calendar cali = Calendar.getInstance();
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String type = line.split(";")[0];

            //FOMC = 1
            //ECB = 2
            //both = 3
            if (type.toUpperCase().equalsIgnoreCase("ECB") && (boths & 2) != 2) continue;
            if (type.toUpperCase().equalsIgnoreCase("FOMC") && (boths & 1) != 1) continue;


            String[] params = line.split(";")[1].split("/");

            int day = Integer.valueOf(params[0]);
            int mes = Integer.valueOf(params[1]);
            int anyo = Integer.valueOf(params[2]);

            //System.out.println("added: "+day+" "+mes+" "+anyo);

            cali.set(anyo, mes - 1, day, 0, 0, 0);
            cali.set(Calendar.MILLISECOND, 0);
            //excludedDaysHash.put(cali.getTimeInMillis(), true);

            cali.set(anyo, mes - 1, day - 1, 0, 0, 0);
            cali.set(Calendar.MILLISECOND, 0);
            //excludedDaysHash.put(cali.getTimeInMillis(), true);

            cali.set(anyo, mes - 1, day + 1, 0, 0, 0);
            cali.set(Calendar.MILLISECOND, 0);
            excludedDaysHash.put(cali.getTimeInMillis(), true);
        }
        System.out.println(excludedDaysHash.size());
    }


    private static void doRecalculateStrats(
            double balance,
            String folder, String fileName,
            String filePeriod,
            ArrayList<String> darwinexFiles,
            ArrayList<String> axiFiles,
            ArrayList<String> axiFiles2,
            ArrayList<String> darwinex1Files,
            boolean isRecalculate
    ) throws IOException {

        ;
        int year1 = 2011;
        int year2 = 2021;
        double initialBalance = balance;

        HashMap<String, Boolean> stratsTested = new HashMap<String, Boolean>();

        HashMap<String, ArrayList<QuoteShort>> dataBidHash = new HashMap<String, ArrayList<QuoteShort>>();
        HashMap<String, ArrayList<QuoteShort>> dataAskHash = new HashMap<String, ArrayList<QuoteShort>>();

        boolean isDarwinex = false;
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] params = line.split(";");

            String provider = params[0];
            String currency = params[1];
            //System.out.println(line);

            isDarwinex = true;
            if (!provider.equalsIgnoreCase("DARWINEX")) isDarwinex = false;

            try {
                String askFile = folder + currency + "_";
                String bidFile = folder + currency + "_";
                if (params.length > 4) {
                    String timeframe = params[2];
                    int nbars = Integer.valueOf(params[3]);
                    int dtInt = Integer.valueOf(params[4]);
                    int slInt = Integer.valueOf(params[5]);
                    int riskInt = Integer.valueOf(params[6]);
                    int h1 = Integer.valueOf(params[7]);
                    int h2 = Integer.valueOf(params[8]);
                    int is5m = 1;
                    if (timeframe.equalsIgnoreCase("5MIN")) {
                        askFile = "_5 Mins_Ask_" + filePeriod;//2011.01.01_2020.11.30.csv";
                        bidFile = "_5 Mins_Bid_" + filePeriod;//2011.01.01_2020.11.30.csv";
                    } else if (timeframe.equalsIgnoreCase("15MIN")) {
                        askFile = "_15 Mins_Ask_" + filePeriod;//2011.01.01_2020.11.30.csv";
                        bidFile = "_15 Mins_Bid_" + filePeriod;//2011.01.01_2020.11.30.csv";
                        is5m = 0;
                    } else if (timeframe.equalsIgnoreCase("1MIN")) {
                        askFile = "_1 Min_Ask_" + filePeriod;//2011.01.01_2020.11.30.csv";
                        bidFile = "_1 Min_Bid_" + filePeriod;//2011.01.01_2020.11.30.csv";
                        is5m = 0;
                    }

                    int minAtr = 0;
                    double dt = dtInt * 0.01;
                    double sl = slInt * 0.1;
                    double risk = 0.1 * riskInt;
                    String curr = currency;
                    String subTail = ".csv";
                    if (bidFile.contains("_1 Min_")) {
                        subTail = "_1min.csv";
                    }
                    if (askFile.contains("_15 Mins_")) {
                        subTail = "_15min.csv";
                    }
                    String tailStr = "_" + nbars + "_" + dtInt
                            + "_" + slInt
                            + "_" + riskInt
                            + "_" + h1
                            + "_" + h2
                            + subTail;

                    String outFileName = folder + curr + tailStr;
                    if (provider.equalsIgnoreCase("DARWINEX1")) darwinex1Files.add(outFileName);
                    else if (provider.equalsIgnoreCase("DARWINEX")) darwinexFiles.add(outFileName);
                    else if (provider.equalsIgnoreCase("AXI")) axiFiles.add(outFileName);
                    else axiFiles2.add(outFileName);

                    if (stratsTested.containsKey(curr + tailStr)) continue;
                    stratsTested.put(curr + tailStr, true);

                    if (isRecalculate && params.length > 3)
                        AnalisisResultadosConjuntos2021.getBollingueResults(folder, curr,
                                initialBalance, nbars, dt, sl, risk, year1, year2, Calendar.JANUARY, Calendar.DECEMBER, h1, h2,
                                bidFile, askFile, outFileName, minAtr, 1);
                } else {
                    //metodo de buffering...
                    if (params.length == 3) {
                        String outFileName = folder + params[2];

                        if (provider.equalsIgnoreCase("DARWINEX1")) darwinex1Files.add(outFileName);
                        else if (provider.equalsIgnoreCase("DARWINEX")) darwinexFiles.add(outFileName);
                        else if (provider.equalsIgnoreCase("AXI")) axiFiles.add(outFileName);
                        else axiFiles2.add(outFileName);
                    }
                }
            } catch (Exception e) {
                System.out.println("error: " + line + " msg= " + e.getMessage());
            }
        }
    }


    /**
     * {16-09-2021] Desactivar en d�as FOMC+1
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String filePeriod = "2011.11.01_2021.11.27.csv";
        String path = "f:\\fxdata\\";
        Path pathP = Paths.get(path);
        if (!Files.exists(pathP)) {
            path = "c:\\fxdata\\";
        }

        double balanceI = 2500;
        ArrayList<String> darwinex1Files = new ArrayList<String>();
        ArrayList<String> darwinexFiles = new ArrayList<String>();
        ArrayList<String> axiFiles = new ArrayList<String>();
        ArrayList<String> axiFiles2 = new ArrayList<String>();
        HashMap<Long, Boolean> excludedDays = new HashMap<Long, Boolean>();
        String stratsFile = path + "estrategias_20211126.csv";
        boolean recalculate = false;

        //LEEMOS LAS ESTRATEGIAS y calculamos
        doRecalculateStrats(balanceI, path, stratsFile, filePeriod, darwinexFiles, axiFiles, axiFiles2, darwinex1Files, recalculate);
        System.out.println("axiFiles2 , axifiles, darwinexfiles,darwinex1,: " + axiFiles2.size() + " " + axiFiles.size() + " " + darwinexFiles.size() + " " + darwinex1Files.size());

        ArrayList<String> files = new ArrayList<String>();
        String dataBidPath = path + "eurusd_5 Mins_Bid_" + filePeriod;

        ArrayList<QuoteShort> dataI = null;
        ArrayList<QuoteShort> dataS = null;
        dataI = new ArrayList<QuoteShort>();
        dataI = DAO.retrieveDataShort5m(dataBidPath, DataProvider.DUKASCOPY_FOREX4);
        TestLines.calculateCalendarAdjustedSinside(dataI);
        dataS = TradingUtils.cleanWeekendDataS(dataI);
        ArrayList<QuoteShort> dataBid = dataS;
        HashMap<Integer, Integer> atrHash = TradingUtils.calculateATR(dataBid, 20);
        //System.out.println(atrHash.size());


        int isEndInt = 0;
        StratPerformance sp = new StratPerformance();
        double perAcc = 0;
        double maxDDAcc = 0;
        int totalTrials = 0;
        int totalDays = 0;
        double accDayReturnAvg = 0;
        double accDayReturnDT = 0;
        ArrayList<Double> ddArray = new ArrayList<Double>();
        for (double balance = balanceI; balance <= balanceI - 0; balance += 1000) {
            //System.out.println("***balance= "+balance);
            for (int offset = 0; offset <= -1; offset += 3) {
                sp.reset();
                sp.setInitialBalance(balance);
                for (int minATR = 0; minATR <= 0; minATR += 1) {
                    //isEndInt = doTestProductionSystem("",path,darwinex1Files,balance,atrHash,offset,-1,true,false,minATR,sp);
                    //isEndInt = doTestProductionSystem("",path,darwinexFiles,balance,atrHash,offset,-1,true,false,minATR,sp);
                    //isEndInt = doTestProductionSystem("",path,axiFiles,balance,atrHash,offset,-1,true,false,minATR,sp);
                    isEndInt = doTestProductionSystem("", path, axiFiles2, balance, atrHash, offset, 3, true, false, minATR, sp, 0);//exludesdAys
                    //isEndInt = doTestProductionSystem("",path,axiFiles2,balance,atrHash,offset,120,true,false,minATR,sp,1);//exludesdAys
                    //isEndInt = doTestProductionSystem("",path,axiFiles2,balance,atrHash,offset,120,true,false,minATR,sp,2);//exludesdAys
                    totalTrials++;
                    perAcc += sp.getActualBalance() * 100.0 / sp.getInitialBalance() - 100.0;
                    maxDDAcc += sp.getMaxDD();
                    ddArray.add(sp.getMaxDD());
                    accDayReturnAvg += sp.dayReturnDays() * sp.dayReturnAvg();
                    accDayReturnDT += sp.dayReturnDays() * sp.dayReturnDT();
                    totalDays += sp.dayReturnDays();
                    if (isEndInt > 0) break;
                }
            }
        }
        double avgPer = perAcc * 1.0 / totalTrials;
        double avgMaxDD = maxDDAcc * 1.0 / totalTrials;
        double dtdd = Math.sqrt(MathUtils.varianceD(ddArray));
        //days
        double avgDayPer = accDayReturnAvg * 1.0 / totalDays;
        double avgDayDt = accDayReturnDT * 1.0 / totalDays;
        System.out.println(PrintUtils.leftpad("", 50)
                + " avgPer: " + PrintUtils.Print2(avgPer, false)
                + " avgDD: " + PrintUtils.Print2(avgMaxDD, false)
                + " avgDD + dt: " + PrintUtils.Print2(avgMaxDD + dtdd, false)
                + " || "
                + " avgPer: " + PrintUtils.Print2(avgDayPer, false)
                + " avgPer + dt: " + PrintUtils.Print2(avgDayPer + avgDayDt, false)
        );

        boolean isAgregado = true;
        ArrayList<String> currList = new ArrayList<String>();
        currList.add("eurusd");
        //currList.add("usdjpy");
        ////currList.add("audusd");
        //currList.add("eurjpy");
        //currList.add("audnzd");
        //currList.add("gbpusd");
		
		/*currList.add("usdcad");
		currList.add("nzdusd");
		currList.add("gbpusd");
		currList.add("eurgbp");
		currList.add("eurcad");
		currList.add("euraud");
		currList.add("audcad");
		currList.add("audnzd");*/

        if (currList.size() == 1) {
            //System.out.println("Curr: "+currList.get(0));
        }

        if (currList.size() == 0) {
            return;
        }

        //DARWINEX FILES

        double initialBalance = 2500;
        int year1 = 2011;
        int year2 = 2021;
        int h1 = 23;
        int h2 = 25;


        //5 5 87 10 7 10 || sharpem=01.19 || %Profit=10.33 || MaxDD=01.85 || avgWinLos=0.42 || avgTimePeak=18.58 maxTimePeak=357.00 || timeFromPeak=0.00 ||  4261 76.60 1.36 1.42
        //6 6 84 10 7 10 || sharpem=00.64 || %Profit=06.00 || MaxDD=02.38 || avgWinLos=0.38 || avgTimePeak=33.25 maxTimePeak=630.00 || timeFromPeak=4.00 ||  3772 76.33 1.23 1.24
        //7 7 69 10 7 10 || sharpem=00.91 || %Profit=06.55 || MaxDD=02.04 || avgWinLos=0.40 || avgTimePeak=29.98 maxTimePeak=583.00 || timeFromPeak=2.00 ||  2898 77.43 1.36 1.38
        //8 8 33 10 7 10 || sharpem=01.08 || %Profit=05.59 || MaxDD=02.01 || avgWinLos=0.53 || avgTimePeak=26.74 maxTimePeak=542.00 || timeFromPeak=3.00 ||  2033 74.27 1.53 1.53
        int printOptions = 0;
        String periodBidStr = "_1 Min_Bid_" + filePeriod;
        String periodAskStr = "_1 Min_Ask_" + filePeriod;
        String tailStr = "";
        String header = "";

        String subTail = ".csv";
        if (periodBidStr.contains("_1 Min_")) {
            subTail = "_1min.csv";
        }
        if (periodBidStr.contains("_15 Mins_")) {
            subTail = "_15min.csv";
        }

        //System.out.println("**** PERIOD "+year1+" "+year2);
        for (int h3 = -1; h3 <= -1; h3++) {
            if (h3 >= 0) {
                h1 = h3;
                h2 = h3;
            }
            for (int nbars = 80; nbars <= 250; nbars += 5) {
                for (int dtInt = 15; dtInt <= 15; dtInt += 5) {
                    for (int slInt = 10; slInt <= 10; slInt += 4) {
                        for (double risk = 0.1; risk <= 0.1; risk += 0.10) {

                            SimpleStats ss = new SimpleStats(nbars, dtInt, slInt);
                            int monthStep = 4;
                            for (int year = year1; year <= year2; year++) {
                                for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER - (monthStep - 1); month += monthStep) {
                                    for (int minAtr = 0; minAtr <= 0; minAtr += 500) {
                                        double dt = dtInt * 0.01;
                                        double sl = slInt * 0.1;
                                        int riskInt = (int) (risk * 10);
                                        header = "_" + nbars + "_" + dtInt
                                                + "_" + slInt
                                                + "_" + riskInt
                                                + "_" + h1
                                                + "_" + h2
                                                + " " + subTail
                                        ;
                                        tailStr = "_" + nbars + "_" + dtInt
                                                + "_" + slInt
                                                + "_" + riskInt
                                                + "_" + h1
                                                + "_" + h2
                                                + subTail;

                                        for (int i = 0; i < currList.size(); i++) {
                                            String curr = currList.get(i);
                                            String outFileName = path + curr + tailStr;
                                            header = outFileName;
                                            //System.out.println("antes de bollinger");
                                            StratPerformance spIndividual = AnalisisResultadosConjuntos2021.getBollingueResults(path, curr,
                                                    initialBalance, nbars, dt, sl, risk, year, year, month, month + (monthStep - 1), h1, h2,
                                                    periodBidStr, periodAskStr, outFileName, minAtr, printOptions);

                                            if (spIndividual.getTrades() > 0)
                                                ss.addStats(spIndividual.getSharpeRatio(), spIndividual.getTrades());
                                        }
                                    }//minAtr
                                }//month
                            }//years
                            if ((ss.getPositivePercent() >= 0))
                                System.out.println(currList.get(0) + " " + year1 + " " + year2 + " " + h1 + " " + h2 + " || " + ss.toString());
                        }
                    }
                }
            }
            //}//year10
        }
    }
}
