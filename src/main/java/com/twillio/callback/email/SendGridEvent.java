package com.twillio.callback.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.ToString;

@ToString
public class SendGridEvent {

	public final String email;
	public final String eventType;
	public final String url;
	public final String sgMessageId;
	public final long timestamp;
	public final String sgEventId;

	public SendGridEvent(@JsonProperty("email") String email, @JsonProperty("event") String eventType,
			@JsonProperty("url") String url, @JsonProperty("sg_message_id") String sgMessageId,
			@JsonProperty("timestamp") long timestamp, @JsonProperty("sg_event_id") String sgEventId) {
		this.email = email;
		this.eventType = eventType;
		this.url = url;
		this.sgMessageId = sgMessageId;
		this.timestamp = timestamp;
		this.sgEventId = sgEventId;
	}
}