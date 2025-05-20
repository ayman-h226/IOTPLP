package com.plp.iotplatform.model;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqttMessage {
    private Double value;
    private Long timestamp;
}