package com.drosa.efx.domain.model.experimental.basicStrategies.base;

import com.drosa.efx.domain.utils.DateUtils;
import com.drosa.efx.domain.utils.MathUtils;
import com.drosa.efx.domain.utils.PrintUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;


public class StratPerformance {

    String spDescriptor = "";
    double sharpeRatio = 0;
    double pf = 0.0;
    double pfYears = 0.0;
    int years = 0;
    double avgPips = 0.0;
    int winPips = 0;
    int lostPips = 0;
    double winPips$ = 0;
    double lostPips$ = 0;
    int trades = 0;
    int wins = 0;
    int losses = 0;
    int maxAdversionAcc = 0;
    double maxAdversionAvg = 0;

    double initialBalance = 0;
    double maxBalance = 0;
    double actualBalance = 0;
    double actualEquitity = 0;
    double maxEquitity = 0;
    double maxDD = 0;
    double profitPer = 0.0;

    int winPipsNoSLTP = 0;
    int lostPipsNoSLTP = 0;

    HashMap<Integer, ArrayList<Double>> dayData = new HashMap<Integer, ArrayList<Double>>();
    HashMap<Integer, ArrayList<Double>> monthData = new HashMap<Integer, ArrayList<Double>>();
    HashMap<Integer, ArrayList<Integer>> monthTradesP = new HashMap<Integer, ArrayList<Integer>>();
    HashMap<Integer, ArrayList<Integer>> monthTradesSL = new HashMap<Integer, ArrayList<Integer>>();
    HashMap<Integer, ArrayList<Double>> monthEquitity = new HashMap<Integer, ArrayList<Double>>();

    ArrayList<Long> dayEquitityTime = new ArrayList<Long>();
    ArrayList<Double> dayEquitityArr = new ArrayList<Double>();
    ArrayList<Double> dayReturnArr = new ArrayList<Double>();
    HashMap<Integer, Integer> dayPositions = new HashMap<Integer, Integer>();
    int maxTradesDays = 0;


    public String getSpDescriptor() {
        return spDescriptor;
    }

    public void setSpDescriptor(String spDescriptor) {
        this.spDescriptor = spDescriptor;
    }

    public double getSharpeRatio() {
        return sharpeRatio;
    }

    public void setSharpeRatio(double sharpeRatio) {
        this.sharpeRatio = sharpeRatio;
    }

    public int getWinPipsNoSLTP() {
        return winPipsNoSLTP;
    }

    public void setWinPipsNoSLTP(int winPipsNoSLTP) {
        this.winPipsNoSLTP = winPipsNoSLTP;
    }

    public int getLostPipsNoSLTP() {
        return lostPipsNoSLTP;
    }

    public void setLostPipsNoSLTP(int lostPipsNoSLTP) {
        this.lostPipsNoSLTP = lostPipsNoSLTP;
    }

    public int getMaxTradesDays() {
        return maxTradesDays;
    }

    public void setMaxTradesDays(int maxTradesDays) {
        this.maxTradesDays = maxTradesDays;
    }

    public double getWinPips$() {
        return winPips$;
    }

    public void setWinPips$(double winPips$) {
        this.winPips$ = winPips$;
    }

    public double getLostPips$() {
        return lostPips$;
    }

    public void setLostPips$(double lostPips$) {
        this.lostPips$ = lostPips$;
    }

    public double getActualEquitity() {
        return actualEquitity;
    }

    public void setActualEquitity(double actualEquitity) {
        this.actualEquitity = actualEquitity;
    }

    public double getMaxEquitity() {
        return maxEquitity;
    }

    public void setMaxEquitity(double maxEquitity) {
        this.maxEquitity = maxEquitity;
    }

    public HashMap<Integer, ArrayList<Double>> getMonthData() {
        return monthData;
    }

    public void setMonthData(HashMap<Integer, ArrayList<Double>> monthData) {
        this.monthData = monthData;
    }

    public HashMap<Integer, ArrayList<Integer>> getMonthTradesP() {
        return monthTradesP;
    }

    public void setMonthTradesP(HashMap<Integer, ArrayList<Integer>> monthTradesP) {
        this.monthTradesP = monthTradesP;
    }

    public HashMap<Integer, ArrayList<Integer>> getMonthTradesSL() {
        return monthTradesSL;
    }

    public void setMonthTradesSL(HashMap<Integer, ArrayList<Integer>> monthTradesSL) {
        this.monthTradesSL = monthTradesSL;
    }

    public double getPf() {
        return pf;
    }

    public void setPf(double pf) {
        this.pf = pf;
    }

    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public double getAvgPips() {
        return avgPips;
    }

    public void setAvgPips(double avgPips) {
        this.avgPips = avgPips;
    }

    public double getPfYears() {
        return pfYears;
    }

    public void setPfYears(double pfYears) {
        this.pfYears = pfYears;
    }

    public double getMaxDD() {
        return maxDD;
    }

    public void setMaxDD(double maxDD) {
        this.maxDD = maxDD;
    }

    public int getTrades() {
        return trades;
    }

    public void setTrades(int trades) {
        this.trades = trades;
    }

    public int getWinPips() {
        return winPips;
    }

    public void setWinPips(int winPips) {
        this.winPips = winPips;
    }

    public int getLostPips() {
        return lostPips;
    }

    public void setLostPips(int lostPips) {
        this.lostPips = lostPips;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
        this.actualBalance = initialBalance;
        this.maxBalance = initialBalance;
        this.actualEquitity = initialBalance;
        this.maxEquitity = initialBalance;
        dayEquitityArr.clear();
    }

    public double getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }

    public double getActualBalance() {
        return actualBalance;
    }

    public void setActualBalance(double actualBalance) {
        this.actualBalance = actualBalance;
    }

    public double getProfitPer() {
        return profitPer;
    }

    public void setProfitPer(double profitPer) {
        this.profitPer = profitPer;
    }

    public int getMaxAdversionAcc() {
        return maxAdversionAcc;
    }

    public void setMaxAdversionAcc(int maxAdversionAcc) {
        this.maxAdversionAcc = maxAdversionAcc;
    }

    public double getMaxAdversionAvg() {
        return maxAdversionAvg;
    }

    public void setMaxAdversionAvg(double maxAdversionAvg) {
        this.maxAdversionAvg = maxAdversionAvg;
    }

    public double getMonthDataWinPer() {
        int total = 0;
        int wins = 0;
        double val = 0;
        ArrayList<Double> values = new ArrayList<Double>();

        Object[] keys = monthTradesP.keySet().toArray();
        Arrays.sort(keys);

        double lastBalance = initialBalance;
        for (Object key : keys) {
            ArrayList<Integer> pipsA = monthTradesP.get(key);
            int acc = 0;
            for (int i = 0; i < pipsA.size(); i++) {
                acc += pipsA.get(i);
            }
            if (pipsA.size() > 0) {
                total++;
                if (acc > 0) wins++;
            }
        }


        return wins * 100.0 / total;
    }

    //avgDD
    public double getMonthDataDD(double dt) {

        double val = 0;
        ArrayList<Double> values = new ArrayList<Double>();

        Object[] keys = monthData.keySet().toArray();
        Arrays.sort(keys);

        double lastBalance = initialBalance;
        for (Object key : keys) {
            ArrayList<Double> v = monthData.get(key);
            double initialBal = v.get(0);
            double maxDD = 0.0;
            double maxBal = initialBal;
            for (int i = 1; i < v.size(); i++) {
                double aBal = v.get(i);
                if (aBal < maxBal) {
                    double dd = 100.0 - aBal * 100.0 / maxBal;
                    if (dd >= maxDD) maxDD = dd;
                } else {
                    maxBal = aBal;
                }
                ;
            }
            //System.out.println(key+" "+maxDD);
            values.add(maxDD);
        }

        double avg = MathUtils.average(values);
        double dt1 = Math.sqrt(MathUtils.variance(values));

        return avg + dt1 * dt;
    }

    public static int getMiniLots(double actualBalance, int sl, double risk) {

        double riskPosition = actualBalance * risk * 1.0 / 100.0;
        double riskPip = riskPosition / (sl * 0.1);
        int microLots = (int) (riskPip / 0.10);
        if (microLots <= 0) microLots = 1;

        return microLots;
    }

    //avgDD
    public double getMonthDataDDRR(double initialBalance, double risk, double dt) {

        double val = 0;
        ArrayList<Double> values = new ArrayList<Double>();

        Object[] keys = monthTradesP.keySet().toArray();
        Arrays.sort(keys);

        Object[] keysSL = monthTradesSL.keySet().toArray();
        Arrays.sort(keysSL);

        double lastBalance = initialBalance;
        for (Object key : keys) {
            ArrayList<Integer> pipsA = monthTradesP.get(key);
            ArrayList<Integer> pipsSL = monthTradesSL.get(key);
            double initialBal = initialBalance;
            double maxDD = 0.0;
            double maxBal = initialBal;
            double actualBal = initialBal;
            for (int i = 0; i < pipsA.size(); i++) {
                int pips = pipsA.get(i);
                int sl = pipsSL.get(i);
                int microLots = StratPerformance.getMiniLots(actualBal, sl, risk);

                double old = actualBal;
                actualBal += microLots * 0.1 * (pips * 0.1);//precio 1 pip * pips reales
				/*System.out.println(microLots+" "+pips
						+" || "+old
						+" || "+actualBal
						+" "+risk
						);*/
                if (actualBal < maxBal) {
                    double dd = 100.0 - actualBal * 100.0 / maxBal;
                    if (dd >= maxDD) {
                        maxDD = dd;
						/*System.out.println("[maxdd] "+v.get(i)
								+" || "+actualBal+" || "+maxBal
								+" || "+dd
								);*/
                    }
                } else {
                    maxBal = actualBal;
                }
                ;
				/*System.out.println(v.get(i)
						+" || "+actualBal+" || "+maxBal
						);*/
            }
            if (pipsA.size() > 0) {
                //System.out.println(key+" "+maxDD);
                values.add(maxDD);
            }
        }

        double avg = MathUtils.average(values);
        double dt1 = Math.sqrt(MathUtils.variance(values));

        //System.out.println(avg+" "+dt1);

        return avg + dt1 * dt;
    }

    public void reset() {
        actualBalance = 0;
        actualEquitity = 0;
        maxBalance = 0;
        maxEquitity = 0;
        pf = 0.0;
        pfYears = 0.0;
        years = 0;
        avgPips = 0.0;
        maxDD = 0;
        winPips = 0;
        lostPips = 0;
        winPipsNoSLTP = 0;
        lostPipsNoSLTP = 0;
        winPips$ = 0;
        lostPips$ = 0;
        trades = 0;
        wins = 0;
        losses = 0;
        monthData.clear();
        monthTradesP.clear();
        monthTradesSL.clear();
        dayEquitityArr.clear();
        monthEquitity.clear();
        dayPositions.clear();
        maxTradesDays = 0;
    }

    public void addTrade(long miniLots,
                         int pips,
                         int sl,//reward:risk
                         int maxAdversion,
                         int comm,
                         Calendar cal,
                         boolean isSLTP
    ) {
        trades++;
        pips -= comm;
        if (pips >= 0) {
            winPips += pips;
            wins++;
            winPips$ += miniLots * 0.1 * pips * 0.1;
            if (!isSLTP) {
                this.winPipsNoSLTP += pips;
            }
        } else {
            lostPips += -pips;
            losses++;
            lostPips$ += -miniLots * 0.1 * pips * 0.1;
            if (!isSLTP) {
                this.lostPipsNoSLTP += -pips;
            }

        }
        //averagemaxloss
        maxAdversionAcc += maxAdversion;
        maxAdversionAvg = maxAdversionAcc * 0.1 / trades;

        actualBalance += miniLots * 0.1 * pips * 0.1;
        if (actualBalance >= maxBalance) {
            maxBalance = actualBalance;
        } else {
            double actualDD = 100.0 - actualBalance * 100.0 / maxBalance;
            if (actualDD >= maxDD) {
                maxDD = actualDD;
            }
        }


        profitPer = actualBalance * 100.0 / initialBalance - 100.0;

        //System.out.println("[new closed] "+pips+" "+actualBalance+" || "+miniLots+" || "+initialBalance);

        int d = cal.get(Calendar.DAY_OF_YEAR);
        int m = cal.get(Calendar.MONTH);
        int y = cal.get(Calendar.YEAR);

        int key = y * 100 + m;
        if (!monthData.containsKey(key)) {
            monthData.put(key, new ArrayList<Double>());
            monthTradesP.put(key, new ArrayList<Integer>());
            monthTradesSL.put(key, new ArrayList<Integer>());
        }

        monthData.get(key).add(actualBalance);
        monthTradesP.get(key).add(pips);
        monthTradesSL.get(key).add(sl);

        //dailyData
        key = y * 365 + d;
        if (!dayData.containsKey(key)) {
            dayData.put(key, new ArrayList<Double>());
        }

        dayData.get(key).add(actualBalance);
    }

    public void addTrade(long miniLots,
                         int pips,
                         int sl,//reward:risk
                         int maxAdversion,
                         int comm,
                         boolean isSLTP
    ) {
        trades++;
        pips -= comm;
        if (pips >= 0) {
            winPips += pips;
            wins++;
            if (!isSLTP) {
                this.winPipsNoSLTP += pips;
            }
        } else {
            lostPips += -pips;
            losses++;
            if (!isSLTP) {
                this.lostPipsNoSLTP += -pips;
            }
        }
        //averagemaxloss
        maxAdversionAcc += maxAdversion;
        maxAdversionAvg = maxAdversionAcc * 0.1 / trades;

        actualBalance += miniLots * 0.1 * pips * 0.1;
        if (actualBalance >= maxBalance) {
            maxBalance = actualBalance;
        } else {
            double actualDD = 100.0 - actualBalance * 100.0 / maxBalance;
            if (actualDD >= maxDD) {
                maxDD = actualDD;
            }
        }

        //System.out.println("[new closed] "+pips+" "+actualBalance+" || "+miniLots);
        profitPer = actualBalance * 100.0 / initialBalance - 100.0;
    }

    public String toString() {
        double winPer = this.wins * 100.0 / this.trades;
        double pf = this.winPips * 1.0 / this.lostPips;
        double avg = (this.winPips - this.lostPips) * 1.0 / this.trades;
        return this.trades + " " + PrintUtils.Print2dec(winPer, false) + " " + PrintUtils.Print2dec(pf, false) + " " + PrintUtils.Print2dec(avg, false)
                + " " + winPips + " " + lostPips
                ;
    }


    public void resetEquitity() {
        // TODO Auto-generated method stub
        this.actualEquitity = this.actualBalance;
    }

    public void updateEquitity(int miniPips, long miniLots) {

        double importe = miniLots * 0.1 * miniPips * 0.1;
        this.actualEquitity += importe;
        //actualizamos equitity
        actualEquitity += importe;
    }

    public void updateMaxEquitityDD() {

        if (actualEquitity >= maxEquitity) {
            maxEquitity = actualEquitity;
        }
        double actualDD = 100.0 - actualEquitity * 100.0 / maxEquitity;
        if (actualDD >= maxDD) {
            maxDD = actualDD;
        }
		/*double actualDD = 100.0-actualBalance*100.0/maxBalance;
		if (actualDD>=maxDD){
			maxDD = actualDD;
		}*/
    }

    public void updateDailyEquitity(long timeInMillis, double dayEquitity) {
        // TODO Auto-generated method stub
        this.dayEquitityTime.add(timeInMillis);
        this.dayEquitityArr.add(dayEquitity);
    }

    public void updateMonthEquitity(int month, double equitity) {
        if (!monthEquitity.containsKey(month)) {
            monthEquitity.put(month, new ArrayList<Double>());
        }
        ArrayList<Double> datam = monthEquitity.get(month);
        datam.add(equitity);
    }

    public void updateDayOpenPositions(int day, int positions, int maxTrades) {
        if (!dayPositions.containsKey(day)) {
            dayPositions.put(day, 0);
        }
        int actual = dayPositions.get(day);
        if (positions > actual) {
            dayPositions.put(day, positions);
            if (positions >= maxTrades) maxTradesDays++;
        }
    }

    /**
     * Proporciona stats sobre
     * el maxDD medio en intervalos de nDays de trading
     *
     * @param nDays
     * @return
     */
    public String maxDDStats(int nDays) {
        String str = "";

        double topDD = 0;
        ArrayList<Double> maxDDArr = new ArrayList<Double>();
        for (int i = 0; i <= dayEquitityArr.size() - nDays; i++) {
            double eq = dayEquitityArr.get(i);

            double maxDD = 0;
            double maxEquitity = eq;
            for (int j = i + 1; j <= i + nDays - 1; j++) {
                double eqj = dayEquitityArr.get(j);

                if (eqj >= maxEquitity) maxEquitity = eqj;
                else {
                    double dd = 100.0 - eqj * 100.0 / maxEquitity;
                    if (dd >= maxDD) {
                        maxDD = dd;
                    }
                }
            }
            maxDDArr.add(maxDD);
            if (maxDD >= topDD) topDD = maxDD;
        }

        double avgDD = MathUtils.average(maxDDArr);

        str = PrintUtils.Print2dec(avgDD, false) + " " + PrintUtils.Print2dec(topDD, false);

        return str.trim();
    }

    public void getDayReturns() {

        Calendar cal = Calendar.getInstance();
        for (int i = 1; i < dayEquitityTime.size(); i++) {
            long millis = dayEquitityTime.get(i);
            double per0 = this.dayEquitityArr.get(i);
            double per = this.dayEquitityArr.get(i);
            double diff = per * 100.0 / per0 - 100.0;
            cal.setTimeInMillis(millis);
            System.out.println(DateUtils.datePrint(cal) + ';' + PrintUtils.Print2dec(diff, false));
        }
    }

    public double getMonthVar(double percent) {

        double res = 0;
        int countPer = 0;
        int count = 0;

        Object[] keys = monthEquitity.keySet().toArray();
        Arrays.sort(keys);

        double lastBalance = -1;
        int lastKey = -1;
        for (Object key : keys) {
            ArrayList<Double> datas = monthEquitity.get(key);
            double maxBal = 0;
            double maxDD = 0;
            for (int i = 0; i < datas.size(); i++) {
                double bal = datas.get(i);
                if (bal >= maxBal) maxBal = bal;
                else {
                    double dd = 100.0 - bal * 100.0 / maxBal;
                    if (dd >= maxDD) {
                        maxDD = dd;
						/*System.out.println(" [getMonthVar] "
								+" "+PrintUtils.Print2dec(maxBal, false)
								+" "+PrintUtils.Print2dec(bal, false)
								+" "+PrintUtils.Print2dec(maxDD, false)
						);*/
                    }

                }
            }

            if (maxDD >= percent) {
                //System.out.println(PrintUtils.Print2dec(maxDD, false));
                countPer++;
            }

            //System.out.println(PrintUtils.Print2dec(maxDD, false));
            count++;
        }

        return countPer * 100.0 / count;
    }

    public double getDailyAvg() {
        double avg = 0.0;

        ArrayList<Double> changes = new ArrayList<Double>();
        for (int i = 1; i < dayEquitityArr.size(); i++) {
            double change = dayEquitityArr.get(i) - dayEquitityArr.get(i - 1);
            changes.add(change * 100.0 / dayEquitityArr.get(i - 1));
        }

        avg = MathUtils.average(changes);

        return avg;
    }

    public double getDailyDt() {
        double avg = 0.0;
        ArrayList<Double> changes = new ArrayList<Double>();
        for (int i = 1; i < dayEquitityArr.size(); i++) {
            double change = dayEquitityArr.get(i) - dayEquitityArr.get(i - 1);
            changes.add(change * 100.0 / dayEquitityArr.get(i - 1));
        }

        avg = Math.sqrt(MathUtils.varianceD(changes));

        return avg;
    }

    public double getPerAbove(double per) {
        int count = 0;
        int total = 0;
        ArrayList<Double> changes = new ArrayList<Double>();
        for (int i = 1; i < dayEquitityArr.size(); i++) {
            double change = dayEquitityArr.get(i) - dayEquitityArr.get(i - 1);
            changes.add(change * 100.0 / dayEquitityArr.get(i - 1));

            double changePer = change * 100.0 / dayEquitityArr.get(i - 1);

            if (changePer >= per) count++;

            total++;
        }

        return count * 100.0 / total;
    }

    public double getPerBelow(double per) {
        int count = 0;
        int total = 0;
        ArrayList<Double> changes = new ArrayList<Double>();
        for (int i = 1; i < dayEquitityArr.size(); i++) {
            double change = dayEquitityArr.get(i) - dayEquitityArr.get(i - 1);
            changes.add(change * 100.0 / dayEquitityArr.get(i - 1));

            double changePer = change * 100.0 / dayEquitityArr.get(i - 1);

            if (changePer <= per) count++;

            total++;
        }

        return count * 100.0 / total;
    }

    public void addDayReturn(double dayPer) {
        dayReturnArr.add(dayPer);
    }

    public double dayReturnAvg() {
        return MathUtils.average(dayReturnArr);
    }

    public double dayReturnDT() {
        return Math.sqrt(MathUtils.variance(dayReturnArr));
    }

    public int dayReturnDays() {
        return dayReturnArr.size();
    }
}
