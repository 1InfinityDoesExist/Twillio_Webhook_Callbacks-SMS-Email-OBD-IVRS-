package com.twillio.callback.email;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sendgrid.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * The 202 Accepted Status Code returned from SendGrid does not really indicate
 * that the message has been successfully delivered to the recipients inbox. It
 * indicates that the message is valid and has been "Queued For Delivery".
 * 
 * @author gaian
 *
 */

@Slf4j
@RestController
@RequestMapping("/v1.0/twillio")
public class EmailController {

	@Autowired
	private EmailService emailService;

	@Value("${email.verification.body}")
	private String emailBody;

	@PostMapping("/sendEmail")
	public ResponseEntity<?> sendEmail(@RequestBody EmailModel model) {
		log.info("----EmailModel : {}", model);
		Response sendMail = emailService.sendMail(model.getEmailTo(),
				String.format("Your one time password (OTP) - %s", 100000), String.format(emailBody, 100000));
		return ResponseEntity.status(HttpStatus.OK).body(new ModelMap().addAttribute("msg", sendMail));
	}

	@RequestMapping(value = "/inbound", method = { RequestMethod.POST }, consumes = { "application/json" })
	public ResponseEntity<?> sendgridWebhook(HttpServletRequest request, HttpServletResponse response,
			@RequestBody List<SendGridEvent> events) {
		log.info("-----Process sendgridWebhook------");
		log.info(String.format("Received %d events", events.size()));
		events.forEach(event -> {
			log.info("-----Event : {}", event);
		});
		return ResponseEntity.status(HttpStatus.OK).body(new ModelMap().addAttribute("msg", "See logs"));
	}

}
