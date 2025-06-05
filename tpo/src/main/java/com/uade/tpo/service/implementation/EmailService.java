package com.uade.tpo.service.implementation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  
        message.setTo(to);  
        message.setSubject(subject); 
        message.setText(text); 

        emailSender.send(message);
    }

    public List<String> createEmailContentForAppointment(Long id, LocalDate date, LocalTime time, String name_doctor, String action){
        String subject = "Turno n°" + Long.toString(id) + " " + action;
        String one = "Tu turno fue " + action + " con exito:\n";
        String two = "Dia: " + date.toString() + "\n";
        String three = "Hora: " + time.toString() + "\n";
        String four = "Profesional: " + name_doctor + "\n \n";
        String five = "Gracias por usar nuestra pagina :)";
        String text = one + two + three + four + five;
        return List.of(subject, text);
    }

    public List<String> createEmailContentForInsurance(String digits, String insurance, String action){
        String subject = "Obra Social " + action + " correctamente";
        String one = "Su obra social ha sido " + action + " con exito. Para confirmar, verifique los siguientes datos:\n";
        String two = "Obra Social: " + insurance + "\n";
        String three = "Numero de afiliado: Termina en" + digits + "\n";
        String four = "Gracias por usar nuestra pagina :)";
        String text = one + two + three + four;
        return List.of(subject, text);
    }

    public List<String> createEmailContentForUser(String name, String action){
        String subject = name + " " + action;
        String text = "Querido " + name + ", tu perfil ha sido " + action;
        return List.of(subject, text);
    }

    public List<String> createEmailContentForCode(int code, String name){
        String subject = "Su codigo  para el cambio de contraseña es [" + Integer.toString(code) + "]";
        String text1 = "Querido " + name + ", su codigo  para el cambio de contraseña es [" + Integer.toString(code) + "] \n ";
        String text = text1 + "Advertencia: el codigo expirará en 5 minutos. Asegure utilizarlo en ese lapso de tiempo";
        return List.of(subject, text);
    }
}