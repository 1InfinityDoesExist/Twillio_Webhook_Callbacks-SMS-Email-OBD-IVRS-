package com.twillio.callback.ivrs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twillio.callback.utility.EncryptDecryptUtils;
import com.twillio.callback.utility.KeysRetrivalServiceImpl;
import com.twillio.callback.utility.QueryParamParser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class IVRSController {

	@Autowired
	private IVRSService ivrSServiceImpl;

	@Value("${reset.password.encryptionKey}")
	private String encryptionKey;

	@Value("${reset.password.characterEncoding}")
	private String characterEncoding;

	@Value("${reset.password.cipherTransformation}")
	private String cipherTransformation;

	@Value("${reset.password.aesEncryptionAlgorithem}")
	private String aesEncryptionAlgorithem;

	@Value("${twillio.account.id}")
	private String accountID;

	@Value("${twillio.account.oauth.token}")
	private String authToken;

	@Value("${twillio.account.from.phone.number}")
	private String fromPhoneNumber;

	@Autowired
	private KeysRetrivalServiceImpl keysRetrivalServiceImpl;

	@GetMapping("/test")
	public void testing(HttpServletRequest request, HttpServletResponse response) throws IOException {

		try {
			try {
				ivrSServiceImpl.doPost(request, response);
			} catch (URISyntaxException | InterruptedException | UnirestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@PostMapping(value = "/get-gather-voice", produces = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> getGatheredSay(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
			@RequestParam("securityToken") String securityToken) {

		log.info("-------Inside Single Level IVR Controller--- token : {}", securityToken);

		String securityTokenDetails = EncryptDecryptUtils.decrypt(securityToken.replaceAll(" ", "+"),
				cipherTransformation, characterEncoding, aesEncryptionAlgorithem, encryptionKey);
		log.info("----------After Decrypting--- {}", securityTokenDetails);
		Map<String, String> parameters = QueryParamParser.getQueryMap(securityTokenDetails);
		String keys = parameters.get("keys");

		log.info("-----Keys after decryption : {}", keys);
		String config = parameters.get("config");

		log.info("------config  after decryption : {}", config);
		String level = parameters.get("level");

		log.info("-----level after decryption : {}", level);

		Map<String, Object> payloadObject = null;
		Map<String, String> keysValue = new LinkedHashMap<>();
		Map<String, Object> configObject = new LinkedHashMap<>();
		try {
			keysValue = new ObjectMapper().readValue(keys, Map.class);

			log.info("------Converting String keys to Object (Map Object ) : {}", keysValue);
			configObject = new ObjectMapper().readValue(config, Map.class);
			log.info("------Converting String config to Object (Map Object ) : {}", configObject);

			payloadObject = (Map<String, Object>) configObject.get("payload");
			log.info("PayloadObject : {}", payloadObject);

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Twilio.init(accountID, authToken);

		String numberPressed = servletRequest.getParameter("Digits");
		log.info("-------Gather Number Pressed : {}", numberPressed);

		String callSid = servletRequest.getParameter("CallSid");
		log.info("-------CallSID : {}", callSid);

		Say sayMessage = null;
		Gather input = null;
		if (!ObjectUtils.isEmpty(keysValue.get(numberPressed))) {
			log.info("----------Number Pressed : {}", keysValue.get(numberPressed));
			if (payloadObject.get(keysValue.get(numberPressed)) instanceof String) {

				String finalOutput = (String) payloadObject.get(keysValue.get(numberPressed));
				log.info("-----Final Outpu to be spoken  : {} ", finalOutput);

				log.info("----------To be spoken over call : {}", finalOutput);
				sayMessage = new Say.Builder(finalOutput).voice(Say.Voice.ALICE).language(Say.Language.EN_US).build();
				input = new Gather.Builder().inputs(Gather.Input.DTMF).numDigits(1).say(sayMessage).finishOnKey("#")
						.language(Gather.Language.EN_US).debug(true).build();

			} else {
				log.info("-----Response is a object ------");
				List list = (List) payloadObject.get(keysValue.get(numberPressed));
				log.info("------Level : {} and Keys list : {}", level, list);
				Map<String, Object> otherPaylaodLevel = new LinkedHashMap<String, Object>();
				otherPaylaodLevel.put("payload", (Map<String, Object>) list.get(0));

				log.info("--------OtherPayload : {}", otherPaylaodLevel);

				String newLevel = level + numberPressed + "-";
				log.info("------OtherLevel but hardcoded to 0-: {}", newLevel);

//				Map<String, String> levelKeys = keysRetrivalServiceImpl.getKeys(otherPaylaodLevel, newLevel);
				Map<String, String> levelKeys = keysRetrivalServiceImpl.getKeys(otherPaylaodLevel, "0-");
				log.info("-----Next level : {}", levelKeys);

				String speak = levelKeys.entrySet().stream().map(e -> e.getValue()).collect(Collectors.joining(","));
				log.info("-----Speak : {}", speak);

				sayMessage = new Say.Builder(speak).voice(Say.Voice.ALICE).language(Say.Language.EN_US).build();
				log.info("---Iterative--SayMessage : {}", sayMessage);

				try {
					String newSecurityToken = EncryptDecryptUtils.encrypt(
							"keys=" + new ObjectMapper().writeValueAsString(levelKeys) + "&" + "config="
									+ new ObjectMapper().writeValueAsString(otherPaylaodLevel) + "&" + "level="
									+ newLevel,
							cipherTransformation, characterEncoding, aesEncryptionAlgorithem, encryptionKey);
					log.info("--------NewSecurityToken : {}", newSecurityToken);
					input = new Gather.Builder().inputs(Gather.Input.DTMF).numDigits(1)
							.action("https://sengagement.herokuapp.com/get-gather-voice?securityToken="
									+ newSecurityToken)
							.timeout(30).say(sayMessage).finishOnKey("#").language(Gather.Language.EN_US).debug(true)
							.build();

					log.info("----Iterative -Input : {}", input);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		} else {
			throw new RuntimeException("---------You have pressed unknown number-------");
		}

		VoiceResponse twiml = new VoiceResponse.Builder().gather(input).build();

		log.info("--------Call SID before  : {}", callSid);
		log.info("-----Twillio XML : {}", twiml.toXml());
		Call updateCall = Call.updater(callSid).setTwiml(new com.twilio.type.Twiml(twiml.toXml()))
				.setStatusCallback(URI.create("https://ingress-gateway.gaiansolutions.com/utility-service/events"))
				.setStatusCallbackMethod(HttpMethod.POST).update();
		log.info("---------Call SID After : {}", updateCall.getSid());

		return ResponseEntity.status(HttpStatus.OK).body(twiml.toXml());

	}
}
