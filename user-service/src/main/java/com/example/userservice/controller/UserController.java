package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.ResponseUser;
import com.example.userservice.vo.ReuqestUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
    }

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in User Service "
                + ", port(local.server.port)=" +  environment.getProperty("local.server.port")
                + ", port(server.port)=" +  environment.getProperty("server.port")
                + ", token secret=" +  environment.getProperty("token.secret")
                + ", token expiration time=" +  environment.getProperty("token.expiration_time")
        );
    }

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUsersByAll();

        List<ResponseUser> responseUsers = new ArrayList<>();
        userList.forEach(user -> {
            responseUsers.add(new ModelMapper().map(user, ResponseUser.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(responseUsers);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUsers(@PathVariable String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser responseUser = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createUser(@RequestBody ReuqestUser user) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = modelMapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
}
