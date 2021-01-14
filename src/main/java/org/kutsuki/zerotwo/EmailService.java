package org.kutsuki.zerotwo;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private static final String EMSP = "&emsp;";
    private static final String EXCEPTION_SUBJECT = "Exception Thrown";
    private static final String LINE_BREAK = "<br/>";
    private static final String IMAGE = "<img src=\"cid:";
    private static final String IMAGE_END = "\"/>";

    @Value("${spring.mail.username}")
    private String from;

    @Value("${email.home}")
    private String home;

    @Autowired
    private JavaMailSender javaMailSender;

    // emailHome
    public void email(String subject, String htmlBody) {
	email(null, subject, htmlBody, null);
    }

    // emailHome
    public void email(String subject, String htmlBody, DataSource inlineImage) {
	email(null, subject, htmlBody, inlineImage);
    }

    // email
    public void email(String bcc, String subject, String htmlBody, DataSource inlineImage) {
	try {
	    MimeMessage msg = javaMailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(msg, true);
	    helper.setFrom(from);
	    helper.setTo(home);
	    helper.setSubject(subject);

	    if (bcc != null) {
		helper.setBcc(StringUtils.split(bcc, ','));
	    }

	    if (inlineImage != null) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(htmlBody)) {
		    sb.append(htmlBody);
		    sb.append(LINE_BREAK);
		    sb.append(LINE_BREAK);
		}

		sb.append(IMAGE);
		sb.append(inlineImage.getName());
		sb.append(IMAGE_END);

		// text needs to be set first before inline
		helper.setText(sb.toString(), true);

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
		mimeBodyPart.setContentID('<' + inlineImage.getName() + '>');
		mimeBodyPart.setDataHandler(new DataHandler(inlineImage));
		mimeBodyPart.setFileName(inlineImage.getName());
		helper.getMimeMultipart().addBodyPart(mimeBodyPart);
	    } else {
		helper.setText(htmlBody, true);
	    }

	    javaMailSender.send(msg);
	} catch (MessagingException e) {
	    LOGGER.error("Error trying to Email: " + htmlBody, e);
	}
    }

    // emailException
    public void emailException(String message, Throwable e) {
	StringBuilder sb = new StringBuilder();
	sb.append(message);
	sb.append(LINE_BREAK);
	sb.append(LINE_BREAK);
	sb.append(e.getClass().getName());
	sb.append(':').append(StringUtils.SPACE);
	sb.append(e.getMessage());
	sb.append(LINE_BREAK);

	for (StackTraceElement element : e.getStackTrace()) {
	    sb.append(EMSP);
	    sb.append(element.toString());
	    sb.append(LINE_BREAK);
	}

	email(EXCEPTION_SUBJECT, sb.toString());
    }

    // getLinkBreak
    public String getLineBreak() {
	return LINE_BREAK;
    }
}
