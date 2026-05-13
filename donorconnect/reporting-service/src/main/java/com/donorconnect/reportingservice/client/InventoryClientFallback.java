package com.donorconnect.reportingservice.client;

import com.donorconnect.reportingservice.dto.InventoryBalanceDto;
import com.donorconnect.reportingservice.dto.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {
    @Override
    public ServiceResponse<List<InventoryBalanceDto>> getAllInventory() {
        log.warn("InventoryClient fallback: inventory-service unavailable");
        ServiceResponse<List<InventoryBalanceDto>> response = new ServiceResponse<>();
        response.setData(Collections.emptyList());
        return response;
    }
}
