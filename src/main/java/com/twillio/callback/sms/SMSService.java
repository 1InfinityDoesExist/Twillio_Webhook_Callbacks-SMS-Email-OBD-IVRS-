package com.twillio.callback.sms;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SMSService {
	@Value("${twillio.sms.account_sid}")
	private String ACCOUNT_SID;

	@Value("${twillio.sms.oauth_token}")
	private String AUTH_TOKEN;

	@Value("${twillio.sms.from}")
	private String FROM;

	@Value("${twillio.sms.callback_uri}")
	private String CALLBACK_URI;

	public void sendSMS(String to, String msg) {

		log.info(" {} , {}, {}, {}", CALLBACK_URI, FROM, AUTH_TOKEN, ACCOUNT_SID);
		log.info("----Sending sms via twillio----");
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message message = Message
				.creator(new com.twilio.type.PhoneNumber(to), new com.twilio.type.PhoneNumber(FROM), msg)
				.setStatusCallback(URI.create(CALLBACK_URI)).create();

		log.info("----Message : {}", message.toString());

	}

}
