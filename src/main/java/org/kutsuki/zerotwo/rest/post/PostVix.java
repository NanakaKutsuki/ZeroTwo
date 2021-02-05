package org.kutsuki.zerotwo.rest.post;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostVix {
    @JsonProperty("$VIX.X")
    public Vix vix;

    public Vix getVix() {
	return vix;
    }

    public class Vix {
	private String lastPrice;

	public String getLastPrice() {
	    return lastPrice;
	}
    }
}
