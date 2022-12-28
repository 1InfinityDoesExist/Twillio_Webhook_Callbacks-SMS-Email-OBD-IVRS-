package com.twillio.callback.obd;

import java.net.URI;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ObdService {

	@Value("${twillio.account.id}")
	private String accountID;

	@Value("${twillio.account.oauth.token}")
	private String authToken;

	@Value("${twillio.account.from.phone.number}")
	private String fromPhoneNumber;

	public String runOBD(String toPhoneNumber) {

		log.info("-----Run OBD----");
		Twilio.init(accountID, authToken);
		Call call = Call
				.creator(new com.twilio.type.PhoneNumber(toPhoneNumber),
						new com.twilio.type.PhoneNumber(fromPhoneNumber),
						URI.create("http://demo.twilio.com/docs/classic.mp3"))
				.setMethod(HttpMethod.GET)
				.setStatusCallback(URI.create("https://ingress-gateway.gaiansolutions.com/utility-service/events"))
				.setStatusCallbackEvent(Arrays.asList("queued", "initiated", "in-progress", "ringing", "completed"))
				.setStatusCallbackMethod(HttpMethod.POST).create();

		log.info("-----SID : {}", call.getSid());
		return call.getSid();
	}

}
