package org.kutsuki.zerotwo.portfolio;

public class OrderLegCollection {
    private String instruction;
    private int quantity;
    private Instrument instrument;

    public void createInstrument(String symbol) {
	Instrument instrument = new Instrument();
	instrument.setSymbol(symbol);
	setInstrument(instrument);
    }

    public String getInstruction() {
	return instruction;
    }

    public void setInstruction(String instruction) {
	this.instruction = instruction;
    }

    public int getQuantity() {
	return quantity;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    public Instrument getInstrument() {
	return instrument;
    }

    public void setInstrument(Instrument instrument) {
	this.instrument = instrument;
    }
}
