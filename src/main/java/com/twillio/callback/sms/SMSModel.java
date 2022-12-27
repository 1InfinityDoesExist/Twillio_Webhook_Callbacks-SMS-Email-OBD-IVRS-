package com.twillio.callback.sms;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SMSModel implements Serializable {
	private String to;
	private String msg;

}
