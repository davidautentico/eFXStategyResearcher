package com.drosa.efx.domain.model.experimental.basicStrategies.base;

public class BollingerStrat {
	String currency;
	int hour = -1;
	int nbars = 50;
	double dt = 0.20;
	double sl = 0.4;
	double risk = 0.1;
	boolean isEnabled = true;
	
	public BollingerStrat(String aCurrency,int hour,int nbars,double dt,double sl,double risk,boolean isEnabled){
		this.currency = aCurrency;
		this.hour = hour;
		this.nbars = nbars;
		this.dt = dt;
		this.sl = sl;
		this.risk = risk;
		this.isEnabled = isEnabled;
	}
	
	public void setParameters(int nbars,double dt,double sl,double risk,boolean isEnabled){
		this.nbars = nbars;
		this.dt = dt;
		this.sl = sl;
		this.risk = risk;
		this.isEnabled = isEnabled;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getNbars() {
		return nbars;
	}
	public void setNbars(int nbars) {
		this.nbars = nbars;
	}
	public double getDt() {
		return dt;
	}
	public void setDt(double dt) {
		this.dt = dt;
	}
	public double getSl() {
		return sl;
	}
	public void setSl(double sl) {
		this.sl = sl;
	}

	public double getRisk() {
		return risk;
	}

	public void setRisk(double risk) {
		this.risk = risk;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	

}
