package com.drosa.efx.domain.model.finances;

public class TimeFrecuency {
	
	long frecuency;
		
	public long getFrecuency() {
		return frecuency;
	}
	public void setFrecuency(long frecuency) {
		this.frecuency = frecuency;
	}
	
	public void incFrecuency(){
		frecuency++;
	}
}
