package com.iot.kiwicontent.client;

import com.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "kiwi-hub-user", path = "/stats")
public interface UserServiceClient {

    @PostMapping("/article/count/{userId}/{delta}")
    Result<Object> updateArticleCount(@PathVariable("userId") String userId, @PathVariable("delta") int delta);
}