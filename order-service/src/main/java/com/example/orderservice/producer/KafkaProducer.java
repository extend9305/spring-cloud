package com.example.orderservice.producer;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderDto send(String kafkaTopic, OrderDto orderDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInString = "";

        try{
            jsonInString = objectMapper.writeValueAsString(orderDto);
        }catch (Exception e){
            e.printStackTrace();
        }
        kafkaTemplate.send(kafkaTopic, jsonInString);
        log.info("Order sent to kafka orderDto {}", orderDto);

        return orderDto;
    }
}
