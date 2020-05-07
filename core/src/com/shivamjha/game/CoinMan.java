package com.shivamjha.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch;

	// Texture is a way to add an image to our app
	Texture background;

	// We have to create an illusion of man running, hence array
	Texture[] man;

	// Integer to loops through man's frames
	int manState;

	// Integer to give pause to our app, to slow man's speed
	int pause = 0;

	// Setup gravity
	float gavity = 0.2f;
	float velocity = 0;

	// Y coordinate for man
	int manY;

	// Make a bunch of coin objects for X and Y positions
	ArrayList<Integer> coinXs = new ArrayList<Integer>();
	ArrayList<Integer> coinYs = new ArrayList<Integer>();
	ArrayList<Rectangle> coinRectangles = new ArrayList<>(); // Rectangles allows us to add shape to our object
	Texture coin;
	int coinCount;

	Random random;

	// For bombs
	ArrayList<Integer> bombXs = new ArrayList<>();
	ArrayList<Integer> bombYs = new ArrayList<>();
	ArrayList<Rectangle> bombRectangles = new ArrayList<>();
	Texture bomb;
	int bombCount;

	// COLLISION STUFF
	// Note -> Import Rectangle from jdx, not from java
	Rectangle manRectangle;

	int score = 0;

	// Displaying score
	BitmapFont font;

	//For ending the game when man collides with Bomb
    int gameState = 0;

    // Texture for man, when the game is over
	Texture dizzy;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");

		man = new Texture[4];
		man[0] = new Texture("frame-1.png");
		man[1] = new Texture("frame-2.png");
		man[2] = new Texture("frame-3.png");
		man[3] = new Texture("frame-4.png");

		manY = Gdx.graphics.getHeight()/2;

		coin = new Texture("coin.png");
		bomb = new Texture("bomb.png");
		random = new Random();
		manRectangle = new Rectangle();
		font = new BitmapFont();

		font.setColor(Color.WHITE);
		font.getData().setScale(10);

		dizzy = new Texture("dizzy-1.png");
	}

	public void makeCoin() {
		// Gives a float bw 0 and 1 and then multiply with screen height
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		coinYs.add((int) height);
		coinXs.add(Gdx.graphics.getWidth());
	}

	public void makeBomb() {
		float height = random.nextFloat() * Gdx.graphics.getHeight();
		bombYs.add((int) height);
		bombXs.add(Gdx.graphics.getWidth());
	}

	// Order of things is important here
	// Things created later will appear over those created before..
	@Override
	public void render () {
		batch.begin();
		// Draw a image
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if(gameState == 1) {
		    // GAME IS LIVE

            // Make a coin every 100 times the loop runs
            if(coinCount < 100) {
                coinCount ++;
            } else {
                coinCount = 0;
                makeCoin();
            }

            // To draw all coins inside ArrayList
            coinRectangles.clear();
            for(int i = 0; i < coinXs.size(); i++) {
                batch.draw(coin, coinXs.get(i), coinYs.get(i));

                // move coin 4 units from where they are, at x - axis
                coinXs.set(i, coinXs.get(i) - 4);

                coinRectangles.add(new Rectangle(coinXs.get(i), coinYs.get(i), coin.getWidth(), coin.getHeight()));
            }

            // BOMBS
            if(bombCount < 300) {
                bombCount ++;
            } else {
                bombCount = 0;
                makeBomb();
            }

            bombRectangles.clear();
            for(int i = 0; i < bombXs.size(); ++ i) {
                batch.draw(bomb, bombXs.get(i), bombYs.get(i));
                bombXs.set(i, bombXs.get(i) - 6);

                bombRectangles.add(new Rectangle(bombXs.get(i), bombYs.get(i), bomb.getWidth(), bomb.getHeight()));
            }

            // Logic for jumping up when screen touched
            if(Gdx.input.justTouched()) {
                velocity = -10;
            }

            // Slowing down man's speed 6 times
            if(pause < 6) {
                pause++;
            } else {
                pause = 0;
                if(manState < 3) {
                    manState ++;
                } else {
                    manState = 0;
                }
            }

            // v = u + at :) , since the render() running again and again, therefore 't' is implemented
            velocity += gavity;
            manY -= velocity;

            // Prevent man from falling off the screen
            if(manY <= 0) {
                manY = 0;
            }

        } else if(gameState == 0) {
		    // Waiting to start
            if(Gdx.input.justTouched()) {
                gameState = 1;
            }

        } else if(gameState == 2) {
		    // GAME OVER
            if(Gdx.input.justTouched()) {
                gameState = 1;

                manY = Gdx.graphics.getHeight()/2;
                score = 0;
                velocity = 0;
                coinXs.clear();
                coinYs.clear();
                coinRectangles.clear();
                coinCount = 0;
                bombXs.clear();
                bombYs.clear();
                bombRectangles.clear();
                bombCount = 0;
            }
        }

		if(gameState == 2) {
			batch.draw(dizzy, Gdx.graphics.getWidth()/2 - man[manState].getWidth()/2, manY);
		} else {
			batch.draw(man[manState], Gdx.graphics.getWidth()/2 - man[manState].getWidth()/2, manY);
		}

		// Check for collision and do something after it
		manRectangle = new Rectangle(Gdx.graphics.getWidth()/2, manY, man[manState].getWidth(), man[manState].getHeight());
		// COIN and man
		for(int i = 0; i < coinRectangles.size(); i++) {
			if(Intersector.overlaps(manRectangle, coinRectangles.get(i))) {
				score++;
				coinRectangles.remove(i);
				coinXs.remove(i);
				coinYs.remove(i);
				break;
			}
		}
		// BOMB and man
		for(int i = 0; i < bombRectangles.size(); i++) {
			if(Intersector.overlaps(manRectangle, bombRectangles.get(i))) {

                gameState = 2;
			}
		}

		font.draw(batch, String.valueOf(score), 100, 200);

		batch.end();
}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
