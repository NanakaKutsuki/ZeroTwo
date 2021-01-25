package org.kutsuki.zerotwo.document;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Skip extends AbstractDocument {
    private int tradeId;

    public int getTradeId() {
	return tradeId;
    }

    public void setTradeId(int tradeId) {
	this.tradeId = tradeId;
    }
}
