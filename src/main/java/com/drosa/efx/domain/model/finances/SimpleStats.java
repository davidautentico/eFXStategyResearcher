package com.drosa.efx.domain.model.finances;

import java.util.ArrayList;
import java.util.List;

import com.drosa.efx.domain.utils.MathUtils;
import com.drosa.efx.domain.utils.PrintUtils;


public class SimpleStats {
	
	int nBars;
	int dt;
	int sl;
	int positiveYears;
	int negativeYears;
	List<Double> sharpeList;
	List<Integer> tradesList;
	
	public SimpleStats(int nBars,int dt, int sl){
		this.nBars = nBars;
		this.dt = dt;
		this.sl = sl;
		this.positiveYears = 0;
		this.negativeYears = 0;
		sharpeList = new ArrayList<Double>();
		tradesList = new ArrayList<Integer>();
	}

	public int getPositiveYears() {
		return positiveYears;
	}


	public void setPositiveYears(int positiveYears) {
		this.positiveYears = positiveYears;
	}


	public int getNegativeYears() {
		return negativeYears;
	}


	public void setNegativeYears(int negativeYears) {
		this.negativeYears = negativeYears;
	}


	public List<Double> getSharpeList() {
		return sharpeList;
	}


	public void setSharpeList(List<Double> sharpeList) {
		this.sharpeList = sharpeList;
	}


	public List<Integer> getTrades() {
		return tradesList;
	}


	public void setTrades(List<Integer> trades) {
		this.tradesList = trades;
	}
	
	public double getPositivePercent(){
		return positiveYears*100.0/(positiveYears+negativeYears);
	}
		
	public void addStats(double sharpe, int trades){
		if (sharpe>0) this.positiveYears ++;
		else this.negativeYears++;
		this.sharpeList.add(sharpe);
		this.tradesList.add(trades);
	}
	
	@Override
	public String toString(){
		
		double positivePer = positiveYears*100.0/(positiveYears+negativeYears);
		double avgSharpe = sharpeList.stream()
	    .mapToDouble(Double::doubleValue)
	    .average()
	    .orElse(Double.NaN);
		
		double dtv = Math.sqrt(MathUtils.variance(sharpeList));
		
		double avgTrades = tradesList.stream()
			    .mapToInt(Integer::intValue)
			    .average()
			    .orElse(Double.NaN);
		
		return nBars+" "+dt+" "+PrintUtils.Print2Int(sl, 2)
				+" || " + PrintUtils.Print2dec(avgSharpe, false)
				+" "+PrintUtils.Print2dec(dtv, false)
				+" "+PrintUtils.Print2dec(avgTrades, false)
				+" "+PrintUtils.Print2dec(positivePer, false);
	}
}
