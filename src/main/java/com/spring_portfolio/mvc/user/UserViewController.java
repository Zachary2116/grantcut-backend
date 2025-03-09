package com.spring_portfolio.mvc.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

// Built using article: https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html
// or similar: https://asbnotebook.com/2020/04/11/spring-boot-thymeleaf-form-validation-example/
@Controller
@RequestMapping("/mvc/user")
public class UserViewController {
    // Autowired enables Control to connect HTML and POJO Object to database easily for CRUD
    @Autowired
    private DetailsService repository;

    @GetMapping("/read")
    public String user(Model model) {
        List<User> list = repository.listAll();
        model.addAttribute("list", list);
        return "user/read";
    }

    /*  The HTML template Forms and UserForm attributes are bound
        @return - template for User form
        @param - User Class
    */
    @GetMapping("/create")
    public String userAdd(User user) {
        return "user/create";
    }

    /* Gathers the attributes filled out in the form, tests for and retrieves validation error
    @param - User object with @Valid
    @param - BindingResult object
     */
    @PostMapping("/create")
    public String userSave(@Valid User user, BindingResult bindingResult) {
        // Validation of Decorated UserForm attributes
        if (bindingResult.hasErrors()) {
            return "user/create";
        }
        repository.save(user);
        repository.addRoleToUser(user.getEmail(), "ROLE_STUDENT");
        // Redirect to next step
        return "redirect:/mvc/user/read";
    }

    @GetMapping("/update/{id}")
    public String userUpdate(@PathVariable("id") int id, Model model) {
        model.addAttribute("user", repository.get(id));
        return "user/update";
    }

    @PostMapping("/update")
    public String userUpdateSave(@Valid User user, BindingResult bindingResult) {
        // Validation of Decorated UserForm attributes
        if (bindingResult.hasErrors()) {
            return "user/update";
        }
        repository.save(user);
        repository.addRoleToUser(user.getEmail(), "ROLE_STUDENT");

        // Redirect to next step
        return "redirect:/mvc/user/read";
    }

    @GetMapping("/delete/{id}")
    public String userDelete(@PathVariable("id") long id) {
        repository.delete(id);
        return "redirect:/mvc/user/read";
    }

    @GetMapping("/search")
    public String user() {
        return "user/search";
    }

}
