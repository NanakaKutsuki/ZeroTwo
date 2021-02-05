package org.kutsuki.zerotwo.rest.post;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostSpx {
    public String underlyingPrice;

    public PutExpDateMap putExpDateMap;

    public String getUnderlyingPrice() {
	return underlyingPrice;
    }

    public PutExpDateMap getPutExpDateMap() {
	return putExpDateMap;
    }

    public class PutExpDateMap {
	@JsonProperty("2022-01-21:350")
	private PutExpDate putExpDate;

	public PutExpDate getPutExpDate() {
	    return putExpDate;
	}
    }

    public static class PutExpDate {
	@JsonProperty("2000.0")
	private Strike[] strikes;

	public Strike[] getStrikes() {
	    return strikes;
	}
    }

    public static class Strike {
	private String mark;
	private String theoreticalVolatility;

	public String getMark() {
	    return mark;
	}

	public String getTheoreticalVolatility() {
	    return theoreticalVolatility;
	}
    }
}
