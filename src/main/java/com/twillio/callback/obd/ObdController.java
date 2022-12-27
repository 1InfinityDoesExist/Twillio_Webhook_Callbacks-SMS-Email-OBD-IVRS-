package com.twillio.callback.obd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
