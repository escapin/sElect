package de.uni.trier.infsec.eVotingSystem.smtp;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.uni.trier.infsec.eVotingSystem.apps.AppParams;
 
public class TLSEmail {
     
	
// public static void main(String[] args) {
//    
//	 sendEmail("scapin.enrico@gmail.com", "test1", "test2");
//      
// }
 
 // just a wrapper
 public static void sendEmail(String toEmail, String subject, String body){
	 Properties props = new Properties();
	 props.put("mail.smtp.host", AppParams.SMTP_HOST);			//SMTP Host
	 props.put("mail.smtp.port", AppParams.SMTP_PORT);			//TLS Port
	 props.put("mail.smtp.starttls.enable", "true");			//enable STARTTLS
	 props.put("fromMailAddress", AppParams.FROM_EMAIL);
	 props.put("fromMailName", AppParams.FROM_NAME);
	 Session session;
	 if(AppParams.PASSWORD!=null){
		 props.put("mail.smtp.auth", "true"); //enable authentication
		 // create Authenticator object to pass in Session.getInstance argument
		 Authenticator auth = new Authenticator() {
		 //override the getPasswordAuthentication method
		 protected PasswordAuthentication getPasswordAuthentication() {
			 	return new PasswordAuthentication(AppParams.FROM_EMAIL, AppParams.PASSWORD);
		 	}
		 };
		session = Session.getInstance(props, auth);
	 } else {
		 props.put("mail.smtp.auth", "false"); //enable authentication
		 session = Session.getInstance(props);
	 }
	 sendEmail(session, toEmail, subject, body);
 }
 
 private static void sendEmail(Session session, String toEmail, String subject, String body){
     try {
     	
       MimeMessage msg = new MimeMessage(session);
       // set message headers
       msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
       msg.addHeader("format", "flowed");
       msg.addHeader("Content-Transfer-Encoding", "8bit");
       
       Properties props = session.getProperties();
       
       InternetAddress ia = new InternetAddress(	(String) props.get("fromMailAddress"), 
     		  									(String) props.get("fromMailName"));
       msg.setFrom(ia);
       
       msg.setReplyTo(new InternetAddress[]{ia});

       msg.setSubject(subject, "UTF-8");

       msg.setText(body, "UTF-8");

       msg.setSentDate(new Date());

       msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
       System.out.println("Sending email to: " + toEmail);
       Transport.send(msg);  
       System.out.println("...email sent!");
     
     } catch (Exception e) {
       e.printStackTrace();
     }
 }
 
 
}