package study.querydsl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.common.ResponseOneData;

@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping("")
    public ResponseEntity<ResponseOneData> hello() {
        return ResponseEntity.ok(
            ResponseOneData.createHello()
        );
    }
}
