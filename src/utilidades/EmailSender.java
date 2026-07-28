package utilidades;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // Configuración del correo emisor
    private static final String EMAIL_FROM = "miguel.ineztroda@gmail.com";
    // IMPORTANTE: Para Gmail, debes usar una "Contraseña de aplicación" (App
    // Password)
    // No uses tu contraseña personal. Ve a Seguridad en tu cuenta de Google para
    // generarla.
    private static final String PASSWORD = "jdxt dlgl avdt hapq";

    public static boolean enviarCorreoRecuperacion(String emailDestino, String token) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Recuperación de Contraseña - Inversiones Olvan");

            String htmlContent = "<h2>Solicitud de Recuperación de Contraseña</h2>"
                    + "<p>Has solicitado recuperar tu contraseña en el Sistema de Inversiones Olvan.</p>"
                    + "<p>Tu código de recuperación es: <strong>" + token + "</strong></p>"
                    + "<p>Este código expirará en 15 minutos.</p>"
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
}
