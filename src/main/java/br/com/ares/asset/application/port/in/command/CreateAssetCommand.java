package br.com.ares.asset.application.port.in.command;

import java.util.Map;
import java.util.UUID;

public record CreateAssetCommand(UUID customerId, String type, String name, String brand,
                                 String model, String serialNumber, Map<String, String> attributes) {
}

