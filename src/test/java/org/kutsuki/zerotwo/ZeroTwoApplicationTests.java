package org.kutsuki.zerotwo;

import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private OpeningRepository repository;

    @Autowired
    private EmailService service;

    @Test
    public void contextLoads() {
	// boolean test = true;
	// String msg = "#527 FILLED BOT +1 BUTTERFLY RIOT 22 JAN 21 25/30/35 CALL @.65
	// ISE";
	// manager.parseMessage(msg, "", test);

	String image = "https://i.imgur.com/hnAJbSW.png";
	RestTemplate template = new RestTemplate();
	byte[] response = template.getForObject(image, byte[].class);
	String type = "image/" + StringUtils.substringAfterLast(image, '.');
	ByteArrayDataSource attachment = new ByteArrayDataSource(response, type);
	String name = StringUtils.substringAfterLast(image, '/');
	attachment.setName(name);

	service.email(null, "Test " + System.currentTimeMillis(), "Test", attachment);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
