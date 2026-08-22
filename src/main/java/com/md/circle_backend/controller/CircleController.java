package com.md.circle_backend.controller;

import com.md.circle_backend.model.AngleRequest;
import com.md.circle_backend.model.CircleState;
import com.md.circle_backend.service.CircleStateService;
import com.md.circle_backend.service.CircleTcpService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/circle")
public class CircleController {

    private final CircleStateService stateService;
    private final CircleTcpService tcpService;

    public CircleController(CircleStateService stateService, CircleTcpService tcpService) {
        this.stateService = stateService;
        this.tcpService = tcpService;
    }

    @GetMapping("/status")
    public CircleState getStatus() {
        return stateService.getState();
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startRotation() {

        tcpService.startRotation();

        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopRotation() {

        tcpService.stopRotation();

        return ResponseEntity
                .accepted()
                .build();
    }

    @PutMapping("/angle")
    public ResponseEntity<Void> goToAngle(@RequestBody AngleRequest request) {

        double angle = request.getAngle();

        if (angle < 0 || angle > 359.9) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        tcpService.goToAngle(angle);

        return ResponseEntity
                .accepted()
                .build();
    }
}