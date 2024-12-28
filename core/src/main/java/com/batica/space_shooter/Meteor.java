package com.batica.space_shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Meteor {
    private Animation<TextureRegion> animation;
    private TextureAtlas atlas;
    private float stateTime;
    private float x, y;
    private float speed;
    private int screenHeight;
    private boolean isVisible;
    private Rectangle hitbox;
    private int health; // New health variable
    private boolean isDestroyed = false;

    // Updated constructor to include health parameter
    public Meteor(String atlasPath, String animationName, float frameDuration,
                  float initialX, float initialY, float speed, int initialHealth) {
        // Load the atlas
        atlas = new TextureAtlas(Gdx.files.internal(atlasPath));

        // Ensure regions exist
        if (atlas.findRegions(animationName).isEmpty()) {
            throw new IllegalArgumentException("No regions found for animationName: " + animationName);
        }

        // Create the animation
        animation = new Animation<>(frameDuration, atlas.findRegions(animationName), Animation.PlayMode.LOOP);

        // Ensure animation has valid frames
        if (animation.getKeyFrames().length == 0) {
            throw new IllegalStateException("Animation has no frames. Check the atlas and region name.");
        }

        // Initialize position and speed
        this.x = initialX;
        this.y = initialY;
        this.speed = speed;

        // Get the screen height
        screenHeight = Gdx.graphics.getHeight();

        // Set the meteor to be initially visible
        isVisible = true;

        stateTime = 0f;

        // Set initial health
        this.health = initialHealth;

        // Initialize the hitbox
        hitbox = new Rectangle(x, y, getWidth(), getHeight());
    }

    // New method to handle taking damage
    public void takeDamage(int damageAmount) {
        this.health -= damageAmount;
        if (this.health <= 0) {
            isVisible = false; // Meteor is destroyed when health reaches 0
        }
    }

    // Getter for health
    public int getHealth() {
        return health;
    }

    // Setter for health
    public void setHealth(int health) {
        this.health = health;
    }

    // New Getter for X position
    public float getX() {
        return x;
    }

    // New Getter for Y position
    public float getY() {
        return y;
    }

    public void update(float deltaTime) {
        // Move the meteor down
        y -= speed * deltaTime;

        // Update hitbox position
        hitbox.setPosition(x, y);

        // Mark as not visible if it moves out of bounds
        if (y + getHeight() < 0) {
            isVisible = false;
        }

        // Update animation time
        stateTime += deltaTime;
    }

    public void render(SpriteBatch batch) {
        if (isVisible) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime);
            batch.draw(currentFrame, x, y);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public float getWidth() {
        if (animation.getKeyFrames().length == 0) return 0;
        return animation.getKeyFrame(0).getRegionWidth();
    }

    public float getHeight() {
        if (animation.getKeyFrames().length == 0) return 0;
        return animation.getKeyFrame(0).getRegionHeight();
    }

    public void dispose() {
        atlas.dispose();
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }
}
