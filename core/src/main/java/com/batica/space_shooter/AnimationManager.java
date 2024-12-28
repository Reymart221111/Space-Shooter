package com.batica.space_shooter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;

public class AnimationManager {
    private Animation<TextureRegion> animation;
    private TextureAtlas atlas;
    private Texture idleSprite;
    private float stateTime;
    private boolean moving;
    private boolean horizontalMovement;

    public AnimationManager(String atlasPath, String animationName, float frameDuration) {
        atlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        animation = new Animation<>(frameDuration, atlas.findRegions(animationName), Animation.PlayMode.LOOP);
        idleSprite = new Texture(Gdx.files.internal("sprite.png")); // Load the idle sprite
        stateTime = 0f;
        moving = false;
        horizontalMovement = false; // Start as not moving horizontally
    }

    public void render(SpriteBatch batch, float x, float y, float deltaTime, boolean facingRight) {
        TextureRegion currentFrame;

        if (!horizontalMovement) {
            // Render idle sprite
            currentFrame = new TextureRegion(idleSprite);

            // Handle flipping for idle sprite
            if (!facingRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (facingRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }

        } else {
            // Render animation
            stateTime += deltaTime;
            currentFrame = animation.getKeyFrame(stateTime);

            if (!facingRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (facingRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
        }

        batch.draw(currentFrame, x, y);
    }

    public void setMoving(boolean moving, boolean horizontalMovement) {
        this.moving = moving;
        this.horizontalMovement = horizontalMovement;

        if (!moving) {
            stateTime = 0; // Reset animation when not moving
        }
    }

    public void dispose() {
        atlas.dispose();
        idleSprite.dispose();
    }

    public Texture getSampleTexture() {
        return new Texture("atlas/sprite_movement.png"); // Replace with an actual texture path
    }

}
