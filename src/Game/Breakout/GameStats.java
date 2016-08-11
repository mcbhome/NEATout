package Game.Breakout;

import Game.neat.Simulation;

import java.util.Observable;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class GameStats extends Observable {
    private static GameStats instance;

    private static final int START_LIVES = 1;
    private static final int POINTS_PER_BRICK = 100;

    public static final int POINTS_TO_WIN_GAME = 10000;

    public static final double MAX_BALL_SPEED = 2;
    public static final double MAX_PADDLE_SPEED = 3;

    boolean gameStarted;
    boolean[][] bricks;
    Paddle paddle;
    Ball ball;
    private boolean gameWon;
    private boolean gameLost;
    private boolean inGame;
    private boolean gamePaused;
    private boolean simulationMode;
    private int level = 1;
    private int lives;
    private int score;
    private int shots;
    private boolean playerIsDead;

    private GameStats() {
        bricks = new boolean[11][7];
        gameStarted = false;
    }

    public static GameStats getInstance() {
        if (instance == null)
            instance = new GameStats();

        return instance;
    }

    public void newGame() {
        this.lives = START_LIVES;
        this.playerIsDead = false;
        this.score = 0;
        this.shots = 0;
        this.level = 1;
        this.gameLost = false;
        this.gameWon = false;
        properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.NEW_GAME));
    }

    public void gameInit() {
        paddle = new Paddle(120,360);
        ball = new Ball(150, 100, 5);
        ball.reset(simulationMode);
        gameStarted = true;
    }

    public void resetGame() {
        newGame();
        gameInit();
    }

    public void playerDied() {
        decrementLives();
        checkIfGameOver();
        if (!gameLost) {
            ball.reset(simulationMode);
            paddle = new Paddle(120, 360);
        }
        properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.PLAYER_DIED));
    }

    public void brickHit(int i, int j) {
        this.score += POINTS_PER_BRICK;
        bricks[i][j] = false;
        if (this.score > POINTS_TO_WIN_GAME) {
            properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.GAME_WON));
        } else {
            properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.BRICK_CHANGE, i, j, false));
        }
    }

    public void incrementShots() {
        this.shots++;
        properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.SCORE_CHANGED));
    }

    public void decrementLives() {
        this.lives--;
    }

    public void checkIfGameOver() {
        if (this.getLives() == 0) {
            this.gameLost = true;
            setPlayerIsDead();
        }
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

    public void setPlayerIsDead() {
        this.playerIsDead = true;
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

    public void movementOccured() {
        properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.MOVEMENT));
    }

    public void setBrickState(int i, int j, boolean state) {
        bricks[i][j] = state;
        if (gameStarted) {
            properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.BRICK_CHANGE, i, j, state));
        }
    }

    public void incrementLevel() {
        this.level++;
        properlyNotify(new Simulation.ObservableArg(Simulation.Update_Args.MISC));
    }

    public void properlyNotify(Simulation.ObservableArg arg) {
        setChanged();
        notifyObservers(arg);
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isPlayerDead() {
        return playerIsDead;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    public void setSimulationMode(boolean simulationMode) {
        this.simulationMode = simulationMode;
    }

    public void pauseGame() {
        this.inGame = false;
        this.gamePaused = true;
    }

    public void unPauseGame() {
        this.inGame = true;
        this.gamePaused = false;
    }

    public void clearBricks() {
        for (int i = 0; i < bricks.length; i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                bricks[i][j] = false;
            }
        }
    }
}
