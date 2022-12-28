package com.twillio.callback.obd;

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
public class ObdController {

	@Autowired
	private ObdService obdService;

	@PostMapping("/sendOBD")
	public ResponseEntity<?> runObdWebhook(@RequestBody OBDModel obdModel) {
		obdService.runOBD(obdModel.getToPhoneNumber());
		return ResponseEntity.status(HttpStatus.OK).body(new ModelMap().addAttribute("msg", "Success"));
	}

	@RequestMapping(value = "/events", method = RequestMethod.POST)
	public void obdCallBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("-----Obd service callback-----");
		String callStatus = request.getParameter("CallStatus");
		String duration = request.getParameter("Duration");
		String callDuration = request.getParameter("CallDuration");
		String sipResponseCode = request.getParameter("SipResponseCode");
		String recordingSid = request.getParameter("RecordingSid");
		String recordingUrl = request.getParameter("RecordingUrl");
		String recordingDuration = request.getParameter("RecordingDuration");
		String timestamp = request.getParameter("Timestamp");
		String callbackSource = request.getParameter("CallbackSource");
		String sequenceNumber = request.getParameter("SequenceNumber");

		// use CallSid
		log.info("----DialCallerSID : {} and sid :{}", request.getParameter("DialCallSid"),
				request.getParameter("CallSid"));
		log.info(
				" ----CallStatus : {}, duration : {}, callDuration : {}, sipResponseCode : {}, recordingSid : {}, recordingUrl :{}, recordingDuration :{}, timestamp : {}, callBackSource : {}, sequenceNumber : {}",
				callStatus, duration, callDuration, sipResponseCode, recordingSid, recordingUrl, recordingDuration,
				timestamp, callbackSource, sequenceNumber);
	}
}
