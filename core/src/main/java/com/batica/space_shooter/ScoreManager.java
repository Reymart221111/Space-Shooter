package com.batica.space_shooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScoreManager {
    private int score;
    private BitmapFont font;

    public ScoreManager() {
        // Create a new default BitmapFont
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        // Initialize score
        score = 0;
    }

    public void render(SpriteBatch batch) {
        // Render score in top right corner
        font.draw(batch, "Score: " + score,
            Gdx.graphics.getWidth() - 200,
            Gdx.graphics.getHeight() - 20);
    }

    public void addScore(int points) {
        score += points;
    }

    public void resetScore() {
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}
