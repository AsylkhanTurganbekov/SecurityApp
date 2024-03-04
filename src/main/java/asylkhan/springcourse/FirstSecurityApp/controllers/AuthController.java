package asylkhan.springcourse.FirstSecurityApp.controllers;

import asylkhan.springcourse.FirstSecurityApp.dto.AuthenticationDTO;
import asylkhan.springcourse.FirstSecurityApp.dto.PersonDTO;
import asylkhan.springcourse.FirstSecurityApp.models.Person;
import asylkhan.springcourse.FirstSecurityApp.security.JWTUtil;
import asylkhan.springcourse.FirstSecurityApp.services.RegistrationService;
import asylkhan.springcourse.FirstSecurityApp.util.PersonValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;

    private final AuthenticationManager authenticationManager;
    private final RegistrationService registrationService;

    private final JWTUtil jwtUtil;

    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(PersonValidator personValidator, AuthenticationManager authenticationManager, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper) {
        this.personValidator = personValidator;
        this.authenticationManager = authenticationManager;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("person") Person person) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    public ResponseEntity<Map<String, String>> performRegistration(@Valid @RequestBody PersonDTO personDTO,
                                                                   BindingResult bindingResult) {
        Map<String, String> response = new HashMap<>();

        // Validate incoming request
        personValidator.validate(personDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            response.put("message", "Validation error");
            return ResponseEntity.badRequest().body(response);
        }

        // Convert DTO to entity
        Person person = convertToPerson(personDTO);

        // Perform registration
        registrationService.register(person);

        // Generate JWT token
        String token = jwtUtil.generateToken(person.getUsername());
        response.put("jwt-token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthenticationDTO authenticationDTO) {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authenticationDTO.getUsername(),
                        authenticationDTO.getPassword());

        try {
            authenticationManager.authenticate(authInputToken);
        } catch (BadCredentialsException e) {
            return Map.of("message", "Incorrect credentials!");
        }

        String token = jwtUtil.generateToken(authenticationDTO.getUsername());
        return Map.of("jwt-token", token);
    }

    public Person convertToPerson(PersonDTO personDTO) {
        Person person = new Person();
        person.setUsername(personDTO.getUsername());
        person.setYearOfBirth(personDTO.getYearOfBirth());
        person.setPassword(personDTO.getPassword());

        return person;
    }
}
