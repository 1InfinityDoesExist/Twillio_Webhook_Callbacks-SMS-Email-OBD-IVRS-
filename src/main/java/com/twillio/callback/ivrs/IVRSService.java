package com.twillio.callback.ivrs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import com.twillio.callback.utility.EncryptDecryptUtils;
import com.twillio.callback.utility.KeysRetrivalServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IVRSService {
	@Value("${reset.password.encryptionKey:ABCDEFGHIJKLMNOP}")
	private String encryptionKey;

	@Value("${reset.password.characterEncoding:UTF8}")
	private String characterEncoding;

	@Value("${reset.password.cipherTransformation:AES/CBC/PKCS5PADDING}")
	private String cipherTransformation;

	@Value("${reset.password.aesEncryptionAlgorithem:AES}")
	private String aesEncryptionAlgorithem;

	@Value("${twillio.account.id:AC0663502436ec89dba188c12bbc40dec0}")
	private String accountID;

	@Value("${twillio.account.oauth.token:5ddb6756f37fed72ddc537086150d655}")
	private String authToken;

	@Value("${twillio.account.from.phone.number:+15165189868}")
	private String fromPhoneNumber;

	@Autowired
	private KeysRetrivalServiceImpl keysRetrivalServiceImpl;

	public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws IOException, URISyntaxException, InterruptedException, UnirestException {

		Twilio.init(accountID, authToken);

		VoiceResponse response = getPlanets(servletRequest, servletResponse);
		// Response resp = getPlanet();

		Call call = Call
				.creator(new PhoneNumber("+919354125136"), new PhoneNumber(fromPhoneNumber),
						new Twiml(response.toXml()))
				.setStatusCallback(URI.create("https://ingress-gateway.gaiansolutions.com/utility-service/events"))
				.setStatusCallbackEvent(Arrays.asList("queued", "initiated", "in-progress", "ringing", "completed"))
				.setStatusCallbackMethod(HttpMethod.POST).create();

		servletResponse.setContentType("text/xml");
		try {
			servletResponse.getWriter().write(response.toXml());
		} catch (TwiMLException e) {
			throw new RuntimeException(e);
		}

	}

	private VoiceResponse getPlanets(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws JsonMappingException, JsonProcessingException {

		String str = "{\n" + "    \"payload\": {\n" + "        \"Press 1 for hindi\": [\n" + "            {\n"
				+ "                \"Press 1 for Hindi Classic\": [\n" + "                    {\n"
				+ "                        \"Press 1 for Hindi Classic HR\": \"Hello piyush\",\n"
				+ "                        \"Press 2 for Hindi Classic Abazz\": \"Its  by abazz right here\"\n"
				+ "                    }\n" + "                ],\n" + "                \"Press 2 for Hindi Pop\": [\n"
				+ "                    {\n"
				+ "                        \"Press 1 for Hindi Pop HR\": \"Hello  Avinash\",\n"
				+ "                        \"Press 2 for Hindi Pop Abazz\": \"Its hi  abazz right here\"\n"
				+ "                    }\n" + "                ]\n" + "            }\n" + "        ],\n"
				+ "        \"Press 2 for English\": [\n" + "            {\n"
				+ "                \"Press 1 for English Classic\": [\n" + "                    {\n"
				+ "                        \"Press 1 for English Classic HR\": \"Hello Saketh\",\n"
				+ "                        \"Press 2 for English Classic Abazz\": \"Its  wrong abazz right here\"\n"
				+ "                    }\n" + "                ],\n"
				+ "                \"Press 2 for English Pop\": [\n" + "                    {\n"
				+ "                        \"Press 1 for English Pop HR\": \"Hello Paket\",\n"
				+ "                        \"Press 2 for English Pop Abazz\": \"Its right abazz right here\"\n"
				+ "                    }\n" + "                ]\n" + "            }\n" + "        ]\n" + "    }\n"
				+ "}";

		Map<String, Object> tenantConfig = null;
		tenantConfig = new ObjectMapper().readValue(str, Map.class);

		Map<String, String> keys = keysRetrivalServiceImpl.getKeys(tenantConfig, "0-");
		keys.entrySet().stream().sorted(Map.Entry.<String, String>comparingByKey());

		String speak = keys.entrySet().stream().map(e -> e.getValue()).collect(Collectors.joining(","));
		log.info("-----To be speaked for fist time : {}", speak);

		Say sayMessage = new Say.Builder("Welcome to Gaian Solutions, I V R S , " + speak).voice(Say.Voice.POLLY_ADITI)
				.language(Say.Language.EN_IN).build();

		try {

			log.info("-------keys : {}", new ObjectMapper().writeValueAsString(keys));
			log.info("-------config : {}", new ObjectMapper().writeValueAsString(tenantConfig));

			String securityToken = EncryptDecryptUtils.encrypt(
					"keys=" + new ObjectMapper().writeValueAsString(keys) + "&" + "config="
							+ new ObjectMapper().writeValueAsString(tenantConfig) + "&" + "level=" + "0-",
					cipherTransformation, characterEncoding, aesEncryptionAlgorithem, encryptionKey);

			Gather input = new Gather.Builder().inputs(Gather.Input.DTMF).numDigits(1)
					.action("https://sengagement.herokuapp.com/get-gather-voice?securityToken=" + securityToken)
					.timeout(30).say(sayMessage).finishOnKey("#").language(Gather.Language.EN_IN).debug(true).build();
			VoiceResponse twiml = new VoiceResponse.Builder().gather(input).build();
			log.info("----------Voice Response : {}", twiml);
			return twiml;
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		throw new RuntimeException("-----Failed ----");

	}

}
