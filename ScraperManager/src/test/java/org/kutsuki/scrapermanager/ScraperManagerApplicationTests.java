package org.kutsuki.scrapermanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScraperManagerApplicationTests {
    @Autowired
    private org.kutsuki.scrapermanager.rest.ScraperRest rest;

    @Test
    void contextLoads() {
	System.out.println(rest.getShadowLink());
    }
}
