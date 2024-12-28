package com.batica.space_shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class LivesManager {
    private TextureAtlas lifeAtlas;
    private Animation<TextureRegion> lifeAnimation;
    private float stateTime = 0f;
    private int currentLives;
    private static final int MAX_LIVES = 3;

    public LivesManager(TextureAtlas lifeAtlas) {
        this.lifeAtlas = lifeAtlas;
        this.currentLives = MAX_LIVES;

        // Create animation from the atlas
        lifeAnimation = new Animation<>(
            0.1f,
            lifeAtlas.findRegions("life"),
            Animation.PlayMode.LOOP
        );
    }

    public void render(SpriteBatch batch, float deltaTime) {
        stateTime += deltaTime;

        // Render lives on screen (top left corner)
        for (int i = 0; i < currentLives; i++) {
            TextureRegion currentFrame = lifeAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, 10 + (i * 50), Gdx.graphics.getHeight() - 60, 50, 50);
        }
    }

    public void loseLife() {
        if (currentLives > 0) {
            currentLives--;
        }
    }

    public boolean isGameOver() {
        return currentLives <= 0;
    }

    public void resetLives() {
        currentLives = MAX_LIVES;
    }

    public int getCurrentLives() {
        return currentLives;
    }

    public void dispose() {
        if (lifeAtlas != null) {
            lifeAtlas.dispose();
        }
    }
}
