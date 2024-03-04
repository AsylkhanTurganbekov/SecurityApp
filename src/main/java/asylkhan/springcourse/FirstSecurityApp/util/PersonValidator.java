package asylkhan.springcourse.FirstSecurityApp.util;


import asylkhan.springcourse.FirstSecurityApp.dto.PersonDTO;
import asylkhan.springcourse.FirstSecurityApp.models.Person;
import asylkhan.springcourse.FirstSecurityApp.services.PersonDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {

    private final PersonDetailsService personDetailsService;

    @Autowired
    public PersonValidator(PersonDetailsService personDetailsService) {
        this.personDetailsService = personDetailsService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Person.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PersonDTO personDTO = (PersonDTO) o;

        try {
            personDetailsService.loadUserByUsername(personDTO.getUsername());
            errors.rejectValue("username", "", "A person with this username already exists.");
        } catch (UsernameNotFoundException ignored) {
            // Everything is fine, the user does not exist
        }
    }

}
