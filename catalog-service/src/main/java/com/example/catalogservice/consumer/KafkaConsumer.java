package com.example.catalogservice.consumer;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final CatalogRepository catalogRepository;

    @KafkaListener(topics="example-order-kafka")
    public void processMessage(String kafkaMessage) {
        log.info("Kafka Message ====> " + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            map = objectMapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        }catch (Exception e){
            e.printStackTrace();
        }
        CatalogEntity catalogEntity = catalogRepository.findByProductId((String) map.get("productId"));
        catalogEntity.setStock(catalogEntity.getStock() - (Integer) map.get("qty"));

        catalogRepository.save(catalogEntity);

    }
}
