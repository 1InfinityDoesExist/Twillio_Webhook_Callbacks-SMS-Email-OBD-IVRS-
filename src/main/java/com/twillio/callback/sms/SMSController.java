package com.twillio.callback.sms;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1.0/twillio")
public class SMSController {

	@Autowired
	private SMSService smsService;

	@PostMapping("/sendSMS")
	public ResponseEntity<?> sendSMS(@RequestBody SMSModel model) {
		log.info("-----TO : {} and MSG : {}", model.getTo(), model.getMsg());
		smsService.sendSMS(model.getTo(), model.getMsg());
		return ResponseEntity.status(HttpStatus.OK).body(new ModelMap().addAttribute("msg", "Success"));
	}

	// Deploy this code on heroku or gaian server to https
	/**
	 * Twillio MSG CallBack URL
	 * 
	 * Queued or Accepted: Twilio has accepted your API request, and has queued your
	 * message to be sent.
	 * 
	 * Sending: Twilio is forwarding your message request to one of our Super
	 * Network partners.
	 * 
	 * Sent: Twilio has received a confirmation from our Super Network partner
	 * advising they have accepted the message.
	 * 
	 * 
	 * Delivered: Twilio has received a confirmation from our Super Network partner
	 * advising the message has been delivered.
	 * 
	 * 
	 * 
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/MessageStatus", method = RequestMethod.POST)
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String messageSid = request.getParameter("MessageSid");
		String messageStatus = request.getParameter("MessageStatus");
		log.info("SID: {}, Status: {}", messageSid, messageStatus);
	}

}
