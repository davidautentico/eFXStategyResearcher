package com.drosa.efx.domain.model.experimental.basicStrategies.base;

import com.drosa.efx.domain.model.finances.*;
import com.drosa.efx.domain.utils.*;
import com.drosa.efx.infrastructure.DAO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class TestPriceBufferGlobal$$ {

    public static double doTest(
            String header,
            ArrayList<QuoteShort> data,
            ArrayList<Integer> maxMins,
            int y1, int y2,
            int m1, int m2,
            int dayWeek1, int dayWeek2,
            ArrayList<StrategyConfig> configs,
            int hf,
            int maxTrades,
            int idxTest,
            int sizeCandle,
            boolean isMa,
            double aStd,
            double balance,
            double risk,
            double comm,
            boolean debug,
            boolean printSummary,
            int returnMode,
            HashMap<Integer, Integer> dayTotalPips,
            String outfileName,
            StratPerformance sp
    ) {

        sp.reset();
        sp.setInitialBalance(balance);

        double balanceInicial = balance;
        double actualBalance = balance; //actual equitity
        double actualEquitity = balance;
        double maxBalance = balance;
        double actualDD = 0.0;
        double maxDD = 0.0;
        int actualDDPips = 0;
        int maxWinPips = 0;
        int maxLostPips = 0;
        int maxPips = 0;

        ArrayList<PositionCore> positions = new ArrayList<PositionCore>();

        int lastDay = -1;
        int lastDayPips = 0;
        int dayPips = 0;
        int wins = 0;
        int losses = 0;
        int winPips = 0;
        int lostPips = 0;
        double winPips$$ = 0;
        double lostPips$$ = 0;
        int totalDays = 0;
        int totalL = 0;
        int totalLL = 0;
        int totalLLL = 0;
        int totalW = 0;
        int totalWL = 0;
        int totalRiskedPips = 0;
        Calendar cal = Calendar.getInstance();
        Calendar calqm = Calendar.getInstance();
        QuoteShort qm = new QuoteShort();
        double ma0 = -1;
        double std0 = -1;
        dayTotalPips.clear();
        ArrayList<Integer> days = new ArrayList<Integer>();

        ArrayList<Integer> dayRes = new ArrayList<Integer>();
        ArrayList<Double> dds = new ArrayList<Double>();
        ArrayList<Integer> ddPips = new ArrayList<Integer>();
        ArrayList<Integer> ddWinPips = new ArrayList<Integer>();
        ArrayList<Integer> ddLostPips = new ArrayList<Integer>();
        ArrayList<Double> ddPfs = new ArrayList<Double>();
        double dayDD = 0.0;
        int dayDDPip = 0;


        for (int i = 100; i < data.size() - 1; i++) {
            QuoteShort q1 = data.get(i - 1);
            QuoteShort q = data.get(i);
            QuoteShort q_1 = data.get(i + 1);
            QuoteShort.getCalendar(cal, q);

            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_YEAR);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int dayWeek = cal.get(Calendar.DAY_OF_WEEK);

            if (y < y1 || y > y2) continue;
            if (m < m1 || m > m2) continue;

            days.add(q.getOpen5());

            if (day != lastDay) {

                if (lastDay >= 0 && dayPips != 0) {
                    //System.out.println("pips: "+dayPips+" || "+(winPips-lostPips));

                    if (lastDayPips < 0) {
                        if (dayPips < 0) {
                            totalLL++;
                        }
                        totalL++;
                    }

                    if (lastDayPips > 0) {
                        if (dayPips < 0) {
                            totalWL++;
                        }
                        totalW++;
                    }

                    dayRes.add(dayPips);

                    totalDays++;

                    int dayKey = cal.get(Calendar.MONTH) * 31 + cal.get(Calendar.DAY_OF_MONTH);

                    if (!dayTotalPips.containsKey(dayKey)) {
                        dayTotalPips.put(dayKey, dayPips);
                    } else {
                        dayTotalPips.put(dayKey, dayTotalPips.get(dayKey) + dayPips);
                    }

                    lastDayPips = dayPips;

                    double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                    dds.add(dd);
                    dayDD = dd;

                    int ddPip = maxPips - (winPips - lostPips);
                    //int varWins = winPips-lastWinPips;
                    //int varLosses = lostPips -lastLostPips;
                    ddPips.add(ddPip);
                    ddWinPips.add(winPips);
                    ddLostPips.add(lostPips);
                    dayDDPip = ddPip;
                    //ddPfs.add(varWins*1.0/varLosses);

                    //lastWin
                }
                ma0 = MathUtils.average(days, days.size() - 14 * 288, days.size() - 1);
                std0 = Math.sqrt(MathUtils.variance(days, days.size() - 14 * 288, days.size() - 1));

                dayPips = 0;
                lastDay = day;
            }

            StrategyConfig config = configs.get(h);

            //modulo de entrada
            if (positions.size() < maxTrades
                    && dayWeek1 <= dayWeek && dayWeek <= dayWeek2
                    && (h > 0 || min >= 15)
            ) {
                if (config != null && config.isEnabled()) {
                    int thr = config.getThr();
                    int begin = i - config.getBarsBack();
                    //begin = i-1;//debug
                    int end = i - 1;
                    int index = TestPriceBuffer.getMinMaxBuff(maxMins, begin, end, thr);

                    int HC = q1.getHigh5() - q1.getClose5();
                    int CL = q1.getClose5() - q1.getLow5();
                    if (index >= 0
                        //&& sizeCandle1<=sizeCandle*10
                    ) {
                        int maxMin = maxMins.get(index);
                        //System.out.println("[INDEX>=0] "+DateUtils.datePrint(cal)+" "+thr+" "+(end-index)+" || "+data.get(index).toString()+" "+maxMin);


                        double realRisk = risk;
                        //if (dayDDPip<30000.0) realRisk =0.3;
						/*if (maxTrades*risk>=80){
							realRisk = 80.0/maxTrades;
						}*/

                        double maxRisk$$ = realRisk * actualEquitity / 100.0;
                        double pipValue = maxRisk$$ * 1.0 / config.getSl();
                        int miniLots = (int) (pipValue / 0.10);
                        pipValue = miniLots * 0.10;
                        int sizeHL = q.getOpen5() - data.get(i - 36).getLow5();
                        int sizeLH = data.get(i - 36).getHigh5() - q.getOpen5();
                        if (pipValue <= 0.10) pipValue = 0.10;//como minimo 0.01 lots
                        if (maxMin >= thr
                            //&& sizeHL==sizeCandle*10
                            //&& HC>=sizeCandle*10
                            //&& (!isMa || (isMa && ma0>-1 && q.getOpen5()<ma0-aStd*std0))
                            //&& ma0>-1 && q.getOpen5()>ma0+aStd*std0

                        ) {
                            PositionCore pos = new PositionCore();
                            pos.setEntry(q.getOpen5());
                            pos.setTp(q.getOpen5() - 10 * config.getTp());
                            pos.setSl(q.getOpen5() + 10 * config.getSl());
                            pos.setEntryIndex(i);
                            pos.setMaxIndex(i + config.getMaxBars());
                            pos.setPositionType(PositionType.SHORT);
                            pos.setIndexMinMax(end - index);
                            pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                            totalRiskedPips += config.getSl();
                            //pipValue
                            pos.setPipValue(pipValue);
                            pos.setMicroLots(miniLots);


                            //System.out.println("[SHORT] "+maxRisk$$ +" "+pipValue+" "+miniLots+" "+10*config.getSl());
                            positions.add(pos);
                        } else if (maxMin <= -thr
                            //&& sizeLH==sizeCandle*10
                            //&& CL>=sizeCandle*10
                            //&& (!isMa || (isMa && ma0>-1 && q.getOpen5()>ma0+aStd*std0))
                            //&& ma0>-1 && q.getOpen5()<ma0-aStd*std0

                        ) {
                            //System.out.println("[OPEN LONG] "+q.toString()+" || "+actualEquitity+" "+pipValue);

                            PositionCore pos = new PositionCore();
                            pos.setEntry(q.getOpen5());
                            pos.setTp(q.getOpen5() + 10 * config.getTp());
                            pos.setSl(q.getOpen5() - 10 * config.getSl());
                            pos.setEntryIndex(i);
                            pos.setMaxIndex(i + config.getMaxBars());
                            pos.setPositionType(PositionType.LONG);
                            pos.setIndexMinMax(end - index);
                            pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                            totalRiskedPips += config.getSl();
                            //pipValue
                            pos.setPipValue(pipValue);
                            pos.setMicroLots(miniLots);

                            //System.out.println("[LONG] "+q.toString());
                            positions.add(pos);
                        }
                    }

                }
            }

            //evaluacion trades
            int j = 0;
            actualEquitity = actualBalance;

            while (j < positions.size()) {
                PositionCore pos = positions.get(j);
                boolean isClosed = false;
                int pips = 0;
                int closedMode = 0;
                boolean isSLTP = true;
                if (pos.getPositionType() == PositionType.SHORT) {
                    if (i >= pos.getMaxIndex() || (dayWeek == Calendar.FRIDAY && (h >= hf))
                    ) {
                        isClosed = true;

                        pips = q_1.getOpen5() - pos.getEntry();
                        isSLTP = false;
                    } else {
                        if (q.getHigh5() >= pos.getSl()) {
                            isClosed = true;
                            pips = pos.getEntry() - pos.getSl();
                        } else if (q.getLow5() <= pos.getTp()) {
                            isClosed = true;
                            pips = pos.getEntry() - pos.getTp();
                        }
                    }

                    if (isClosed) {
                        pips = pos.getEntry() - q_1.getOpen5();
                    }
                } else if (pos.getPositionType() == PositionType.LONG) {
                    if (i >= pos.getMaxIndex() || (dayWeek == Calendar.FRIDAY && (h >= hf))
                    ) {
                        isClosed = true;

                        pips = q_1.getOpen5() - pos.getEntry();
                        closedMode = 1;
                        isSLTP = false;
                    } else {
                        if (q.getLow5() <= pos.getSl()) {
                            isClosed = true;
                            pips = -pos.getEntry() + pos.getSl();
                            closedMode = 2;
                        } else if (q.getHigh5() >= pos.getTp()) {
                            isClosed = true;
                            pips = -pos.getEntry() + pos.getTp();
                            closedMode = 3;
                        }
                    }
                    if (isClosed) {
                    }
                }

                //actualizacion equitity
                actualEquitity = actualEquitity + (pips - comm * 10) * 0.1 * pos.getMicroLots();

                if (isClosed) {
                    if (idxTest == -1 || pos.getIndexMinMax() == idxTest) {
                        pips -= comm * 10;

                        if (pips >= 0) {
                            wins++;
                            winPips += pips;
                            winPips$$ += pips * 0.1 * pos.getMicroLots();
                        } else {
                            losses++;
                            lostPips += -pips;
                            lostPips$$ += -pips * 0.1 * pos.getMicroLots();
                            //System.out.println("[LOSS] "+-pips+" "+pos.getMicroLots()+" || "+pos.toString()+" || "+q_1.toString()+" || "+pos.getSl()+" || "+closedMode);
                        }
                        //System.out.println("pips "+" "+pips+" || "+winPips+" "+lostPips);
                        dayPips += pips;

                        actualBalance += pips * 0.1 * pos.getMicroLots();

                        if (debug) {
                            System.out.println("[CLOSED] " + DateUtils.datePrint(cal) + " || " + PrintUtils.Print2dec(pips, false) + " " + PrintUtils.Print2dec(actualBalance, false) + " || " + pos.toString());
                        }
                        //if (actualBalance<=0) break;

                        if (actualBalance >= maxBalance) {
                            maxBalance = actualBalance;
                        } else {
                            double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                            if (dd >= maxDD) {
                                maxDD = dd;
                            }
                        }
                        sp.addTrade((long) pos.getMicroLots(), pips, Math.abs(pos.getEntry() - pos.getTp()),
                                Math.abs(pos.getEntry() - pos.getTp()), (int) (comm * 10),
                                cal, isSLTP
                        );

                        //para debug
                    }
                    positions.remove(j);
                } else {
                    j++;
                }

                if (winPips - lostPips >= maxPips) {
                    maxWinPips = winPips;
                    maxLostPips = lostPips;
                    maxPips = winPips - lostPips;
                }
            }
        }//data

        totalLL = 0;
        totalLLL = 0;
        int totalLLLL = 0;
        for (int i = 0; i < dayRes.size(); i++) {
            int pips = dayRes.get(i);

            if (i >= 3) {
                if (dayRes.get(i - 1) < 0
                        && dayRes.get(i - 2) < 0
                        && dayRes.get(i - 3) < 0
                ) {
                    totalLLL++;
                    if (dayRes.get(i) < 0) {
                        totalLLLL++;
                    }
                }
				/*if (dayRes.get(i-1)<0 && dayRes.get(i-2)<0){
					totalLL++;
					if (dayRes.get(i)<0){
						totalLLL++;
					}
				}*/
            }
        }


		/*for (double af=0.0;af<=20.0;af+=0.5){
			int count = 0;
			double acc = 0;
			for (int i=0;i<dds.size();i++){
				double ddi = dds.get(i);
				if (ddi>=af){
					//System.out.println(PrintUtils.Print2dec(acc*1.0/count, false));
					int j = i+10;
					if (j<=dds.size()-1){
						//System.out.println(PrintUtils.Print2dec(dds.get(j)-ddi, false));
						count++;
						acc+=dds.get(j)-ddi;
					}
				}
			}

			if (winPips-lostPips>=maxPips) maxPips = winPips-lostPips;

			System.out.println(PrintUtils.Print2dec(af, false)+";"+count+";"+PrintUtils.Print2dec(acc/count, false));
		}*/

        for (int af = 0; af <= 40000; af += 1000) {
            int count = 0;
            double acc = 0;
            double accPf = 0;
            int accPfw = 0;
            int accPfl = 0;
            for (int i = 0; i < ddPips.size(); i++) {
                int ddi = ddPips.get(i);
                int wp = ddWinPips.get(i);
                int lp = ddLostPips.get(i);
                if (ddi >= af) {
                    //System.out.println(PrintUtils.Print2dec(acc*1.0/count, false));
                    int j = i + 30;
                    if (j <= ddPips.size() - 1) {
                        //System.out.println(PrintUtils.Print2dec(dds.get(j)-ddi, false));
                        count++;
                        acc += ddPips.get(j) - ddi;
                        accPf += (ddWinPips.get(j) - wp) - (ddLostPips.get(j) - lp);
                        accPfw += (ddWinPips.get(j) - wp);
                        accPfl += (ddLostPips.get(j) - lp);
                    }
                }
            }

			/*System.out.println(PrintUtils.Print2dec(af, false)
					+";"+count
					//+";"+PrintUtils.Print2dec(acc/count, false)
					//+";"+PrintUtils.Print2dec(accPf/count, false)
					+";"+PrintUtils.Print2dec(accPfw*1.0/accPfl, false)
					);*/
        }

        double perLL = totalLL * 100.0 / totalL;
        double perLLL = totalLLL * 100.0 / totalLL;
        double perLLLL = totalLLLL * 100.0 / totalLLL;
        double perWL = totalWL * 100.0 / totalW;
		/*System.out.println(totalL
				+" "+PrintUtils.Print2dec(perLL, false)
				+" || "+totalW+" "+PrintUtils.Print2dec(perWL, false)
		);*/

        int trades = wins + losses;
        double winPer = wins * 100.0 / trades;
        double pf = winPips * 1.0 / lostPips;
        double pf$$ = winPips$$ * 1.0 / lostPips$$;
        double avg = (winPips - lostPips) * 0.1 / trades;

        double perWin = actualBalance * 100.0 / balance - 100.0;
        double perMaxWin = maxBalance * 100.0 / balance - 100.0;
        double actualBalance30 = actualBalance / (maxDD / 30.0); //balance con max 30%
        double yield = (winPips - lostPips) * 0.1 * 100 / totalRiskedPips;

        int totalAnios = y2 - y1 + 1;

        double tae = 100.0 * (Math.pow(actualBalance / (balanceInicial), 1.0 / totalAnios) - 1);
        double taeFactor = tae / maxDD;

        sp.setMaxDD(maxDD);
        double var95 = sp.getMonthDataDDRR(sp.getInitialBalance(), risk, 2);
        if (printSummary) {
            //if (pf<0.6)
            System.out.println(
                    header
                            + " || "
                            + " " + trades
                            + " " + PrintUtils.Print2dec(winPer, false)
                            + " " + PrintUtils.Print2dec(pf, false)
                            + " " + PrintUtils.Print2dec(pf$$, false)
                            //+" "+winPips+" "+lostPips
                            //+" "+PrintUtils.Print2dec(winPips$$, false)
                            //+" "+PrintUtils.Print2dec(lostPips$$, false)
                            + " || " + PrintUtils.Print2dec(avg, false)
                            + " " + PrintUtils.Print2dec(yield, false)
                            + " || "
                            + " " + PrintUtils.Print2dec2(actualBalance, true)
                            + " " + PrintUtils.Print2dec2(maxBalance, true)
                            + " " + PrintUtils.Print2dec(perMaxWin, false)
                            + " || MaxDD=" + PrintUtils.Print2dec(maxDD, false)
                            + " || VAR95= " + PrintUtils.Print2dec(var95, false)
                            + " || Factor=" + PrintUtils.Print2dec(perMaxWin / maxDD, false)
                            + " || " + PrintUtils.Print2dec(taeFactor, false)

            );
        }

        if (maxDD >= 100.0) return 0.0;

        //return actualBalance;
        return pf;
    }

    public static double doTestF(
            String header,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            ArrayList<Integer> maxMins,
            HashMap<Integer, Integer> atrHash,
            Calendar calFrom, Calendar calTo,
            int dayWeek1, int dayWeek2,
            int aMin,
            ArrayList<StrategyConfig> configs,
            int maxTrades,
            int idxTest,
            boolean isMa,
            double balance,
            int maxLostPositionPips,
            double maxDiff,
            boolean debug,
            boolean printSummary,
            int returnMode,
            HashMap<Integer, Integer> dayTotalPips,
            boolean isModeNormal,
            boolean isReverse,
            StratPerformance sp
    ) {


        sp.reset();
        sp.setInitialBalance(balance);

        double balanceInicial = balance;
        double actualBalance = balance; //actual equitity
        double actualEquitity = balance;
        double maxBalance = balance;
        double actualDD = 0.0;
        double maxDD = 0.0;
        int actualDDPips = 0;
        int maxWinPips = 0;
        int maxLostPips = 0;
        int maxPips = 0;
        //double maxLeverage = 3000;

        ArrayList<PositionCore> positions = new ArrayList<PositionCore>();

        int lastDay = -1;
        int lastDayPips = 0;
        int dayPips = 0;
        int wins = 0;
        int losses = 0;
        int winPips = 0;
        int lostPips = 0;
        double winPips$$ = 0;
        double lostPips$$ = 0;
        int totalDays = 0;
        int totalL = 0;
        int totalLL = 0;
        int totalLLL = 0;
        int totalW = 0;
        int totalWL = 0;
        int totalRiskedPips = 0;
        int maxLeverage = 3000;
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        Calendar calqm = Calendar.getInstance();

        QuoteShort qm = new QuoteShort();
        double ma0 = -1;
        double std0 = -1;
        dayTotalPips.clear();
        ArrayList<Integer> days = new ArrayList<Integer>();

        ArrayList<Integer> dayRes = new ArrayList<Integer>();
        ArrayList<Double> dds = new ArrayList<Double>();
        ArrayList<Integer> ddPips = new ArrayList<Integer>();
        ArrayList<Integer> ddWinPips = new ArrayList<Integer>();
        ArrayList<Integer> ddLostPips = new ArrayList<Integer>();
        ArrayList<Double> ddPfs = new ArrayList<Double>();
        ArrayList<Double> dailyChanges = new ArrayList<Double>();
        double dayDD = 0.0;
        int dayDDPip = 0;
        ArrayList<Integer> ranges = new ArrayList<Integer>();
        int high = -1;
        int low = -1;
        int dayRange = 700;

        int minSizeData = dataBid.size();
        if (dataAsk.size() < minSizeData) minSizeData = dataAsk.size();

        QuoteShort qi = dataBid.get(100);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        sp.updateMonthEquitity(y * 12 + m, balance);
        int actualMonth = -1;
        int lastMonth = -1;
        int actualDay = -1;
        double dailyMaxLeverage = 0;
        double lastDayEquitity = balance;
        for (int i = 100; i < minSizeData - 2; i++) {
            QuoteShort qb1 = dataBid.get(i - 1);
            QuoteShort qb = dataBid.get(i);
            QuoteShort qb_1 = dataBid.get(i + 1);
            QuoteShort qa1 = dataAsk.get(i - 1);
            QuoteShort qa = dataAsk.get(i);
            QuoteShort qa_1 = dataAsk.get(i + 1);
            QuoteShort.getCalendar(cal, qb);
            QuoteShort.getCalendar(cal1, qb1);
            double currentSpread = qa.getOpen5() - qb.getOpen5();

            y = cal.get(Calendar.YEAR);
            m = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_YEAR);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
            long timeInMillis = cal.getTimeInMillis();
            actualMonth = y * 12 + m;
            actualDay = y * 365 + day;

            int actualDayATR = TradingUtils.getActualDay(y, day);
            int atr = atrHash.get(actualDayATR);

            //if (m!=m1) continue;

            if (timeInMillis < calFrom.getTimeInMillis() || timeInMillis > calTo.getTimeInMillis()) continue;

            days.add(qb.getOpen5());

            if (day != lastDay) {
                if (lastDay >= 0) {
                    int actualRange = high - low;
                    ranges.add(actualRange);
                    dayRange = (int) MathUtils.average(ranges, ranges.size() - 20, ranges.size() - 1);

                    double equitityChange = actualEquitity - lastDayEquitity;
                    double dayPer = equitityChange * 100.0 / lastDayEquitity;
                    dailyChanges.add(dayPer);
                    //sp.addDailyChange(dayPer);
                }

                if (lastDay >= 0 && dayPips != 0) {
                    //System.out.println("pips: "+dayPips+" || "+(winPips-lostPips));

					/*if (dailyMaxLeverage>=15.00){
						System.out.println(DateUtils.datePrint(cal1)
								+" "+PrintUtils.Print2dec(dailyMaxLeverage,false)
								);
					}*/

                    if (lastDayPips < 0) {
                        if (dayPips < 0) {
                            totalLL++;
                        }
                        totalL++;
                    }

                    if (lastDayPips > 0) {
                        if (dayPips < 0) {
                            totalWL++;
                        }
                        totalW++;
                    }

                    dayRes.add(dayPips);

                    totalDays++;

                    int dayKey = cal.get(Calendar.MONTH) * 31 + cal.get(Calendar.DAY_OF_MONTH);

                    if (!dayTotalPips.containsKey(dayKey)) {
                        dayTotalPips.put(dayKey, dayPips);
                    } else {
                        dayTotalPips.put(dayKey, dayTotalPips.get(dayKey) + dayPips);
                    }

                    lastDayPips = dayPips;

                    double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                    dds.add(dd);
                    dayDD = dd;

                    int ddPip = maxPips - (winPips - lostPips);
                    //int varWins = winPips-lastWinPips;
                    //int varLosses = lostPips -lastLostPips;
                    ddPips.add(ddPip);
                    ddWinPips.add(winPips);
                    ddLostPips.add(lostPips);
                    dayDDPip = ddPip;
                }
                ma0 = MathUtils.average(days, days.size() - 14 * 288, days.size() - 1);
                std0 = Math.sqrt(MathUtils.variance(days, days.size() - 14 * 288, days.size() - 1));

                high = -1;
                low = -1;
                dayPips = 0;
                lastDay = day;
                dailyMaxLeverage = 0;

                //actualizamos day equitity
                sp.updateDailyEquitity(cal.getTimeInMillis(), actualEquitity);
            }

            for (int t = 0; t < configs.size(); t++) {
                StrategyConfig config = configs.get(t);
                if (config == null) continue;
                if (config.getHour1() != h) continue;

                //modulo de entrada
                if (positions.size() < maxTrades
                        && dayWeek1 <= dayWeek && dayWeek <= dayWeek2
                        && (h > 0 || min >= 0)
                        //&& currentSpread>=maxDiff
                        && atr >= maxDiff
                        && (min == aMin || aMin < 0)
                ) {
                    if (config != null && config.isEnabled()) {
                        int thr = config.getThr();
                        int begin = i - config.getBarsBack();
                        //begin = i-1;//debug
                        int end = i - 1;

                        //configuracion especial para probar 1 por 1
                        //end = i-config.getBarsBack();

                        int index = TestPriceBuffer.getMinMaxBuff(maxMins, begin, end, thr);
                        double risk = config.getRisk();

                        int HC = qb1.getHigh5() - qb1.getClose5();
                        int CL = qb1.getClose5() - qb1.getLow5();
                        if (index >= 0
                                //&& sizeCandle1<=sizeCandle*10
                                && risk >= 0.1
                        ) {
                            int maxMin = maxMins.get(index);
                            //System.out.println("[INDEX>=0] "+DateUtils.datePrint(cal)+" "+thr+" "+(end-index)+" || "+dataAsk.get(index).toString()+" "+maxMin);


                            double realRisk = risk;
                            //System.out.println(risk);
                            //if (dayDDPip<30000.0) realRisk =0.3;
							/*if (maxTrades*risk>=80){
								realRisk = 80.0/maxTrades;
							}*/
                            int tpPips = 0;
                            int slPips = 0;
                            int tp1 = 0;
                            int sl1 = 0;
                            if (isModeNormal) {
                                tpPips = (int) (config.getTp() * 10);
                                slPips = (int) (config.getSl() * 10);
                                tp1 = (int) (tpPips * 0.1);
                                sl1 = (int) (slPips * 0.1);
                            } else {
                                tpPips = (int) (config.getTpf() * dayRange);
                                slPips = (int) (config.getSlf() * dayRange);
                                tp1 = (int) (tpPips * 0.1);
                                sl1 = (int) (slPips * 0.1);
                            }

                            double maxRisk$$ = realRisk * actualEquitity / 100.0;
                            //maxRisk$$ =realRisk*actualBalance/100.0;
                            double pipValue = maxRisk$$ * 1.0 / config.getSl();//sl en formato pip
                            int miniLots = (int) (pipValue / 0.10);//1 mini lot es $0.10
                            pipValue = miniLots * 0.10;
                            int sizeHL = qb.getOpen5() - dataBid.get(i - 36).getLow5();
                            int sizeLH = dataBid.get(i - 36).getHigh5() - qb.getOpen5();
                            if (pipValue <= 0.10) pipValue = 0.10;//como minimo 0.01 lots
                            if (miniLots < 1) miniLots = 1;

                            //System.out.println(miniLots);

                            if (maxMin >= thr
                                //&& high-qb.getOpen5()>=maxDiff
                            ) {

                                int transactionCosts = TradingUtils.getTransactionCosts(null, y, h, 3);
                                //transactionCosts = 0;

                                PositionCore pos = new PositionCore();
                                pos.setEntry(qb.getOpen5());
                                pos.setEntryIndex(i);
                                pos.setMaxIndex(i + config.getMaxBars());
                                pos.setPositionStatus(PositionStatus.OPEN);

                                if (isReverse) {
                                    pos.setTp(qb.getOpen5() - tpPips);
                                    pos.setSl(qb.getOpen5() + slPips);
                                    pos.setPositionType(PositionType.SHORT);
                                } else {
                                    pos.setTp(qb.getOpen5() + slPips);
                                    pos.setSl(qb.getOpen5() - tpPips);
                                    pos.setPositionType(PositionType.LONG);
                                }

                                pos.setIndexMinMax(end - index);
                                pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                                totalRiskedPips += sl1;
                                //pipValue
                                pos.setPipValue(pipValue);
                                pos.setMicroLots(miniLots);
                                pos.setTransactionCosts(transactionCosts);
                                if (debug)
                                    System.out.println("[SHORT] " + DateUtils.datePrint(cal) + " " + PrintUtils.Print2dec(actualEquitity, false)
                                            + " " + PrintUtils.Print2dec(maxRisk$$, false) + " " + miniLots
                                            + " || " + pos.getEntry() + " " + pos.getTp() + " " + pos.getSl()
                                            + " || " + tpPips + " " + slPips
                                            + " ||| " + qb1.toString() + " | " + qb.toString()
                                    );
                                long totalMicroLots = TradingUtils.getOpenSize2(positions);
                                double leverage = (totalMicroLots + miniLots) * 1000.0 / actualEquitity;
                                if (leverage > dailyMaxLeverage) dailyMaxLeverage = leverage;

                                if (leverage < maxLeverage) positions.add(pos);
                            } else if (maxMin <= -thr
                                //&& qa.getOpen5()-low>=maxDiff
                            ) {
                                int transactionCosts = TradingUtils.getTransactionCosts(null, y, h, 3);
                                //transactionCosts = 0;

                                //System.out.println("[OPEN LONG] "+q.toString()+" || "+actualEquitity+" "+pipValue);
                                PositionCore pos = new PositionCore();
                                pos.setEntry(qa.getOpen5());
                                pos.setEntryIndex(i);
                                pos.setMaxIndex(i + config.getMaxBars());
                                pos.setPositionStatus(PositionStatus.OPEN);
                                pos.setIndexMinMax(end - index);
                                pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                                totalRiskedPips += sl1;
                                //pipValue
                                pos.setPipValue(pipValue);
                                pos.setMicroLots(miniLots);
                                pos.setTransactionCosts(transactionCosts);

                                if (isReverse) {
                                    pos.setTp(qa.getOpen5() + tpPips);
                                    pos.setSl(qa.getOpen5() - slPips);
                                    pos.setPositionType(PositionType.LONG);
                                } else {
                                    pos.setTp(qb.getOpen5() - slPips);
                                    pos.setSl(qb.getOpen5() + tpPips);
                                    pos.setPositionType(PositionType.SHORT);
                                }

                                if (debug)
                                    System.out.println("[LONG] " + DateUtils.datePrint(cal) + " " + PrintUtils.Print2dec(actualEquitity, false) + " " + PrintUtils.Print2dec(maxRisk$$, false) + " " + miniLots
                                            + " || " + pos.getEntry() + " " + pos.getTp() + " " + pos.getSl()
                                            + " || " + tpPips + " " + slPips
                                    );
                                long totalMicroLots = TradingUtils.getOpenSize2(positions);
                                double leverage = (totalMicroLots + miniLots) * 1000.0 / actualEquitity;
                                if (leverage > dailyMaxLeverage) dailyMaxLeverage = leverage;
                                //System.out.println((totalMicroLots+miniLots)+" || "+);
                                if (leverage < maxLeverage) positions.add(pos);
                            }
                        }

                    }
                }//maxTrades
            }//t

            //evaluacion trades
            int j = 0;
            actualEquitity = actualBalance;
            sp.updateDayOpenPositions(actualDay, positions.size(), maxTrades);
            while (j < positions.size()) {
                PositionCore pos = positions.get(j);
                boolean isClosed = false;
                int pips = 0;
                int closedMode = 0;
                boolean isSLTP = true;
                if (pos.getPositionType() == PositionType.SHORT) {
                    if (i >= pos.getMaxIndex()
                        //|| (dayWeek==Calendar.FRIDAY && (h>=hf))
                    ) {
                        closedMode = 1;
                        isClosed = true;
                        pips = pos.getEntry() - qa_1.getOpen5();
                        isSLTP = false;
                    }
                    if (!isClosed) {
                        if (qa.getHigh5() >= pos.getSl()) {
                            //if (q_1.getOpen5()>=pos.getSl()){
                            isClosed = true;
                            pips = pos.getEntry() - pos.getSl();
                            closedMode = 2;
                        } else if (qa.getLow5() <= pos.getTp()) {
                            //}else if (q_1.getOpen5()<=pos.getTp()){
                            isClosed = true;
                            pips = pos.getEntry() - pos.getTp();
                            closedMode = 3;
                        }
						/*if (closeMode==0)
							//aqui evaluamos si est� en p�rdidas y que hora es..
							pips = pos.getEntry()-qa_1.getOpen5();
							if (pips<-maxLostPositionPips && h>=hmax) {
								isClosed = true;
							}
						}*/
                    }
                    if (isClosed) {
                        pips = pos.getEntry() - qa_1.getOpen5();
                        if (closedMode == 2) {
                            pips = pos.getEntry() - pos.getSl();
                        } else if (closedMode == 3) {
                            pips = pos.getEntry() - pos.getTp();
                        }
                    }
                } else if (pos.getPositionType() == PositionType.LONG) {
                    if (i >= pos.getMaxIndex()
                        //|| (dayWeek==Calendar.FRIDAY && (h>=hf))
                    ) {
                        closedMode = 1;
                        isClosed = true;
                        pips = qb_1.getOpen5() - pos.getEntry();
                        isSLTP = false;
                    }

                    if (!isClosed) {
                        if (qb.getLow5() <= pos.getSl()) {
                            //if (q_1.getOpen5()<=pos.getSl()){
                            isClosed = true;
                            pips = pos.getSl() - pos.getEntry();
                            closedMode = 2;
                        } else if (qb.getHigh5() >= pos.getTp()) {
                            //}else if (q_1.getOpen5()>=pos.getTp()){
                            isClosed = true;
                            pips = pos.getTp() - pos.getEntry();
                            closedMode = 3;
                        }
						/*else {
							//aqui evaluamos si est� en p�rdidas y que hora es..
							pips = qb_1.getOpen5()-pos.getEntry();
							if (pips<-maxLostPositionPips && h>=hmax) {
								isClosed = true;
							}
						}*/
                    }
                    if (isClosed) {
                        pips = qb_1.getOpen5() - pos.getEntry();
                        if (closedMode == 2) {
                            pips = pos.getSl() - pos.getEntry();
                        } else if (closedMode == 3) {
                            pips = pos.getTp() - pos.getEntry();
                        }
                    }
                }
                //actualizacion equitity
                actualEquitity = actualEquitity + (pips - pos.getTransactionCosts()) * 0.1 * pos.getMicroLots() * 0.1;

                if (isClosed) {
                    if (idxTest == -1 || pos.getIndexMinMax() == idxTest) {

                        sp.addTrade((long) pos.getMicroLots(), pips, Math.abs(pos.getEntry() - pos.getTp()),
                                Math.abs(pos.getEntry() - pos.getTp()), pos.getTransactionCosts(), cal, isSLTP);
                        pips -= pos.getTransactionCosts();

                        if (pips >= 0) {
                            wins++;
                            winPips += pips;
                            winPips$$ += pips * 0.1 * pos.getMicroLots() * 0.1;//1 microLot = 0.1$/pip
                        } else {
                            losses++;
                            lostPips += -pips;
                            lostPips$$ += -pips * 0.1 * pos.getMicroLots() * 0.1;
                            //System.out.println("[LOSS 2] "+-pips+" "+pos.getMicroLots()+" || "+pos.toString()+" || "+q_1.toString()+" || "+pos.getSl()+" || "+closedMode);
                        }
                        //System.out.println("pips "+" "+pips+" || "+winPips+" "+lostPips);
                        dayPips += pips;

                        actualBalance += pips * 0.1 * pos.getMicroLots() * 0.1;


                        //if (actualBalance<=0) break;

                        if (actualBalance >= maxBalance) {
                            maxBalance = actualBalance;
                        } else {
                            double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                            if (dd >= maxDD) {
                                maxDD = dd;
                            }
                        }


                        if (debug) {
                            String str = "WIN";
                            if (pips < 0) str = "LOSS";

                            System.out.println("[CLOSED " + str + "] " + DateUtils.datePrint(cal)
                                    + " || " + closedMode + " || " + pips
                                    //+" || "+q_1.toString()
                                    //+" || open="+q_1.getOpen()+" tp="+pos.getTp()
                                    + " || " + pos.getMicroLots() + " " + PrintUtils.Print2dec(actualBalance, false)
                                    + " || " + pos.toString()
                                    + " || " + winPips + " " + lostPips
                                    + " || " + PrintUtils.Print2dec(winPips$$, false) + " " + PrintUtils.Print2dec(lostPips$$, false)
                                    + " || " + PrintUtils.Print2dec(maxDD, false)
                            );
                        }

                        //para debug
                    }
                    positions.remove(j);
                } else {
                    j++;
                }

                if (winPips - lostPips >= maxPips) {
                    maxWinPips = winPips;
                    maxLostPips = lostPips;
                    maxPips = winPips - lostPips;
                }
            }
            //ACTUALIZAMOS EQUITITY MENSUAL
            sp.updateMonthEquitity(actualMonth, actualEquitity);

            if (high == -1 || qb.getHigh5() >= high) high = qb.getHigh5();
            if (low == -1 || qb.getLow5() <= low) low = qb.getLow5();
        }//data

        totalLL = 0;
        totalLLL = 0;
        int totalLLLL = 0;
        for (int i = 0; i < dayRes.size(); i++) {
            int pips = dayRes.get(i);

            if (i >= 3) {
                if (dayRes.get(i - 1) < 0
                        && dayRes.get(i - 2) < 0
                        && dayRes.get(i - 3) < 0
                ) {
                    totalLLL++;
                    if (dayRes.get(i) < 0) {
                        totalLLLL++;
                    }
                }
				/*if (dayRes.get(i-1)<0 && dayRes.get(i-2)<0){
					totalLL++;
					if (dayRes.get(i)<0){
						totalLLL++;
					}
				}*/
            }
        }


		/*for (double af=0.0;af<=20.0;af+=0.5){
			int count = 0;
			double acc = 0;
			for (int i=0;i<dds.size();i++){
				double ddi = dds.get(i);
				if (ddi>=af){
					//System.out.println(PrintUtils.Print2dec(acc*1.0/count, false));
					int j = i+10;
					if (j<=dds.size()-1){
						//System.out.println(PrintUtils.Print2dec(dds.get(j)-ddi, false));
						count++;
						acc+=dds.get(j)-ddi;
					}
				}
			}

			if (winPips-lostPips>=maxPips) maxPips = winPips-lostPips;

			System.out.println(PrintUtils.Print2dec(af, false)+";"+count+";"+PrintUtils.Print2dec(acc/count, false));
		}*/

        for (int af = 0; af <= 40000; af += 1000) {
            int count = 0;
            double acc = 0;
            double accPf = 0;
            int accPfw = 0;
            int accPfl = 0;
            for (int i = 0; i < ddPips.size(); i++) {
                int ddi = ddPips.get(i);
                int wp = ddWinPips.get(i);
                int lp = ddLostPips.get(i);
                if (ddi >= af) {
                    //System.out.println(PrintUtils.Print2dec(acc*1.0/count, false));
                    int j = i + 30;
                    if (j <= ddPips.size() - 1) {
                        //System.out.println(PrintUtils.Print2dec(dds.get(j)-ddi, false));
                        count++;
                        acc += ddPips.get(j) - ddi;
                        accPf += (ddWinPips.get(j) - wp) - (ddLostPips.get(j) - lp);
                        accPfw += (ddWinPips.get(j) - wp);
                        accPfl += (ddLostPips.get(j) - lp);
                    }
                }
            }

			/*System.out.println(PrintUtils.Print2dec(af, false)
					+";"+count
					//+";"+PrintUtils.Print2dec(acc/count, false)
					//+";"+PrintUtils.Print2dec(accPf/count, false)
					+";"+PrintUtils.Print2dec(accPfw*1.0/accPfl, false)
					);*/
        }

        double perLL = totalLL * 100.0 / totalL;
        double perLLL = totalLLL * 100.0 / totalLL;
        double perLLLL = totalLLLL * 100.0 / totalLLL;
        double perWL = totalWL * 100.0 / totalW;
		/*System.out.println(totalL
				+" "+PrintUtils.Print2dec(perLL, false)
				+" || "+totalW+" "+PrintUtils.Print2dec(perWL, false)
		);*/

        int trades = wins + losses;
        double winPer = wins * 100.0 / trades;
        double pf = winPips * 1.0 / lostPips;
        double pf$$ = winPips$$ * 1.0 / lostPips$$;
        double avg = (winPips - lostPips) * 0.1 / trades;

        double perWin = actualBalance * 100.0 / balance - 100.0;
        double perMaxWin = maxBalance * 100.0 / balance - 100.0;
        double actualBalance30 = actualBalance / (maxDD / 30.0); //balance con max 30%
        double yield = (winPips - lostPips) * 0.1 * 100 / totalRiskedPips;

        //int totalA�os = y2-y1+1;

        //double tae = 100.0*(Math.pow(actualBalance/(balanceInicial), 1.0/totalA�os)-1);
        //double taeFactor = tae/maxDD;


        double avgWin = winPips * 0.1 / wins;
        double avgLoss = lostPips * 0.1 / losses;
        double dAvg = sp.getDailyAvg();
        double dDT = sp.getDailyDt();

        sp.setMaxDD(maxDD);
        //double var95 = sp.getMonthDataDDRR(sp.getInitialBalance(),risk,2);
        if (printSummary) {
            if (maxDD < 3000.0
                    //&& perMaxWin>=500.0
                    && sp.getTrades() > 0
            )
                System.out.println(
                        header
                                + " || "
                                + " || %Profit=" + PrintUtils.Print2dec(actualBalance * 100.0 / balance - 100.0, false)
                                + " || MaxDD=" + PrintUtils.Print2dec(maxDD, false)
                                + " || " + trades
                                + " " + PrintUtils.Print2dec(winPer, false)
                                + " " + PrintUtils.Print2dec(pf, false)
                                + " " + PrintUtils.Print2dec(pf$$, false)
                                + " " + winPips + " " + lostPips
                                + " " + PrintUtils.Print2dec(winPips$$, false)
                                + " " + PrintUtils.Print2dec(lostPips$$, false)
                                + "|| " + PrintUtils.Print2dec(avg, false)
                                + " " + PrintUtils.Print2dec(avgWin, false)
                                + " " + PrintUtils.Print2dec(avgLoss, false)
                                + " " + PrintUtils.Print2dec(yield, false)
                                + " || "
                                + " " + PrintUtils.Print2dec2(actualBalance, true)
                                + " " + PrintUtils.Print2dec2(maxBalance, true)

                                //+" || VAR95= "+PrintUtils.Print2dec(var95, false)
                                + " || Factor=" + PrintUtils.Print2dec(perMaxWin / maxDD, false)
                                //+" || "+PrintUtils.Print2dec(taeFactor, false)
                                //+" || "+sp.maxDDStats(20)
                                //+" || "+sp.maxDDStats(40)
                                + " || " + PrintUtils.Print2dec(dAvg + dDT, true)
                                + " " + PrintUtils.Print2dec(dAvg, true)
                                + " " + PrintUtils.Print2dec(dDT, true)
                                + " || " + PrintUtils.Print2dec(sp.getPerBelow(-4.0), true)
                );
        }

        if (maxDD >= 100.0) return 0.0;

        //return actualBalance;
        return pf$$;
    }

    public static double doTestG(
            String header,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            ArrayList<Integer> maxMins,
            HashMap<Integer, Integer> atrHash,
            Calendar calFrom, Calendar calTo,
            int dayWeek1, int dayWeek2,
            int aMin,
            ArrayList<StrategyConfig> configs,
            int maxTrades,
            int idxTest,
            boolean isMa,
            double balance,
            int maxLostPositionPips,
            double maxDiff,
            int sma,
            double thrSma,
            int minAcceptableDiffFromPeak,
            boolean debug,
            int printSummary,
            int returnMode,
            HashMap<Integer, Integer> dayTotalPips,
            boolean isModeNormal,
            boolean isReverse,
            boolean printDayProfit,
            String outFileName,
            StratPerformance sp
    ) throws IOException {


        sp.reset();
        sp.setInitialBalance(balance);

        double balanceInicial = balance;
        double actualBalance = balance; //actual equitity
        double actualEquitity = balance;
        double maxBalance = balance;
        double actualDD = 0.0;
        double maxDD = 0.0;
        int actualDDPips = 0;
        int maxWinPips = 0;
        int maxLostPips = 0;
        int maxPips = 0;
        //double maxLeverage = 3000;

        ArrayList<PositionCore> positions = new ArrayList<PositionCore>();

        ArrayList<Integer> resPositives = new ArrayList<Integer>();
        ArrayList<Integer> resNegatives = new ArrayList<Integer>();
        for (int i = 0; i <= 31; i++) {
            resPositives.add(0);
            resNegatives.add(0);
        }

        int lastDay = -1;
        int lastDayPips = 0;
        int dayPips = 0;
        int wins = 0;
        int losses = 0;
        int winPips = 0;
        int lostPips = 0;
        double winPips$$ = 0;
        double lostPips$$ = 0;
        int totalDays = 0;
        int totalL = 0;
        int totalLL = 0;
        int totalLLL = 0;
        int totalW = 0;
        int totalWL = 0;
        int totalRiskedPips = 0;
        int maxLeverage = 3000;
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        Calendar calqm = Calendar.getInstance();

        //delimitacion �ltimos 3 mesaes
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
        //seteamos los Ends a la �ltima fecha
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

        QuoteShort qm = new QuoteShort();
        double ma0 = -1;
        double std0 = -1;
        dayTotalPips.clear();
        ArrayList<Integer> days = new ArrayList<Integer>();

        ArrayList<Integer> dayRes = new ArrayList<Integer>();
        ArrayList<Double> dds = new ArrayList<Double>();
        ArrayList<Integer> ddPips = new ArrayList<Integer>();
        ArrayList<Integer> ddWinPips = new ArrayList<Integer>();
        ArrayList<Integer> ddLostPips = new ArrayList<Integer>();
        ArrayList<Double> ddPfs = new ArrayList<Double>();
        ArrayList<Double> dailyChanges = new ArrayList<Double>();
        ArrayList<Double> monthChanges = new ArrayList<Double>();
        double dayDD = 0.0;
        int dayDDPip = 0;
        ArrayList<Integer> ranges = new ArrayList<Integer>();
        int high = -1;
        int low = -1;
        int dayRange = 700;

        int minSizeData = dataBid.size();
        if (dataAsk.size() < minSizeData) minSizeData = dataAsk.size();

        FileWriter myWriter = null;
        //System.out.println(outFileName);
        if (printDayProfit && outFileName.trim().length() > 0) {
            File f = new File(outFileName);
            if (f.exists() && !f.isDirectory()) {
                // do something
                f.delete();
            }
            myWriter = new FileWriter(outFileName);
            //System.out.println(outFileName);
        }

        QuoteShort qi = dataBid.get(100);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        sp.updateMonthEquitity(y * 12 + m, balance);
        int actualMonth = -1;
        int lastMonth = -1;
        int actualDay = -1;
        double dailyMaxLeverage = 0;
        double lastDayEquitity = balance;
        double lastMonthEquity = balance;
        double maxDayDD = 0;
        double dayBalance = balance;

        boolean canTrade = true;
        double thrClose = 4.0;
        int lastH = -1;
        double hourBalance = balance;
        int totalOpenPositions = 0;
        int totalMicrolots = 0;
        int lastValidDay = -1;
        int lastPeakDay = -1;
        int timeFromLastPeak = 0;
        double maxDayEquity = balance;
        double actualDayEquity = balance;
        int totalMonths = 0;
        ArrayList<Integer> arrayPeaks = new ArrayList<Integer>();
        int maxDDPeak = 0;
        //int sma = 120;
        //double thrSma = 0.5;
        ArrayList<Integer> openArr = new ArrayList<Integer>();
        int lastPrice = -1;
        String isPeak = "";
        double maxEquitity = balance;
        for (int i = 100; i < minSizeData - 2; i++) {
            QuoteShort qb1 = dataBid.get(i - 1);
            QuoteShort qb = dataBid.get(i);
            QuoteShort qb_1 = dataBid.get(i + 1);
            QuoteShort qa1 = dataAsk.get(i - 1);
            QuoteShort qa = dataAsk.get(i);
            QuoteShort qa_1 = dataAsk.get(i + 1);
            QuoteShort.getCalendar(cal, qb);
            QuoteShort.getCalendar(cal1, qb1);
            double currentSpread = qa.getOpen5() - qb.getOpen5();

            openArr.add(qb.getOpen5());
            y = cal.get(Calendar.YEAR);
            m = cal.get(Calendar.MONTH);
            int daym = cal.get(Calendar.DAY_OF_MONTH);
            int day = cal.get(Calendar.DAY_OF_YEAR);
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
            long timeInMillis = cal.getTimeInMillis();
            actualMonth = y * 12 + m;
            actualDay = TradingUtils.getActualDay(y, day);

            int actualDayATR = TradingUtils.getActualDay(y, day);
            int atr = atrHash.get(actualDayATR);

            if (timeInMillis < calFrom.getTimeInMillis() || timeInMillis > calTo.getTimeInMillis()) continue;

            if (lastPeakDay < 0) lastPeakDay = actualDay;
            lastValidDay = actualDay;

            if (cal.getTimeInMillis() >= calBegin3m.getTimeInMillis()) {
                if (!isInit) {
                    isInit = true;
                    initialBalance3m = actualBalance;
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

            days.add(qb.getOpen5());
            int smaValue = (int) MathUtils.average(days, days.size() - 1 - sma, days.size() - 1);


            //cada 15 min.. para mas precesion
            int actualCheck = cal.get(Calendar.MINUTE) / 15;
            //actualCheck = actualH;
            if (actualCheck != lastH) {
                if (lastH != -1) {
                    double perProfit = actualEquitity * 100.0 / hourBalance - 100.0;
                    if (printDayProfit) {
                        double totalamountrisked = 0.0;//totalPipsRisk*0.1*totalMicrolots*0.1;
                        double totalRiskPer = 0.0;//amountRisked*100.0/sp.getActualBalance();
                        if (actualEquitity > maxEquitity) {
                            maxEquitity = actualEquitity;
                            isPeak = "PEAK";
                        }
                        String strdp = DateUtils.datePrint(cal1)
                                + ";" + PrintUtils.Print3dec(perProfit, false)
                                + ";" + totalOpenPositions
                                + ";" + totalMicrolots
                                + ";" + PrintUtils.Print2dec(totalRiskPer, false)
                                + ";" + PrintUtils.Print2dec(sp.getActualBalance(), false)
                                + ";" + PrintUtils.Print2dec(actualEquitity, false)
                                + ";" + PrintUtils.Print2dec(maxEquitity, false)
                                + ";" + isPeak
                                //+";"+PrintUtils.Print2dec(dayBalance, false)
                                ;
                        if (myWriter != null)
                            myWriter.write(strdp + '\n');
                    }
                }
                isPeak = "";
                lastH = actualCheck;
                hourBalance = actualEquitity;
            }

            if (day != lastDay) {
                canTrade = true;
                if (lastDay >= 0) {
                    int actualRange = high - low;
                    ranges.add(actualRange);
                    dayRange = (int) MathUtils.average(ranges, ranges.size() - 20, ranges.size() - 1);

                    double equitityChange = actualEquitity - lastDayEquitity;
                    double dayPer = equitityChange * 100.0 / lastDayEquitity;
                    dailyChanges.add(dayPer);

                    if (actualBalance > maxDayEquity && lastPeakDay >= 0) {
                        maxDayEquity = actualBalance;
                        int dayp = actualDay - lastPeakDay;
                        arrayPeaks.add(dayp);
                        lastPeakDay = actualDay;
                        if (dayp >= maxDDPeak) maxDDPeak = dayp;
                        //System.out.println("[***PEAK***] "+DateUtils.datePrintYMD(cal)+" "+dayp +" "+actualBalance);
                    } else {
                        //System.out.println(DateUtils.datePrintYMD(cal)+" "+actualBalance);
                    }
                }

                if (lastDay >= 0 && dayPips != 0) {
                    //System.out.println("pips: "+dayPips+" || "+(winPips-lostPips));

                    if (dailyMaxLeverage >= 15.00) {
						/*System.out.println(DateUtils.datePrint(cal1)
								+" "+PrintUtils.Print2dec(dailyMaxLeverage,false)
								);*/
                    }
                    if (maxDayDD >= thrClose) {
						/*System.out.println(DateUtils.datePrint(cal1)
								+" maxDayDD= "+PrintUtils.Print2dec(maxDayDD,false)
								+" profitDD= "+PrintUtils.Print2dec(actualEquitity*100.0/dayBalance-100.0,false)
								);*/
                    }

                    if (lastDayPips < 0) {
                        if (dayPips < 0) {
                            totalLL++;
                        }
                        totalL++;
                    }

                    if (lastDayPips > 0) {
                        if (dayPips < 0) {
                            totalWL++;
                        }
                        totalW++;
                    }

                    dayRes.add(dayPips);

                    totalDays++;

                    int dayKey = cal.get(Calendar.MONTH) * 31 + cal.get(Calendar.DAY_OF_MONTH);

                    if (!dayTotalPips.containsKey(dayKey)) {
                        dayTotalPips.put(dayKey, dayPips);
                    } else {
                        dayTotalPips.put(dayKey, dayTotalPips.get(dayKey) + dayPips);
                    }

                    lastDayPips = dayPips;

                    double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                    dds.add(dd);
                    dayDD = dd;

                    int ddPip = maxPips - (winPips - lostPips);
                    //int varWins = winPips-lastWinPips;
                    //int varLosses = lostPips -lastLostPips;
                    ddPips.add(ddPip);
                    ddWinPips.add(winPips);
                    ddLostPips.add(lostPips);
                    dayDDPip = ddPip;
                    maxDayDD = 0;
                    dayBalance = actualBalance;
                }
                ma0 = MathUtils.average(days, days.size() - 14 * 288, days.size() - 1);
                std0 = Math.sqrt(MathUtils.variance(days, days.size() - 14 * 288, days.size() - 1));

                high = -1;
                low = -1;
                dayPips = 0;
                lastDay = day;
                dailyMaxLeverage = 0;

                if (actualMonth != lastMonth) {
                    double mchange = actualEquitity * 100.0 / lastMonthEquity - 100.0;
                    monthChanges.add(mchange);
                    lastMonthEquity = actualEquitity;
                    totalMonths++;
                }

                //actualizamos day equitity
                sp.updateDailyEquitity(cal.getTimeInMillis(), actualEquitity);
            }

            for (int t = 0; t < configs.size(); t++) {
                StrategyConfig config = configs.get(t);
                if (config == null) continue;
                if (!config.isEnabled()) continue;
                //System.out.println(config.toString());
                if (config.getHour1() != h) continue;
                if (!canTrade) continue;
                //System.out.println(config.toString());
                //modulo de entrada
                if (positions.size() < maxTrades
                        && dayWeek1 <= dayWeek && dayWeek <= dayWeek2
                        && (h > 0 || min >= 0)
                        //&& currentSpread>=maxDiff
                        && atr >= maxDiff
                        && (min == aMin || aMin < 0)
                ) {
                    if (config != null && config.isEnabled()) {
                        int thr = config.getThr();
                        int begin = i - config.getBarsBack();
                        //begin = i-1;//debug
                        int end = i - 1;

                        //configuracion especial para probar 1 por 1
                        //end = i-config.getBarsBack();

                        int index = TestPriceBuffer.getMinMaxBuff(maxMins, begin, end, thr);
                        double risk = config.getRisk();

                        int HC = qb1.getHigh5() - qb1.getClose5();
                        int CL = qb1.getClose5() - qb1.getLow5();
                        if (index >= 0
                                //&& sizeCandle1<=sizeCandle*10
                                && risk >= 0.1
                        ) {
                            int maxMin = maxMins.get(index);
                            //System.out.println("[INDEX>=0] "+DateUtils.datePrint(cal)+" "+thr+" "+(end-index)+" || "+dataAsk.get(index).toString()+" "+maxMin);


                            double realRisk = risk;
                            //System.out.println(risk);
                            //if (dayDDPip<30000.0) realRisk =0.3;
							/*if (maxTrades*risk>=80){
								realRisk = 80.0/maxTrades;
							}*/
                            int tpPips = 0;
                            int slPips = 0;
                            int tp1 = 0;
                            int sl1 = 0;
                            if (isModeNormal) {
                                tpPips = (int) (config.getTp() * 10);
                                slPips = (int) (config.getSl() * 10);
                                tp1 = (int) (tpPips * 0.1);
                                sl1 = (int) (slPips * 0.1);
                            } else {
                                tpPips = (int) (config.getTpf() * dayRange);
                                slPips = (int) (config.getSlf() * dayRange);
                                tp1 = (int) (tpPips * 0.1);
                                sl1 = (int) (slPips * 0.1);
                            }

                            double maxRisk$$ = realRisk * actualEquitity / 100.0;
                            //maxRisk$$ =realRisk*actualBalance/100.0;
                            double pipValue = maxRisk$$ * 1.0 / config.getSl();//sl en formato pip
                            int miniLots = (int) (pipValue / 0.10);//1 mini lot es $0.10
                            pipValue = miniLots * 0.10;
                            int sizeHL = qb.getOpen5() - dataBid.get(i - 36).getLow5();
                            int sizeLH = dataBid.get(i - 36).getHigh5() - qb.getOpen5();
                            if (pipValue <= 0.10) pipValue = 0.10;//como minimo 0.01 lots
                            if (miniLots < 1) miniLots = 1;

                            //System.out.println(miniLots);

                            if (maxMin >= thr
                                //&& high-qb.getOpen5()>=maxDiff
                                //&& (sma==0 || qb.getOpen5()-smaValue>=thrSma*dayRange)
                                //&& (lastPrice==-1 || qb.getOpen5()>lastPrice)
                            ) {
                                //System.out.println(qb.getOpen5()+" "+smaValue+" "+dayRange);
                                int transactionCosts = TradingUtils.getTransactionCosts(null, y, h, 3);
                                //transactionCosts = 0;

                                PositionCore pos = new PositionCore();
                                pos.setEntry(qb.getOpen5());
                                pos.setEntryIndex(i);
                                pos.setMaxIndex(i + config.getMaxBars());
                                pos.setPositionStatus(PositionStatus.OPEN);

                                if (isReverse) {
                                    pos.setTp(qb.getOpen5() - tpPips);
                                    pos.setSl(qb.getOpen5() + slPips);
                                    pos.setPositionType(PositionType.SHORT);
                                } else {
                                    pos.setTp(qb.getOpen5() + slPips);
                                    pos.setSl(qb.getOpen5() - tpPips);
                                    pos.setPositionType(PositionType.LONG);
                                }

                                pos.setIndexMinMax(end - index);
                                pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                                totalRiskedPips += sl1;
                                //pipValue
                                pos.setPipValue(pipValue);
                                pos.setMicroLots(miniLots);
                                pos.setTransactionCosts(transactionCosts);
                                if (debug)
                                    System.out.println("[SHORT] " + DateUtils.datePrint(cal) + " " + PrintUtils.Print2dec(actualEquitity, false)
                                            + " " + PrintUtils.Print2dec(maxRisk$$, false) + " " + miniLots
                                            + " || " + pos.getEntry() + " " + pos.getTp() + " " + pos.getSl()
                                            + " || " + tpPips + " " + slPips
                                            + " ||| " + qb1.toString() + " | " + qb.toString()
                                    );
                                long totalMicroLots = TradingUtils.getOpenSize2(positions);
                                double leverage = (totalMicroLots + miniLots) * 1000.0 / actualEquitity;
                                if (leverage > dailyMaxLeverage) dailyMaxLeverage = leverage;

                                if (leverage < maxLeverage) positions.add(pos);
                                lastPrice = qa.getOpen5();
                            } else if (maxMin <= -thr
                                //&& (sma==0 || smaValue-qa.getOpen5()>=thrSma*dayRange)
                                //&& (lastPrice==-1 || qa.getOpen5()<lastPrice)
                            ) {
                                int transactionCosts = TradingUtils.getTransactionCosts(null, y, h, 3);
                                //transactionCosts = 0;

                                //System.out.println("[OPEN LONG] "+q.toString()+" || "+actualEquitity+" "+pipValue);
                                PositionCore pos = new PositionCore();
                                pos.setEntry(qa.getOpen5());
                                pos.setEntryIndex(i);
                                pos.setMaxIndex(i + config.getMaxBars());
                                pos.setPositionStatus(PositionStatus.OPEN);
                                pos.setIndexMinMax(end - index);
                                pos.getOpenCal().setTimeInMillis(cal.getTimeInMillis());
                                totalRiskedPips += sl1;
                                //pipValue
                                pos.setPipValue(pipValue);
                                pos.setMicroLots(miniLots);
                                pos.setTransactionCosts(transactionCosts);

                                if (isReverse) {
                                    pos.setTp(qa.getOpen5() + tpPips);
                                    pos.setSl(qa.getOpen5() - slPips);
                                    pos.setPositionType(PositionType.LONG);
                                } else {
                                    pos.setTp(qb.getOpen5() - slPips);
                                    pos.setSl(qb.getOpen5() + tpPips);
                                    pos.setPositionType(PositionType.SHORT);
                                }

                                if (debug)
                                    System.out.println("[LONG] " + DateUtils.datePrint(cal) + " " + PrintUtils.Print2dec(actualEquitity, false) + " " + PrintUtils.Print2dec(maxRisk$$, false) + " " + miniLots
                                            + " || " + pos.getEntry() + " " + pos.getTp() + " " + pos.getSl()
                                            + " || " + tpPips + " " + slPips
                                    );
                                long totalMicroLots = TradingUtils.getOpenSize2(positions);
                                double leverage = (totalMicroLots + miniLots) * 1000.0 / actualEquitity;
                                if (leverage > dailyMaxLeverage) dailyMaxLeverage = leverage;
                                //System.out.println((totalMicroLots+miniLots)+" || "+);
                                if (leverage < maxLeverage) positions.add(pos);
                                lastPrice = qa.getOpen5();
                            }
                        }

                    }
                }
            }//configs

            //evaluacion trades
            int j = 0;
            actualEquitity = actualBalance;
            totalMicrolots = 0;
            sp.updateDayOpenPositions(actualDay, positions.size(), maxTrades);
            while (j < positions.size()) {
                PositionCore pos = positions.get(j);
                boolean isClosed = false;
                int pips = 0;
                int closedMode = 0;
                boolean isSLTP = true;
                if (pos.getPositionType() == PositionType.SHORT) {

                    if (qa.getLow5() < pos.getBestPrice() || pos.getBestPrice() < 0) pos.setBestPrice(qa.getLow5());
                    if (qa.getHigh5() > pos.getWorstPrice() || pos.getWorstPrice() < 0)
                        pos.setWorstPrice(qa.getHigh5());

                    if (i >= pos.getMaxIndex()
                        //|| (dayWeek==Calendar.FRIDAY && (h>=hf))
                    ) {
                        closedMode = 1;
                        isClosed = true;
                        pips = pos.getEntry() - qa_1.getOpen5();
                        isSLTP = false;
                    }
                    if (!isClosed) {
                        if (qa.getHigh5() >= pos.getSl()) {
                            //if (q_1.getOpen5()>=pos.getSl()){
                            isClosed = true;
                            pips = pos.getEntry() - pos.getSl();
                            closedMode = 2;
                        } else if (qa.getLow5() <= pos.getTp()) {
                            //}else if (q_1.getOpen5()<=pos.getTp()){
                            isClosed = true;
                            pips = pos.getEntry() - pos.getTp();
                            closedMode = 3;
                        }
                    }
                    if (isClosed) {
                        pips = pos.getEntry() - qa_1.getOpen5();
                        if (closedMode == 2) {
                            pips = pos.getEntry() - pos.getSl();
                        } else if (closedMode == 3) {
                            pips = pos.getEntry() - pos.getTp();
                        }
                    } else {
                        pips = pos.getEntry() - qa_1.getOpen5();
                    }
                } else if (pos.getPositionType() == PositionType.LONG) {

                    if (qa.getHigh5() > pos.getBestPrice() || pos.getBestPrice() < 0) pos.setBestPrice(qa.getHigh5());
                    if (qa.getLow5() < pos.getWorstPrice() || pos.getWorstPrice() < 0) pos.setWorstPrice(qa.getLow5());

                    if (i >= pos.getMaxIndex()
                        //|| (dayWeek==Calendar.FRIDAY && (h>=hf))
                    ) {
                        closedMode = 1;
                        isClosed = true;
                        pips = qb_1.getOpen5() - pos.getEntry();
                        isSLTP = false;
                    }

                    if (!isClosed) {
                        if (qb.getLow5() <= pos.getSl()) {
                            //if (q_1.getOpen5()<=pos.getSl()){
                            isClosed = true;
                            pips = pos.getSl() - pos.getEntry();
                            closedMode = 2;
                        } else if (qb.getHigh5() >= pos.getTp()) {
                            //}else if (q_1.getOpen5()>=pos.getTp()){
                            isClosed = true;
                            pips = pos.getTp() - pos.getEntry();
                            closedMode = 3;
                        }
                    }
                    if (isClosed) {
                        pips = qb_1.getOpen5() - pos.getEntry();
                        if (closedMode == 2) {
                            pips = pos.getSl() - pos.getEntry();
                        } else if (closedMode == 3) {
                            pips = pos.getTp() - pos.getEntry();
                        }
                    } else {
                        pips = qb_1.getOpen5() - pos.getEntry();
                    }
                }
                //actualizacion equitity
                actualEquitity = actualEquitity + (pips - pos.getTransactionCosts()) * 0.1 * pos.getMicroLots() * 0.1;

                if (isClosed) {
                    if (idxTest == -1 || pos.getIndexMinMax() == idxTest) {

                        sp.addTrade((long) pos.getMicroLots(), pips, Math.abs(pos.getEntry() - pos.getTp()),
                                Math.abs(pos.getEntry() - pos.getTp()), pos.getTransactionCosts(), cal, isSLTP);
                        pips -= pos.getTransactionCosts();

                        if (pips >= 0) {
                            wins++;
                            winPips += pips;
                            winPips$$ += pips * 0.1 * pos.getMicroLots() * 0.1;//1 microLot = 0.1$/pip
                        } else {
                            losses++;
                            lostPips += -pips;
                            lostPips$$ += -pips * 0.1 * pos.getMicroLots() * 0.1;
                            //System.out.println("[LOSS 2] "+-pips+" "+pos.getMicroLots()+" || "+pos.toString()+" || "+q_1.toString()+" || "+pos.getSl()+" || "+closedMode);
                        }
                        //System.out.println("pips "+" "+pips+" || "+winPips+" "+lostPips);
                        dayPips += pips;

                        actualBalance += pips * 0.1 * pos.getMicroLots() * 0.1;

                        double res$$ = pips * 0.1 * pos.getMicroLots() * 0.1;
                        if (pips >= 0) {
                            resPositives.set(daym, pips);
                        } else {
                            resNegatives.set(daym, -pips);
                        }

                        //if (actualBalance<=0) break;

                        if (actualBalance >= maxBalance) {
                            maxBalance = actualBalance;
                            //lastPeakDay = actualDay;
                        } else {
                            double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                            if (dd >= maxDD) {
                                maxDD = dd;
                            }
                        }

                        if (debug) {
                            String str = "WIN";
                            if (pips < 0) str = "LOSS";

                            System.out.println("[CLOSED " + str + "] " + DateUtils.datePrint(cal)
                                    + " || " + closedMode + " || " + pips
                                    //+" || "+q_1.toString()
                                    //+" || open="+q_1.getOpen()+" tp="+pos.getTp()
                                    + " || " + pos.getMicroLots() + " " + PrintUtils.Print2dec(actualBalance, false)
                                    + " || " + pos.toString()
                                    + " || " + winPips + " " + lostPips
                                    + " || " + PrintUtils.Print2dec(winPips$$, false) + " " + PrintUtils.Print2dec(lostPips$$, false)
                                    + " || " + PrintUtils.Print2dec(maxDD, false)
                            );
                        }

                        //para debug
                    }
                    positions.remove(j);
                } else {
                    totalMicrolots += pos.getMicroLots();
                    j++;
                }

                if (winPips - lostPips >= maxPips) {
                    maxWinPips = winPips;
                    maxLostPips = lostPips;
                    maxPips = winPips - lostPips;
                }
            }

            if (positions.size() == 0) lastPrice = -1;
            //actualizamod DD diario
            if (actualEquitity < dayBalance) {
                double actualDayDD = 100.0 - actualEquitity * 100.0 / dayBalance;
                //System.out.println(actualEquitity +" "+dayBalance+" "+actualDayDD);
                if (actualDayDD > maxDayDD) {
                    maxDayDD = actualDayDD;
                    if (actualDayDD >= thrClose) {
                        //cerramos todo
						/*System.out.println(
								DateUtils.datePrint(cal) +" NO SE PUEDE TRADER "+
								" "+positions.size()
								+" "+actualEquitity
								+" "+dayBalance+" "+actualDayDD);*/
                        canTrade = false;
                        j = 0;
                        actualEquitity = actualBalance;
                        sp.updateDayOpenPositions(actualDay, positions.size(), maxTrades);
                        while (j < positions.size()) {
                            PositionCore pos = positions.get(j);
                            boolean isClosed = true;
                            int pips = 0;
                            int closedMode = 3;
                            boolean isSLTP = true;
                            if (pos.getPositionType() == PositionType.SHORT) {
                                pips = pos.getEntry() - qa_1.getOpen5();
                            } else if (pos.getPositionType() == PositionType.LONG) {
                                pips = qb_1.getOpen5() - pos.getEntry();
                            }
                            //actualizacion equitity
                            actualEquitity = actualEquitity + (pips - pos.getTransactionCosts()) * 0.1 * pos.getMicroLots() * 0.1;
                            if (isClosed) {
                                sp.addTrade((long) pos.getMicroLots(), pips, Math.abs(pos.getEntry() - pos.getTp()),
                                        Math.abs(pos.getEntry() - pos.getTp()), pos.getTransactionCosts(), cal, isSLTP);
                                pips -= pos.getTransactionCosts();

                                if (pips >= 0) {
                                    wins++;
                                    winPips += pips;
                                    winPips$$ += pips * 0.1 * pos.getMicroLots() * 0.1;//1 microLot = 0.1$/pip
                                } else {
                                    losses++;
                                    lostPips += -pips;
                                    lostPips$$ += -pips * 0.1 * pos.getMicroLots() * 0.1;
                                    //System.out.println("[LOSS 2] "+-pips+" "+pos.getMicroLots()+" || "+pos.toString()+" || "+q_1.toString()+" || "+pos.getSl()+" || "+closedMode);
                                }
                                //System.out.println("pips "+" "+pips+" || "+winPips+" "+lostPips);
                                dayPips += pips;

                                actualBalance += pips * 0.1 * pos.getMicroLots() * 0.1;

                                if (actualBalance >= maxBalance) {
                                    maxBalance = actualBalance;
                                    int diffPK = actualDay - lastPeakDay;
                                    //lastPeakDay = actualDay;
                                } else {
                                    double dd = 100.0 - actualBalance * 100.0 / maxBalance;
                                    if (dd >= maxDD) {
                                        maxDD = dd;
                                    }
                                }

                                if (debug) {
                                    String str = "WIN";
                                    if (pips < 0) str = "LOSS";

                                    System.out.println("[CLOSED " + str + "] " + DateUtils.datePrint(cal)
                                            + " || " + closedMode + " || " + pips
                                            //+" || "+q_1.toString()
                                            //+" || open="+q_1.getOpen()+" tp="+pos.getTp()
                                            + " || " + pos.getMicroLots() + " " + PrintUtils.Print2dec(actualBalance, false)
                                            + " || " + pos.toString()
                                            + " || " + winPips + " " + lostPips
                                            + " || " + PrintUtils.Print2dec(winPips$$, false) + " " + PrintUtils.Print2dec(lostPips$$, false)
                                            + " || " + PrintUtils.Print2dec(maxDD, false)
                                    );
                                }
                                positions.remove(j);
                            } else { //isClosed
                                j++;
                            }
                        }//while
                        //System.out.println("total positions: "+positions.size());
                    }//actualDD<=max
                }
            }//actualEquitity<dayBalance
            actualEquitity = actualBalance;//porque se han cerrado todas..
            if (actualEquitity < dayBalance) {
                double actualDayDD = 100.0 - actualEquitity * 100.0 / dayBalance;
                //System.out.println(actualEquitity +" "+dayBalance+" "+actualDayDD);
                if (actualDayDD > maxDayDD) {
                    maxDayDD = actualDayDD;
                    //cerramos todo
					/*System.out.println(
							DateUtils.datePrint(cal) +" NO SE PUEDE TRADER 2"+
							" "+positions.size()
							+" "+actualEquitity
							+" "+dayBalance+" "+actualDayDD);*/
                }
            }

            //ACTUALIZAMOS EQUITITY MENSUAL
            sp.updateMonthEquitity(actualMonth, actualEquitity);

            if (high == -1 || qb.getHigh5() >= high) high = qb.getHigh5();
            if (low == -1 || qb.getLow5() <= low) low = qb.getLow5();
        }//data

        totalLL = 0;
        totalLLL = 0;
        int totalLLLL = 0;
        for (int i = 0; i < dayRes.size(); i++) {
            int pips = dayRes.get(i);

            if (i >= 3) {
                if (dayRes.get(i - 1) < 0
                        && dayRes.get(i - 2) < 0
                        && dayRes.get(i - 3) < 0
                ) {
                    totalLLL++;
                    if (dayRes.get(i) < 0) {
                        totalLLLL++;
                    }
                }
				/*if (dayRes.get(i-1)<0 && dayRes.get(i-2)<0){
					totalLL++;
					if (dayRes.get(i)<0){
						totalLLL++;
					}
				}*/
            }
        }


        for (int af = 0; af <= 40000; af += 1000) {
            int count = 0;
            double acc = 0;
            double accPf = 0;
            int accPfw = 0;
            int accPfl = 0;
            for (int i = 0; i < ddPips.size(); i++) {
                int ddi = ddPips.get(i);
                int wp = ddWinPips.get(i);
                int lp = ddLostPips.get(i);
                if (ddi >= af) {
                    //System.out.println(PrintUtils.Print2dec(acc*1.0/count, false));
                    int j = i + 30;
                    if (j <= ddPips.size() - 1) {
                        //System.out.println(PrintUtils.Print2dec(dds.get(j)-ddi, false));
                        count++;
                        acc += ddPips.get(j) - ddi;
                        accPf += (ddWinPips.get(j) - wp) - (ddLostPips.get(j) - lp);
                        accPfw += (ddWinPips.get(j) - wp);
                        accPfl += (ddLostPips.get(j) - lp);
                    }
                }
            }

			/*System.out.println(PrintUtils.Print2dec(af, false)
					+";"+count
					//+";"+PrintUtils.Print2dec(acc/count, false)
					//+";"+PrintUtils.Print2dec(accPf/count, false)
					+";"+PrintUtils.Print2dec(accPfw*1.0/accPfl, false)
					);*/
        }

        double perLL = totalLL * 100.0 / totalL;
        double perLLL = totalLLL * 100.0 / totalLL;
        double perLLLL = totalLLLL * 100.0 / totalLLL;
        double perWL = totalWL * 100.0 / totalW;
		/*System.out.println(totalL
				+" "+PrintUtils.Print2dec(perLL, false)
				+" || "+totalW+" "+PrintUtils.Print2dec(perWL, false)
		);*/

        int trades = wins + losses;
        double winPer = wins * 100.0 / trades;
        double pf = winPips * 1.0 / lostPips;
        double pf$$ = winPips$$ * 1.0 / lostPips$$;
        double avg = (winPips - lostPips) * 0.1 / trades;

        double perWin = actualBalance * 100.0 / balance - 100.0;
        double perMaxWin = maxBalance * 100.0 / balance - 100.0;
        double actualBalance30 = actualBalance / (maxDD / 30.0); //balance con max 30%
        double yield = (winPips - lostPips) * 0.1 * 100 / totalRiskedPips;

        //int totalA�os = y2-y1+1;

        //double tae = 100.0*(Math.pow(actualBalance/(balanceInicial), 1.0/totalA�os)-1);
        //double taeFactor = tae/maxDD;


        double avgWin = winPips * 0.1 / wins;
        double avgLoss = lostPips * 0.1 / losses;
        double dAvg = sp.getDailyAvg();
        double dDT = sp.getDailyDt();
        int diffFromPeak = lastValidDay - lastPeakDay;
        double avgPeaks = MathUtils.average(arrayPeaks);
        double avgm = MathUtils.average(monthChanges);
        double dtm = Math.sqrt(MathUtils.variance(monthChanges));
        double sharpe = (avgm / dtm) * Math.sqrt(12.0);
        //sharpe = (dAvg/dDT)*Math.sqrt(252);
        double diff3m = finalBalance3m * 100.0 / initialBalance3m - 100.0;
        double diff6m = finalBalance6m * 100.0 / initialBalance6m - 100.0;
        double diff9m = finalBalance9m * 100.0 / initialBalance9m - 100.0;
        if (diffFromPeak >= maxDDPeak) maxDDPeak = diffFromPeak;

        sp.setMaxDD(maxDD);
        //double var95 = sp.getMonthDataDDRR(sp.getInitialBalance(),risk,2);
        if (printSummary == 2) {
            sp.getDayReturns();
        }

        if (sp.getTrades() > 0) sp.setSharpeRatio(sharpe);
        if (printSummary == 1) {
            if (maxDD < 3000.0
                    && diffFromPeak <= minAcceptableDiffFromPeak
                    && sp.getTrades() > 0
            )
                System.out.println(
                        header
                                + " || "
                                + " || sharpe=" + PrintUtils.Print2dec(sharpe, false)
                                + " || %Profit=" + PrintUtils.leftpad(PrintUtils.Print2dec(actualBalance * 100.0 / balance - 100.0, false), 6)
                                + " || MaxDD=" + PrintUtils.leftpad(PrintUtils.Print2dec(maxDD, false), 6)
                                + " || M3=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff3m, false), 6)
                                + " M6=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff6m, false), 6)
                                + " M9=" + PrintUtils.leftpad(PrintUtils.Print2dec(diff9m, false), 6)
                                + " || avgToPeak= " + PrintUtils.Print2dec(avgPeaks, false) + " maxDDPeak= " + maxDDPeak
                                + " || TimeFromPeak= " + diffFromPeak
                                + " || " + trades
                                + " " + PrintUtils.Print2dec(winPer, false)
                                + " " + PrintUtils.Print2dec(pf, false)
                                + " " + PrintUtils.Print2dec(pf$$, false)
                                + " " + winPips + " " + lostPips
                                + " " + PrintUtils.Print2dec(winPips$$, false)
                                + " " + PrintUtils.Print2dec(lostPips$$, false)
                                + "|| " + PrintUtils.Print2dec(avg, false)
                                + " " + PrintUtils.Print2dec(avgWin, false)
                                + " " + PrintUtils.Print2dec(avgLoss, false)
                                + " " + PrintUtils.Print2dec(yield, false)
                                + " || "
                                + " " + PrintUtils.Print2dec2(actualBalance, true)
                                + " " + PrintUtils.Print2dec2(maxBalance, true)

                                //+" || VAR95= "+PrintUtils.Print2dec(var95, false)
                                + " || Factor=" + PrintUtils.Print2dec(perMaxWin / maxDD, false)
                                //+" || "+PrintUtils.Print2dec(taeFactor, false)
                                //+" || "+sp.maxDDStats(20)
                                //+" || "+sp.maxDDStats(40)
                                + " || " + PrintUtils.Print2dec(dAvg + dDT, true)
                                + " " + PrintUtils.Print2dec(dAvg, true)
                                + " " + PrintUtils.Print2dec(dDT, true)
                                + " || " + PrintUtils.Print2dec(sp.getPerBelow(-4.0), true)
                );
        }

		/*for (int i=1;i<=31;i++) {
			double pfd = resPositives.get(i)*1.0/resNegatives.get(i);
			System.out.println(i+" || "+PrintUtils.Print2dec(pfd,false));
		}*/

        if (maxDD >= 100.0) return 0.0;

        //return actualBalance;
        return pf$$;
    }

    public static void main(String[] args) throws Exception {

        //Path resourceDirectory = Paths.get("data");
        //String path0 = resourceDirectory.toFile().getAbsolutePath() + "\\";
        String path0 = "data" + File.separator;
        Path pathP = Paths.get(path0);
        if (!Files.exists(pathP)) {
            path0 = "c:\\fxdata\\";
        }
        String currency = "eurusd";
        String pathBid = path0 + currency + "_5 Mins_Bid.csv";
        String pathAsk = path0 + currency + "_5 Mins_Ask.csv";
        String pathSpread = path0 + currency + "_spreads_2009_2019.csv";

        System.out.println(currency);

        ArrayList<String> paths = new ArrayList<String>();
        paths.add(pathBid);
        //paths.add(pathEURAUD);paths.add(pathNZDUSD);

        int total = 0;
        ArrayList<Double> pfs = new ArrayList<Double>();
        ArrayList<Double> pf2016s = new ArrayList<Double>();
        ArrayList<Double> pfNoSLArr = new ArrayList<Double>();
        ArrayList<Double> factors = new ArrayList<Double>();
        ArrayList<Double> factors16 = new ArrayList<Double>();
        ArrayList<Double> month10s = new ArrayList<Double>();
        ArrayList<Double> month10_2016s = new ArrayList<Double>();
        ArrayList<Integer> totalTradesArr = new ArrayList<Integer>();
        ArrayList<Integer> tradess = new ArrayList<Integer>();
        ArrayList<Integer> trades_2016s = new ArrayList<Integer>();
        ArrayList<Integer> maxTradesArr = new ArrayList<Integer>();
        ArrayList<Double> profitPerArr = new ArrayList<Double>();
        ArrayList<Double> profitPer16Arr = new ArrayList<Double>();
        ArrayList<Double> srH = new ArrayList<Double>();
        int limit = paths.size() - 1;
        limit = 0;
        String provider = "";
        Sizeof.runGC();
        ArrayList<QuoteShort> dataI = null;
        ArrayList<QuoteShort> dataS = null;
        Calendar calFrom = Calendar.getInstance();
        Calendar calTo = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        //maxima fecha es febrero
        calMax.set(2023, Calendar.SEPTEMBER, 30);
        for (int i = 0; i <= 0; i++) {
            ArrayList<QuoteShort> dataBid = null;
            ArrayList<QuoteShort> dataAsk = null;
            ArrayList<QuoteShort> dataNoise = null;

            dataI = DAO.retrieveDataShort5m(pathBid, DataProvider.DUKASCOPY_FOREX4);
            TestLines.calculateCalendarAdjustedSinside(dataI);
            dataS = TradingUtils.cleanWeekendDataS(dataI);
            dataBid = dataS;

            dataI = DAO.retrieveDataShort5m(pathAsk, DataProvider.DUKASCOPY_FOREX4);
            TestLines.calculateCalendarAdjustedSinside(dataI);
            dataS = TradingUtils.cleanWeekendDataS(dataI);
            dataAsk = dataS;

            //LOS AJUSTES AQUI
            for (int noisePips = 0; noisePips <= 0; noisePips++) {
                //dataNoise = TradingUtils.addNoise(data,0,23,noisePips);

                ArrayList<Integer> maxMins = TradingUtils.calculateMaxMinByBarShortAbsoluteInt(dataBid);
                HashMap<Integer, Integer> atrHash = TradingUtils.calculateATR(dataBid, 20);
                HashMap<Integer, ArrayList<Double>> spreads = new HashMap<Integer, ArrayList<Double>>();
                //DAO.readSpreads(pathSpread,2009,2019,spreads);

                ArrayList<StrategyConfig> configs = new ArrayList<StrategyConfig>();
                //for (int c=0;c<=23;c++) configs.add(null);

                //DMO SETTINGS
                //2012-2016 TP SL NORMAL

                //EURUSD

                //nueva configuracion para 02/03/2020, configuraci�n para max 16 trades
				/*StrategyConfig config = new StrategyConfig();config.setParams(0,0,96,55,90,150,6,4, true);configs.add(config);//12
				StrategyConfig config1 = new StrategyConfig();config1.setParams(1,1,146,115,50,132,12,1, true);configs.add(config1);//12
				StrategyConfig config2 = new StrategyConfig();config2.setParams(2,2,218,15,55,204,6,2, true);configs.add(config2);//6
				StrategyConfig config3 = new StrategyConfig();config3.setParams(3,3,176,30,70,90,1,3, true);configs.add(config3);
				StrategyConfig config4 = new StrategyConfig();config4.setParams(4,4,688,20,50,90,1,1, false);configs.add(config4);
				StrategyConfig config5 = new StrategyConfig();config5.setParams(5,5,196,15,50,72,3,2, true);configs.add(config5);
				StrategyConfig config6 = new StrategyConfig();config6.setParams(6,6,196,15,60,54,6,3, true);configs.add(config6);
				StrategyConfig config7 = new StrategyConfig();config7.setParams(7,7,208,25,75,44,2,3, true);configs.add(config7);
				StrategyConfig config8 = new StrategyConfig();config8.setParams(8,8,500,35,55,15,1,1, false);configs.add(config8);
			    StrategyConfig config9 = new StrategyConfig();config9.setParams(9,9,504,20,37,20,1,1, true);configs.add(config9);
				StrategyConfig config23 = new StrategyConfig();config23.setParams(23,23,86,7,80,156,6,4, true);configs.add(config23);
				*/
                //configuracion axi + mnuevo

				/*StrategyConfig config = new StrategyConfig();config.setParams(0,0,120,42,80,139,6,2, true);configs.add(config);//12
				StrategyConfig config1 = new StrategyConfig();config1.setParams(1,1,192,9,55,155,9,2, true);configs.add(config1);//12
				StrategyConfig config2 = new StrategyConfig();config2.setParams(2,2,214,15,70,132,6,3, true);configs.add(config2);//6
				StrategyConfig config3 = new StrategyConfig();config3.setParams(3,3,176,30,70,90,1,4, true);configs.add(config3);
				StrategyConfig config4 = new StrategyConfig();config4.setParams(4,4,1200,9,55,77,1,5, true);configs.add(config4);
				StrategyConfig config5 = new StrategyConfig();config5.setParams(5,5,480,28,35,81,3,1, true);configs.add(config5);
				StrategyConfig config6 = new StrategyConfig();config6.setParams(6,6,468,92,90,60,5,4, true);configs.add(config6);
				StrategyConfig config7 = new StrategyConfig();config7.setParams(7,7,210,18,85,44,2,2, true);configs.add(config7);
				StrategyConfig config8 = new StrategyConfig();config8.setParams(8,8,500,35,55,15,1,1, false);configs.add(config8);
			    StrategyConfig config9 = new StrategyConfig();config9.setParams(9,9,500,32,35,19,1,1, true);configs.add(config9);
				StrategyConfig config23 = new StrategyConfig();config23.setParams(23,23,154,7,70,139,6,1, true);configs.add(config23);*/

                //NUEVAS CONFIGS PARA 2021
                StrategyConfig config = new StrategyConfig();
                config.setParams(0, 0, 132, 75, 85, 144, 4, 2, true);//r
                configs.add(config);//12
                StrategyConfig config1 = new StrategyConfig();
                config1.setParams(1, 1, 168, 70, 90, 186, 11, 1, true);//r
                configs.add(config1);//12
                StrategyConfig config2 = new StrategyConfig();
                config2.setParams(2, 2, 492, 10, 80, 156, 5, 6, true);//r
                configs.add(config2);//6
                StrategyConfig config3 = new StrategyConfig();
                config3.setParams(3, 3, 480, 15, 60, 102, 2, 4, true);//r
                configs.add(config3);
                StrategyConfig config4 = new StrategyConfig();
                config4.setParams(4, 4, 444, 16, 60, 78, 12, 3, true);//r
                configs.add(config4);
                StrategyConfig config5 = new StrategyConfig();
                config5.setParams(5, 5, 576, 11, 75, 54, 6, 2, true);//r
                configs.add(config5);
                StrategyConfig config6 = new StrategyConfig();
                config6.setParams(6, 6, 540, 30, 40, 60, 6, 4, true);//r
                configs.add(config6);
                StrategyConfig config7 = new StrategyConfig();
                config7.setParams(7, 7, 264, 6, 35, 42, 19, 3, true);//r
                configs.add(config7);
                StrategyConfig config8 = new StrategyConfig();
                config8.setParams(8, 8, 156, 9, 30, 132, 1, 1, true);//r
                configs.add(config8);
                StrategyConfig config9 = new StrategyConfig();
                config9.setParams(9, 9, 600, 20, 40, 18, 2, 1, false);//r no investible
                configs.add(config9);
                StrategyConfig config23 = new StrategyConfig();
                config23.setParams(23, 23, 588, 7, 70, 72, 4, 4, true);
                configs.add(config23);

                HashMap<Integer, Integer> dayTotalPips = new HashMap<Integer, Integer>();

                int maximunTrades = 32;
                double maximunRisk = 1.39;


                ArrayList<StratPerformance> rankingList = new ArrayList<StratPerformance>();
                int printSummary = 1;
                int timeframe = 0;//1=dia 0 = mes
                int periodMult = 12000000;
                int periodLen = 12000000;
                int yearStart = 2012;
                int yearEnd = 2023;
                int minAcceptableDiffFromPeak = 7000;
                printSummary = 1;
                boolean printdayprofit = true;
                boolean printModeRanking = false;
                boolean isTrialsMode = false;
                if (yearEnd != 2023) {
                    calMax.set(yearEnd, 11, 31);
                }
                System.out.println("** TESTPRICEBUFFER YEAR START: " + yearStart + " " + yearEnd + " || n�strats= " + configs.size());
                for (int h = -1; h <= -1; h++) {//-1 para testear todas
                    if (h >= 0) {
                        int ht = h;
                        if (ht == 10) ht = 23;
                        System.out.println("**testeando H=" + h);
                        for (int h0 = 0; h0 <= configs.size() - 1; h0++) {
                            if (configs.get(h0) != null) {
                                if (configs.get(h0).getHour1() != ht) configs.get(h0).setEnabled(false);
                            }
                        }
                    } else {
                        h = 0;
                    }
                    //if (h>=0 && configs.get(h)!=null)
                    //configs.get(h).setEnabled(false);
                    int totalTrials = 0;
                    int totalPosiTiveTrials = 0;
                    srH.clear();
                    for (int min = -1; min <= -1; min += 5)
                        for (int thr = 168; thr <= 168; thr += 12) {
                            for (int tp = 70; tp <= 70; tp += 1) {
                                for (int sl = 90; sl <= 90; sl += 5) {
                                    for (double risk = 0.1; risk <= 0.1; risk += 0.1) {
                                        for (int maxBars = 186; maxBars <= 186; maxBars += 6) {//35
                                            //totalTrials++;
                                            //System.out.println("*****test..."+totalTrials);
                                            for (int barsBack = 11; barsBack <= 11; barsBack++) {
                                                for (int maxH = 0; maxH <= 0; maxH += 1) {
                                                    for (int lostPips = 8000; lostPips <= 8000; lostPips += 50) {
                                                        if (configs.get(h) != null) {
                                                            configs.get(h).setEnabled(true);
                                                            for (int h01 = h; h01 <= h; h01++) {
                                                                int ht = h01;
                                                                //if (ht>=11) configs.get(ht).setEnabled(false);
                                                                //if (h01==10) ht=23;
                                                                //configs.get(h).setEnabled(true);

                                                                //configs.get(ht).setThr(thr);
                                                                //configs.get(ht).setTp(tp);
                                                                //configs.get(ht).setSl(sl);
                                                                //configs.get(ht).setMaxBars(maxBars);
                                                                //configs.get(ht).setBarsBack(barsBack);
                                                                //configs.get(ht).setRisk(risk);
                                                            }
                                                        }
                                                        for (int sma = 0; sma <= 0; sma += 12) {
                                                            for (double thrSma = 0.10; thrSma <= 0.10; thrSma += 0.1) {
                                                                for (int maxTrades = 100; maxTrades <= 100; maxTrades += 5) {//17 7 2.25
                                                                    for (double maxVar = 10; maxVar <= 10; maxVar++) {
                                                                        //System.out.println("**testeando MaxTRades="+maxTrades);
                                                                        StratPerformance sp = new StratPerformance();
                                                                        //para testear un riesgo major
                                                                        for (double maxDiff = 00.00; maxDiff <= 00.0; maxDiff += 50) {
                                                                            for (double comm = 0; comm <= 0; comm += 0.1) {
                                                                                for (int monthTest = -1; monthTest <= -1; monthTest++)
                                                                                    for (int dayWeek1 = Calendar.MONDAY + 0; dayWeek1 <= Calendar.MONDAY + 0; dayWeek1++) {
                                                                                        int dayWeek2 = dayWeek1 + 4;
                                                                                        String header = maxTrades + " " + PrintUtils.Print2dec(risk, false) + " || " + configs.get(h).toString();
                                                                                        int year2016 = 2019;
                                                                                        int totalPositives = 0;
                                                                                        int total2016b = 0;
                                                                                        int total2016 = 0;
                                                                                        int total2016Positives = 0;
                                                                                        double accProfit = 0;
                                                                                        double balance = 2500;
                                                                                        double accYear = 0;
                                                                                        int totalY = 0;
                                                                                        int totalPeriods = 0;
                                                                                        int totalPositivePeriods = 0;
                                                                                        ArrayList<Double> maxDDs = new ArrayList<Double>();
                                                                                        month10s.clear();
                                                                                        month10_2016s.clear();
                                                                                        pfs.clear();
                                                                                        pfNoSLArr.clear();
                                                                                        pf2016s.clear();
                                                                                        factors.clear();
                                                                                        factors16.clear();
                                                                                        maxTradesArr.clear();
                                                                                        trades_2016s.clear();
                                                                                        tradess.clear();
                                                                                        profitPerArr.clear();
                                                                                        profitPer16Arr.clear();
                                                                                        totalTradesArr.clear();
                                                                                        srH.clear();
                                                                                        boolean lastTry = false;
                                                                                        for (int periods = 0; periods <= 10000; periods += 1) {
                                                                                            calFrom.set(yearStart, 00, 01);
                                                                                            if (timeframe == 0) {
                                                                                                calFrom.add(Calendar.MONTH, periods * periodMult);
                                                                                                calTo.setTimeInMillis(calFrom.getTimeInMillis());
                                                                                                calTo.add(Calendar.MONTH, periodLen);
                                                                                                calTo.add(Calendar.DAY_OF_MONTH, -1);
                                                                                            } else if (timeframe == 1) {
                                                                                                calFrom.add(Calendar.DAY_OF_YEAR, periods * periodMult);
                                                                                                calTo.setTimeInMillis(calFrom.getTimeInMillis());
                                                                                                calTo.add(Calendar.DAY_OF_YEAR, periodLen);
                                                                                            }

                                                                                            int toYear = calTo.get(Calendar.YEAR);

                                                                                            if (calTo.getTimeInMillis() > calMax.getTimeInMillis()) {
                                                                                                if (!lastTry)
                                                                                                    calTo.setTimeInMillis(calMax.getTimeInMillis());
                                                                                                else break;
                                                                                                lastTry = true;
                                                                                            }
                                                                                            //testear mes
                                                                                            if (monthTest >= 0 && calFrom.get(Calendar.MONTH) != monthTest)
                                                                                                continue;

                                                                                            int y1 = calFrom.get(Calendar.YEAR);
                                                                                            int y2 = calTo.get(Calendar.YEAR);
                                                                                            int m1 = calFrom.get(Calendar.MONTH);
                                                                                            int m2 = calTo.get(Calendar.MONTH);
                                                                                            String header1 = y1 + " " + y2 + " " + PrintUtils.Print2Int(m1, 2) + " " + PrintUtils.Print2Int(m2, 2) + " " + maxTrades
                                                                                                    + " " + PrintUtils.Print2dec(risk, false)
                                                                                                    + " | " + thr
                                                                                                    + " " + tp
                                                                                                    + ' ' + sl
                                                                                                    + ' ' + maxBars + ' ' + barsBack + " " + maxH + " " + lostPips;
                                                                                            String csvFileName = path0 + "tbglobal_" + thr + "_" + tp + "_" + sl
                                                                                                    + "_" + String.valueOf((int) (risk * 10)) + ".csv";
                                                                                            csvFileName = path0 + "strategies" + File.separator + "tb_global_prod_20231003_global.csv";
                                                                                            //csvFileName = "";
                                                                                            sp.reset();
                                                                                            sp.setActualBalance(balance);
                                                                                            double pf = TestPriceBufferGlobal$$.doTestG(
                                                                                                    header1, dataBid, dataAsk, maxMins, atrHash,
                                                                                                    calFrom, calTo,
                                                                                                    dayWeek1, dayWeek2,
                                                                                                    min,
                                                                                                    configs, maxTrades, -1,
                                                                                                    true, balance,
                                                                                                    //risk,
                                                                                                    lostPips,
                                                                                                    maxDiff,
                                                                                                    sma,
                                                                                                    thrSma,
                                                                                                    minAcceptableDiffFromPeak,
                                                                                                    false,//debug
                                                                                                    printSummary, 0, dayTotalPips,
                                                                                                    true, true, printdayprofit, csvFileName,
                                                                                                    sp);

                                                                                            if (sp.getTrades() > 0) {
                                                                                                double f = sp.getProfitPer() / sp.getMaxDD();
                                                                                                maxDDs.add(sp.getMaxDD());
                                                                                                double m10 = sp.getMonthVar(maxVar);
                                                                                                double pfNoSL = sp.getWinPipsNoSLTP() * 1.0 / sp.getLostPipsNoSLTP();
                                                                                                pfNoSLArr.add(pfNoSL);

                                                                                                srH.add(sp.getSharpeRatio());
                                                                                                totalTradesArr.add(sp.getTrades());
                                                                                                //maxTradesArr.add(sp.getMaxTradesDays());
                                                                                                if (pf >= 3.0)
                                                                                                    pf = 3.0;//no tiene sentido agregar grandes
                                                                                                if (y1 >= year2016) {
                                                                                                    total2016++;
                                                                                                    if (pf >= 1.0)
                                                                                                        total2016Positives++;
                                                                                                    pf2016s.add(pf);
                                                                                                    factors16.add(f);
                                                                                                    profitPer16Arr.add(sp.getProfitPer());

                                                                                                    month10_2016s.add(m10);
                                                                                                    trades_2016s.add(sp.getTrades());
                                                                                                } else {
                                                                                                    total2016b++;
                                                                                                    if (pf >= 1.00)
                                                                                                        totalPositives++;
                                                                                                    pfs.add(pf);
                                                                                                    factors.add(f);
                                                                                                    profitPerArr.add(sp.getProfitPer());
                                                                                                    month10s.add(m10);

                                                                                                    maxTradesArr.add(sp.getMaxTradesDays());
                                                                                                    tradess.add(sp.getTrades());
                                                                                                }
                                                                                                totalY++;
                                                                                                totalPeriods++;
                                                                                                if (pf >= 1.00)
                                                                                                    totalPositivePeriods++;
                                                                                            }
                                                                                        }//periods

                                                                                        if (isTrialsMode) {
                                                                                            double avgsrh = MathUtils.average(srH);
                                                                                            double dt = Math.sqrt(MathUtils.varianceD(srH));
                                                                                            double avgTades = MathUtils.average(totalTradesArr);
                                                                                            System.out.println(
                                                                                                    thr + " " + tp + " " + sl + " " + maxBars + " " + barsBack
                                                                                                            + " || " + totalPeriods + " " + totalPositivePeriods + " " + srH.size()
                                                                                                            + " || " + PrintUtils.Print3dec(totalPositivePeriods * 100.0 / totalPeriods, false)
                                                                                                            + " " + PrintUtils.Print3dec(total2016Positives * 100.0 / total2016, false)
                                                                                                            + " || " + PrintUtils.Print3dec(avgsrh, false)
                                                                                                            + " " + PrintUtils.Print3dec(dt, false)
                                                                                                            + " " + PrintUtils.Print3dec(avgsrh / dt, false)
                                                                                                            + " " + PrintUtils.Print2dec(avgTades, false)
                                                                                            );
                                                                                        }

                                                                                        double positivePer = totalPositives * 100.0 / total2016b;
                                                                                        double positive2016Per = total2016Positives * 100.0 / total2016;
                                                                                        double avgMaxDD = MathUtils.average(maxDDs);
                                                                                        double dtMaxDD = Math.sqrt(MathUtils.varianceD(maxDDs));
                                                                                        double avgPf = MathUtils.average(pfs);
                                                                                        double avg2016Pf = MathUtils.average(pf2016s);
                                                                                        double avgFPf = MathUtils.average(factors);
                                                                                        double avgF2016Pf = MathUtils.average(factors16);
                                                                                        double avgM10 = MathUtils.average(month10s);
                                                                                        double avgM10_2016 = MathUtils.average(month10_2016s);
                                                                                        double avgMaxTrades = MathUtils.average(maxTradesArr);
                                                                                        double avgTrades = MathUtils.average(tradess);
                                                                                        double avgTrades2016 = MathUtils.average(trades_2016s);
                                                                                        double avgProfit = MathUtils.average(profitPerArr);
                                                                                        double dtProfit = MathUtils.variance(profitPerArr);
                                                                                        double spbefore = avgProfit / Math.sqrt(dtProfit);
                                                                                        double avg16Profit = MathUtils.average(profitPer16Arr);
                                                                                        double dt16Profit = MathUtils.variance(profitPer16Arr);
                                                                                        double spafter = avg16Profit / Math.sqrt(dt16Profit);

                                                                                        double avgPfNoSL = MathUtils.average(pfNoSLArr);

                                                                                        if (spbefore > 0.00) {
                                                                                            //totalPosiTiveTrials++;
                                                                                        }
                                                                                        String spDescriptor = thr + ";" + tp + ";" + sl + ";" + maxBars;
                                                                                        boolean isInserted = false;
                                                                                        for (int l = 0; l < rankingList.size(); l++) {
                                                                                            StratPerformance rsp = rankingList.get(l);
                                                                                            if (spbefore > rsp.getSharpeRatio()) {
                                                                                                StratPerformance rsp1 = new StratPerformance();
                                                                                                rsp1.setSharpeRatio(spbefore);
                                                                                                rsp1.setSpDescriptor(spDescriptor);
                                                                                                rankingList.add(l, rsp1);
                                                                                                isInserted = true;
                                                                                                break;
                                                                                            }
                                                                                        }
                                                                                        if (!isInserted) {
                                                                                            StratPerformance rsp1 = new StratPerformance();
                                                                                            rsp1.setSharpeRatio(spbefore);
                                                                                            rsp1.setSpDescriptor(spDescriptor);
                                                                                            rankingList.add(rsp1);
                                                                                        }


                                                                                        /*if (!isTrialsMode)
                                                                                            System.out.println(
                                                                                                    maxTrades + " " + PrintUtils.Print3dec(maxVar, false)
                                                                                                            + " " + maxDiff
                                                                                                            + " " + min
                                                                                                            + " || " + thr
                                                                                                            + " " + tp
                                                                                                            + " " + sl
                                                                                                            + " " + maxBars
                                                                                                            + " " + PrintUtils.Print3dec(risk, false)
                                                                                                            + " || " + maxDDs.size()
                                                                                                            + " || " + PrintUtils.Print3dec(positivePer, false) + " " + PrintUtils.Print3dec(positive2016Per, false)
                                                                                                            + " || " + PrintUtils.Print3dec(spbefore, false) + " " + PrintUtils.Print3dec(spafter, false)
                                                                                                            + " || " + PrintUtils.Print3dec(avgPf, false) + " " + PrintUtils.Print3dec(avg2016Pf, false)
                                                                                                            + " || " + PrintUtils.Print3dec(avgFPf, false) + " " + PrintUtils.Print3dec(avgF2016Pf, false)
                                                                                                            + " || avgNoSl= " + PrintUtils.Print3dec(avgPfNoSL, false)
                                                                                                            + " || >=10%= " + PrintUtils.Print3dec(avgM10, false) + " " + PrintUtils.Print3dec(avgM10_2016, false)
                                                                                                            + " || maxDD= " + PrintUtils.Print3dec(avgMaxDD + dtMaxDD, false) + " | " + PrintUtils.Print3dec(avgMaxDD, false) + " " + PrintUtils.Print3dec(dtMaxDD, false)
                                                                                                            + " || avg%= " + PrintUtils.Print3dec(avgProfit, false) + " " + PrintUtils.Print3dec(dtProfit, false)
                                                                                                            + " || avg2016%= " + PrintUtils.Print3dec(avg16Profit, false) + " " + PrintUtils.Print3dec(dt16Profit, false)
                                                                                                            + " || " + PrintUtils.Print3dec(avgTrades, false) + " " + PrintUtils.Print3dec(avgTrades2016, false)
                                                                                                            + " || " + PrintUtils.Print3dec(avgMaxTrades, false)
                                                                                            );*/
                                                                                    }//dayweek
                                                                            }//comm
                                                                        }//maxDiff
                                                                    }//maxvar
                                                                }//maxtrades
                                                            }//thrsma
                                                        }//sma
                                                    }//lostpips
                                                }//maxH
                                            }//barsback
                                        }//maxBars
                                    }//risk
                                }//sl
                            }//tp
                        }//thr
						/*if (isTrialsMode){
							double avgsrh = MathUtils.average(srH);
							System.out.println(totalPeriods+" "+totalPositivePeriods+" "+srH.size()
									+" || "+PrintUtils.Print3dec(totalPosiTiveTrials*100.0/totalTrials,false)
									+" || "+PrintUtils.Print3dec(avgsrh,false)
							);
						}*/
                }//h

                if (printModeRanking) {
                    for (int l = 0; l < rankingList.size(); l++) {
                        StratPerformance spm = rankingList.get(l);
                        //System.out.println(spm.getSpDescriptor()+";"+PrintUtils.Print3dec(spm.getSharpeRatio(),false));
                    }
                }
            }//NOISE PIPS
        }


        System.out.println("programa finalizado");
    }

    private static void doAnalyzeDays(HashMap<Integer, Integer> mp) {

        int lastMonth = -1;
        int acc = 0;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());

            int month = (int) pair.getKey() / 31;
            if (month != lastMonth) {
                if (lastMonth >= 0) {
                    System.out.println(lastMonth + " " + acc);
                }
                lastMonth = month;
                acc = 0;
            }
            acc += (int) pair.getValue();
            //it.remove(); // avoids a ConcurrentModificationException
        }

    }

}
