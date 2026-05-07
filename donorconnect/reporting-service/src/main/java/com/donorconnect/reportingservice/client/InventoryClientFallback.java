package com.donorconnect.reportingservice.client;
import com.donorconnect.reportingservice.dto.BloodComponentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
@Component @Slf4j
public class InventoryClientFallback implements InventoryClient {
    @Override
    public List<BloodComponentDto> getAllComponents() {
        log.warn("InventoryClient fallback triggered");
        return Collections.emptyList();
    }
}
