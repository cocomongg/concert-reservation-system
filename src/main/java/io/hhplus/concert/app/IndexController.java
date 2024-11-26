package io.hhplus.concert.app;

import io.hhplus.concert.app.common.api.response.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping("/health-check")
    public ApiResult<?> healthCheck() {
        return ApiResult.OK();
    }
}
