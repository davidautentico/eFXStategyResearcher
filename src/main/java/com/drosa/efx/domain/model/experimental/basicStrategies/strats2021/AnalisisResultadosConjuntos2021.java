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

    public static String EURUSD_5MIN_ASK = "EURUSD_5 Mins_Ask.csv";
    public static String EURUSD_5MIN_BID = "EURUSD_5 Mins_Bid.csv";
    public static String ESTRATEGIAS = "estrategias.csv";

    public static StratPerformance getBollingueResults(
            String path0,
            String curr,
            double initialBalance,
            int nbars, double dt, double sl, double risk,
            int year1, int year2,
            int m1, int m2,
            int h1, int h2,
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

        //System.out.println("lastValue: "+dataBid.get(minSize-1));

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
                currList, dataBidHash, dataAskHash, maxMinsHash, atrHashHash, minSize,
                calFrom, calTo, sp, 1, 0, printOptions);

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
                                                    double leverageAlert,
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
                    System.out.println("ERROR: " + fileName + " | " + lines.get(j) + " | " + e.getMessage());
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
            if (actualLeverage >= leverageAlert) {
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
            System.out.println(PrintUtils.leftpad(header, 20)
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
                    //+ " || monthVar [" + PrintUtils.Print2dec(var95, false) + "] " + PrintUtils.Print2dec(lossm95 * 100.0 / totalm, false)
                    //+ " || monthVar [" + PrintUtils.Print2dec(var95_2, false) + "] " + PrintUtils.Print2dec(lossm95_2 * 100.0 / totalm, false)
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
                                             double leverageAlert,
                                             StratPerformance sp,
                                             int excludeFomcDays
    ) throws IOException {
        //if (!headStr.equalsIgnoreCase(""))
            //System.out.println(headStr);
        //String path = "c:\\fxdata\\";


        //COMPROBAMOS EL RESULTADO AGREGADO

        //getexcludeddays
        HashMap<Long, Boolean> excludedDays = new HashMap<Long, Boolean>();
        if (excludeFomcDays != 99)
            getExcludeDays(path + "FOMC.csv", excludedDays, excludeFomcDays);//excluir dia de antes dia y dia despues

        //el resultado mas exacto sale de los bollinger
        int isEndInt = doCalculateResultadoAgregado("SPC", balance, atrHash, excludedDays, files, offset, offset2,
                isAlways, minATR, leverageAlert, sp);

        if (printComponents) {
            ArrayList<String> test = new ArrayList<String>();
            for (int i = 0; i < files.size(); i++) {
                test.clear();
                test.add(files.get(i));
                balance = 7000;
                doCalculateResultadoAgregado("SPI: " + files.get(i), balance, atrHash, excludedDays, test, offset,
                        offset2, true, 0, leverageAlert, sp);
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
        //System.out.println(excludedDaysHash.size());
    }


    public static void doRecalculateStrats(
            double balance,
            int year1,
            int year2,
            String folder, String fileName,
            ArrayList<String> darwinexFiles,
            ArrayList<String> axiFiles,
            ArrayList<String> axiFiles2,
            ArrayList<String> darwinex1Files,
            boolean isRecalculate
    ) throws IOException {
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
                        askFile = "_5 Mins_Ask.csv";
                        bidFile = "_5 Mins_Bid.csv";
                    } else if (timeframe.equalsIgnoreCase("15MIN")) {
                        askFile = "_15 Mins_Ask.csv";
                        bidFile = "_15 Mins_Bid.csv";
                        is5m = 0;
                    } else if (timeframe.equalsIgnoreCase("1MIN")) {
                        askFile = "_1 Min_Ask.csv";
                        bidFile = "_1 Min_Bid.csv";
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

        String path = "data\\";
        String stratsFile = path + "strategies\\" + AnalisisResultadosConjuntos2021.ESTRATEGIAS;
        String eurBidFile = path + AnalisisResultadosConjuntos2021.EURUSD_5MIN_BID;
        int rec_year1 = 2012;
        int rec_year2 = 2023;

        AnalisisResultadosConjuntos2021 ana = new AnalisisResultadosConjuntos2021();


        double balanceI = 2500;
        ArrayList<String> darwinex1Files = new ArrayList<String>();
        ArrayList<String> darwinexFiles = new ArrayList<String>();
        ArrayList<String> axiFiles = new ArrayList<String>();
        ArrayList<String> axiFiles2 = new ArrayList<String>();
        HashMap<Long, Boolean> excludedDays = new HashMap<Long, Boolean>();
        boolean recalculate = Boolean.FALSE;

        //LEEMOS LAS ESTRATEGIAS y calculamos
        doRecalculateStrats(balanceI, rec_year1, rec_year2, path, stratsFile, darwinexFiles, axiFiles, axiFiles2, darwinex1Files, recalculate);
        System.out.println("axiFiles2 , axifiles, darwinexfiles,darwinex1,: " + axiFiles2.size() + " " + axiFiles.size() + " " + darwinexFiles.size() + " " + darwinex1Files.size());

        ArrayList<String> files = new ArrayList<String>();
        String dataBidPath = eurBidFile;

        ArrayList<QuoteShort> dataI = null;
        ArrayList<QuoteShort> dataS = null;
        dataI = new ArrayList<QuoteShort>();
        dataI = DAO.retrieveDataShort5m(dataBidPath, DataProvider.DUKASCOPY_FOREX4);
        TestLines.calculateCalendarAdjustedSinside(dataI);
        dataS = TradingUtils.cleanWeekendDataS(dataI);
        ArrayList<QuoteShort> dataBid = dataS;
        HashMap<Integer, Integer> atrHash = TradingUtils.calculateATR(dataBid, 20);

        int isEndInt = 0;
        StratPerformance sp = new StratPerformance();
        double perAcc = 0;
        double maxDDAcc = 0;
        int totalTrials = 0;
        int totalDays = 0;
        double accDayReturnAvg = 0;
        double accDayReturnDT = 0;
        double leverageAlert = 9.75;
        ArrayList<Double> ddArray = new ArrayList<Double>();
        for (double balance = balanceI; balance <= balanceI - 0; balance += 1000) {
            //System.out.println("***balance= "+balance);
            for (int offset = 0; offset <= -1; offset += 3) {
                sp.reset();
                sp.setInitialBalance(balance);
                for (int minATR = 0; minATR <= 0; minATR += 1) {
                    //isEndInt = doTestProductionSystem("DMO",path,darwinex1Files,balance,atrHash,offset,12,true,false,minATR,sp,0);
                    isEndInt = doTestProductionSystem("DQO", path, darwinexFiles, balance, atrHash, offset, 3, true, false, minATR, leverageAlert, sp, 0);
                    //isEndInt = doTestProductionSystem("",path,axiFiles,balance,atrHash,offset,-1,true,false,minATR,sp);
                    //isEndInt = doTestProductionSystem("",path,axiFiles2,balance,atrHash,offset,3,true,false,minATR,sp,0);//exludesdAys
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
        currList.add("eurjpy");

        if (currList.size() == 1) {
            //System.out.println("Curr: "+currList.get(0));
        }

        if (currList.size() == 0) {
            return;
        }

        //DARWINEX FILES
        //TODO, MEDIR EL DD medio como medida fundamental
        double initialBalance = 2500;
        int year1 = 2012;
        int year2 = 2023;
        int h1 = 5;
        int h2 = 6;

        String filePeriod = "";
        int printOptions = 1;
        String periodBidStr = "_5 Mins_Bid.csv";
        String periodAskStr = "_5 Mins_Ask.csv";
        String tailStr = "";
        String header = "";

        String subTail = ".csv";
        if (periodBidStr.contains("_1 Min_")) {
            subTail = "_1min.csv";
        }
        if (periodBidStr.contains("_15 Mins_")) {
            subTail = "_15min.csv";
        }

        System.out.println("**** PERIOD " + year1 + " " + year2);
        for (int h3 = -1; h3 <= -1; h3++) {
            if (h3 >= 0) {
                h1 = h3;
                h2 = h3;
            }
            for (int nbars = 12; nbars <= 120; nbars += 6) {
                for (int dtInt = 10; dtInt <= 30; dtInt += 5) {
                    for (int slInt = 10; slInt <= 10; slInt += 5) {
                        for (double risk = 0.1; risk <= 0.1; risk += 0.10) {

                            SimpleStats ss = new SimpleStats(nbars, dtInt, slInt);
                            int monthStep = 12;
                            for (int year = year1; year <= year1; year++) {
                                int yearFrom = year1;
                                int yearTo = year2;
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
                                                    initialBalance, nbars, dt, sl, risk, yearFrom, yearTo, month, month + (monthStep - 1), h1, h2,
                                                    periodBidStr, periodAskStr, outFileName, minAtr, printOptions);

                                            if (spIndividual.getTrades() > 0) {
                                                double sharpeRatio = spIndividual.getSharpeRatio() >= 2.5 ? 2.5 : spIndividual.getSharpeRatio();
                                                //System.out.println(year+" "+spIndividual.getTrades()+ " "+PrintUtils.Print2dec(sharpeRatio, false)+" "+sp.getPf());
                                                ss.addStats(sharpeRatio, spIndividual.getTrades());
                                            }
                                        }
                                    }//minAtr
                                }//month
                            }//years
                            //if ((ss.getPositivePercent()>=0))
                            //System.out.println(currList.get(0) + " "+year1+" "+year2+" "+h1+" "+h2+" || "+ss.toString());
                        }
                    }
                }
            }
            //}//year10
        }
    }
}
