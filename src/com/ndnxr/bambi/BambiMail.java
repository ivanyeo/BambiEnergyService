package com.ndnxr.bambi;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class BambiMail extends javax.mail.Authenticator {
	/** Debug mode */
	private boolean debug;

	public String username;
	public String password;

	public String serverAddress;
	public String serverPort;
	public String socketFactoryPort;

	/** SMTP Authentication */
	public boolean authRequired;

	public String from;

	/** String[] of addresses to send email to */
	public String[] toArray;

	public String subject;
	public String messageBody;

	public Multipart multipart;

	private BambiMail() {
		// Initial Setup
		debug = false;
		authRequired = true;

		// Defaults Setting of gmail
		serverAddress = "smtp.gmail.com";
		serverPort = "465"; // Alternative port: 587
		socketFactoryPort = "465";

		// Create MultiPart Object
		multipart = new MimeMultipart();

		// Minor bug in Java Mail; Reference:
		// http://www.jondev.net/articles/Sending_Emails_without_User_Intervention_(no_Intents)_in_Android
		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public BambiMail(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}

	public BambiMail(String username, String password, String serverAddress,
			String serverPort, String from, String[] toArray, String subject,
			String messageBody) {
		this();

		this.username = username;
		this.password = password;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.from = from;
		this.toArray = toArray;
		this.subject = subject;
		this.messageBody = messageBody;
	}

	public boolean sendEmail() {

		// Error Check
		if (!validateSettings()) {
			G.Log("BambiMail::sendEmail(): Failed at validating settings.");
			return false;
		}

		// Create properties for use
		Properties properties = new Properties();
		properties.put("mail.smtp.host", serverAddress);

		if (debug) {
			properties.put("mail.debug", "true");
		}

		if (authRequired) {
			properties.put("mail.smtp.auth", "true");
		}

		properties.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");
		properties.put("mail.smtp.port", serverPort);
		properties.put("mail.smtp.socketFactory.port", socketFactoryPort);

		// Create Session based on Properties
		Session session = Session.getInstance(properties, this);
		MimeMessage message = new MimeMessage(session);
		
		try {
			// Set from
			message.setFrom(new InternetAddress(from));

			// Set to
			ArrayList<String> toList= new ArrayList<String>();
			for (int i = 0; i < toArray.length; i++) {
				if (toArray[i] != null && !toArray[i].equals("")) {
					toList.add(toArray[i]);
				}
			}
			
			toArray = toList.toArray(new String[toList.size()]);
			
			InternetAddress[] addressTo = new InternetAddress[toArray.length];
			for (int i = 0; i < toArray.length; i++) {
				addressTo[i] = new InternetAddress(toArray[i]);
			}
			message.setRecipients(MimeMessage.RecipientType.TO, addressTo);
			
			// Set subject
			message.setSubject(subject);

			// Set current date
			message.setSentDate(new Date());

			// Create message body
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(messageBody);
			multipart.addBodyPart(messageBodyPart);

			// Put multipart content in the message
			message.setContent(multipart);

			// Finally, send email
			Transport.send(message);
		} catch (MessagingException e) {
			G.Log("BambiMail::sendEmail(): " + e.getMessage());
		}

		G.Log("BambiMail::sendEmail(): Success! ");
		
		return true;
	}

	public void addFileAttachment(String filepath) {
		// Create mesasge body
		BodyPart messageMimePart = new MimeBodyPart();
		
		// Set data source
		DataSource source = new FileDataSource(filepath);
		
		// Add to multipart
		try {
			messageMimePart.setDataHandler(new DataHandler(source));
			messageMimePart.setFileName(filepath);
			multipart.addBodyPart(messageMimePart);
		} catch (MessagingException e) {
			G.Log("BambiMail::addFileAttachment(): Unable to add file - " + filepath);
		}
		
	}

	/**
	 * Method that checks all the settings are non-empty or null. This method is
	 * invoked by sendEmail() as a pre-processing step.
	 * 
	 * @return true if all required settings are set; false otherise
	 */
	private boolean validateSettings() {
		return (!serverAddress.equals("") && !serverPort.equals("")
				&& !socketFactoryPort.equals("") && !from.equals("")
				&& toArray.length > 0 && !username.equals("")
				&& !password.equals("") && !subject.equals("")
				&& !messageBody.equals("") && multipart != null);
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}
}
