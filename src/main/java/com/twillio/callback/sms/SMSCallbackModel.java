package com.twillio.callback.sms;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SMSCallbackModel implements Serializable {

	private static final long serialVersionUID = -1235714670015365417L;

	private String messageSid;
	private String messageStatus;
	private String messagingServiceSid;
	private String accountSid;
	private String from;
	private String apiVersion;
	private String to;
	private String smsStatus;
	private String smsSid;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}

}
