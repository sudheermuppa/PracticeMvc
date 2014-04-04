package com.loyaltymethods.fx.run;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
/**
 * Provides basic alerting functionality based on whatever metadata was configured.
 * 
 * @author Emil
 *
 */
public class AlertManager {
	Logger log = Logger.getLogger(AlertManager.class);

	private MailSender mailSender;
	private SimpleMailMessage alertMessage;
	private Map<String, Map<String,String>> alerts;		// defined externally

	/**
	 * Send an alert of type "success" or "failure" to recepients identified
	 * for that type under the alerts map.
	 * 
	 * @param type
	 * @param subject
	 * @param message
	 */
	public void alert(String type, String subject, String message) {
		if(alerts == null)
			return;
		
		Map<String, String> alertSet = alerts.get(type);
		
		if(alertSet == null)
			return;
		
		for( String alert : alertSet.keySet()) {
			alertMessage.setSubject(subject);
			alertMessage.setText(message);
			alertMessage.setTo(alertSet.get(alert).split("\\s*,\\s*"));
			
			try {
				mailSender.send(alertMessage);
			}catch(RuntimeException e) {
				log.error("Can not send alert message out: "+e.toString());
				// we swallow the problem, but blurt out an error log.
			}
		}
	}
	
	public void start(String subject, String message) {
		alert("Start",subject,message);
	}
	
	public void success(String subject, String message) {
		alert("Success",subject,message);
	}
	
	public void error(String subject, String message) {
		alert("Error",subject,message);
	}


	public Map<String, Map<String, String>> getAlerts() {
		return alerts;
	}

	public void setAlerts(Map<String, Map<String, String>> alerts) {
		this.alerts = alerts;
	}

	public MailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public SimpleMailMessage getAlertMessage() {
		return alertMessage;
	}

	public void setAlertMessage(SimpleMailMessage alertMessage) {
		this.alertMessage = alertMessage;
	}
}
