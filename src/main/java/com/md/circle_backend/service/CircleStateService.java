package com.md.circle_backend.service;

import com.md.circle_backend.model.CircleState;
import org.springframework.stereotype.Service;

@Service
public class CircleStateService {

    private volatile double angle = 0.0;
    private volatile boolean rotating = false;

    public CircleState getState() {
        return new CircleState(angle, rotating);
    }

    public void updateState(double angle, boolean rotating) {
        this.angle = angle;
        this.rotating = rotating;
    }
}