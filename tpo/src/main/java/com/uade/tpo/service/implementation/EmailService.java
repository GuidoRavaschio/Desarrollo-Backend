package com.uade.tpo.service.implementation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // HTML enabled
            emailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Error al cargar el contenido del correo.");
        }
    }

    public List<String> createEmailContentForAppointment(Long id, LocalDate date, LocalTime time, String name_doctor, String action) {
        String subject = "Turno N°" + id + " " + action;
        try {
            Map<String, String> data = Map.of(
                "action", action,
                "date", date.toString(),
                "time", time.toString(),
                "doctor", name_doctor
            );
            String html = loadTemplate("templates/appointment_email.html", data);
            return List.of(subject, html);
        } catch (IOException e) {
            return List.of(subject, "Error al cargar el contenido del correo.");
        }
    }


    public List<String> createEmailContentForInsurance(String digits, String insurance, String action) {
        String subject = "Obra Social " + action + " correctamente";
        try {
            Map<String, String> data = Map.of(
                "action", action,
                "insurance", insurance,
                "digits", digits
            );
            String html = loadTemplate("templates/insurance_email.html", data);
            return List.of(subject, html);
        } catch (IOException e) {
            return List.of(subject, "Error al cargar el contenido del correo.");
        }
    }


    public List<String> createEmailContentForUser(String name, String action) {
        String subject = name + " " + action;
        try {
            Map<String, String> data = Map.of(
                "name", name,
                "action", action
            );
            String html = loadTemplate("templates/user_email.html", data);
            return List.of(subject, html);
        } catch (IOException e) {
            return List.of(subject, "Error al cargar el contenido del correo.");
        }
    }


    public List<String> createEmailContentForCode(int code, String name) {
        String subject = "Tu código para cambiar la contraseña es [" + code + "]";
        try {
            Map<String, String> data = Map.of(
                "name", name,
                "code", String.valueOf(code)
            );
            String html = loadTemplate("templates/code_email.html", data);
            return List.of(subject, html);
        } catch (IOException e) {
            return List.of(subject, "Error al cargar el contenido del correo.");
        }
    }

    public List<String> createEmailContentForReminder(LocalDate date, LocalTime time, String name_doctor) {
    String subject = "📢 Recordatorio: turno en 24 horas";
    try {
        Map<String, String> data = Map.of(
            "date", date.toString(),
            "time", time.toString(),
            "doctor", name_doctor
        );
        String html = loadTemplate("templates/reminder_email.html", data);
        return List.of(subject, html);
    } catch (IOException e) {
        return List.of(subject, "Error al cargar el contenido del correo.");
    }
}


    private String loadTemplate(String path, Map<String, String> replacements) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (var inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return content;
        }
    }
/* 
    private String loadTemplateLocal(String path, Map<String, String> replacements) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }
*/
}
