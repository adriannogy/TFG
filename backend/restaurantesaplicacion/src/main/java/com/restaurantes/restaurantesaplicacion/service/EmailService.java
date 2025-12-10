package com.restaurantes.restaurantesaplicacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
/**
     * Envía un email para el restablecimiento de contraseña.
     * @param to La dirección de email del destinatario.
     * @param token El token de restablecimiento a incluir en el enlace.
     */
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Restablecimiento de Contraseña");
        message.setText("Para restablecer tu contraseña, haz clic en el siguiente enlace: \n"
                + "http://localhost:3000/reset-password?token=" + token);
        mailSender.send(message);
    }
/**
     * Envía un email de bienvenida a un nuevo usuario registrado.
     * @param to La dirección de email del nuevo usuario.
     * @param name El nombre del nuevo usuario para personalizar el saludo.
     */
    public void sendWelcomeEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("¡Bienvenido a Gastrolog!");
        message.setText("Hola " + name + ",\n\nTu cuenta ha sido creada con éxito. ¡Ya puedes empezar a valorar restaurantes!");
        mailSender.send(message);
    }

    /**
    * Envía un email de verificación de cuenta.
    * @param to La dirección de email del destinatario.
    * @param token El token de verificación.
    */
    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
     message.setTo(to);
        message.setSubject("Verifica tu cuenta de GastroLog");
        message.setText("Gracias por registrarte. Por favor, haz clic en el siguiente enlace para activar tu cuenta: \n"
            + "http://localhost:8080/api/auth/verify?token=" + token);
        mailSender.send(message);
}
}
