package com.example.item.tracker.component;

import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

@Slf4j
@Component
public class SendMessages {

    public void sendReport(InputStream is, String emailAddress) throws CustomException {
        try {
            byte[] fileContent = IOUtils.toByteArray(is);
            send(makeEmail(fileContent, emailAddress));
        } catch (IOException e) {
            log.error("Failed to send report. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "Failed to send report: ", e.getMessage());
        }
    }

    public void send(MimeMessage message) throws CustomException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            message.writeTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "", e.getMessage());
        }
        ByteBuffer buf = ByteBuffer.wrap(outputStream.toByteArray());
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        SdkBytes data = SdkBytes.fromByteArray(arr);
        RawMessage rawMessage = RawMessage.builder().data(data).build();
        SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();
        log.info("Attempting to send an email through Amazon SES...");
        try (SesClient client = SesClient.builder().region(Region.EU_WEST_3).build()) {
            client.sendRawEmail(rawEmailRequest);
        } catch (SesException e) {
            log.error("Failed to send email. Error: {}", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "Failed to send email: ", e.getMessage());
        }
    }

    private MimeMessage makeEmail(byte[] attachment, String emailAddress) throws CustomException {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        String subject = "Weekly AWS Status Report";
        try {
            message.setSubject(subject, "UTF-8");
            String sender = "jaber.h.abbas@gmail.com";
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));

            MimeBodyPart wrap = getMimeBodyPart();

            MimeMultipart msg = new MimeMultipart("mixed");
            msg.addBodyPart(wrap);

            MimeBodyPart att = new MimeBodyPart();
            DataSource fds = new ByteArrayDataSource(attachment, "application/vnc.openxmlformats-officedocument.spreadsheetml.sheet");
            att.setDataHandler(new DataHandler(fds));
            String attachmentName = "WorkReport.xls";
            att.setFileName(attachmentName);

            msg.addBodyPart(att);
            message.setContent(msg);

        } catch (MessagingException e) {
            log.error("MessagingException while making email. Error {} ", e.getMessage());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "", "MessagingException while making email: " + e.getMessage());
        }
        return message;
    }

    private static MimeBodyPart getMimeBodyPart() throws MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        String bodyText = "Hello,\r\n\r\nPlease see the attached file for a weekly update.";
        textPart.setContent(bodyText, "text/plain; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        String bodyHTML = "<!DOCTYPE html><html lang=\"en-US\"><body><h1>Hello!</h1><p>Please see the attached file for a weekly update.</p></body></html>";
        htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

        MimeMultipart msgBody = new MimeMultipart("alternative");
        msgBody.addBodyPart(textPart);
        msgBody.addBodyPart(htmlPart);

        MimeBodyPart wrap = new MimeBodyPart();
        wrap.setContent(msgBody);
        return wrap;
    }
}
