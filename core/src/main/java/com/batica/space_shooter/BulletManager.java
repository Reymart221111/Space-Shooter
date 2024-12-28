package com.batica.space_shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Iterator;

public class BulletManager {
    private TextureAtlas bulletAtlas;
    private Animation<TextureRegion> bulletAnimation;
    private ArrayList<Bullet> bullets;
    private float bulletSpeed = 500f;

    public BulletManager(String atlasPath, String bulletAnimationName) {
        bulletAtlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        bulletAnimation = new Animation<>(0.1f, bulletAtlas.findRegions(bulletAnimationName), Animation.PlayMode.LOOP);
        bullets = new ArrayList<>();
    }

    public void spawnBullet(float x, float y) {
        Bullet bullet = new Bullet(x, y, bulletSpeed);
        bullets.add(bullet);
    }

    public void updateBullets(float deltaTime) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaTime);

            // Remove bullets that are out of screen
            if (bullet.getY() > Gdx.graphics.getHeight()) {
                iterator.remove();
            }
        }
    }

    public void renderBullets(SpriteBatch batch) {
        float stateTime = 0f;
        for (Bullet bullet : bullets) {
            TextureRegion currentFrame = bulletAnimation.getKeyFrame(stateTime);
            batch.draw(currentFrame, bullet.getX(), bullet.getY());
        }
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }


    public void clearBullets()
    {
        bullets.clear();
    }
    public void dispose() {
        bulletAtlas.dispose();
    }
}

