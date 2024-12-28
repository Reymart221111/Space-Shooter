package com.batica.space_shooter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class SpaceShooter extends InputAdapter implements ApplicationListener {
    private SpriteBatch batch;
    private AnimationManager animationManager;
    private BulletManager bulletManager;
    private LivesManager livesManager;
    private ScoreManager scoreManager;
    private ArrayList<Enemy1> enemies;
    private ArrayList<Enemy2> enemies2;
    private ArrayList<Meteor> meteors;
    private ArrayList<Powerup> powerups;
    private ArrayList<Explosion> explosions; // List to manage explosions

    private Texture background;
    private TextureAtlas powerupAtlas;
    private TextureAtlas bulletPowerupAtlas;
    private TextureAtlas lifeAtlas;
    private TextureAtlas explosionAtlas; // Atlas for explosions

    private float playerX, playerY;
    private float playerSpeed = 300f;
    private boolean facingRight = true;

    private Rectangle playerHitbox;

    private float spawnInterval = 1.5f;
    private float timeSinceLastSpawn = 0f;
    private float timeSincePowerupSpawn = 0f;
    private float shootCooldown = 0.2f;
    private float timeSinceLastShot = 0f;

    private boolean isBulletPowerupActive = false;
    private float bulletPowerupTimer = 0f;

    private Sound bulletShootSound;
    private Sound bulletCollisionsSound;
    private Sound enemyExplosionSound;
    private Sound powerupSound;

    // New Variables for Game Over
    private boolean isGameOver = false;
    private BitmapFont gameOverFont;

    @Override
    public void create() {
        batch = new SpriteBatch();

        powerupAtlas = new TextureAtlas("atlas/powerup.atlas");
        bulletPowerupAtlas = new TextureAtlas("atlas/bullet_powerup.atlas");
        lifeAtlas = new TextureAtlas("atlas/life.atlas");
        explosionAtlas = new TextureAtlas("atlas/explosion.atlas"); // Load explosion atlas

        animationManager = new AnimationManager("atlas/sprite_movement.atlas", "sprite_movement", 0.1f);
        bulletManager = new BulletManager("atlas/bullet.atlas", "bullet");
        livesManager = new LivesManager(lifeAtlas);
        scoreManager = new ScoreManager();

        bulletShootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shooting.mp3"));
        bulletCollisionsSound = Gdx.audio.newSound(Gdx.files.internal("sounds/collision.mp3"));
        enemyExplosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosions.mp3"));
        powerupSound = Gdx.audio.newSound(Gdx.files.internal("sounds/powerups.mp3"));

        enemies = new ArrayList<>();
        enemies2 = new ArrayList<>();
        meteors = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>(); // Initialize explosions list

        background = new Texture("background.jpg");

        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 4f;

        playerHitbox = new Rectangle(playerX, playerY, 30, 30);

        // Initialize Game Over font
        gameOverFont = new BitmapFont();
        gameOverFont.setColor(Color.RED);
        gameOverFont.getData().setScale(3);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (isGameOver) {
            renderGameOverScreen();
        } else {
            update(deltaTime);

            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            animationManager.render(batch, playerX, playerY, deltaTime, facingRight);
            bulletManager.renderBullets(batch);
            livesManager.render(batch, deltaTime);
            scoreManager.render(batch);

            for (Enemy1 enemy : enemies) {
                enemy.render(batch);
            }
            for (Enemy2 enemy2 : enemies2) {
                enemy2.render(batch);
            }
            for (Meteor meteor : meteors) {
                meteor.render(batch);
            }

            for (Powerup powerup : powerups) {
                powerup.render(batch);
            }

            // Render all active explosions
            for (Iterator<Explosion> it = explosions.iterator(); it.hasNext(); ) {
                Explosion explosion = it.next();
                explosion.render(batch);
                if (explosion.isFinished()) {
                    it.remove(); // Remove explosion if finished
                }
            }

            batch.end();

            // Check for game over
            if (livesManager.isGameOver()) {
                isGameOver = true;
            }
        }
    }

    private void renderGameOverScreen() {
        batch.begin();
        gameOverFont.draw(batch, "GAME OVER!!", 200f, 400f);
        gameOverFont.draw(batch, "Press Enter Key to Restart!!", 100f, 100f);
        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (isGameOver && keycode == Input.Keys.ENTER) {
            restartGame();
            return true;
        }
        return super.keyDown(keycode);
    }

    private void restartGame() {
        // Reset game variables
        isGameOver = false;
        livesManager.resetLives();
        scoreManager.resetScore();
        enemies.clear();
        enemies2.clear();
        meteors.clear();
        powerups.clear();
        explosions.clear(); // Clear all active explosions
        bulletManager.clearBullets();

        playerX = Gdx.graphics.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 4f;
    }

    private void update(float deltaTime) {
        handleInput(deltaTime);

        playerHitbox.setPosition(playerX, playerY);

        timeSinceLastSpawn += deltaTime;
        timeSincePowerupSpawn += deltaTime;

        if (timeSinceLastSpawn >= spawnInterval) {
            spawnEnemy();
            spawnEnemy2();
            spawnMeteor();
            timeSinceLastSpawn = 0f;
        }

        if (timeSincePowerupSpawn >= PowerupConfig.SPAWN_INTERVAL) {
            spawnPowerup();
            timeSincePowerupSpawn = 0f;
        }

        if (isBulletPowerupActive) {
            bulletPowerupTimer += deltaTime;
            if (bulletPowerupTimer >= PowerupConfig.LIFETIME) {
                deactivateBulletPowerup();
            }
        }

        bulletManager.updateBullets(deltaTime);
        checkBulletCollisions();
        checkPowerupCollisions();
        checkCollisionsWithPlayer();

        updateAndRemoveEntities(enemies);
        updateAndRemoveEntities(enemies2);
        updateAndRemoveEntities(meteors);
        updateAndRemovePowerups();
        updateExplosions(deltaTime); // Update explosions
    }

    private void updateExplosions(float deltaTime) {
        for (Explosion explosion : explosions) {
            explosion.update(deltaTime);
        }
    }

    private void checkCollisionsWithPlayer() {
        Rectangle playerHitbox = new Rectangle(playerX, playerY, 64, 64);

        // Check collisions with enemies
        for (Enemy1 enemy : new ArrayList<>(enemies)) {
            if (playerHitbox.overlaps(enemy.getHitbox())) {
                livesManager.loseLife();
                enemies.remove(enemy);
                bulletCollisionsSound.play(1f);
                // Trigger explosion
                explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy.getX(), enemy.getY()));
                break;
            }
        }

        for (Enemy2 enemy2 : new ArrayList<>(enemies2)) {
            if (playerHitbox.overlaps(enemy2.getHitbox())) {
                livesManager.loseLife();
                enemies2.remove(enemy2);
                bulletCollisionsSound.play(1f);
                // Trigger explosion
                explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy2.getX(), enemy2.getY()));
                break;
            }
        }

        // Check collisions with meteors
        for (Meteor meteor : new ArrayList<>(meteors)) {
            if (playerHitbox.overlaps(meteor.getHitbox())) {
                livesManager.loseLife();
                meteors.remove(meteor);
                bulletCollisionsSound.play(1f);
                // Trigger explosion
                explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, meteor.getX(), meteor.getY()));
                break;
            }
        }
    }

    private <T> void updateAndRemoveEntities(ArrayList<T> entities) {
        Iterator<T> iterator = entities.iterator();
        while (iterator.hasNext()) {
            T entity = iterator.next();
            if (entity instanceof Enemy1) {
                Enemy1 enemy = (Enemy1) entity;
                enemy.update(Gdx.graphics.getDeltaTime());
                if (!enemy.isVisible() && !enemy.isDestroyed()) {
                    enemyExplosionSound.play(1f);
                    // Trigger explosion
                    explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy.getX(), enemy.getY()));
                    enemy.setDestroyed(true); // Mark as destroyed
                    iterator.remove();
                }
            } else if (entity instanceof Enemy2) {
                Enemy2 enemy2 = (Enemy2) entity;
                enemy2.update(Gdx.graphics.getDeltaTime());
                if (!enemy2.isVisible() && !enemy2.isDestroyed()) {
                    enemyExplosionSound.play(1f);
                    // Trigger explosion
                    explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy2.getX(), enemy2.getY()));
                    enemy2.setDestroyed(true);
                    iterator.remove();
                }
            } else if (entity instanceof Meteor) {
                Meteor meteor = (Meteor) entity;
                meteor.update(Gdx.graphics.getDeltaTime());
                if (!meteor.isVisible() && !meteor.isDestroyed()) {
                    enemyExplosionSound.play(1f);
                    // Trigger explosion
                    explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, meteor.getX(), meteor.getY()));
                    meteor.setDestroyed(true);
                    iterator.remove();
                }
            }
        }
    }

    private void updateAndRemovePowerups() {
        Iterator<Powerup> powerupIterator = powerups.iterator();
        while (powerupIterator.hasNext()) {
            Powerup powerup = powerupIterator.next();
            powerup.update(Gdx.graphics.getDeltaTime());
            if (!powerup.isVisible(Gdx.graphics.getHeight())) {
                powerupIterator.remove();
            }
        }
    }

    private void spawnPowerup() {
        float powerupX = MathUtils.random(0, Gdx.graphics.getWidth() - 64);
        float powerupY = Gdx.graphics.getHeight();

        Powerup powerup = new Powerup(
            powerupAtlas,
            powerupX,
            powerupY,
            PowerupConfig.Type.BULLET_POWERUP
        );
        powerups.add(powerup);
    }

    private void checkPowerupCollisions() {
        Rectangle playerHitbox = new Rectangle(playerX, playerY, 64, 64);

        Iterator<Powerup> powerupIterator = powerups.iterator();
        while (powerupIterator.hasNext()) {
            Powerup powerup = powerupIterator.next();
            if (playerHitbox.overlaps(powerup.getHitbox())) {
                powerupSound.play(1f);
                activateBulletPowerup();
                powerupIterator.remove();
                break;
            }
        }
    }

    private void activateBulletPowerup() {
        isBulletPowerupActive = true;
        bulletPowerupTimer = 0f;
        bulletManager = new BulletManager("atlas/bullet_powerup.atlas", "bullet_powerup");
    }

    private void deactivateBulletPowerup() {
        isBulletPowerupActive = false;
        bulletPowerupTimer = 0f;
        bulletManager = new BulletManager("atlas/bullet.atlas", "bullet");
    }

    private void handleInput(float deltaTime) {
        boolean horizontalMovement = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= playerSpeed * deltaTime;
            facingRight = false;
            horizontalMovement = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += playerSpeed * deltaTime;
            facingRight = true;
            horizontalMovement = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            playerY += playerSpeed * deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            playerY -= playerSpeed * deltaTime;
        }

        timeSinceLastShot += deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && timeSinceLastShot >= shootCooldown) {
            shootBullet();
            timeSinceLastShot = 0f;
        }

        playerX = Math.max(0, Math.min(playerX, Gdx.graphics.getWidth() - 64));
        playerY = Math.max(0, Math.min(playerY, Gdx.graphics.getHeight() - 64));

        animationManager.setMoving(horizontalMovement, horizontalMovement);
    }

    private void shootBullet() {
        float bulletX = playerX + 0;
        float bulletY = playerY + 64;
        bulletManager.spawnBullet(bulletX, bulletY);

        bulletShootSound.play(1.0f); // Volume: 1.0 (full volume)
    }

    private void spawnEnemy() {
        float enemyX = MathUtils.random(0, Gdx.graphics.getWidth() - 64);
        float enemyY = Gdx.graphics.getHeight();
        float enemySpeed = 100f;
        Enemy1 enemy = new Enemy1("atlas/sprite_enemy.atlas", "sprite-enemy", 0.1f, enemyX, enemyY, enemySpeed, 2);
        enemies.add(enemy);
    }

    private void spawnEnemy2() {
        float enemyX = MathUtils.random(0, Gdx.graphics.getWidth() - 64);
        float enemyY = Gdx.graphics.getHeight();
        float enemySpeed = 80f;
        Enemy2 enemy2 = new Enemy2("atlas/sprite_enemy_2.atlas", "sprite_enemy", 0.1f, enemyX, enemyY, enemySpeed, 5);
        enemies2.add(enemy2);
    }

    private void spawnMeteor() {
        float meteorX = MathUtils.random(0, Gdx.graphics.getWidth() - 64);
        float meteorY = Gdx.graphics.getHeight();
        float meteorSpeed = 300f;
        Meteor meteor = new Meteor("atlas/meteor.atlas", "meteors", 0.1f, meteorX, meteorY, meteorSpeed, 10);
        meteors.add(meteor);
    }

    private void checkBulletCollisions() {
        // Temporary list to track bullets that should be removed
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();

        // Iterate through bullets
        for (Bullet bullet : new ArrayList<>(bulletManager.getBullets())) {
            Rectangle bulletHitbox = new Rectangle(bullet.getX(), bullet.getY(), 16, 16);

            // Collision with Enemy1
            for (Iterator<Enemy1> enemyIterator = enemies.iterator(); enemyIterator.hasNext(); ) {
                Enemy1 enemy = enemyIterator.next();
                if (bulletHitbox.overlaps(enemy.getHitbox())) {
                    enemy.takeDamage(isBulletPowerupActive ? 10 : 1); // Powerup damage logic
                    bulletsToRemove.add(bullet); // Mark bullet for removal

                    if (enemy.getHealth() <= 0) {
                        enemyIterator.remove();
                        scoreManager.addScore(1); // Add score for Enemy1
                        // Trigger explosion
                        explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy.getX(), enemy.getY()));
                        enemyExplosionSound.play(1f);
                    }
                    break; // Break after one collision
                }
            }

            // Collision with Enemy2
            for (Iterator<Enemy2> enemy2Iterator = enemies2.iterator(); enemy2Iterator.hasNext(); ) {
                Enemy2 enemy2 = enemy2Iterator.next();
                if (bulletHitbox.overlaps(enemy2.getHitbox())) {
                    enemy2.takeDamage(isBulletPowerupActive ? 10 : 1); // Adjust damage if needed
                    bulletsToRemove.add(bullet);

                    if (enemy2.getHealth() <= 0) {
                        enemy2Iterator.remove();
                        scoreManager.addScore(5); // Add score for Enemy2
                        // Trigger explosion
                        explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, enemy2.getX(), enemy2.getY()));
                        enemyExplosionSound.play(1f);
                    }
                    break;
                }
            }

            // Collision with Meteor
            for (Iterator<Meteor> meteorIterator = meteors.iterator(); meteorIterator.hasNext(); ) {
                Meteor meteor = meteorIterator.next();
                if (bulletHitbox.overlaps(meteor.getHitbox())) {
                    meteor.takeDamage(isBulletPowerupActive ? 10 : 1); // Adjust damage if needed
                    bulletsToRemove.add(bullet);

                    if (meteor.getHealth() <= 0) {
                        meteorIterator.remove();
                        scoreManager.addScore(10); // Add score for Meteor
                        // Trigger explosion
                        explosions.add(new Explosion("atlas/explosion.atlas", "explosion", 0.05f, meteor.getX(), meteor.getY()));
                        enemyExplosionSound.play(1f);
                    }
                    break;
                }
            }
        }

        // Remove bullets marked for removal
        bulletManager.getBullets().removeAll(bulletsToRemove);
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        animationManager.dispose();
        bulletManager.dispose();
        powerupAtlas.dispose();
        bulletPowerupAtlas.dispose();
        lifeAtlas.dispose();
        explosionAtlas.dispose(); // Dispose explosion atlas
        livesManager.dispose();
        bulletCollisionsSound.dispose();
        bulletShootSound.dispose();
        enemyExplosionSound.dispose();
        powerupSound.dispose();
        gameOverFont.dispose();

        for (Enemy1 enemy : enemies) {
            enemy.dispose();
        }
        for (Enemy2 enemy2 : enemies2) {
            enemy2.dispose();
        }
        for (Meteor meteor : meteors) {
            meteor.dispose();
        }
        for (Powerup powerup : powerups) {
            powerup.dispose();
        }
        for (Explosion explosion : explosions) {
            // Assuming Explosion class has a dispose method, but it's optional if using shared atlas
            // explosion.dispose();
        }
        explosionAtlas.dispose(); // Ensure atlas is disposed if not handled in Explosion class
    }

    @Override
    public void pause() {
        System.out.println("Game paused");
    }

    @Override
    public void resume() {
        System.out.println("Game resumed");
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Screen resized to: " + width + "x" + height);
    }
}
