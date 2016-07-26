package Game;

import java.util.Observable;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class GameStats extends Observable {
    private GameStats instance;

    boolean[][] bricks;
    Paddle paddle;
    Ball ball;

    private boolean isInitialized;

    private GameStats() {

    }

    public GameStats getInstance() {
        if (this.instance == null)
            instance = new GameStats();

        return instance;
    }

    public void initialize(boolean[][] bricks, Paddle paddle, Ball ball) {
        this.bricks = bricks;
        this.paddle = paddle;
        this.ball = ball;

        isInitialized = true;
    }

    public void destroyBrick(int i, int j) {
        bricks[i][j] = false;
        notifyObservers();
    }

    public boolean[][] getBricks() {
        return this.bricks;
    }

}
