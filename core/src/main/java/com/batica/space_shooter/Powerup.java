package com.batica.space_shooter;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Powerup {
    private TextureAtlas atlas;
    private TextureRegion currentFrame;
    private float x, y;
    private Rectangle hitbox;
    private PowerupConfig.Type type;
    private float speed;
    private float stateTime = 0f;

    public Powerup(TextureAtlas atlas, float x, float y, PowerupConfig.Type type) {
        this.atlas = atlas;
        this.x = x;
        this.y = y;
        this.type = type;
        this.speed = PowerupConfig.POWERUP_SPEED;

        this.currentFrame = atlas.getRegions().first();
        this.hitbox = new Rectangle(x, y, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        y -= speed * deltaTime;

        currentFrame = atlas.getRegions().get((int)(stateTime * 10) % atlas.getRegions().size);

        hitbox.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentFrame, x, y);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public PowerupConfig.Type getType() {
        return type;
    }

    public boolean isVisible(float screenHeight) {
        return y + currentFrame.getRegionHeight() > 0 && y < screenHeight;
    }

    public void dispose() {
        atlas.dispose();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
