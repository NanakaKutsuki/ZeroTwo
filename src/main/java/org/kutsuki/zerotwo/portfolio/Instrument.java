package org.kutsuki.zerotwo.portfolio;

public class Instrument {
    private static final String OPTION = "OPTION";

    private String symbol;
    private String assetType;

    public Instrument() {
	assetType = OPTION;
    }

    public String getSymbol() {
	return symbol;
    }

    public void setSymbol(String symbol) {
	this.symbol = symbol;
    }

    public String getAssetType() {
	return assetType;
    }

    public void setAssetType(String assetType) {
	this.assetType = assetType;
    }
}
