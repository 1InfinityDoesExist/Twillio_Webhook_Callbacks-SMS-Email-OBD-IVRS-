package com.twillio.callback.email;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class SendGridEvent {

	public final String email;
	public final long timestamp;
	public final String smtpId;
	public final String event;
	public final String category;
	public final String sgEventId;
	public final String sgMessageId;
	public final String userAgent;
	public final String ip;
	public final String url;
	public final long asmGroupId;
	public final String reason;
	public final String status;
	public final String response;
	public final String type;
	public final String attempt;
	public final String urlOffset;

	public SendGridEvent(@JsonProperty("email") String email, @JsonProperty("timestamp") long timestamp,
			@JsonProperty("smtp-id") String smtpId, @JsonProperty("event") String event,
			@JsonProperty("category") String category, @JsonProperty("sg_event_id") String sgEventId,
			@JsonProperty("sg_message_id") String sgMessageId, @JsonProperty("useragent") String userAgent,
			@JsonProperty("ip") String ip, @JsonProperty("url") String url,
			@JsonProperty("asm_group_id") long asmGroupId, @JsonProperty("reason") String reason,
			@JsonProperty("status") String status, @JsonProperty("response") String response,
			@JsonProperty("type") String type, @JsonProperty("attempt") String attempt,
			@JsonProperty("url_offset") String urlOffset) {
		this.email = email;
		this.timestamp = timestamp;
		this.smtpId = smtpId;
		this.event = event;
		this.category = category;
		this.sgEventId = sgEventId;
		this.sgMessageId = sgMessageId;
		this.userAgent = userAgent;
		this.ip = ip;
		this.url = url;
		this.asmGroupId = asmGroupId;
		this.reason = reason;
		this.status = status;
		this.response = response;
		this.type = type;
		this.attempt = attempt;
		this.urlOffset = urlOffset;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}
}