package com.drosa.efx.domain.model.experimental.basicStrategies.strats2023;

import com.drosa.efx.domain.model.experimental.basicStrategies.base.StratPerformance;
import com.drosa.efx.domain.model.experimental.basicStrategies.strats2021.AnalisisResultadosConjuntos2021;
import com.drosa.efx.domain.model.finances.DataProvider;
import com.drosa.efx.domain.model.finances.QuoteShort;
import com.drosa.efx.domain.model.finances.SimpleStats;
import com.drosa.efx.domain.model.finances.TestLines;
import com.drosa.efx.domain.utils.MathUtils;
import com.drosa.efx.domain.utils.PrintUtils;
import com.drosa.efx.domain.utils.TradingUtils;
import com.drosa.efx.infrastructure.DAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.drosa.efx.domain.model.experimental.basicStrategies.strats2021.AnalisisResultadosConjuntos2021.doRecalculateStrats;
import static com.drosa.efx.domain.model.experimental.basicStrategies.strats2021.AnalisisResultadosConjuntos2021.doTestProductionSystem;

public class ExcuteResearch {

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

        double balanceI = 3500;
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
        double leverageAlert = 15;
        ArrayList<Double> ddArray = new ArrayList<Double>();
        for (double balance = balanceI; balance <= balanceI - 0; balance += 1000) {
            //System.out.println("***balance= "+balance);
            for (int offset = 0; offset <= 140; offset += 12) {
                sp.reset();
                sp.setInitialBalance(balance);
                for (int minATR = 0; minATR <= 0; minATR += 1) {
                    isEndInt = doTestProductionSystem("DMO", path, darwinex1Files, balance, atrHash, offset, 12, true, false, minATR, leverageAlert, sp, 0);
                    //isEndInt = doTestProductionSystem("DQO", path, darwinexFiles, balance, atrHash, offset, 12, true, false, minATR, leverageAlert, sp, 0);
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
        currList.add("usdjpy");
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
        //TODO, MEDIR EL DD medio como medida fundamental
        double initialBalance = 2500;
        int year1 = 2011;
        int year2 = 2022;
        int h1 = 5;
        int h2 = 1;

        String filePeriod = "";
        int printOptions = 0;
        String periodBidStr = "_5 Mins_Bid_" + filePeriod;
        String periodAskStr = "_5 Mins_Ask_" + filePeriod;
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
            for (int nbars = 12; nbars <= 0; nbars += 6) {
                for (int dtInt = 15; dtInt <= 20; dtInt += 5) {
                    for (int slInt = 10; slInt <= 10; slInt += 4) {
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
