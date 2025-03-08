package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.error.FeignErrorDecoder;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.commons.config.DefaultsBindHandlerAdvisor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DefaultsBindHandlerAdvisor.MappingsProvider mappingsProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment environment;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
                true,true,true,true,
                new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));

        userRepository.save(userEntity);
        UserDto returnUserDto = modelMapper.map(userEntity, UserDto.class);
        return returnUserDto;
    }


    @Override
    public Iterable<UserEntity> getUsersByAll() {
        return userRepository.findAll();
    }


    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
//        String orderUrl = String.format(environment.getProperty("order_service.url"),userId);
//        ResponseEntity<List<ResponseOrder>> ordersResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                new ParameterizedTypeReference<List<ResponseOrder>>() {
//                });
//        List<ResponseOrder> orders = ordersResponse.getBody();
        /**
         * Using a feign client
         */
//        List<ResponseOrder> orders = null;
//        try {
//            orders = orderServiceClient.getOrders(userId);
//            userDto.setOrders(orders);
//        }catch (FeignException e){
//            log.error(e.getMessage());
//        }
        /**
         * ErrorDecoder
         */
//        List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId)
                , throwable -> new ArrayList<>());

        userDto.setOrders(orders);

        return userDto;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);

        if(userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        return userDto;
    }
}
