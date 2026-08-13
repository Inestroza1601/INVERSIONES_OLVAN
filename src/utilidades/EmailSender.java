package utilidades;

import java.io.File;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender {

    // Configuraci\u00F3n del correo emisor
    private static final String EMAIL_FROM = "caesolutions504@gmail.com";
    private static final String PASSWORD = "hhrk jsit ftrh lfkq";

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
                    }
                });
    }

    public static boolean enviarCorreoRecuperacion(String emailDestino, String token) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Recuperaci\u00F3n de Contrase\u00F1a - Inversiones Olvan");

            String htmlContent = "<h2>Solicitud de Recuperaci\u00F3n de Contrase\u00F1a</h2>"
                    + "<p>Has solicitado recuperar tu contrase\u00F1a en el Sistema de Inversiones Olvan.</p>"
                    + "<p>Tu c\u00F3digo de recuperaci\u00F3n es: <strong>" + token + "</strong></p>"
                    + "<p>Este c\u00F3digo expirar\u00E1 en 15 minutos.</p>"
                    + "<p>Si no solicitaste esto, puedes ignorar este correo.</p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error enviando correo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean enviarReporteConAdjunto(String emailDestino, String rutaPDF, String tituloReporte) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Reporte del Sistema: " + tituloReporte);

            // Cuerpo del correo
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            String htmlContent = "<h2>Adjunto encontrar\u00E1 su reporte: " + tituloReporte + "</h2>"
                    + "<p>Este es un reporte generado autom\u00E1ticamente desde el Sistema de Inversiones Olvan.</p>"
                    + "<p>Saludos.</p>";
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            // Archivo adjunto
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.attachFile(new File(rutaPDF));

            // Combinar texto y archivo
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            message.setContent(multipart);

            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Error enviando correo con adjunto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
