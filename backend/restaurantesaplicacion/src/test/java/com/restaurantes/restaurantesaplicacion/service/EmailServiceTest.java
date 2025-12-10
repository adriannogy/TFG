package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    public void testSendPasswordResetEmail() {
        // Given
        String toEmail = "usuario@dominio.com";
        String token = "reset-token-123";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendPasswordResetEmail(toEmail, token);

        // Then
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertNotNull(capturedMessage.getTo());
        assertEquals(toEmail, capturedMessage.getTo()[0]);
        assertEquals("Restablecimiento de Contraseña", capturedMessage.getSubject());
        assertNotNull(capturedMessage.getText());
        assertTrue(capturedMessage.getText().contains("http://localhost:3000/reset-password?token=" + token));
    }

   
    @Test
    public void testSendWelcomeEmail() {
        // Given
        String toEmail = "nuevo@usuario.com";
        String nombre = "Adrián";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendWelcomeEmail(toEmail, nombre);

        // Then
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertNotNull(capturedMessage.getTo());
        assertEquals(toEmail, capturedMessage.getTo()[0]);
        assertEquals("¡Bienvenido a Gastrolog!", capturedMessage.getSubject());
        assertNotNull(capturedMessage.getText());
        assertTrue(capturedMessage.getText().contains("Hola " + nombre));
    }

   
    @Test
    public void testSendVerificationEmail() {
        // Given
        String toEmail = "verificar@cuenta.com";
        String token = "verify-token-456";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendVerificationEmail(toEmail, token);

        // Then
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertNotNull(capturedMessage.getTo());
        assertEquals(toEmail, capturedMessage.getTo()[0]);
        assertEquals("Verifica tu cuenta de GastroLog", capturedMessage.getSubject());
        assertNotNull(capturedMessage.getText());
        assertTrue(capturedMessage.getText().contains("http://localhost:8080/api/auth/verify?token=" + token));
    }
}