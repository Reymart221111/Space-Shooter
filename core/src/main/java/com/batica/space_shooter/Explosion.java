package com.batica.space_shooter;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;

public class Explosion {
    private Animation<TextureRegion> explosionAnimation;
    private float stateTime;
    private float x, y;
    private boolean finished;

    public Explosion(String atlasPath, String animationName, float frameDuration, float x, float y) {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        explosionAnimation = new Animation<>(frameDuration, atlas.findRegions(animationName), Animation.PlayMode.NORMAL);
        stateTime = 0f;
        this.x = x;
        this.y = y;
        this.finished = false;
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        if (explosionAnimation.isAnimationFinished(stateTime)) {
            finished = true;
        }
    }

    public void render(SpriteBatch batch) {
        if (!finished) {
            TextureRegion currentFrame = explosionAnimation.getKeyFrame(stateTime);
            batch.draw(currentFrame, x, y);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void dispose(TextureAtlas atlas) {
        atlas.dispose();
    }
}
