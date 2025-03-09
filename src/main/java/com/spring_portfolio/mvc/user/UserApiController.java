package com.spring_portfolio.mvc.user;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserJpaRepository repository;

    @Autowired
    private DetailsService detailsService;

    @GetMapping("/")
    public ResponseEntity<List<User>> getPeople() {
        return new ResponseEntity<>(repository.findAllByOrderByNameAsc(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        Optional<User> optional = repository.findById(id);
        return optional.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<User> deleteUser(@RequestParam("email") String email) {
        User user = repository.findByEmail(email);
        if (user != null) {
            repository.deleteByEmail(email);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/createUser")
    public ResponseEntity<Object> postUser(@RequestBody User userRequest) {
        try {
            Date dob = userRequest.getDob();
            User user = new User(userRequest.getEmail(), userRequest.getPassword(), userRequest.getName(), dob);
            detailsService.save(user);
            return new ResponseEntity<>(Map.of("message", userRequest.getEmail() + " is created successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error processing the request."), HttpStatus.BAD_REQUEST);
        }
    }


   
    

    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> userSearch(@RequestBody final Map<String, String> map) {
        String term = map.get("term");
        List<User> list = repository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping(value = "/stats/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Map<String, Object>>> getStatsById(@PathVariable long id) {
        Optional<User> optional = repository.findById(id);
        if (optional.isPresent()) {
            User user = optional.get();
            Map<String, Map<String, Object>> stats = user.getStats();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update")
    public ResponseEntity<Object> putUser(@RequestParam("email") String email, @RequestBody User userRequest) {
        User user = repository.findByEmail(email);
        if (user != null) {
            user.setPassword(userRequest.getPassword());
            user.setName(userRequest.getName());
            user.setDob(userRequest.getDob());
            repository.save(user);
            return new ResponseEntity<>(Map.of("message", email + " is updated successfully"), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of("error", "user not found"), HttpStatus.NOT_FOUND);
    }

    @PostMapping("/createAdmin")
    public ResponseEntity<Object> postAdminuser(@RequestBody User userRequest) {
        try {
            Date dob = userRequest.getDob();
            User user = new User(userRequest.getEmail(), userRequest.getPassword(), userRequest.getName(), dob);
            detailsService.save(user);
            detailsService.addRoleToUser(userRequest.getEmail(), "ROLE_ADMIN");
            return new ResponseEntity<>(Map.of("message", userRequest.getEmail() + " is created successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", "Error processing the request."), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/image/post")
    public ResponseEntity<String> saveImage(MultipartFile image, @RequestParam("username") String username) throws IOException {
        User existingFileOptional = repository.findByEmail(username);
        if (existingFileOptional != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] bytearr = image.getBytes();
            String encodedString = encoder.encodeToString(bytearr);
            existingFileOptional.setImageEncoder(encodedString);
            repository.save(existingFileOptional);
            return new ResponseEntity<>("Image saved successfully", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("user not found", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/image/{email}")
    public ResponseEntity<?> downloadImage(@PathVariable String email) {
        User user = repository.findByEmail(email);
        if (user != null && user.getImageEncoder() != null) {
            byte[] imageBytes = DatatypeConverter.parseBase64Binary(user.getImageEncoder());
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.valueOf("image/png"))
                    .body(imageBytes);
        }
        return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
    }
}
