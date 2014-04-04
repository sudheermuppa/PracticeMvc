package com.loyaltymethods.fx.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations={"/MailTest.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class MailTest {

	@Autowired
	private MailSender mailSender;
	
	@Autowired
	private SimpleMailMessage alertMailMessage;
	
	@Test
	public void test() {
		alertMailMessage.setText("Hey something weird happened.");
		this.mailSender.send(alertMailMessage);
	}

}
