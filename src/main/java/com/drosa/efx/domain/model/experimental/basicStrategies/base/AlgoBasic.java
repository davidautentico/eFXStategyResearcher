package com.drosa.efx.domain.model.experimental.basicStrategies.base;

import com.drosa.efx.domain.model.finances.PositionShort;
import com.drosa.efx.domain.model.finances.PositionStatus;
import com.drosa.efx.domain.model.finances.PositionType;
import com.drosa.efx.domain.model.finances.QuoteShort;
import com.drosa.efx.domain.utils.DateUtils;
import com.drosa.efx.domain.utils.MathUtils;
import com.drosa.efx.domain.utils.PrintUtils;
import com.drosa.efx.domain.utils.TradingUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public abstract class AlgoBasic {

    int high = -1;
    int low = -1;
    Calendar cali = Calendar.getInstance();
    int comm = 15;
    double trailPer = 0.00;
    int trades = 0;
    int lastDayTrade = -1;
    int totalDaysTrading = 0;
    double risk = 0.20;
    boolean printAnyway = true;
    HashMap<Integer, ArrayList<Double>> spreads = null;
    int isTransactionHours = 1;
    int debug = 0;
    int lastCrossed = 0;
    int atr20 = 800;
    int actualH = 0;
    int actualY = 0;
    int actualM = 0;
    int maxPositions = 0;
    int minAtr20 = 0;
    boolean printDayProfit = false;
    boolean printMonthProfit = false;
    String outFileName = "";
    String outMonthFileName = "";
    int swapCostLong = 0;
    int swapCostShort = 0;

    public void doTrailPositions(ArrayList<QuoteShort> dataBid, ArrayList<QuoteShort> dataASK, int i, ArrayList<PositionShort> positions) {
		
		/*if (trailPer<=0.0) return;
		
		QuoteShort q = dataid.get(i);
		int j = 0;
		while (j<positions.size()){
			PositionShort p = positions.get(j);
			boolean isClosed = false;
			int pips = 0;
			if (p.getPositionStatus()==PositionStatus.OPEN){
				if (p.getPositionType()==PositionType.LONG){
					int trail = q.getOpen5()-p.getEntry();
					if (trail>=200){
						int trailPips = (int) (trail*trailPer);
						if (trailPips>=20){
							if (p.getEntry()+trailPips>p.getSl() && p.getEntry()+trailPips<q.getOpen5()){
								p.setSl(p.getEntry()+trailPips);
							}
						}
					}
				}else if (p.getPositionType()==PositionType.SHORT){
					int trail = -q.getOpen5()+p.getEntry();
					if (trail>=200){
						int trailPips = (int) (trail*trailPer);
						if (trailPips>=20){
							if (p.getEntry()-trailPips<p.getSl() && p.getEntry()-trailPips>q.getOpen5()){
								p.setSl(p.getEntry()-trailPips);
							}
						}
					}
				}
			}
			j++;
		}*/
    }

    public double doTest(
            String header,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            ArrayList<Integer> maxMins,
            HashMap<Integer, Integer> atrHash,
            HashMap<Integer, ArrayList<Double>> spreads,
            Calendar calFrom, Calendar calTo,
            StratPerformance sp,
            int isTrasactionHours,
            int debug,
            int printOptions
    ) {


        ArrayList<PositionShort> positions = new ArrayList<PositionShort>();
        ArrayList<Integer> ranges = new ArrayList<Integer>();

        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        int lastDay = -1;
        int totalDays = 0;
        totalDaysTrading = 0;
        int lastDayPips = 0;
        this.spreads = spreads;
        this.isTransactionHours = isTrasactionHours;
        this.debug = debug;
        this.lastCrossed = 0; //para bollinger cuando se cruza por ultima vez

        //sp.reset();
        //sp.setInitialBalance(20000);
        //bucle de datos
        boolean dayTraded = false;
        int maxDayLossAcc = 0;
        double maxDayLossAcc$$ = 0;
        double dayBalance = 0;
        int maxDayPositions = 0;

        int minSize = dataBid.size();
        if (dataAsk.size() < minSize) minSize = dataAsk.size();

        int actualDay = -1;
        int actualMonth = -1;
        int lastMonth = -1;
        int lastH = -1;
        boolean isDated = true;
        for (int i = 1; i < minSize; i++) {
            QuoteShort qb = dataBid.get(i);
            QuoteShort qb1 = dataBid.get(i - 1);
            QuoteShort qa = dataAsk.get(i);
            QuoteShort qa1 = dataAsk.get(i - 1);
            QuoteShort.getCalendar(cal, qb);
            QuoteShort.getCalendar(cal1, qb1);
            boolean canTrade = true;
            isDated = true;
            //System.out.println(DateUtils.datePrint(cal)+" "+positions.size());
            if (cal.compareTo(calFrom) < 0 || cal.compareTo(calTo) > 0) {
                continue;
            }

            int day = cal.get(Calendar.DAY_OF_YEAR);
            actualH = cal.get(Calendar.HOUR_OF_DAY);
            actualY = cal.get(Calendar.YEAR);
            actualM = cal.get(Calendar.MONTH);
            actualDay = TradingUtils.getActualDay(actualY, day);
            actualMonth = actualY * 12 + actualM;

            if (actualH != lastH) {

            }

            if (day != lastDay) {
                if (lastDay != -1) {
                    totalDays++;
                    if (dayTraded) totalDaysTrading++;

                    int actualDayPips = sp.getWinPips() - sp.getLostPips();
                    int diffPips = actualDayPips - lastDayPips;
					/*if (maxDayPositions>0)
					System.out.println(DateUtils.datePrint(cal1)
							+" "+maxDayPositions
							+" "+diffPips
							+" "+maxDayLossAcc
							+" || "+PrintUtils.Print2dec(maxDayLossAcc$$, false)
							+" || "+PrintUtils.Print2dec(maxDayLossAcc$$*100.0/dayBalance, false)
							);*/
                    double diffPer = maxDayLossAcc$$ * 100.0 / dayBalance;
                    if (diffPer <= -1.0) {
                        System.out.println(DateUtils.datePrint(cal1)
                                + " || " + PrintUtils.Print2dec(diffPer, false)
                        );
                    }
                    lastDayPips = actualDayPips;
                }
                //nuevo m�todo
                //if (!atrHash.containsKey(actualDay)){
                //	//System.out.println("no tiene: "+actualY+" "+day+" "+actualDay);
                //}
                atr20 = atrHash.get(actualDay);

                dayBalance = sp.getActualBalance();
                maxDayLossAcc = 0;
                maxDayLossAcc$$ = 0;
                maxDayPositions = 0;
                dayTraded = false;
                lastDay = day;
                high = -1;
                low = -1;
            }

            if (isDated) {
                if (actualMonth != lastMonth) {
                    sp.updateMonthEquitity(actualMonth, sp.getActualEquitity());
                    lastMonth = actualMonth;
                }

                //evaluamos entradas
                if (atr20 >= minAtr20) {
                    int ntrades = doEvaluateEntries("", dataBid, dataAsk, maxMins, spreads, i, positions, canTrade, sp);
                    if (ntrades > 0) {
                        dayTraded = true;
                    }
                }
                //evaluamos SL y Tp
                sp.resetEquitity();
                int j = 0;
                int lossAccumulated = 0;
                double lossAccumulated$$ = 0;
                if (positions.size() > maxDayPositions) maxDayPositions = positions.size();
                sp.updateDayOpenPositions(actualDay, positions.size(), maxPositions);
                while (j < positions.size()) {
                    PositionShort p = positions.get(j);
                    boolean isClosed = false;
                    int pips = 0;
                    String modeStr = "";
                    if (p.getPositionStatus() == PositionStatus.OPEN) {
                        if (p.getPositionType() == PositionType.LONG) {
                            int testValue = qb.getOpen5();
                            pips = testValue - p.getEntry();
                            if (testValue <= p.getSl() && p.getSl() >= 0) {
                                pips = testValue - p.getEntry();
                                isClosed = true;
                                //System.out.println("[long sl touched] "+pips);
                            } else if (testValue >= p.getTp() && p.getTp() >= 0) {
                                pips = testValue - p.getEntry();
                                isClosed = true;
                            }

                            if (p.getEntry() - testValue >= p.getMaxLoss()) {
                                p.setMaxLoss(p.getEntry() - testValue);
                            }
                        } else if (p.getPositionType() == PositionType.SHORT) {
                            int testValue = qa.getOpen5();
                            pips = -testValue + p.getEntry();
                            if (testValue >= p.getSl() && p.getSl() >= 0) {
                                pips = -testValue + p.getEntry();
                                isClosed = true;
                                modeStr = "SL";
                            } else if (testValue <= p.getTp() && p.getTp() >= 0) {
                                pips = -testValue + p.getEntry();
                                isClosed = true;
                                modeStr = "TP";
                            }

                            if (-p.getEntry() + testValue >= p.getMaxLoss()) {
                                p.setMaxLoss(-p.getEntry() + testValue);
                            }
                        }
                    }

                    sp.updateEquitity(pips, p.getMicroLots());
                    if (isClosed) {
                        int pipsSL = Math.abs(p.getEntry() - p.getSl());
                        double rr = pips * 1.0 / pipsSL;
                        //System.out.println(rr);
                        sp.addTrade(p.getMicroLots(), pips, pipsSL, p.getMaxLoss(), p.getTransactionCosts(), cal, true);

                        if (debug == 1) {
                            System.out.println(" [CLOSED " + modeStr + "] " + DateUtils.datePrint(cal)
                                    + " || pips = " + pips
                                    + " || " + p.toString2()
                                    + " || " + PrintUtils.Print2dec(sp.getMaxDD(), false)
                            );
                        }

                        lossAccumulated += pips;
                        lossAccumulated$$ += pips * p.getMicroLots() * 0.10 * 0.10;
                        if (lossAccumulated < maxDayLossAcc) maxDayLossAcc = lossAccumulated;
                        if (lossAccumulated$$ < maxDayLossAcc$$) maxDayLossAcc$$ = lossAccumulated$$;
                        j++;

                        positions.remove(j);
                    } else {
                        int pipsSL = Math.abs(p.getEntry() - p.getSl());
                        //System.out.println(pipsSL+" || "+p.getMicroLots());
                        lossAccumulated += pips;
                        lossAccumulated$$ += pips * p.getMicroLots() * 0.10 * 0.10;
                        if (lossAccumulated < maxDayLossAcc) maxDayLossAcc = lossAccumulated;
                        if (lossAccumulated$$ < maxDayLossAcc$$) maxDayLossAcc$$ = lossAccumulated$$;
                        j++;
                    }
                }

                //evaluamos salidas especiales
                doEvaluateExits("", dataBid, dataAsk, i, actualH, positions, sp);

                //custom manage
                doManagePositions(dataBid, dataAsk, i, positions);

                doTrailPositions(dataBid, dataAsk, i, positions);

                sp.updateMonthEquitity(actualMonth, sp.getActualEquitity());
                //EL dd se calcula con el equitity
                sp.updateMaxEquitityDD();
            }
            //actualizamos high low
            if (high == -1 || qb.getHigh5() >= high) high = qb.getHigh5();
            if (low == -1 || qb.getLow5() <= low) low = qb.getLow5();
        }

        int actualDayPips = sp.getWinPips() - sp.getLostPips();
        int diffPips = actualDayPips - lastDayPips;
        //if (diffPips<0)
        //System.out.println(DateUtils.datePrint(cal)+" "+diffPips);

        //if (printResults){
        double pf$$ = 0;
        if (sp.getTrades() > 0) {
            double winPer = sp.getWins() * 100.0 / sp.getTrades();
            double pf = sp.getWinPips() * 1.0 / sp.getLostPips();
            pf$$ = sp.getWinPips$() * 1.0 / sp.getLostPips$();
            double avgPips = (sp.getWinPips() - sp.getLostPips()) * 0.1 / sp.getTrades();
            double avgWin = sp.getWinPips() * 0.1 / sp.getWins();
            double avgLoss = sp.getLostPips() * 0.1 / sp.getLosses();
            double perDays = totalDaysTrading * 100.0 / totalDays;
            double factor = sp.getProfitPer() / sp.getMaxDD();
            double avgMaxAdversion = sp.getMaxAdversionAvg();
            //double var95 = sp.getMonthDataDD(2);
            double var95 = sp.getMonthDataDDRR(sp.getInitialBalance(), this.risk, 2);
            double profitPer = sp.getActualBalance() * 100.0 / sp.getInitialBalance() - 100.0;
            double yield = 0;
            if (true
                    && (printOptions == 1 || (printOptions == 2
                    && sp.getMaxDD() <= 30.0
                    //&& sp.getProfitPer()>=100.0
                    && perDays >= 30.0)
            )
            )
                System.out.println(
                        header
                                + " || %Profit=" + PrintUtils.Print2dec(profitPer, false, 2)
                                + " || MaxDD=" + PrintUtils.Print2dec(sp.getMaxDD(), false, 2)
                                + " || "
                                + " " + sp.getTrades()
                                + " " + PrintUtils.Print2dec(winPer, false)
                                + " " + PrintUtils.Print2dec(pf, false)
                                + " " + PrintUtils.Print2dec(pf$$, false)
                                + " " + sp.getWinPips() + " " + sp.getLostPips()
                                + " " + PrintUtils.Print2dec(sp.getWinPips$(), false)
                                + " " + PrintUtils.Print2dec(sp.getLostPips$(), false)
                                + "|| " + PrintUtils.Print2dec(avgPips, false)
                                + " " + PrintUtils.Print2dec(avgWin, false)
                                + " " + PrintUtils.Print2dec(avgLoss, false)
                                + " " + PrintUtils.Print2dec(yield, false)
                                + " || "
                                + " " + PrintUtils.Print2dec2(sp.getActualBalance(), true)
                                + " " + PrintUtils.Print2dec2(sp.getMaxBalance(), true)
                                //+" || VAR95= "+PrintUtils.Print2dec(var95, false)
                                + " || Factor=" + PrintUtils.Print2dec(profitPer / sp.getMaxDD(), false)
                                //+" || "+PrintUtils.Print2dec(taeFactor, false)
                                + " || " + sp.maxDDStats(20)
                                + " || " + sp.maxDDStats(40)

                );
        }
        return pf$$;
    }

    public double doTest(
            String header,
            ArrayList<String> currList,
            HashMap<String, ArrayList<QuoteShort>> dataBidHash,
            HashMap<String, ArrayList<QuoteShort>> dataAskHash,
            HashMap<String, ArrayList<Integer>> maxMinsHash,
            HashMap<String, HashMap<Integer, Integer>> atrHashHash,
            int minSize,
            Calendar calFrom, Calendar calTo,
            StratPerformance sp,
            int isTrasactionHours,
            int debug,
            int printOptions
    ) throws IOException {


        ArrayList<PositionShort> positions = new ArrayList<PositionShort>();
        ArrayList<Integer> ranges = new ArrayList<Integer>();
        ArrayList<Double> dayChanges = new ArrayList<Double>();
        ArrayList<Double> monthChanges = new ArrayList<Double>();
        ArrayList<Integer> arrayDD = new ArrayList<Integer>();


        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        int lastDay = -1;
        int totalDays = 0;
        totalDaysTrading = 0;
        int lastDayPips = 0;
        this.spreads = spreads;
        this.isTransactionHours = isTrasactionHours;
        this.debug = debug;
        this.lastCrossed = 0; //para bollinger cuando se cruza por ultima vez

        //sp.reset();
        //sp.setInitialBalance(20000);
        //bucle de datos
        boolean dayTraded = false;
        int maxDayLossAcc = 0;
        double maxDayLossAcc$$ = 0;
        double dayBalance = 0;
        int maxDayPositions = 0;

        int actualDay = -1;
        int actualMonth = -1;
        int lastMonth = -1;
        boolean isDated = true;

        FileWriter myWriter = null;
        if (outFileName.trim().length() > 0) {
            File f = new File(this.outFileName);
            if (f.exists() && !f.isDirectory()) {
                // do something
                f.delete();
            }
            myWriter = new FileWriter(outFileName);
        }
        FileWriter myWriterM = null;
        if (outMonthFileName.trim().length() > 0) {
            File f = new File(this.outMonthFileName);
            if (f.exists() && !f.isDirectory()) {
                // do something
                f.delete();
            }
            myWriterM = new FileWriter(outMonthFileName);
        }
        int lastH = -1;
        double hourBalance = sp.getActualBalance();
        int totalMicrolots = 0;
        int totalPipsRisk = 0;
        int totalOpenPositions = 0;
        int lastPeakDay = -1;
        double amountRisked = 0;
        double maxEquity = sp.getActualBalance();
        int maxTimePeak = 0;
        int maxPips = 0;
        int actualPips = 0;
        //delimitacion �ltimos 3 mesaes
        Calendar calqm = Calendar.getInstance();
        Calendar calEnd3m = Calendar.getInstance();
        Calendar calBegin3m = Calendar.getInstance();
        Calendar calEnd6m = Calendar.getInstance();
        Calendar calBegin6m = Calendar.getInstance();
        Calendar calEnd9m = Calendar.getInstance();
        Calendar calBegin9m = Calendar.getInstance();
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
        String curr = currList.get(0);
        ArrayList<QuoteShort> dataBid = dataBidHash.get(curr);
        QuoteShort qlast = dataBid.get(dataBid.size() - 1);
        QuoteShort.getCalendar(calqm, qlast);
        calEnd3m.setTimeInMillis(calqm.getTimeInMillis());
        calEnd6m.setTimeInMillis(calqm.getTimeInMillis());
        calEnd9m.setTimeInMillis(calqm.getTimeInMillis());
        calBegin3m.setTimeInMillis(calqm.getTimeInMillis());
        calBegin3m.add(Calendar.MONTH, -3);
        calBegin6m.setTimeInMillis(calqm.getTimeInMillis());
        calBegin6m.add(Calendar.MONTH, -6);
        calBegin9m.setTimeInMillis(calqm.getTimeInMillis());
        calBegin9m.add(Calendar.MONTH, -9);
		
		/*String curr0 = currList.get(0);
		ArrayList<QuoteShort> dataBid0 = dataBidHash.get(curr0);
		QuoteShort qb0	= dataBid0.get(minSize-1);
		QuoteShort.getCalendar(cal, qb0);
		System.out.println(DateUtils.datePrint(cal));*/
        double lastMonthBalance = sp.getActualBalance();
        boolean maxBalanceUpdated = false;
        for (int i = 1; i < minSize; i++) {
            for (int w = 0; w < currList.size(); w++) {
                curr = currList.get(w);
                dataBid = dataBidHash.get(curr);
                ArrayList<QuoteShort> dataAsk = dataAskHash.get(curr);
                ArrayList<Integer> maxMins = maxMinsHash.get(curr);
                HashMap<Integer, Integer> atrHash = atrHashHash.get(curr);

                QuoteShort qb = dataBid.get(i);
                QuoteShort qb1 = dataBid.get(i - 1);
                QuoteShort qa = dataAsk.get(i);
                QuoteShort qa1 = dataAsk.get(i - 1);
                QuoteShort.getCalendar(cal, qb);
                QuoteShort.getCalendar(cal1, qb1);
                boolean canTrade = true;
                isDated = true;

                int day = cal.get(Calendar.DAY_OF_YEAR);

                if (cal.compareTo(calFrom) < 0 || cal.compareTo(calTo) > 0) {
                    continue;
                }
                actualH = cal.get(Calendar.HOUR_OF_DAY);
                actualY = cal.get(Calendar.YEAR);
                actualM = cal.get(Calendar.MONTH);
                actualDay = TradingUtils.getActualDay(actualY, day);
                actualMonth = actualY * 12 + actualM;

                if (lastPeakDay < 0) lastPeakDay = actualDay;

                if (cal.getTimeInMillis() >= calBegin3m.getTimeInMillis()) {
                    if (!isInit) {
                        isInit = true;
                        initialBalance3m = sp.getActualEquitity();
                        //System.out.println("[Algobasic 3m] "+DateUtils.datePrint(cal)+" "+PrintUtils.Print2dec(initialBalance3m,false));
                    }
                    if (cal.getTimeInMillis() <= calEnd3m.getTimeInMillis()) {
                        finalBalance3m = sp.getActualEquitity();

                    }
                }

                if (cal.getTimeInMillis() >= calBegin6m.getTimeInMillis()) {
                    if (!isInit6) {
                        isInit6 = true;
                        initialBalance6m = sp.getActualEquitity();
                    }
                    if (cal.getTimeInMillis() <= calEnd6m.getTimeInMillis()) {
                        finalBalance6m = sp.getActualEquitity();
                    }
                }

                if (cal.getTimeInMillis() >= calBegin9m.getTimeInMillis()) {
                    if (!isInit9) {
                        isInit9 = true;
                        initialBalance9m = sp.getActualEquitity();
						/*System.out.println("[actual9M] "
								+" "+DateUtils.datePrint(cal)
								+" "+PrintUtils.Print2dec(sp.getActualEquitity(), false)
								);*/
                    }
                    if (cal.getTimeInMillis() <= calEnd9m.getTimeInMillis()) {
                        finalBalance9m = sp.getActualEquitity();
                    }
                }

                //cada 15 min.. para mas precesion
                int actualCheck = cal.get(Calendar.MINUTE) / 15;
                //actualCheck = actualH;
                if (actualCheck != lastH) {
                    if (lastH != -1) {
                        double perProfit = sp.getActualEquitity() * 100.0 / hourBalance - 100.0;

                        //if (perProfit<=-1.0) System.out.println(atr20+" "+PrintUtils.Print2dec(perProfit,false));

                        if (printDayProfit) {
                            double totalamountrisked = totalPipsRisk * 0.1 * totalMicrolots * 0.1;
                            double totalRiskPer = amountRisked * 100.0 / sp.getActualBalance();
                            String strdp = DateUtils.datePrint(cal1)
                                    + ";" + PrintUtils.Print3dec(perProfit, false)
                                    + ";" + totalOpenPositions
                                    + ";" + totalMicrolots
                                    + ";" + PrintUtils.Print2dec(totalRiskPer, false)
                                    + ";" + PrintUtils.Print2dec(sp.getActualEquitity(), false)
                                    + ";" + PrintUtils.Print2dec(sp.getActualBalance(), false)
                                    //+";"+PrintUtils.Print2dec(dayBalance, false)
                                    ;
                            myWriter.write(strdp + '\n');
                        }
                    }
                    lastH = actualCheck;
                    hourBalance = sp.getActualEquitity();
                }

                if (day != lastDay) {

                    if (lastDay != -1) {
                        totalDays++;
                        if (dayTraded) totalDaysTrading++;

                        int actualDayPips = sp.getWinPips() - sp.getLostPips();
                        int diffPips = actualDayPips - lastDayPips;
						/*if (maxDayPositions>0)
						System.out.println(DateUtils.datePrint(cal1)
								+" "+maxDayPositions
								+" "+diffPips
								+" "+maxDayLossAcc
								+" || "+PrintUtils.Print2dec(maxDayLossAcc$$, false)
								+" || "+PrintUtils.Print2dec(maxDayLossAcc$$*100.0/dayBalance, false)
								);*/
                        double diffPer = maxDayLossAcc$$ * 100.0 / dayBalance;
                        double perProfit = sp.getActualEquitity() * 100.0 / dayBalance - 100.0;

                        dayChanges.add(perProfit);

                        if (perProfit >= 987587) {
                            System.out.println(
                                    DateUtils.datePrint(cal1)
                                            + ";" + PrintUtils.Print2dec(perProfit, false)
                                            + ";actualBalance= " + PrintUtils.Print2dec(sp.getActualBalance(), false)
                                            + ";lastDayBalance= " + PrintUtils.Print2dec(dayBalance, false)
                                            + ";maxEq = " + PrintUtils.Print2dec(maxEquity, false)
                                            + ";maxTimePeak = " + maxTimePeak
                            );
                        }
						/*if (printDayProfit){
							String strdp = DateUtils.datePrint(cal1)
									+";"+PrintUtils.Print2dec(perProfit, false)
									+";"+PrintUtils.Print2dec(sp.getActualBalance(), false)
									+";"+PrintUtils.Print2dec(dayBalance, false)
									;
							myWriter.write(strdp+'\n');
						}*/
                        lastDayPips = actualDayPips;
                    }


                    if (maxBalanceUpdated) {
                        int ddPeak = actualDay - lastPeakDay;
                        arrayDD.add(ddPeak);
                        if (ddPeak > maxTimePeak) {
							/*System.out.println(
									"***MaxTimePeak*** "
									+" "+DateUtils.datePrint(cal1)
									+" "+ddPeak
									+" "+PrintUtils.Print2dec(maxEquity, false)
							);*/
                            maxTimePeak = ddPeak;
                        }

                        lastPeakDay = actualDay;
                    }
                    //nuevo m�todo
                    //if (!atrHash.containsKey(actualDay)){
                    //	//System.out.println("no tiene: "+actualY+" "+day+" "+actualDay);
                    //}
                    atr20 = atrHash.get(actualDay);
                    maxBalanceUpdated = false;
                    dayBalance = sp.getActualBalance();
                    maxDayLossAcc = 0;
                    maxDayLossAcc$$ = 0;
                    maxDayPositions = 0;
                    dayTraded = false;
                    lastDay = day;
                    high = -1;
                    low = -1;

                    //System.out.println("[NUEVO DAY] "+qb.toString()+" ATR= "+atr20);
                }

                if (isDated) {
                    if (actualMonth != lastMonth) {
                        double perProfit = sp.getActualEquitity() * 100.0 / lastMonthBalance - 100.0;
                        monthChanges.add(perProfit);
                        lastMonthBalance = sp.getActualEquitity();
                        sp.updateMonthEquitity(actualMonth, sp.getActualEquitity());
                        lastMonth = actualMonth;

                        if (printMonthProfit) {
                            double totalamountrisked = totalPipsRisk * 0.1 * totalMicrolots * 0.1;
                            double totalRiskPer = amountRisked * 100.0 / sp.getActualBalance();
                            String strdp = DateUtils.datePrint(cal1)
                                    + ";" + PrintUtils.Print3dec(perProfit, false)
                                    + ";" + totalOpenPositions
                                    + ";" + totalMicrolots
                                    + ";" + PrintUtils.Print2dec(totalRiskPer, false)
                                    + ";" + PrintUtils.Print2dec(sp.getActualEquitity(), false)
                                    + ";" + PrintUtils.Print2dec(sp.getActualBalance(), false)
                                    //+";"+PrintUtils.Print2dec(dayBalance, false)
                                    ;
                            myWriterM.write(strdp + '\n');
                        }
                    }

                    //evaluamos entradas
                    if (atr20 >= minAtr20) {
                        int ntrades = doEvaluateEntries(curr, dataBid, dataAsk, maxMins, spreads, i, positions, canTrade, sp);
                        if (ntrades > 0) {
                            dayTraded = true;
                            //System.out.println("[agregados trades] "+positions.size());
                        }
                    }

                    //evaluamos SL y Tp
                    sp.resetEquitity();
                    int j = 0;
                    int lossAccumulated = 0;
                    double lossAccumulated$$ = 0;
                    int testValue = qb.getOpen5();
                    int testValueH = -1;
                    int testValueL = -1;
                    if (positions.size() > maxDayPositions) maxDayPositions = positions.size();
                    sp.updateDayOpenPositions(actualDay, positions.size(), maxPositions);
                    totalMicrolots = 0;
                    totalPipsRisk = 0;
                    totalOpenPositions = 0;
                    amountRisked = 0;
                    while (j < positions.size()) {
                        PositionShort p = positions.get(j);
                        boolean isClosed = false;
                        int pips = 0;
                        String modeStr = "";
                        if (p.getPositionStatus() == PositionStatus.OPEN) {
                            if (p.getPositionType() == PositionType.LONG) {
                                testValue = qb.getOpen5();
                                testValueH = qb.getHigh5();
                                testValueL = qb.getLow5();
                                pips = testValue - p.getEntry();
                                if (testValueL <= p.getSl() && p.getSl() >= 0) {
                                    pips = p.getSl() - p.getEntry();
                                    isClosed = true;
                                    modeStr = "SL";
                                    //System.out.println("[long sl touched] "+pips);
                                } else if (testValueH >= p.getTp() && p.getTp() >= 0) {
                                    pips = testValue - p.getEntry();
                                    isClosed = true;
                                    modeStr = "TP";
                                }

                                if (p.getEntry() - testValue >= p.getMaxLoss()) {
                                    p.setMaxLoss(p.getEntry() - testValue);
                                }
                            } else if (p.getPositionType() == PositionType.SHORT) {
                                testValue = qa.getOpen5();
                                testValueH = qa.getHigh5();
                                testValueL = qa.getLow5();
                                pips = -testValue + p.getEntry();
                                if (testValueH >= p.getSl() && p.getSl() >= 0) {
                                    pips = -p.getSl() + p.getEntry();
                                    isClosed = true;
                                    modeStr = "SL";
                                } else if (testValueL <= p.getTp() && p.getTp() >= 0) {
                                    pips = -testValue + p.getEntry();
                                    isClosed = true;
                                    modeStr = "TP";
                                }

                                if (-p.getEntry() + testValue >= p.getMaxLoss()) {
                                    p.setMaxLoss(-p.getEntry() + testValue);
                                }
                            }
                        }

                        sp.updateEquitity(pips, p.getMicroLots());
                        if (isClosed) {
                            int pipsSL = Math.abs(p.getEntry() - p.getSl());
                            double rr = pips * 1.0 / pipsSL;

                            if (modeStr.equalsIgnoreCase("SL")) {
                                pips = -pipsSL;
                            }
                            //System.out.println(rr);
                            sp.addTrade(p.getMicroLots(), pips, pipsSL, p.getMaxLoss(), p.getTransactionCosts(), cal, true);

                            if (debug == 1) {
                                System.out.println(" [CLOSED ab " + modeStr + "] "
                                        + DateUtils.datePrint(cal)
                                        + " || pips = " + pips
                                        + " || " + p.toString2()
                                        + " || " + testValue
                                        + " || " + PrintUtils.Print2dec(sp.getMaxDD(), false)
                                );
                            }
                            lossAccumulated += pips;
                            lossAccumulated$$ += pips * p.getMicroLots() * 0.10 * 0.10;
                            if (lossAccumulated < maxDayLossAcc) maxDayLossAcc = lossAccumulated;
                            if (lossAccumulated$$ < maxDayLossAcc$$) maxDayLossAcc$$ = lossAccumulated$$;

                            positions.remove(j);
                        } else {
                            totalOpenPositions++;
                            totalMicrolots += p.getMicroLots();
                            amountRisked += Math.abs(p.getEntry() - p.getSl()) * 0.1 * p.getMicroLots() * 0.1;
                            int pipsSL = Math.abs(p.getEntry() - p.getSl());
                            //System.out.println(pipsSL+" || "+p.getMicroLots());
                            lossAccumulated += pips;
                            lossAccumulated$$ += pips * p.getMicroLots() * 0.10 * 0.10;
                            if (lossAccumulated < maxDayLossAcc) maxDayLossAcc = lossAccumulated;
                            if (lossAccumulated$$ < maxDayLossAcc$$) maxDayLossAcc$$ = lossAccumulated$$;
                            j++;
                        }
                    }//while

                    double actualLeverage = totalMicrolots * 1000.0 / sp.getActualEquitity();
					/*if (actualLeverage>=7.0) {
						System.out.println("[ActualLeverage] "+DateUtils.datePrint(cal)+" "+actualLeverage);
					}*/

                    //evaluamos salidas especiales
                    if (positions.size() > 0)
                        doEvaluateExits(curr, dataBid, dataAsk, i, actualH, positions, sp);

                    //custom manage
                    doManagePositions(dataBid, dataAsk, i, positions);

                    doTrailPositions(dataBid, dataAsk, i, positions);

                    sp.updateMonthEquitity(actualMonth, sp.getActualEquitity());
                    //EL dd se calcula con el equitity
                    sp.updateMaxEquitityDD();

                    if (sp.getActualEquitity() > maxEquity) {
						/*System.out.println("[maxeq] "
								+" "+DateUtils.datePrint(cal)
								+" "+PrintUtils.Print2dec(sp.getActualEquitity(), false)
								);*/

                        maxEquity = sp.getActualEquitity();
                        maxBalanceUpdated = true;
                    }
                }//if dated

                actualPips = sp.getWinPips() - sp.getLostPips();
                //if (sp.getActualEquitity()>maxEquity) {
                //maxEquity = sp.getActualEquitity();
                //maxBalanceUpdated = true;
                //}
                //actualizamos high low
                if (high == -1 || qb.getHigh5() >= high) high = qb.getHigh5();
                if (low == -1 || qb.getLow5() <= low) low = qb.getLow5();
            }
        }
		
		/*System.out.println(
				"MaxTimePeak "
				+" "+DateUtils.datePrint(cal1)
				+" "+maxTimePeak
		);*/

        if (myWriter != null)
            myWriter.close();

        int actualDayPips = sp.getWinPips() - sp.getLostPips();
        int diffPips = actualDayPips - lastDayPips;
        //if (diffPips<0)
        if (actualMonth != lastMonth) {
            double perProfit = sp.getActualEquitity() * 100.0 / lastMonthBalance - 100.0;
            monthChanges.add(perProfit);
            lastMonthBalance = sp.getActualEquitity();
            sp.updateMonthEquitity(actualMonth, sp.getActualEquitity());
            lastMonth = actualMonth;
        }

        //if (printResults){
        double pf$$ = 0;
        if (sp.getTrades() > 0) {

            double winPer = sp.getWins() * 100.0 / sp.getTrades();
            double pf = sp.getWinPips() * 1.0 / sp.getLostPips();
            pf$$ = sp.getWinPips$() * 1.0 / sp.getLostPips$();
            double avgPips = (sp.getWinPips() - sp.getLostPips()) * 0.1 / sp.getTrades();
            double avgWin = sp.getWinPips() * 0.1 / sp.getWins();
            double avgLoss = sp.getLostPips() * 0.1 / sp.getLosses();
            double perDays = totalDaysTrading * 100.0 / totalDays;
            double factor = sp.getProfitPer() / sp.getMaxDD();
            double avgMaxAdversion = sp.getMaxAdversionAvg();
            //double var95 = sp.getMonthDataDD(2);
            double var95 = sp.getMonthDataDDRR(sp.getInitialBalance(), this.risk, 2);
            double profitPer = sp.getActualBalance() * 100.0 / sp.getInitialBalance() - 100.0;
            double yield = 0;
            double avgDayChange = MathUtils.average(dayChanges);
            double dtDay = Math.sqrt(MathUtils.varianceD(dayChanges));
            double sharpe = (avgDayChange / dtDay) * Math.sqrt(252);
            double avgDayChangem = MathUtils.average(monthChanges);
            double dtDaym = Math.sqrt(MathUtils.varianceD(monthChanges));
            double sharpem = (avgDayChangem / dtDaym) * Math.sqrt(12);
            double diff3m = sp.getActualEquitity() * 100.0 / initialBalance3m - 100.0;
            double diff6m = sp.getActualEquitity() * 100.0 / initialBalance6m - 100.0;
            double diff9m = sp.getActualEquitity() * 100.0 / initialBalance9m - 100.0;
            int timeFromPeak = actualDay - lastPeakDay;
            if (timeFromPeak > maxTimePeak) maxTimePeak = timeFromPeak;
            arrayDD.add(timeFromPeak);
            double avgTimePeak = MathUtils.average(arrayDD);
			
			/*System.out.println("final "+DateUtils.datePrint(cal)
				+" "+PrintUtils.Print2dec(initialBalance3m,false)
				+" "+PrintUtils.Print2dec(finalBalance3m,false)
				+" "+PrintUtils.Print2dec(sp.getActualBalance(),false))
			;*/

            sp.setSharpeRatio(sharpem);

            if (true
                    && (printOptions == 1 || (printOptions == 2
                    && sp.getMaxDD() <= 30.0
                    //&& sp.getProfitPer()>=100.0
                    && perDays >= 30.0)
            )
            )
                System.out.println(
                        header
                                + " || sharpem=" + PrintUtils.Print2dec(sharpem, false, 2)
                                //+" || sharpe="+PrintUtils.Print2dec(sharpe, false,2)
                                + " || %Profit=" + PrintUtils.Print2dec(profitPer, false, 2)
                                + " || MaxDD=" + PrintUtils.Print2dec(sp.getMaxDD(), false, 2)
                                + " || M3=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff3m, false), 6)
                                + " M6=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff6m, false), 6)
                                + " M9=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff9m, false), 6)
                                + " || avgWinLos=" + PrintUtils.Print2dec(avgWin / avgLoss, false)
                                + " || avgTimePeak=" + PrintUtils.Print2dec(avgTimePeak, false) + " maxTimePeak=" + PrintUtils.Print2dec(maxTimePeak, false)
                                + " || timeFromPeak=" + PrintUtils.Print2dec(timeFromPeak, false)
                                + " || "
                                + " " + sp.getTrades()
                                + " " + PrintUtils.Print2dec(winPer, false)
                                + " " + PrintUtils.Print2dec(pf, false)
                                + " " + PrintUtils.Print2dec(pf$$, false)
                                + " " + sp.getWinPips() + " " + sp.getLostPips()
                                + " " + PrintUtils.Print2dec(sp.getWinPips$(), false)
                                + " " + PrintUtils.Print2dec(sp.getLostPips$(), false)
                                + "|| avgPips=" + PrintUtils.Print2dec(avgPips, false)
                                + " " + PrintUtils.Print2dec(yield, false)
                                + " || "
                                + " " + PrintUtils.Print2dec2(sp.getActualEquitity(), true)
                                + " " + PrintUtils.Print2dec2(sp.getMaxBalance(), true)
                                //+" || VAR95= "+PrintUtils.Print2dec(var95, false)
                                + " || Factor=" + PrintUtils.Print2dec(profitPer / sp.getMaxDD(), false)
                                //+" || "+PrintUtils.Print2dec(taeFactor, false)
                                + " || " + sp.maxDDStats(20)
                                + " || " + sp.maxDDStats(40)

                );
        }
        return pf$$;
    }

    abstract public void doManagePositions(ArrayList<QuoteShort> dataBid, ArrayList<QuoteShort> dataAsk, int i, ArrayList<PositionShort> positions);

    abstract public void doEvaluateExits(
            String curr,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            int i, int actualH2, ArrayList<PositionShort> positions,
            StratPerformance sp);


    abstract public int doEvaluateEntries(
            String curr,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            ArrayList<Integer> maxMins,
            HashMap<Integer, ArrayList<Double>> spreads,
            int i, ArrayList<PositionShort> positions,
            boolean canTrade, StratPerformance sp);

}
