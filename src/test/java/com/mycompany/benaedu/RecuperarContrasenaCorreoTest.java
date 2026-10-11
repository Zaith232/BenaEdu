package com.mycompany.benaedu;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** Verifica los destinatarios de recuperación sin enviar correos ni modificar usuarios. */
public final class RecuperarContrasenaCorreoTest {
    private RecuperarContrasenaCorreoTest() { }

    /** Comprueba copia al administrador, ausencia de duplicados y validación del correo. */
    public static void main(String[] args) throws Exception {
        MimeMessage mensaje = new MimeMessage(Session.getInstance(new Properties()));
        mensaje.setRecipient(Message.RecipientType.TO, new InternetAddress("usuario@example.com"));
        RecuperarContraseña.agregarCopiaAdministrador(mensaje, " admin@example.com ", "usuario@example.com");
        if (mensaje.getRecipients(Message.RecipientType.TO).length != 1
                || mensaje.getRecipients(Message.RecipientType.CC).length != 1
                || !((InternetAddress) mensaje.getRecipients(Message.RecipientType.CC)[0])
                        .getAddress().equals("admin@example.com")) {
            throw new AssertionError("El usuario y el administrador deben recibir el mismo mensaje");
        }
        MimeMessage propio = new MimeMessage(Session.getInstance(new Properties()));
        propio.setRecipient(Message.RecipientType.TO, new InternetAddress("admin@example.com"));
        RecuperarContraseña.agregarCopiaAdministrador(propio, "ADMIN@example.com", "admin@example.com");
        if (propio.getAllRecipients().length != 1) throw new AssertionError("Se duplicó al administrador");
        for (String correo : new String[]{null, "", "   ", "correo-invalido", "a@example.com,b@example.com"}) {
            try {
                RecuperarContraseña.validarCorreoAdministrador(correo);
                throw new AssertionError("Se aceptó un correo de administrador inválido");
            } catch (MessagingException esperada) {
                // Estos correos deben impedir el cambio de contraseña antes de acceder a la base de datos.
            }
        }
        System.out.println("OK: copia al administrador, sin duplicados y validación de correo.");
    }
}
