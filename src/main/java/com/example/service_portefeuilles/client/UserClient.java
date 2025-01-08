package com.example.service_portefeuilles.client;

import com.example.service_portefeuilles.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-utilisateur", url = "https://userservice-production-c8af.up.railway.app/api/user")
public interface UserClient {
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable Long id);
}
