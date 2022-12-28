package com.twillio.callback.orchestrator;

import java.util.Date;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.twillio.callback.utility.GenericRestTemplateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Ref :
 * https://developer.twitter.com/en/docs/twitter-api/premium/account-activity-api/guides/securing-webhooks
 * 
 * @author gaian
 *
 */

@RestController
@Slf4j
public class OrchestratorCallBack {

	@Autowired
	private GenericRestTemplateUtil genericRestTemplateUtil;

	private final String ORCHESTRATOR_SECRET = "gAUztDbnOxh3ewCZdVSiUqpH8tPFcWHvywfpzBtJUVelSPEptE";

	@GetMapping("/orchestrator/callback/webhooks")
	public ResponseEntity<?> crcTwitter(@RequestParam(value = "crc_token") String crcToken) {
		String hash = null;
		log.info("----CRC Token from Orchestrator : {} and orchestaror Key : {}", crcToken, ORCHESTRATOR_SECRET);
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(ORCHESTRATOR_SECRET.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			hash = Base64.encodeBase64String(sha256_HMAC.doFinal(crcToken.getBytes()));
			log.info("----Hash Code that will be validated by orchestrator side : {}", hash);

		} catch (Exception e) {
			throw new RuntimeException("----Use agaian exception and throw proper error msg.");
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ModelMap().addAttribute("response_token", "sha256=" + hash));
	}

//	@PostMapping("/orchestrator/callback/webhooks")
//	public void eventHandler(HttpServletRequest request) throws Exception {
//		log.info("---------Data sent to webhook : {}", data);
//		String pushedJsonAsString = IOUtils.toString(request.getInputStream(), "utf-8");
//		log.info(" Event response : {}", pushedJsonAsString);
//
//		JSONObject entries = (JSONObject) new JSONParser().parse(pushedJsonAsString);
//
//		log.info("-----Data sent via orchestrator------");
//	}

	// Challenge Response Check
	@GetMapping("/register/orchestrator/webhook")
	public ResponseEntity<?> registerWebhook(@RequestParam(value = "webhook_url") String webhook_url, String tenantId)
			throws ParseException {

		log.info("-----Webhook ur : {}", webhook_url);
		// Fetch tenantData

		String tenantSecret = "gAUztDbnOxh3ewCZdVSiUqpH8tPFcWHvywfpzBtJUVelSPEptE";
		String crc = UUID.randomUUID().toString() + new Date().toString();

		MultiValueMap<String, String> customHeaders = new LinkedMultiValueMap<>();
		customHeaders.add("Accept", "application/json");
		customHeaders.add("Content-Type", "application/json");

		HttpHeaders headers = new HttpHeaders(customHeaders);
		webhook_url = webhook_url + "?crc_token=" + crc;

		String response = genericRestTemplateUtil.performRestCall(HttpMethod.GET, webhook_url, headers, null,
				String.class);

		log.info("-------Response : {}", response);
		JSONObject jsonObject = (JSONObject) new JSONParser().parse(response);

		String userToken = (String) jsonObject.get("response_token");
		log.info("----------UserGeneratedToken : {}", userToken);

		String orchestratorToken = getOrchestratorToken(crc, tenantSecret);
		log.info("------OrchestratorGeneratedToken : {}", orchestratorToken);

		if (userToken.equals(orchestratorToken)) {
			log.info("-----------Success----------");
		}

		// now register it in db

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ModelMap().addAttribute("msg", "Webhook url registered successfully"));

	}

	private String getOrchestratorToken(String crcToken, String tenantSecret) {
		String hash = null;
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(tenantSecret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			hash = Base64.encodeBase64String(sha256_HMAC.doFinal(crcToken.getBytes()));
			log.info("----Hash Code that will be validated by orchestrator side : {}", hash);
			return "sha256=" + hash;
		} catch (Exception e) {
			throw new RuntimeException("----Use agaian exception and throw proper error msg.");
		}
	}

}
