package com.batica.space_shooter;

public class Bullet {
    private float x, y;
    private float speed;

    public Bullet(float x, float y, float speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void update(float deltaTime) {
        // Move bullet straight upwards
        y += speed * deltaTime;
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
