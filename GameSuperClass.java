package com.gimm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Preferences;
import java.util.Random;

public class GameSuperClass extends ApplicationAdapter {
  private BitmapFont titleFont;
  private BitmapFont scoreFont;
  private BitmapFont menuFont;
  private BitmapFont debugFont;
  private Circle birdCircle;
  private Random randomGenerator;
  private SpriteBatch batch;
  private Texture topTube;
  private Texture bottomTube;
  private Texture background;
  private Texture gameover;
  private Texture menuBackground;
  private Texture playButton;
  private Texture birdAlive;
  private Texture birdDead;
  private Texture missingTexture;

  private float birdY;
  private float[] distanceBetweenTubes;
  private float maxTubeOffset;
  private float velocity;
  private float birdRotation;

  private int flapState;
  private int score;
  private int highScore;
  private int scoringTube;
  private int gameState;
  private int width;
  private int height;
  private int flapCounter;
  private int frameCount;
  private long startTime;
  private float fps;

  private final float gap = 400f;
  private final float tubeVelocity = 4f;
  private final int numberOfTubes = 4;
  private final float gravity = 15f;
  private final float flapStrength = 450f;
  private final float maxVelocity = 400f;

  private final float[] tubeX = new float[numberOfTubes];
  private final float[] tubeOffset = new float[numberOfTubes];

  private final Rectangle[] topTubeRectangles = new Rectangle[numberOfTubes];
  private final Rectangle[] bottomTubeRectangles = new Rectangle[numberOfTubes];
  
  private Preferences prefs;
  private GlyphLayout glyphLayout;

  // Debug variables
  private boolean showDebug = true;
  private StringBuilder debugLog;
  private int debugLogSize = 10;
  private int collisionCount = 0;
  private int tapCount = 0;
  private Exception lastException = null;

  // Konstanta untuk state game
  private static final int STATE_MENU = 0;
  private static final int STATE_PLAYING = 1;
  private static final int STATE_GAME_OVER = 2;

  @Override
  public void create() {
    try {
      debugLog = new StringBuilder();
      logDebug("Game initializing...");
      
      batch = new SpriteBatch();
      logDebug("SpriteBatch created successfully");
      
      // Load preferences untuk menyimpan high score
      prefs = Gdx.app.getPreferences("FlappyBirdPrefs");
      highScore = prefs.getInteger("highScore", 0);
      logDebug("Preferences loaded - High Score: " + highScore);
      
      // Load textures dengan error handling
      loadTextures();
      
      birdCircle = new Circle();
      glyphLayout = new GlyphLayout();
      
      // Setup fonts
      setupFonts();
      
      width = Gdx.graphics.getWidth();
      height = Gdx.graphics.getHeight();
      logDebug("Screen dimensions: " + width + "x" + height);

      // Initialize game variables
      distanceBetweenTubes = new float[numberOfTubes];
      maxTubeOffset = height / 2 - gap / 2 - 100;
      randomGenerator = new Random();
      
      startTime = System.currentTimeMillis();
      frameCount = 0;
      
      startGame();
      logDebug("Game created successfully");
      
    } catch (Exception e) {
      handleException("Create method failed", e);
    }
  }

  private void loadTextures() {
    try {
      background = loadTextureSafe("bg.png");
      gameover = loadTextureSafe("gameover.png");
      menuBackground = loadTextureSafe("bg.png");
      playButton = loadTextureSafe("play_button.png");
      birdAlive = loadTextureSafe("bird.png");
      birdDead = loadTextureSafe("bird2.png");
      topTube = loadTextureSafe("toptube.png");
      bottomTube = loadTextureSafe("bottomtube.png");
      
      // Create missing texture placeholder
      missingTexture = createMissingTexture();
      
    } catch (Exception e) {
      handleException("Texture loading failed", e);
    }
  }

  private Texture loadTextureSafe(String path) {
    try {
      Texture texture = new Texture(Gdx.files.internal(path));
      logDebug("Loaded texture: " + path + " (" + texture.getWidth() + "x" + texture.getHeight() + ")");
      return texture;
    } catch (Exception e) {
      logDebug("ERROR: Failed to load texture: " + path + " - " + e.getMessage());
      return createMissingTexture();
    }
  }

  private Texture createMissingTexture() {
    try {
      // Create a simple missing texture pattern menggunakan Pixmap
      Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
      
      // Fill with magenta and black checkerboard pattern
      for (int x = 0; x < 64; x++) {
        for (int y = 0; y < 64; y++) {
          if ((x / 8 + y / 8) % 2 == 0) {
            pixmap.setColor(Color.MAGENTA);
          } else {
            pixmap.setColor(Color.BLACK);
          }
          pixmap.drawPixel(x, y);
        }
      }
      
      Texture texture = new Texture(pixmap);
      pixmap.dispose(); // Important: dispose pixmap after creating texture
      
      logDebug("Created missing texture placeholder");
      return texture;
      
    } catch (Exception e) {
      logDebug("CRITICAL: Cannot create missing texture placeholder");
      return null;
    }
  }

  private void setupFonts() {
    try {
      // Font untuk judul
      titleFont = new BitmapFont();
      titleFont.setColor(Color.GOLD);
      titleFont.getData().setScale(6);
      
      // Font untuk score dalam game
      scoreFont = new BitmapFont();
      scoreFont.setColor(Color.WHITE);
      scoreFont.getData().setScale(8);
      
      // Font untuk menu
      menuFont = new BitmapFont();
      menuFont.setColor(Color.WHITE);
      menuFont.getData().setScale(3);
      
      // Font untuk debug info
      debugFont = new BitmapFont();
      debugFont.setColor(Color.RED);
      debugFont.getData().setScale(2);
      
      logDebug("Fonts setup completed");
      
    } catch (Exception e) {
      handleException("Font setup failed", e);
    }
  }

  private void startGame() {
    try {
      logDebug("Starting new game...");
      
      birdY = height / 2 - getBirdTexture().getHeight() / 2;
      score = 0;
      scoringTube = 0;
      velocity = 0;
      birdRotation = 0;
      flapCounter = 0;
      collisionCount = 0;
      tapCount = 0;

      // Set jarak berbeda untuk setiap tiang
      float baseDistance = width * 3 / 4;
      for (int i = 0; i < numberOfTubes; i++) {
        distanceBetweenTubes[i] = baseDistance * (0.8f + randomGenerator.nextFloat() * 0.4f);
        tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (height - gap - 200);
        
        if (i == 0) {
          tubeX[i] = width / 2 - topTube.getWidth() / 2 + width;
        } else {
          tubeX[i] = tubeX[i-1] + distanceBetweenTubes[i-1];
        }
        
        topTubeRectangles[i] = new Rectangle();
        bottomTubeRectangles[i] = new Rectangle();
        
        logDebug("Tube " + i + " - Distance: " + distanceBetweenTubes[i] + ", Offset: " + tubeOffset[i]);
      }
      
      logDebug("Game started successfully");
      
    } catch (Exception e) {
      handleException("Game start failed", e);
    }
  }

  @Override
  public void render() {
    try {
      frameCount++;
      
      // Calculate FPS every 60 frames
      if (frameCount % 60 == 0) {
        long currentTime = System.currentTimeMillis();
        fps = 1000.0f / ((currentTime - startTime) / 60.0f);
        startTime = currentTime;
      }
      
      batch.begin();
      
      // Gambar background
      drawTextureSafe(background, 0, 0, width, height);

      switch (gameState) {
        case STATE_MENU:
          drawMenu();
          break;
          
        case STATE_PLAYING:
          updateGameplay();
          break;
          
        case STATE_GAME_OVER:
          drawGameOver();
          break;
      }

      // Gambar burung (kecuali di menu)
      if (gameState != STATE_MENU) {
        drawBird();
        
        // Update collision circle dan cek tabrakan
        if (gameState == STATE_PLAYING) {
          checkCollisions();
        }
      }
      
      // Draw debug info
      if (showDebug) {
        drawDebugInfo();
      }
      
      batch.end();
      
    } catch (Exception e) {
      handleException("Render loop failed", e);
      try {
        if (batch != null) {
          batch.end();
        }
      } catch (Exception ex) {
        // Ignore double exception
      }
    }
  }

  private void drawTextureSafe(Texture texture, float x, float y, float width, float height) {
    if (texture != null) {
      batch.draw(texture, x, y, width, height);
    } else {
      // Draw missing texture pattern
      batch.setColor(Color.MAGENTA);
      if (missingTexture != null) {
        batch.draw(missingTexture, x, y, width, height);
      } else {
        // Fallback: draw a colored rectangle
        batch.setColor(Color.RED);
        // We can't draw rectangle directly, so we'll skip this for now
      }
      batch.setColor(Color.WHITE);
    }
  }

  // Overloaded method untuk draw texture dengan ukuran asli
  private void drawTextureSafe(Texture texture, float x, float y) {
    if (texture != null) {
      batch.draw(texture, x, y);
    } else {
      drawTextureSafe(missingTexture, x, y, 64, 64);
    }
  }

  private void drawMenu() {
    try {
      // Gambar judul dengan shadow effect
      String title = "AIS ZAYANG";
      
      // Shadow
      titleFont.setColor(Color.DARK_GRAY);
      titleFont.draw(batch, title, width/2 - 280 + 4, height/2 + 200 - 4);
      
      // Main text
      titleFont.setColor(Color.GOLD);
      titleFont.draw(batch, title, width/2 - 280, height/2 + 200);

      // Gambar button play
      float buttonX = width/2 - (playButton != null ? playButton.getWidth() : 128) / 2;
      float buttonY = height/2 - 100;
      if (playButton != null) {
        batch.draw(playButton, buttonX, buttonY);
      } else {
        // Draw placeholder for play button
        batch.setColor(Color.GREEN);
        batch.draw(missingTexture, buttonX, buttonY, 128, 64);
        batch.setColor(Color.WHITE);
      }

      // High score
      menuFont.setColor(Color.CYAN);
      glyphLayout.setText(menuFont, "High Score: " + highScore);
      menuFont.draw(batch, "High Score: " + highScore, 
                   width/2 - glyphLayout.width/2, height/2 - 200);

      // Instruction
      menuFont.setColor(Color.LIGHT_GRAY);
      glyphLayout.setText(menuFont, "Tap to Play!");
      menuFont.draw(batch, "Tap to Play!", 
                   width/2 - glyphLayout.width/2, buttonY - 50);

      if (Gdx.input.justTouched()) {
        tapCount++;
        logDebug("Menu tap detected - Total taps: " + tapCount);
        gameState = STATE_PLAYING;
        startGame();
      }
      
    } catch (Exception e) {
      handleException("Menu drawing failed", e);
    }
  }

  private void updateGameplay() {
    try {
      // Update score ketika melewati tiang
      if (tubeX[scoringTube] < width / 2 - (topTube != null ? topTube.getWidth() : 100) / 2) {
        score++;
        logDebug("Score increased to: " + score + " (Tube: " + scoringTube + ")");
        if (score > highScore) {
          highScore = score;
          // Simpan high score baru
          prefs.putInteger("highScore", highScore);
          prefs.flush();
          logDebug("New high score: " + highScore);
        }
        scoringTube = (scoringTube + 1) % numberOfTubes;
      }

      // Kontrol burung yang lebih smooth
      if (Gdx.input.justTouched()) {
        tapCount++;
        velocity = -flapStrength;
        flapCounter = 0;
        logDebug("Flap detected - Velocity: " + velocity + " - Total taps: " + tapCount);
      }

      // Update fisika burung
      float deltaTime = Gdx.graphics.getDeltaTime();
      velocity += gravity * deltaTime;
      velocity = Math.min(velocity, maxVelocity);
      birdY -= velocity * deltaTime;

      // Update rotasi burung berdasarkan velocity
      birdRotation = Math.max(-90, Math.min(30, velocity * 0.2f));

      // Update posisi tiang
      for (int i = 0; i < numberOfTubes; i++) {
        float tubeWidth = topTube != null ? topTube.getWidth() : 100;
        
        if (tubeX[i] < -tubeWidth) {
          tubeX[i] = tubeX[(i + numberOfTubes - 1) % numberOfTubes] + 
                     distanceBetweenTubes[(i + numberOfTubes - 1) % numberOfTubes];
          tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (height - gap - 200);
          logDebug("Tube " + i + " recycled - New offset: " + tubeOffset[i]);
        } else {
          tubeX[i] -= tubeVelocity;
        }

        final float topY = height / 2 + gap / 2 + tubeOffset[i];
        final float bottomY = height / 2 - gap / 2 - (bottomTube != null ? bottomTube.getHeight() : 500) + tubeOffset[i];

        if (topTube != null) {
          batch.draw(topTube, tubeX[i], topY);
        }
        if (bottomTube != null) {
          batch.draw(bottomTube, tubeX[i], bottomY);
        }

        topTubeRectangles[i] = new Rectangle(tubeX[i], topY, tubeWidth, topTube != null ? topTube.getHeight() : 500);
        bottomTubeRectangles[i] = new Rectangle(tubeX[i], bottomY, tubeWidth, bottomTube != null ? bottomTube.getHeight() : 500);
      }

      // Cek game over condition
      float birdHeight = getBirdTexture() != null ? getBirdTexture().getHeight() : 50;
      if (birdY <= 0 || birdY >= height - birdHeight) {
        logDebug("Game over - Bird out of bounds. Y: " + birdY + ", Height: " + height);
        gameState = STATE_GAME_OVER;
      }

      // Gambar score dengan shadow effect
      String scoreText = String.valueOf(score);
      
      // Shadow
      scoreFont.setColor(Color.BLACK);
      scoreFont.draw(batch, scoreText, width/2 - 20 + 2, height - 50 - 2);
      
      // Main score
      scoreFont.setColor(Color.WHITE);
      scoreFont.draw(batch, scoreText, width/2 - 20, height - 50);
      
    } catch (Exception e) {
      handleException("Gameplay update failed", e);
    }
  }

  private Texture getBirdTexture() {
    return (gameState == STATE_GAME_OVER) ? birdDead : birdAlive;
  }

  private void drawBird() {
    try {
      flapCounter++;
      
      // Animasi kepakan sayap
      if (gameState == STATE_PLAYING) {
        flapState = (flapCounter / 10) % 2;
      }
      
      Texture currentBird = getBirdTexture();
      if (currentBird == null) {
        currentBird = missingTexture;
      }
      
      float birdX = width / 2 - currentBird.getWidth() / 2;
      
      if (gameState == STATE_PLAYING) {
        // Gambar burung dengan rotasi
        batch.draw(currentBird, 
                  birdX, birdY, 
                  currentBird.getWidth()/2, currentBird.getHeight()/2,
                  currentBird.getWidth(), currentBird.getHeight(),
                  1, 1, 
                  birdRotation, 
                  flapState * currentBird.getHeight(), 0, 
                  currentBird.getWidth(), currentBird.getHeight(), 
                  false, false);
      } else {
        batch.draw(currentBird, birdX, birdY);
      }
      
      // Update collision circle
      birdCircle.set(width / 2, birdY + currentBird.getHeight() / 2, currentBird.getWidth() / 3);
      
    } catch (Exception e) {
      handleException("Bird drawing failed", e);
    }
  }

  private void checkCollisions() {
    try {
      for (int i = 0; i < numberOfTubes; i++) {
        if (topTubeRectangles[i] != null && bottomTubeRectangles[i] != null) {
          if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || 
              Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
            collisionCount++;
            logDebug("Collision detected with tube " + i + " - Total collisions: " + collisionCount);
            gameState = STATE_GAME_OVER;
            break;
          }
        }
      }
    } catch (Exception e) {
      handleException("Collision detection failed", e);
    }
  }

  private void drawGameOver() {
    try {
      // Gambar game over panel semi transparan
      batch.setColor(0, 0, 0, 0.7f);
      batch.draw(background, width/2 - 300, height/2 - 250, 600, 500);
      batch.setColor(Color.WHITE);
      
      // Game Over text
      titleFont.setColor(Color.RED);
      glyphLayout.setText(titleFont, "GAME OVER");
      titleFont.draw(batch, "GAME OVER", 
                    width/2 - glyphLayout.width/2, height/2 + 150);
      
      // Score
      menuFont.setColor(Color.CYAN);
      glyphLayout.setText(menuFont, "Score: " + score);
      menuFont.draw(batch, "Score: " + score, 
                   width/2 - glyphLayout.width/2, height/2 + 50);
      
      // High Score
      menuFont.setColor(Color.GOLD);
      glyphLayout.setText(menuFont, "High Score: " + highScore);
      menuFont.draw(batch, "High Score: " + highScore, 
                   width/2 - glyphLayout.width/2, height/2 - 50);
      
      // Instruction
      menuFont.setColor(Color.LIGHT_GRAY);
      glyphLayout.setText(menuFont, "Tap to Continue");
      menuFont.draw(batch, "Tap to Continue", 
                   width/2 - glyphLayout.width/2, height/2 - 150);

      if (Gdx.input.justTouched()) {
        tapCount++;
        logDebug("Game over tap - Returning to menu");
        gameState = STATE_MENU;
      }
      
    } catch (Exception e) {
      handleException("Game over screen failed", e);
    }
  }

  private void drawDebugInfo() {
    try {
      debugFont.setColor(Color.YELLOW);
      float yPos = height - 30;
      
      // Basic debug info
      String[] debugLines = {
        "FPS: " + String.format("%.1f", fps),
        "State: " + getStateName(gameState),
        "Score: " + score + " | High: " + highScore,
        "Bird: Y=" + String.format("%.1f", birdY) + " V=" + String.format("%.1f", velocity),
        "Taps: " + tapCount + " | Collisions: " + collisionCount,
        "Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB",
        "Tubes: " + scoringTube + "/" + numberOfTubes
      };
      
      for (String line : debugLines) {
        debugFont.draw(batch, line, 10, yPos);
        yPos -= 25;
      }
      
      // Show last exception if any
      if (lastException != null) {
        debugFont.setColor(Color.RED);
        String errorMsg = lastException.getMessage();
        if (errorMsg != null && errorMsg.length() > 50) {
          errorMsg = errorMsg.substring(0, 50) + "...";
        }
        debugFont.draw(batch, "LAST ERROR: " + errorMsg, 10, yPos - 50);
      }
      
      // Toggle debug with D key
      if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) {
        showDebug = !showDebug;
        logDebug("Debug display " + (showDebug ? "enabled" : "disabled"));
      }
      
    } catch (Exception e) {
      // Don't try to handle debug drawing errors to avoid infinite recursion
      Gdx.app.error("DEBUG", "Debug drawing failed: " + e.getMessage());
    }
  }

  private String getStateName(int state) {
    switch (state) {
      case STATE_MENU: return "MENU";
      case STATE_PLAYING: return "PLAYING";
      case STATE_GAME_OVER: return "GAME_OVER";
      default: return "UNKNOWN";
    }
  }

  private void logDebug(String message) {
    String logMessage = "[" + System.currentTimeMillis() + "] " + message;
    if (debugLog != null) {
      debugLog.append(logMessage).append("\n");
      
      // Keep log size manageable
      String[] lines = debugLog.toString().split("\n");
      if (lines.length > debugLogSize) {
        
