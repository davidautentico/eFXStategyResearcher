package com.drosa.efx.domain.model.experimental.basicStrategies.base;

import com.drosa.efx.domain.model.finances.*;
import com.drosa.efx.domain.utils.*;
import com.drosa.efx.infrastructure.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Bollinger extends AlgoBasic {

    int nbars = 50;
    double dt = 2.0;
    int tp = 0;
    double sl = 0;
    int distance = 0;
    int h1 = 0;
    int h2 = 23;
    int thr = -1;
    int lastModeCrossed = 0;
    int minDiff = 12;
    int maxSpread = 0;
    boolean forward = false;
    //para testear meses concretos
    int testMonth1 = 0;
    int testMonth2 = 11;
    private ArrayList<Integer> values = new ArrayList<Integer>();

    HashMap<Integer, ArrayList<BollingerStrat>> strats = new HashMap<Integer, ArrayList<BollingerStrat>>();


    public HashMap<Integer, ArrayList<BollingerStrat>> getStrats() {
        return strats;
    }

    public void setStrats(HashMap<Integer, ArrayList<BollingerStrat>> strats) {
        this.strats = strats;
    }


    public void setPrintDayProfit(String fileName, boolean printDayProfit) {
        this.outFileName = fileName;
        this.printDayProfit = printDayProfit;
    }

    public void setPrintMonthProfit(String fileName, boolean printMonthProfit) {
        this.outMonthFileName = fileName;
        this.printMonthProfit = printMonthProfit;
    }

    public void setParameters(int anbars, double adt, int atp, double asl, int amaxPositions, int adistance, int aThr, int ah1, int ah2,
                              boolean aForward, double aRisk, int aMinDiff, int minAtr, int aMaxSpread) {
        nbars = anbars;
        dt = adt;
        tp = atp;
        sl = asl;
        maxPositions = amaxPositions;
        distance = adistance;
        h1 = ah1;
        h2 = ah2;
        forward = aForward;
        risk = aRisk;
        thr = aThr;
        minDiff = aMinDiff;
        minAtr20 = minAtr;
        maxSpread = aMaxSpread;
        values.clear();
    }

    @Override
    public void doManagePositions(ArrayList<QuoteShort> dataBid, ArrayList<QuoteShort> dataAsk, int i, ArrayList<PositionShort> positions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void doEvaluateExits(
            String curr,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            int i, int actualH, ArrayList<PositionShort> positions,
            StratPerformance sp) {

        if (positions.size() <= 0) return;
        QuoteShort qb = dataBid.get(i);
        QuoteShort qa = dataAsk.get(i);

        double avgDiffb = 0;
        double avgDiffa = 0;
        int lookback = 24;
        int thrPips = 30;

        int pipsAtr = atr20;
        int maxAdv = (int) (0.10 * pipsAtr);
        //evaluamos SL y Tp
        int j = 0;
        while (j < positions.size()) {
            PositionShort p = positions.get(j);
            int stratCode = p.getStratOrder();
            //comprobamos moneda
            if (curr.trim().length() > 0 && !p.getCurrency().equalsIgnoreCase(curr)) continue;
            boolean isClosed = false;
            int pips = 0;
            int swapCosts = 0;
            if (p.getPositionStatus() == PositionStatus.OPEN) {
                int h = p.gethOpen();
                int nbars = strats.get(h).get(stratCode).getNbars();
                int sma = (int) MathUtils.average(values, values.size() - nbars, values.size() - 1);
                if (p.getPositionType() == PositionType.LONG) {
                    swapCosts = swapCostLong;
                    if (!forward) {
                        if (qb.getOpen5() >= sma
                        ) {
                            pips = qb.getOpen5() - p.getEntry();
                            isClosed = true;
                            //System.out.println("[long sma touched] "+pips+" "+pips);
                        } else {
                            if (avgDiffb <= thrPips) {
                                pips = qb.getOpen5() - p.getEntry();
                                //isClosed = true;
                            }
                        }
                    } else {
                        if (qb.getOpen5() <= sma) {
                            pips = qb.getOpen5() - p.getEntry();
                            isClosed = true;
                            //System.out.println("[short sl touched] "+pips+" "+pips);
                        }
                    }
                } else if (p.getPositionType() == PositionType.SHORT) {
                    swapCosts = swapCostShort;
                    if (!forward) {
                        if (qa.getOpen5() <= sma
                        ) {
                            pips = p.getEntry() - qa.getOpen5();
                            isClosed = true;
                            //System.out.println("[short sl touched] "+pips+" "+pips);
                        } else {
                            if (avgDiffa <= thrPips) {
                                pips = p.getEntry() - qa.getOpen5();
                                //isClosed = true;
                            }
                        }
                    } else {
                        if (qa.getOpen5() >= sma) {
                            pips = p.getEntry() - qa.getOpen5();
                            isClosed = true;
                            //System.out.println("[long sl touched] "+pips+" "+pips);
                        }
                    }
                }
            }

            if (isClosed) {
                int tc = 0;
                if (p.gethOpen() == 23 && actualH >= 0) {
                    tc = p.getTransactionCosts() + swapCosts;
                    p.setTransactionCosts(tc);
                }
                int pipsSL = Math.abs(p.getEntry() - p.getSl());
                double rr = pips * 1.0 / pipsSL;
                //if (tc==0)//para evaluar solo swaps
                sp.addTrade(p.getMicroLots(), pips, pipsSL, p.getMaxLoss(), p.getTransactionCosts(), cali, true);

                if (debug == 1) {
                    System.out.println("[CLOSED ee] " + DateUtils.datePrint(cali) + " || " + p.toString2() + " || " + pips
                            + " || " + PrintUtils.Print2dec(sp.getActualBalance(), false)
                            + " || " + PrintUtils.Print2dec(sp.getMaxBalance(), false) + " " + PrintUtils.Print2dec(sp.getMaxDD(), false)
                    );
                }
                positions.remove(j);
            } else {
                j++;
            }
        }

    }

    @Override
    public int doEvaluateEntries(
            String curr,
            ArrayList<QuoteShort> dataBid,
            ArrayList<QuoteShort> dataAsk,
            ArrayList<Integer> maxMins,
            HashMap<Integer, ArrayList<Double>> spreads,
            int i,
            ArrayList<PositionShort> positions,
            boolean canTrade,
            StratPerformance sp) {

        QuoteShort qb = dataBid.get(i);
        QuoteShort qa = dataAsk.get(i);
        QuoteShort.getCalendar(cali, qb);
        int y = cali.get(Calendar.YEAR);
        int h = cali.get(Calendar.HOUR_OF_DAY);
        int min = cali.get(Calendar.MINUTE);
        int day = cali.get(Calendar.DAY_OF_YEAR);

        values.add(qb.getOpen5());

        int newtrades = 0;

        int nbars = -1;
        double sl = 0.4;
        double dt = 0.2;
        double risk = 0.1;
        int nTest = -1;
        int distanceCrossed = 0;
        boolean ishAllowed = false;
        if (strats.containsKey(h)) {
            for (int w = 0; w <= strats.get(h).size() - 1; w++) {
                //System.out.println("evaluando entradas");
                BollingerStrat bs = strats.get(h).get(w);
                if (curr.trim().length() > 0 && !bs.getCurrency().equalsIgnoreCase(curr)) continue;

                //System.out.println("evaluando entradas2");

                ishAllowed = bs.isEnabled;
                nbars = bs.getNbars();
                sl = bs.getSl();
                dt = bs.getDt();
                risk = bs.getRisk();
                int spread = qa.getOpen5() - qb.getOpen5();
                if (!ishAllowed) return 0;
                //System.out.println("hallowed");
                if (i < nbars) return 0;
                if (h == 0 && min < 15) return 0;
                if (positions.size() >= maxPositions) return 0;
                if (spread >= maxSpread) return 0;

                //System.out.println("aaded: "+values.get(values.size()-1)+" "+values.size());


                int sma = (int) MathUtils.average(values, values.size() - nbars, values.size() - 1);
                int dtpips = (int) Math.sqrt(MathUtils.variance(values, values.size() - nbars, values.size() - 1));

                int sma5 = sma;
                if (values.size() - nbars - nTest >= 0)
                    sma5 = (int) MathUtils.average(values, values.size() - nbars - nTest, values.size() - nTest);
                //System.out.println("aaded: "+values.get(values.size()-1)+" "+values.size()+" "+sma );
                int pipsAtr = atr20;
                dtpips = pipsAtr;
                int slPips = (int) (sl * dtpips);
                //upper band
                int upperBand = (int) (sma + dt * dtpips);
                int upperBandLimit = (int) (sma + 1 * dt * dtpips);
                //down band
                int lowerBand = (int) (sma - dt * dtpips);
                int lowerBandLimit = (int) (sma - 1 * dt * dtpips);
                //diferencia
                int pipsA = qb.getOpen5() - upperBand;
                int pipsAb = qa.getOpen5() - upperBand;
                int pipsAbl = qa.getOpen5() - upperBandLimit;

                int pipsB = lowerBand - qa.getOpen5();
                int pipsBa = lowerBand - qb.getOpen5();
                int pipsBl = lowerBandLimit - qa.getOpen5();
                int maxMin = maxMins.get(i - 1);
                int minDiff = 00;

                //if (cali.get(Calendar.MONTH)==Calendar.DECEMBER && cali.get(Calendar.DAY_OF_MONTH)==9)
				/*System.out.println(DateUtils.datePrint(cali)
						+" || "+sma+" "+upperBand+" "+lowerBand
						+"  || "+qa.getOpen5()+" "+qb.getOpen5()
						+" || "+pipsA+" "+pipsB
						);*/

                int transactionCosts = TradingUtils.getTransactionCosts(spreads, y, h, 3);
                if (isTransactionHours != 1)
                    transactionCosts = TradingUtils.getTransactionCostsMinutes(spreads, y, h * 12 + min / 5, 3);

                //transactionCosts = 0;
                //System.out.println(y+" || "+h+" || "+transactionCosts);
                int lastPrice = -1;
                int dprice = -999;
                if (positions.size() > 0) {
                    lastPrice = positions.get(positions.size() - 1).getEntry();
                }

                //System.out.println("antes de ishallowed: "+upperBand+" "+lowerBand);
                if (ishAllowed
                    //&& atr20<=minAtr20
                ) {
                    if (true
                            && pipsA >= minDiff && pipsAb >= minDiff// && pipsAbl <=0
                            //&& (nTest<0 || dataBid.get(i-nTest).getOpen5()>=sma5)
                            //&& (q.getOpen5()-sma)>=30
                            //&& (thr<0 || maxMin>=thr)
                            && i - lastCrossed >= distanceCrossed
                            && ((lastPrice == -1) || (qb.getOpen5() >= lastPrice + distance))
                        //&& qb.getOpen5()<=dataBid.get(i-1).getOpen5()
                    ) {
                        if (lastModeCrossed <= 0) {
                            lastCrossed = i;
                            lastModeCrossed = 1;
                        }
                        PositionShort pos = new PositionShort();
                        pos.setEntry(qb.getOpen5());
                        pos.setPositionStatus(PositionStatus.OPEN);
                        pos.setStratOrder(w);
                        pos.setCurrency(curr);
                        if (forward) {
                            pos.setPositionType(PositionType.LONG);
                            //pos.setSl((sma));
                            pos.setTp((int) (qb.getOpen5() + slPips));
                            pos.setSl((int) (qb.getOpen5() - slPips));
                        } else {
                            pos.setPositionType(PositionType.SHORT);
                            pos.setSl(pos.getEntry() + slPips);

                            //pos.setTp((int) (q.getOpen5()-1*distance));
                            //System.out.println("[open short] "+qb.getOpen5()+" "+sma+" "+pos.getSl()+" "+pipsAtr+" "+pipsA+" "+qb.toString());
                        }
                        //calculate miniLots 0.01
                        int minPips = slPips;
                        double riskPosition = sp.getActualEquitity() * risk * 1.0 / 100.0;
                        double riskPip = riskPosition / (minPips * 0.1);
                        int microLots = (int) (riskPip / 0.10);
                        if (microLots <= 0) microLots = 1;

                        if (actualM < testMonth1 || actualM > testMonth2) microLots = 0;

                        if (microLots > 0
                        ) {
                            int diff = i - lastCrossed;
                            long actualSize = TradingUtils.getOpenSize(positions);
                            double actualLeverage = (actualSize + microLots) * 1000.0 / sp.getActualBalance();
                            //System.out.println(PrintUtils.Print2dec(actualSize,false)+" || "+PrintUtils.Print2dec(actualLeverage,false));
                            if (actualLeverage < 30.0
                                //&& diff>=minDiff
                            ) {
                                pos.sethOpen(h);
                                pos.getOpenCal().setTimeInMillis(cali.getTimeInMillis());
                                pos.setMicroLots(microLots);
                                pos.setTransactionCosts(transactionCosts);
                                positions.add(pos);
                                lastDayTrade = day;
                                trades++;
                                newtrades++;

                                if (debug == 1) {
                                    System.out.println("[open SHORT] " + DateUtils.datePrint(cali)
                                            + " " + pos.getEntry()
                                            + " " + pos.getSl()
                                            + " " + pos.getMicroLots() + " " + riskPosition
                                            + " || " + slPips
                                            + " || " + risk + " || " + sp.getActualBalance()
                                            + " " + qb.toString()
                                    );
                                }
                            }
                        }
                    } else if (
                            true
                                    && pipsB >= minDiff && pipsBa >= minDiff //&& pipsBl<=0
                                    //&& (nTest<0 || dataAsk.get(i-nTest).getOpen5()<=sma5)
                                    //&& ((lastPrice==-1) || (qa.getOpen5()<=lastPrice-distance ))
                                    //&& (sma-q.getOpen5())>=30
                                    //&& (thr<0 || maxMin<=-thr)
                                    && i - lastCrossed >= distanceCrossed
                                    && ((lastPrice == -1) || (qa.getOpen5() <= lastPrice - distance))
                        //&& qa.getOpen5()>=dataAsk.get(i-1).getOpen5()
                    ) {
                        //distance =100;
                        if (lastModeCrossed >= 0) {
                            lastCrossed = i;
                            lastModeCrossed = -1;
                        }

                        //abrimos posicion long
                        PositionShort pos = new PositionShort();
                        pos.setEntry(qa.getOpen5());
                        pos.setPositionStatus(PositionStatus.OPEN);
                        pos.setStratOrder(w);
                        pos.setCurrency(curr);
                        if (forward) {
                            pos.setPositionType(PositionType.SHORT);
                            //pos.setSl((int) (sma));
                            pos.setTp((int) (qa.getOpen5() - slPips));
                            pos.setTp((int) (qa.getOpen5() + slPips));
                        } else {
                            pos.setPositionType(PositionType.LONG);
                            pos.setSl(qa.getOpen5() - slPips);
                            //pos.setTp((int) (q.getOpen5() + 1*distance));
                            //System.out.println("[open long] "+q.getOpen5()+" "+sma+" "+pos.getSl());
                        }
                        //calculate miniLots
                        int minPips = slPips;
                        double riskPosition = sp.getActualBalance() * risk * 1.0 / 100.0;
                        double riskPip = riskPosition / (minPips * 0.1);
                        int microLots = (int) (riskPip / 0.10);
                        if (microLots <= 0) microLots = 1;
						
						/*System.out.println("[newPosition] "
								+" "+PrintUtils.Print2dec(sp.getActualBalance(),false)
								+" "+PrintUtils.Print2dec(riskPosition,false)
								+" "+PrintUtils.Print2dec(riskPip,false)
								+" "+slPips
								+" "+PrintUtils.Print2dec(microLots,false)
								);*/

                        if (actualM < testMonth1 || actualM > testMonth2) microLots = 0;

                        if (microLots > 0) {
                            long actualSize = TradingUtils.getOpenSize(positions);
                            double actualLeverage = (actualSize + microLots) * 1000.0 / sp.getActualBalance();
                            int diff = i - lastCrossed;
                            if (actualLeverage < 30.0
                                    && diff >= minDiff
                            ) {
                                pos.sethOpen(h);
                                pos.getOpenCal().setTimeInMillis(cali.getTimeInMillis());
                                pos.setMicroLots(microLots);
                                pos.setTransactionCosts(transactionCosts);
                                positions.add(pos);
                                lastDayTrade = day;
                                trades++;
                                newtrades++;

                                if (debug == 1) {
                                    System.out.println("[open LONG] " + DateUtils.datePrint(cali)
                                            + " " + pos.getEntry()
                                            + " " + pos.getMicroLots() + " " + riskPosition + " || " + slPips + " || " + risk + " || " + sp.getActualBalance());
                                }
                            }

                            //System.out.println("[open] "+microLots+" "+riskPosition+" || "+slPips+" || "+risk+" || "+sp.getActualBalance());
                        }
                    }
                }
            }
        }

        return newtrades;
    }

    public static void main(String[] args) throws IOException {
        String path0 = "f:\\fxdata\\";
        String currency = "eurjpy321321";

        String pathSpread = path0 + currency + "_spreads_2014_2019.csv";
        String bidSuffix = "_5 Mins_Bid_2004.01.01_2020.06.08.csv";
        String askSuffix = "_5 Mins_Bid_2004.01.01_2020.06.08.csv";
        String pathBid = path0 + currency + "_5 Mins_Bid_2011.01.01_2020.12.05.csv";
        String pathAsk = path0 + currency + "_5 Mins_Ask_2011.01.01_2020.12.05.csv";
        String pathNews = path0 + "News.csv";


        ArrayList<String> paths = new ArrayList<String>();
        paths.add("eurusd");
        //paths.add("eurjpy");

        int total = 0;
        int limit = paths.size() - 1;
        limit = 0;
        String provider = "";
        try {
            Sizeof.runGC();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ArrayList<QuoteShort> dataI = null;
        ArrayList<QuoteShort> dataS = null;
        ArrayList<FFNewsClass> news = new ArrayList<FFNewsClass>();
        HashMap<Integer, ArrayList<Double>> spreads = new HashMap<Integer, ArrayList<Double>>();

        int isHours = 1;
        //if (isHours==1) DAO.readSpreads(pathSpread,2014,2019,spreads);
        //else DAO.readSpreadsMinutes(pathSpread,2014,2019,spreads);
        ArrayList<Double> pfs = new ArrayList<Double>();
        ArrayList<Double> pf2016s = new ArrayList<Double>();
        ArrayList<Double> factors = new ArrayList<Double>();
        ArrayList<Double> factors16 = new ArrayList<Double>();
        ArrayList<Double> profitPerArr = new ArrayList<Double>();
        ArrayList<Double> profitPer16Arr = new ArrayList<Double>();
        ArrayList<Double> month10s = new ArrayList<Double>();
        ArrayList<Double> month10_2016s = new ArrayList<Double>();
        ArrayList<Integer> maxTradesArr = new ArrayList<Integer>();
        ArrayList<Double> maxDDs = new ArrayList<Double>();

        Calendar calFrom = Calendar.getInstance();
        Calendar calTo = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        //maxima fecha es febrero

        //obtenci�n de datos
        HashMap<String, ArrayList<QuoteShort>> dataAskHash = new HashMap<String, ArrayList<QuoteShort>>();
        HashMap<String, ArrayList<QuoteShort>> dataBidHash = new HashMap<String, ArrayList<QuoteShort>>();
        HashMap<String, ArrayList<Integer>> maxMinsHash = new HashMap<String, ArrayList<Integer>>();
        HashMap<String, HashMap<Integer, Integer>> atrHashHash = new HashMap<String, HashMap<Integer, Integer>>();

        ArrayList<Tick> ticks = new ArrayList<Tick>();
        int minSize = -1;
        for (int i = 0; i <= paths.size() - 1; i++) {
            String currencyStr = paths.get(i);
            pathBid = path0 + currencyStr + "_5 Mins_Bid_2011.01.01_2020.12.05.csv";
            dataI = new ArrayList<QuoteShort>();
            dataI = DAO.retrieveDataShort5m(pathBid, DataProvider.DUKASCOPY_FOREX4);
            TestLines.calculateCalendarAdjustedSinside(dataI);
            dataS = TradingUtils.cleanWeekendDataS(dataI);
            ArrayList<QuoteShort> dataBid = dataS;

            pathAsk = path0 + currencyStr + "_5 Mins_Ask_2011.01.01_2020.12.05.csv";
            dataI = new ArrayList<QuoteShort>();
            dataI = DAO.retrieveDataShort5m(pathAsk, DataProvider.DUKASCOPY_FOREX4);
            TestLines.calculateCalendarAdjustedSinside(dataI);
            dataS = TradingUtils.cleanWeekendDataS(dataI);
            ArrayList<QuoteShort> dataAsk = dataS;


            System.out.println("curr= " + currencyStr + " dataBid/Ask: " + pathBid + " " + dataBid.size() + " " + dataAsk.size());
            if (minSize == -1 || dataBid.size() < minSize) minSize = dataBid.size();
            if (minSize == -1 || dataAsk.size() < minSize) minSize = dataAsk.size();

            ArrayList<Integer> maxMins = TradingUtils.calculateMaxMinByBarShortAbsoluteInt(dataBid);
            HashMap<Integer, Integer> atrHash = TradingUtils.calculateATR(dataBid, 20);

            dataAskHash.put(currencyStr, dataAsk);
            dataBidHash.put(currencyStr, dataBid);
            maxMinsHash.put(currencyStr, maxMins);
            atrHashHash.put(currencyStr, atrHash);
        }

        Bollinger mm = new Bollinger();
        StratPerformance sp = new StratPerformance();

        int printSummary = 1;
        int periodMult = 12000;
        int periodLen = 12;
        printSummary = 1;
        boolean printDayProfit = true;
        boolean isTrialsMode = false;
        int yearStart = 2011;
        int yearEnd = 2020;
        int yearHalf = 2021;
        calMax.set(yearEnd, 11, 31);
        System.out.println("**BOLLINGER YEAR START: " + yearStart + " " + yearEnd);

        HashMap<Integer, ArrayList<BollingerStrat>> strats = new HashMap<Integer, ArrayList<BollingerStrat>>();
        //5 min
        String aCurr = paths.get(0);


        //EURAUD
        for (int w = 0; w <= 23; w++) {
            BollingerStrat bs1 = new BollingerStrat(aCurr, 0, 39, 0.12, 0.8, 0.3, false);
            ArrayList<BollingerStrat> list1 = new ArrayList<BollingerStrat>();
            list1.add(bs1);
            strats.put(w, list1);
        }
        BollingerStrat bs0 = strats.get(0).get(0);
        bs0.setParameters(36, 0.10, 0.6, 0.2, true);
        BollingerStrat bs1 = strats.get(1).get(0);
        bs1.setParameters(36, 0.10, 0.6, 0.2, true);
        BollingerStrat bs2 = strats.get(2).get(0);
        bs2.setParameters(36, 0.20, 0.6, 0.3, true);
        BollingerStrat bs5 = strats.get(5).get(0);
        bs5.setParameters(66, 0.15, 0.4, 0.2, true);
        BollingerStrat bs6 = strats.get(6).get(0);
        bs6.setParameters(66, 0.15, 0.4, 0.2, true);
        BollingerStrat bs7 = strats.get(7).get(0);
        bs7.setParameters(66, 0.15, 0.4, 0.2, true);
        BollingerStrat bs8 = strats.get(8).get(0);
        bs8.setParameters(66, 0.15, 0.4, 0.2, true);
        BollingerStrat bs9 = strats.get(9).get(0);
        bs9.setParameters(12, 0.10, 0.1, 0.1, true);
        BollingerStrat bs12 = strats.get(12).get(0);
        bs12.setParameters(12, 0.10, 0.2, 0.1, true);
        BollingerStrat bs13 = strats.get(13).get(0);
        bs13.setParameters(36, 0.20, 0.2, 0.1, true);
        BollingerStrat bs14 = strats.get(14).get(0);
        bs14.setParameters(12, 0.10, 0.2, 0.2, true);
        BollingerStrat bs18 = strats.get(18).get(0);
        bs18.setParameters(12, 0.10, 0.2, 0.1, true);
        BollingerStrat bs22 = strats.get(22).get(0);
        bs22.setParameters(84, 0.15, 0.9, 0.2, true);
        BollingerStrat bs23 = strats.get(23).get(0);
        bs23.setParameters(36, 0.10, 0.6, 0.2, true);

        sp = new StratPerformance();
        ArrayList<Double> srH = new ArrayList<Double>();
        for (int ah1 = -1; ah1 <= -1; ah1++) {
            int h1 = ah1;
            if (h1 > 23) h1 -= 24;
            int h2 = h1 + 0;
            if (h1 >= 0) {
                System.out.println("**testeando H=" + h1);
                for (int t = 0; t <= 23; t++) {
                    if (strats.get(t) != null)
                        strats.get(t).get(0).setEnabled(false);
                }
                if (strats.get(h1) != null)
                    strats.get(h1).get(0).setEnabled(true);
            } else {
                h1 = 0;
            }
            //EURJPY
            //strats.get(0).setParameters(39,0.12,0.8,0.1,true);
            //strats.get(1).setParameters(39,0.12,0.8,0.1,true);

            int totalTrials = 0;
            int totalPosiTiveTrials = 0;
            srH.clear();
            for (int nbars = 39; nbars <= 39; nbars += 5) {
                for (double dt = 0.10; dt <= 0.10; dt += 0.05) {
                    for (double sl = 0.6; sl <= 0.6; sl += 0.10) {
                        for (int thr = -1; thr <= -1; thr += 100) {
                            int minDiff = 0;
                            for (int minAtr = 0; minAtr <= 0; minAtr += 100) {
                                for (int maxSpread = 99999; maxSpread <= 99999; maxSpread += 05)
                                    for (double risk = 0.20; risk <= 0.20; risk += 0.10) {
                                        for (int w = 9; w <= 0; w++) {
                                            if (w == 10 || w == 11 || w == 13) continue;
                                            int ah = w;
                                            if (w > 23) ah -= 24;
                                            BollingerStrat bs = strats.get(ah).get(0);
                                            //if (w<2)
                                            //strats.get(w).setParameters(nbars,dt,sl,risk,true);
                                            //else
                                            //strats.get(w).setParameters(nbars,dt,sl,0.0,false);
											
											/*bs.setNbars(nbars);
											bs.setDt(dt);
											bs.setRisk(risk);
											bs.setSl(sl);
											bs.setEnabled(true);*/
                                        }

                                        //System.out.println("risk= "+PrintUtils.Print2dec(risk, false));
                                        totalTrials++;
                                        for (int delay = 0; delay <= 0; delay += 10)
                                            for (double balance = 7000; balance <= 7000; balance += 1000)
                                                for (int maxPositions = 35; maxPositions <= 35; maxPositions += 5) {
                                                    String header = nbars
                                                            + " " + PrintUtils.Print2dec(dt, false)
                                                            + " " + PrintUtils.Print2dec(sl, false)
                                                            + " " + PrintUtils.Print2dec(risk, false)
                                                            + " " + maxPositions;
                                                    int totalPositives = 0;
                                                    int total2016 = 0;
                                                    double accProfit = 0;
                                                    //double balance = 1000;
                                                    double accYear = 0;
                                                    int totalY = 0;
                                                    double maxVar = 6.5;
                                                    int total2016b = 0;
                                                    int total2016Positives = 0;
                                                    int totalPeriods = 0;
                                                    int countMaxDD10 = 0;
                                                    maxDDs.clear();
                                                    month10s.clear();
                                                    month10_2016s.clear();
                                                    pfs.clear();
                                                    pf2016s.clear();
                                                    factors.clear();
                                                    factors16.clear();
                                                    maxTradesArr.clear();
                                                    profitPerArr.clear();
                                                    profitPer16Arr.clear();

                                                    for (int periods = 0; periods <= 300; periods += 1) {
                                                        calFrom.set(yearStart, 00, 01);
                                                        calFrom.add(Calendar.MONTH, periods * periodMult);
                                                        calTo.setTimeInMillis(calFrom.getTimeInMillis());
                                                        calTo.add(Calendar.MONTH, periodLen);
                                                        calTo.add(Calendar.DAY_OF_MONTH, -1);

                                                        if (calTo.getTimeInMillis() > calMax.getTimeInMillis()) break;

                                                        int y1 = calFrom.get(Calendar.YEAR);
                                                        int y2 = calTo.get(Calendar.YEAR);
                                                        int m1 = calFrom.get(Calendar.MONTH);
                                                        int m2 = calTo.get(Calendar.MONTH);

                                                        String strParam =
                                                                nbars
                                                                        + " " + dt
                                                                        + " " + sl
                                                                        + " " + maxPositions
                                                                        + " " + delay
                                                                        + " " + thr
                                                                        + " " + h1
                                                                        + " " + h2
                                                                        + " " + risk
                                                                        + " " + minDiff
                                                                        + " " + minAtr
                                                                        + " " + maxSpread;
                                                        //System.out.println(strParam);
                                                        //	dt,0,sl,maxPositions,delay,thr,h1,h2,false,risk,minDiff,minAtr,maxSpread);

                                                        mm.setParameters(nbars, dt, 0, sl, maxPositions, delay, thr, h1, h2, false, risk, minDiff, minAtr, maxSpread);
                                                        mm.setStrats(strats);
                                                        mm.setPrintDayProfit("f:\\fxdata\\results.csv", printDayProfit);
                                                        String header1 = y1 + " " + y2 + " " + m1 + " " + m2 + " " + h1 + " " + h2 + " || " + header;

                                                        sp.reset();
                                                        sp.setInitialBalance(balance);
                                                        sp.setActualBalance(balance);
                                                        double pf = mm.doTest(header1, paths, dataBidHash, dataAskHash, maxMinsHash, atrHashHash,
                                                                minSize, calFrom, calTo, sp, isHours, 0, printSummary);

                                                        if (sp.getTrades() > 0) {
                                                            //System.out.println(PrintUtils.Print3dec(sp.getProfitPer(),false));
                                                            double f = sp.getProfitPer() / sp.getMaxDD();
                                                            maxDDs.add(sp.getMaxDD());
                                                            double m10 = sp.getMonthVar(maxVar);

                                                            //maxTradesArr.add(sp.getMaxTradesDays());
                                                            if (pf >= 3.0)
                                                                pf = 3.0;//no tiene sentido agregar grandes
                                                            if (y1 >= yearHalf) {
                                                                total2016++;
                                                                if (pf >= 1.0) total2016Positives++;
                                                                pf2016s.add(pf);
                                                                factors16.add(f);
                                                                profitPer16Arr.add(sp.getProfitPer());

                                                                month10_2016s.add(m10);
                                                                if (sp.getMaxDD() >= 10.0) countMaxDD10++;
                                                            } else {
                                                                total2016b++;
                                                                if (pf >= 1.00) totalPositives++;
                                                                pfs.add(pf);
                                                                factors.add(f);
                                                                month10s.add(m10);
                                                                profitPerArr.add(sp.getProfitPer());

                                                                maxTradesArr.add(sp.getMaxTradesDays());
                                                            }

                                                            totalY++;
                                                            totalPeriods++;
                                                        }//sp.getTrades()>0
                                                    }//for periods
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
                                                    double avgProfit = MathUtils.average(profitPerArr);
                                                    double dtProfit = MathUtils.variance(profitPerArr);
                                                    double spbefore = avgProfit / Math.sqrt(dtProfit);
                                                    double avg16Profit = MathUtils.average(profitPer16Arr);
                                                    double dt16Profit = MathUtils.variance(profitPer16Arr);
                                                    double spafter = avg16Profit / Math.sqrt(dt16Profit);

                                                    double winpt = sp.getWinPips() * 1.0 / sp.getWins();
                                                    double lostpt = sp.getLostPips() * 1.0 / sp.getLosses();
                                                    if (spbefore > 0.00) {
                                                        totalPosiTiveTrials++;
                                                    }
                                                    srH.add(spbefore);
                                                    if (!isTrialsMode)
                                                        System.out.println(
                                                                PrintUtils.Print3dec(balance, false)
                                                                        + " " + maxPositions
                                                                        + " " + minAtr
                                                                        + " " + delay
                                                                        //+" "+PrintUtils.Print3dec(maxVar,false)
                                                                        + " || " + h1 + " " + h2 + " " + nbars
                                                                        + " " + PrintUtils.Print3dec(dt, false)
                                                                        + " " + PrintUtils.Print3dec(sl, false)
                                                                        + " " + PrintUtils.Print2dec(risk, false)
                                                                        + " || " + maxDDs.size()
                                                                        + " || " + PrintUtils.Print3dec(positivePer, false) + " " + PrintUtils.Print3dec(positive2016Per, false)
                                                                        + " || " + PrintUtils.Print3dec(spbefore, false) + " " + PrintUtils.Print3dec(spafter, false)
                                                                        + " || " + PrintUtils.Print3dec(avgPf, false) + " " + PrintUtils.Print3dec(avg2016Pf, false)
                                                                        + " || " + PrintUtils.Print3dec(avgFPf, false) + " " + PrintUtils.Print3dec(avgF2016Pf, false)
                                                                        + " || >=10%=" + PrintUtils.Print3dec(avgM10, false) + " " + PrintUtils.Print3dec(avgM10_2016, false)
                                                                        + " || maxDD= " + PrintUtils.Print3dec(avgMaxDD + dtMaxDD, false) + " | " + PrintUtils.Print3dec(avgMaxDD, false) + " " + PrintUtils.Print3dec(dtMaxDD, false)
                                                                        + " || avg16Profit= " + PrintUtils.Print3dec(avg16Profit, false)
                                                                        + " || " + PrintUtils.Print3dec(avgMaxTrades, false)
                                                                        + " || " + PrintUtils.Print3dec(winpt, false) + " " + PrintUtils.Print3dec(lostpt, false)
                                                        );
                                                }//maxPos
                                    }//risk
                            }//minDiff
                        }//thr
                    }//fsl
                }//fdiff
            }//nbars

            if (isTrialsMode) {
                double avgsrh = MathUtils.average(srH);
                System.out.println(totalTrials + " " + totalPosiTiveTrials
                        + " || " + PrintUtils.Print3dec(totalPosiTiveTrials * 100.0 / totalTrials, false)
                        + " || " + PrintUtils.Print3dec(avgsrh, false)
                );
            }
        }//h

    }


}
