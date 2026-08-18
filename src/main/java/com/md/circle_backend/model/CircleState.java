package com.md.circle_backend.model;

public class CircleState {

    private double angle;
    private boolean rotating;

    public CircleState() {
    }

    public CircleState(double angle, boolean rotating) {
        this.angle = angle;
        this.rotating = rotating;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isRotating() {
        return rotating;
    }

    public void setRotating(boolean rotating) {
        this.rotating = rotating;
    }
}