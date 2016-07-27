package Game;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class GameStats extends Observable {
    private static GameStats instance;

    private static final int START_LIVES = 3;
    private static final int POINTS_PER_BRICK = 100;

    boolean[][] bricks;
    Paddle paddle;
    Ball ball;
    private boolean gameWon;
    private boolean gameLost;
    private int level;
    private int lives;
    private int score;
    private int shots;
    private boolean playerIsDead;

    private boolean isInitialized;

    private GameStats() {
        bricks = new boolean[11][7];
    }

    public static GameStats getInstance() {
        if (instance == null)
            instance = new GameStats();

        return instance;
    }

    public void initialize(boolean[][] bricks, Paddle paddle, Ball ball) {
        this.bricks = bricks;
        this.paddle = paddle;
        this.ball = ball;
        isInitialized = true;
    }

    public void newGame() {
        this.lives = START_LIVES;
        this.playerIsDead = false;
        this.score = 0;
        this.shots = 0;
        this.level = 1;
    }

    public void gameInit() {
        paddle = new Paddle(120,360);
        ball = new Ball(150, 100, 5);
        ball.reset();
        for (int i = 0; i < bricks.length; i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                bricks[i][j] = false;
            }
        }
    }

    public void playerDied() {
        decrementLives();
        ball.reset();
        paddle = new Paddle(120,360);
    }

    public void brickHit() {
        this.score += POINTS_PER_BRICK;
    }

    public void destroyBrick(int i, int j) {
        bricks[i][j] = false;
        notifyObservers();
    }

    public void incrementShots() {
        this.shots++;
    }

    public void decrementLives() {
        this.lives--;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean[][] getBricks() {
        return this.bricks;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getShots() {
        return shots;
    }

    public Ball getBall() {
        return ball;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public int getLevel() {
        return level;
    }

    public void setBricks(boolean[][] bricks) {
        this.bricks = bricks;
    }

    public void setPaddle(Paddle paddle) {
        this.paddle = paddle;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public void setPlayerIsDead(boolean dead) {
        this. playerIsDead = true;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setBrickState(int i, int j, boolean state) {
        bricks[i][j] = state;
    }

    public void incrementLevel() {
        this.level++;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public boolean isPlayerDead() {
        return playerIsDead;
    }
}
