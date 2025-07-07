package com.uade.tpo.service.implementation;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.tpo.entity.TemporaryData;
import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.RegisterRequest;
import com.uade.tpo.entity.dto.TdRequest;
import com.uade.tpo.entity.dto.UserRequest;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Role;
import com.uade.tpo.repository.TemporaryDataRepository;
import com.uade.tpo.repository.UserRepository;
import com.uade.tpo.security.AuthenticationResponse;
import com.uade.tpo.security.Jwt;
import com.uade.tpo.service.interfaces.UserServiceInterface;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TemporaryDataRepository tdRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private Jwt jwt;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthenticationResponse registerUser(RegisterRequest registerRequest){
        checkForErrors(registerRequest);
                var user = User.builder()
                                .name(registerRequest.getName())
                                .email(registerRequest.getEmail())
                                .password(passwordEncoder.encode(registerRequest.getPassword()))
                                .DNI(registerRequest.getDNI())
                                .build();
                if(registerRequest.getRole() != null){
                        user.setRole(registerRequest.getRole());
                }
                userRepository.save(user);
        return AuthenticationResponse.builder()
                                    .authToken(jwt.generateToken(registerRequest.getEmail()))
                                    .build();
    }

    @Override
    public AuthenticationResponse logIn(UserRequest userRequest){
        User u = userRepository.findByEmail(userRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("El usuario no existe"));
        if (passwordEncoder.matches(userRequest.getPassword(), u.getPassword())){
            return AuthenticationResponse.builder()
                                    .authToken(jwt.generateToken(u.getEmail()))
                                    .build();
        }else{
            throw new RuntimeException("La contraseña es incorrecta");
        }
    }

    @Override
    public User getUser(String code) {
        String email = jwt.getUsernameFromToken(code);
        User u = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No existe usuario"));
        return u;
    }

    @Override
    public void userAuthority(User user, Role role){
        if (user.getRole() != role){
            throw new RuntimeException("El usuario no esta autorizado");
        }
    }

    @Override
    public void deleteUser(String code) {
        User u = getUser(code);
        List<String> emailContent = emailService.createEmailContentForUser(u.getName(), "eliminado");
        userRepository.delete(u);
        emailService.sendEmail(u.getEmail(), emailContent.get(0), emailContent.get(1));
    }

    @Override
    public void editUser(String code, UserRequest userRequest) {
        User u = getUser(code);
        String name = userRequest.getName();
        if (name != null){
            u.setName(name);
        }
        String password = userRequest.getPassword();
        if (password != null){
            u.setPassword(passwordEncoder.encode(password));
        }else{
            throw new RuntimeException("La contraseña no puede ser vacia");
        }
        userRepository.save(u);
        List<String> emailContent = emailService.createEmailContentForUser(u.getName(), "editado");
        emailService.sendEmail(u.getEmail(), emailContent.get(0), emailContent.get(1));
    }

    @Override
    public void uploadInsurance(String code, UserRequest userRequest) {
        User u = getUser(code);
        String action;
        if (u.getCompany() == Company.PARTICULAR){
            action = "registrada";
        }else{
            action = "actualizada";
        }
        Company company = userRequest.getCompany();
        u.setCompany(company);
        String affiliate = userRequest.getAffiliateNumber();
        String digits = affiliate.substring(affiliate.length()-4);
        u.setAffiliateNumber(cryptoService.encrypt(affiliate));
        userRepository.save(u);
        List<String> emailContent = emailService.createEmailContentForInsurance(digits, company.toString(), action);
        emailService.sendEmail(u.getEmail(), emailContent.get(0), emailContent.get(1));
    }

    @Override
    public UserRequest getInsurance(String code) {
        User u = getUser(code);
        UserRequest userRequest = new UserRequest();
        userRequest.setCompany(u.getCompany());
        String encrypted = u.getAffiliateNumber();
        if (encrypted != null && !encrypted.isBlank()) {
            try {
                String decrypted = cryptoService.decrypt(encrypted);
                userRequest.setAffiliateNumber(decrypted);
            } catch (Exception e) {
                throw new RuntimeException("Error desencriptando número de afiliado", e);
            }
        } else {
            userRequest.setAffiliateNumber(null); // no tiene número cargado
        }
        return userRequest;
}

    @Override
    public void requestChangePassword(String email){
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); 
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("EL email no esta registrado en el sistema"));
        save(code, u);
        List<String> emailContent = emailService.createEmailContentForCode(code, email);
        emailService.sendEmail(email, emailContent.get(0), emailContent.get(1));
    }

    @Transactional
    private TemporaryData save(int data, User user) {
        tdRepository.deleteByUser(user);
        TemporaryData temp = TemporaryData.builder()
            .data(data)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .user(user)
            .build();

        return tdRepository.save(temp);
}

    @Override
    public void validateCode(TdRequest tdRequest) {
        tdRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        TemporaryData td = tdRepository.findByEmail(tdRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("El codigo ha expirado"));
        if (tdRequest.getCode() != td.getData()){
            throw new RuntimeException("El codigo no es correcto");
        }
        tdRepository.delete(td);
    }

    @Override
    public void changePassword(UserRequest userRequest) {
        User u = userRepository.findByEmail(userRequest.getEmail()).orElseThrow(() -> new RuntimeException("El usuario no existe"));
        if (userRequest.getPassword() == null ? userRequest.getConfirm_password() == null : userRequest.getPassword().equals(userRequest.getConfirm_password())){
            u.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            userRepository.save(u);
        }else{
            throw new RuntimeException("La contraseña no coincide o se encuentra vacia");
        }
    }
    
    public UserRequest getUserRequest(String code){
        User u = getUser(code);
        UserRequest ur = new UserRequest();
        ur.setEmail(u.getEmail());
        ur.setName(u.getName());
        return ur;
    }
    private void checkForErrors(RegisterRequest userRequest){
        int dni = userRequest.getDNI();
        if (userRepository.existsByDNI(dni)){
                throw new RuntimeException("El usuario ya existe");
        }
        String email = userRequest.getEmail();
        if (!EmailValidator.getInstance().isValid(email)){
                throw new RuntimeException("Email invalido");
        }
        String password = userRequest.getPassword();
        if (!(password == null ? userRequest.getConfirm_password() == null : password.equals(userRequest.getConfirm_password()))){
                throw new RuntimeException("La contraseña no coincide");
        }
        }
}
