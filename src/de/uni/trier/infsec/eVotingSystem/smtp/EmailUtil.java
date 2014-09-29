package de.uni.trier.infsec.eVotingSystem.smtp;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class EmailUtil {
 
    
    public static void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
        	
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
          System.out.println("Message is ready");
          Transport.send(msg);  
 
          System.out.println("EMail Sent Successfully!!");
        }
        catch (Exception e) {
          e.printStackTrace();
        }
    }
}