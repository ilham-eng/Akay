// GameContent.java - Basic Bouncing Ball Game
public class GameContent {
    
    // Game configuration - bisa diubah via URL
    public static final int BALL_SPEED = 12;
    public static final int BALL_RADIUS = 40;
    public static final String BALL_COLOR = "#FF6B35";
    public static final String BG_COLOR = "#2E294E";
    public static final boolean ENABLE_GRAVITY = false;
    public static final int TOUCH_BONUS = 8;
    
    // Game state variables
    private int ballX, ballY;
    private int ballSpeedX, ballSpeedY;
    private int score = 0;
    private int highScore = 0;
    private int screenWidth, screenHeight;
    private long startTime;
    private int gameTime = 0;
    
    // Game objects
    private java.util.ArrayList<PowerUp> powerUps;
    private boolean isGameActive = true;
    
    public void initGame(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        
        // Initialize ball position
        ballX = width / 2;
        ballY = height / 2;
        ballSpeedX = BALL_SPEED;
        ballSpeedY = BALL_SPEED;
        
        // Initialize power-ups
        powerUps = new java.util.ArrayList<>();
        spawnPowerUp();
        
        startTime = System.currentTimeMillis();
    }
    
    public void updateGame() {
        if (!isGameActive) return;
        
        // Update game time
        gameTime = (int)((System.currentTimeMillis() - startTime) / 1000);
        
        // Update ball position
        ballX += ballSpeedX;
        ballY += ballSpeedY;
        
        // Boundary collision with score
        if (ballX <= BALL_RADIUS) {
            ballSpeedX = Math.abs(ballSpeedX);
            score += 1;
        }
        if (ballX >= screenWidth - BALL_RADIUS) {
            ballSpeedX = -Math.abs(ballSpeedX);
            score += 1;
        }
        if (ballY <= BALL_RADIUS) {
            ballSpeedY = Math.abs(ballSpeedY);
            score += 1;
        }
        if (ballY >= screenHeight - BALL_RADIUS) {
            ballSpeedY = -Math.abs(ballSpeedY);
            score += 1;
        }
        
        // Update high score
        if (score > highScore) {
            highScore = score;
        }
        
        // Update power-ups
        updatePowerUps();
        
        // Randomly spawn new power-ups
        if (Math.random() < 0.002) { // 0.2% chance per frame
            spawnPowerUp();
        }
    }
    
    public void drawGame(java.awt.Canvas canvas) {
        // Draw background
        canvas.drawColor(android.graphics.Color.parseColor(BG_COLOR));
        
        // Draw ball with gradient effect
        android.graphics.Paint ballPaint = new android.graphics.Paint();
        ballPaint.setColor(android.graphics.Color.parseColor(BALL_COLOR));
        ballPaint.setStyle(android.graphics.Paint.Style.FILL);
        
        // Draw main ball
        canvas.drawCircle(ballX, ballY, BALL_RADIUS, ballPaint);
        
        // Draw inner circle for 3D effect
        android.graphics.Paint innerPaint = new android.graphics.Paint();
        innerPaint.setColor(android.graphics.Color.parseColor("#FF8C5A"));
        canvas.drawCircle(ballX - 10, ballY - 10, BALL_RADIUS - 15, innerPaint);
        
        // Draw power-ups
        drawPowerUps(canvas);
        
        // Draw UI
        drawUI(canvas);
    }
    
    private void drawUI(java.awt.Canvas canvas) {
        android.graphics.Paint textPaint = new android.graphics.Paint();
        textPaint.setColor(android.graphics.Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        // Draw scores
        canvas.drawText("Score: " + score, 20, 50, textPaint);
        canvas.drawText("High Score: " + highScore, 20, 100, textPaint);
        canvas.drawText("Time: " + gameTime + "s", 20, 150, textPaint);
        
        // Draw game title
        textPaint.setTextSize(42);
        textPaint.setColor(android.graphics.Color.parseColor("#FFD166"));
        canvas.drawText("Bouncing Ball Pro", screenWidth / 2 - 180, 60, textPaint);
        
        // Draw instructions
        textPaint.setTextSize(24);
        textPaint.setColor(android.graphics.Color.LTGRAY);
        canvas.drawText("Touch screen to change direction", screenWidth / 2 - 160, screenHeight - 50, textPaint);
        
        if (!isGameActive) {
            // Draw game over screen
            android.graphics.Paint overlayPaint = new android.graphics.Paint();
            overlayPaint.setColor(android.graphics.Color.argb(200, 0, 0, 0));
            canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);
            
            textPaint.setTextSize(48);
            textPaint.setColor(android.graphics.Color.RED);
            canvas.drawText("GAME OVER", screenWidth / 2 - 140, screenHeight / 2 - 50, textPaint);
            
            textPaint.setTextSize(36);
            textPaint.setColor(android.graphics.Color.WHITE);
            canvas.drawText("Final Score: " + score, screenWidth / 2 - 120, screenHeight / 2 + 20, textPaint);
            canvas.drawText("Tap to restart", screenWidth / 2 - 100, screenHeight / 2 + 80, textPaint);
        }
    }
    
    private void drawPowerUps(java.awt.Canvas canvas) {
        for (PowerUp powerUp : powerUps) {
            android.graphics.Paint paint = new android.graphics.Paint();
            
            switch (powerUp.type) {
                case "SPEED":
                    paint.setColor(android.graphics.Color.GREEN);
                    break;
                case "SCORE":
                    paint.setColor(android.graphics.Color.YELLOW);
                    break;
                case "SLOW":
                    paint.setColor(android.graphics.Color.BLUE);
                    break;
            }
            
            canvas.drawCircle(powerUp.x, powerUp.y, 15, paint);
            
            // Draw plus sign for power-ups
            android.graphics.Paint textPaint = new android.graphics.Paint();
            textPaint.setColor(android.graphics.Color.BLACK);
            textPaint.setTextSize(20);
            textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("+", powerUp.x, powerUp.y + 7, textPaint);
        }
    }
    
    private void updatePowerUps() {
        java.util.Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.duration--;
            
            if (powerUp.duration <= 0) {
                iterator.remove();
            }
            
            // Check collision with ball
            double distance = Math.sqrt(Math.pow(ballX - powerUp.x, 2) + Math.pow(ballY - powerUp.y, 2));
            if (distance < BALL_RADIUS + 15) {
                applyPowerUp(powerUp);
                iterator.remove();
            }
        }
    }
    
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.type) {
            case "SPEED":
                ballSpeedX *= 1.5;
                ballSpeedY *= 1.5;
                score += 10;
                break;
            case "SCORE":
                score += 25;
                break;
            case "SLOW":
                ballSpeedX *= 0.7;
                ballSpeedY *= 0.7;
                score += 5;
                break;
        }
    }
    
    private void spawnPowerUp() {
        String[] types = {"SPEED", "SCORE", "SLOW"};
        String type = types[(int)(Math.random() * types.length)];
        
        int x = (int)(Math.random() * (screenWidth - 100)) + 50;
        int y = (int)(Math.random() * (screenHeight - 200)) + 100;
        
        powerUps.add(new PowerUp(x, y, type));
    }
    
    public boolean handleTouch(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            if (!isGameActive) {
                // Restart game
                initGame(screenWidth, screenHeight);
                isGameActive = true;
                return true;
            }
            
            // Reverse direction with bonus points
            ballSpeedX = -ballSpeedX;
            ballSpeedY = -ballSpeedY;
            score += TOUCH_BONUS;
            
            // Chance to spawn power-up on touch
            if (Math.random() < 0.3) { // 30% chance
                spawnPowerUp();
            }
            
            return true;
        }
        return false;
    }
    
    public void setGameActive(boolean active) {
        this.isGameActive = active;
    }
    
    // PowerUp inner class
    class PowerUp {
        int x, y;
        String type;
        int duration = 300; // frames
        
        PowerUp(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
}
