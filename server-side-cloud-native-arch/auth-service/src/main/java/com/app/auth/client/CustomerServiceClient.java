package com.app.auth.client;

import com.app.auth.dto.CustomerOnboardRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @PostMapping("/api/customers/register")
    void register(@RequestBody CustomerOnboardRequest request);
}
