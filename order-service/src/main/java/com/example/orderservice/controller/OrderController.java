package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.producer.KafkaProducer;
import com.example.orderservice.producer.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {
    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's Working in Order Service on Port %s"
                , env.getProperty("local.server.port"));
    }

    //http://127.0.0.1/order-service/{user_id}/order
    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder orderDetails) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = modelMapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /**
         * jpa
         */
        //OrderDto createdOrder = orderService.createOrder(orderDto);

        /**
         * kafka
         */
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());
        /**
         * Send an order to the kafka
         */
        kafkaProducer.send("example-order-kafka",orderDto);
        orderProducer.send("orders",orderDto);


        ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {
        List<ResponseOrder> responseOrderList = new ArrayList<>();
        orderService.getOrdersByUserId(userId).forEach(order -> responseOrderList.add(new ModelMapper().map(order, ResponseOrder.class)));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrderList);
    }

}
