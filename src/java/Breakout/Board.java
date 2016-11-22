package Breakout; /**
The Board class includes the game logic as well
as game objects, such as the brick, paddle and ball,
and all the methods to draw components to screen
*/

import UserInterface.NEATDiagnostics;

import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

public class Board extends JPanel
{
    private ArrayList<Brick> bricks;
    private Timer timer;
    private boolean skippedMainMenu = false;
    private GameStats gameStats;
    private Commons commons;
    private int ballCollisionCount = 0;
    private final int MAX_BALL_COLLISIONS = 3;
    private String bg = "landingScreen.jpg";

    private static Board instance;


    /**
     * Constructor. Configures refresh rate and initiates threads
     */
    private Board()
    {
        gameStats = GameStats.getInstance();
        gameInit();
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), 8, 8);
    }

    public static Board getInstance() {
        if (instance == null)
            instance = new Board();

        return instance;
    }

    public void setSimSpeed(int i) {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), i, i);
    }
    /**
     * Initializes game logic and variables
     * Bricks are initialized for the first
     * level and randomly initialized for every other
     * level, executed FIRST
     */
    public void gameInit()
    {
        commons = new Commons();

        if (bricks == null){
            bricks = new ArrayList<>();
        } else {
            List<Brick> bricksSync = Collections.synchronizedList(bricks);
            bricksSync.clear();
        }

        gameStats.gameInit();
    }

    /**
     * executed AFTER gameInit
     */
    public void initStats() {
        gameStats.clearBricks();
        //randomizeBricks();
        gameStats.newGame();
        gameStats.setGamePaused(false);
        gameStats.setInGame(true);
    }

    public void newLevel() {
        gameStats.incrementLevel();
        gameInit();
        randomizeBricks();
    }

    /**
     * Draws all components to screen
     * depending on the game state.
     * Game states include main menu, pause menu,
     * game over screen and game screen.
     *
     * @param g
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        ImageIcon img = new ImageIcon(this.getClass().getResource(bg));
        Image image = img.getImage();
        g.drawImage(image, 0, 0, null);

        if (gameStats.isInGame())
        {
            drawGameState(g);
        }
        else
        {
            if (gameStats.isPlayerDead())
            {
                drawGameState(g);
                if (!gameStats.isSimulationMode()) {
                    drawGameOver(g);
                }
            }
            else if (gameStats.isGamePaused())
            {
                drawGameState(g);
                drawGamePaused(g);
            }
            else if (!skippedMainMenu)
            {
                drawTitles(g);
            }
        }
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }

    /**
     * Draw all game components (paddle, bricks, ball, points, lives)
     *
     * @param g
     */
    public synchronized void drawGameState(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        drawPaddle(g2);
        drawBricks(g2);
        drawBall(g2);
        drawPoints(g);
        drawLives(g);
        drawLevel(g);
    }

    /**
     * Draw main menu. Set font, coordinates
     * and color for every string drawn to screen.
     *
     * @param g
     */
    public void drawTitles(Graphics g)
    {
        String drawnString = "Game.Breakout";
        Font font = new Font("Bank Gothic", Font.BOLD, 35);
        FontMetrics metr = this.getFontMetrics(font);

        Color letterColor = new Color(248,207,144);
        g.setColor(letterColor);
        g.setFont(font);
        g.drawString(drawnString,
                (commons.getWidth() - metr.stringWidth(drawnString)) / 2,
                (int) (commons.getHeight() * 0.15));
        drawnString = "AN IZAC JOINT";
        font = new Font("OCR A Std", Font.BOLD, 10);
        g.setFont(font);
        g.drawString(drawnString, 6, (int) (commons.getHeight() * 0.98));
        drawnString = "PRESS <SPACE> TO PLAY";

        font = new Font("OCR A Std", Font.BOLD, 15);
        metr = this.getFontMetrics(font);
        g.setFont(font);
        g.drawString(drawnString,
                (commons.getWidth() - metr.stringWidth(drawnString)) / 2,
                (int) (commons.getHeight() * 0.65));
        g.drawString("PRESS <CTRL> to train NEAT", (int)(commons.getWidth() * 0.03),
                (int)(commons.getHeight() * 0.75));
    }
    /**
     * Event Listener. Determines when SPACE
     * is pressed to start the game and transition
     * to the inGame game state. Also used to restart
     * the game in the game over state
     *
     * @param e
     */
    public void keyToStart(KeyEvent e)
    {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE)
        {
            if (gameStats.isPlayerDead()) {
                startNewGame();
            }
            if (!skippedMainMenu)
            {
                gameStats.setInGame(true);
                skippedMainMenu = true;
                startNewGame();
            }
            if (gameStats.isGamePaused())
            {
                gameStats.unPauseGame();
            }
        }

        if (key == KeyEvent.VK_CONTROL) {
            if (!skippedMainMenu) {
                gameStats.setSimulationMode(true);
                skippedMainMenu = true;
                startNewGame();

                new NEATDiagnostics();
            }
        }
    }

    /**
     * Event listener. Pauses game when
     * ESC is pressed
     *f
     */
    public void pauseGame()
    {
        if (skippedMainMenu)
        {
            gameStats.pauseGame();
        }
    }

    /**
     * Draw points to screen.
     *
     * @param g
     */
    public void drawPoints(Graphics g)
    {
        String score = Integer.toString(gameStats.getScore());
        String totalPoints = "";

        int i = score.length();
        while (i < 8)
        {
            totalPoints += "0";
            i++;
        }

        totalPoints += score;
        Font font = new Font("OCR A Std", Font.BOLD, 12);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(totalPoints,
            (int)(commons.getWidth() * 0.75),
            (int)(commons.getHeight() * 0.98));

    }

    /**
     * Draw number of lives left
     * to screen.
     *
     * @param g
     */
    public void drawLives(Graphics g)
    {
        Font font = new Font("OCR A Std", Font.PLAIN, 12);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("LIVES "+gameStats.getLives(),
            7,
            (int)(commons.getHeight() * 0.98));
    }

    public void drawLevel(Graphics g) {
        Font font = new Font("OCR A Std", Font.PLAIN, 12);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("LEVEL "+ gameStats.getLevel(), 7, (int)(commons.getHeight() * 0.93));
    }

    /**
     * Draw game over message to screen
     *
     * @param g
     */
    public void drawGameOver(Graphics g)
    {
        Font font = new Font("OCR A Std", Font.BOLD, 30);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("GAME OVER",
                (int) (commons.getWidth() * 0.17),
                (int) (commons.getHeight() * 0.5));
        font = new Font("OCR A Std", Font.PLAIN, 11);
        g.setFont(font);
        g.drawString("Press <SPACE> to start a new game",
                (int) (commons.getWidth() * 0.07),
                (int) (commons.getHeight() * 0.55));
    }

    /**
     * Draw game paused message to screen
     *
     * @param g
     */
    public void drawGamePaused(Graphics g)
    {
        Font font = new Font("OCR A Std", Font.BOLD, 35);
        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString("PAUSED",
            (int)(commons.getWidth() * 0.25),
            (int)(commons.getHeight() * 0.5));
        font = new Font("OCR A Std", Font.PLAIN, 11);
        g.setFont(font);
        g.drawString("Press <SPACE> to return to game",
                (int) (commons.getWidth() * 0.1),
                (int) (commons.getHeight() * 0.55));
    }

    /**
     * Convert paddle to rectangle and
     * draw it to screen.
     *
     * @param g2
     */
    public void drawPaddle(Graphics2D g2)
    {
        g2.setColor(Color.WHITE);
        Rectangle paddleRect = gameStats.getPaddle().paddleAsRect();
        g2.draw(paddleRect);
        g2.fill(paddleRect);
    }

    /**
     * Convert ArrayList of bricks to rectangles and
     * draw them to screen.
     *
     * @param g2
     */
    public void drawBricks(Graphics2D g2)
    {
        //Rectangle brickRect[] = new Rectangle[40];
        ArrayList<Rectangle> brickRect;
        brickRect = new ArrayList<Rectangle>();
        List<Brick> bricksSync = Collections.synchronizedList(bricks);
        int i = 0;
        g2.setColor(Color.WHITE);

        synchronized (bricksSync) {
            for (Brick a : bricksSync) {
                brickRect.add(bricksSync.get(i).brickAsRect());

                if (!bricksSync.get(i).isDestroyed()) {
                    g2.draw(brickRect.get(i));
                    g2.fill(brickRect.get(i));
                }

                i++;
            }
        }
    }

    /**
     * Convert bal to Ellipse and draw it to screen.
     *
     * @param g2
     */
    public void drawBall(Graphics2D g2)
    {
        Ellipse2D.Double ballShape = gameStats.getBall().ballAsEllipse();
        g2.setColor(Color.WHITE);
        g2.fill(ballShape);
        g2.draw(ballShape);
    }

    /**
     * Initialize brick position and add
     * bricks to the ArrayList for the first
     * level.
     */
    /*public void initializeBricks()
    {
        commons.setBWidth(38);

        for (int i=20; i<commons.getHeight()*0.2; i += commons.getBHeight()+6)
        {
            for (int j=2; j<commons.getWidth()-15; j += commons.getBWidth()+5)
            {
                bricks.add(new Brick(j,i));
            }
        }
    } */

    /**
     * Randomly initialize brick position and add
     * bricks to the ArrayList for every
     * next level.
     */

    public void randomizeBricks()
    {

        Random r = new Random();
        int randomInt;
        List<Brick> bricksSync = Collections.synchronizedList(bricks);

        synchronized (bricksSync) {
            for (int i = 20, k = 0; i < commons.getHeight() * 0.4; i += commons.getBHeight() + 6, k++) {
                for (int j = 10, l = 0; j < commons.getWidth() - 15; j += commons.getBWidth() + 5, l++) {
                    randomInt = r.nextInt(2);
                    if (randomInt == 1 || (gameStats.isSimulationMode() && gameStats.getLevel() == 1)) {
                        bricksSync.add(new Brick(j, i, new int[]{k, l}));
                        gameStats.setBrickState(k, l, true);
                    }
                }
            }
        }
    }

    /**
     * Check if ball intersects paddle
     * and change its speed and direction
     * accordingly.
     */
    public void collisionCheck()
    {
        if ((gameStats.getBall().ballAsEllipse()).intersects(gameStats.getPaddle().paddleAsRect()))
        {
            ballCollisionCount++;
            if (ballCollisionCount < MAX_BALL_COLLISIONS) {
                gameStats.getBall().changeVerticalDirection();
                adjustBallSpeedRelativeToPaddleIntersection();
                gameStats.incrementShots();
            }
        } else {
            ballCollisionCount = 0;
        }
        // If ball hits a brick, remove it
        brickCollisionDetection();
        checkIfPlayerIsDead();
    }

    /**
     * Remove brick from the ArrayList
     * when it is hit by the ball.
     */
    public void brickCollisionDetection()
    {
        int i = 0;
        //ArrayList copy to avoid ConcurrentModificationError
        List<Brick> bricksSync = Collections.synchronizedList(bricks);
        ArrayList<Brick> bricksToRemove = new ArrayList();

        synchronized (bricksSync) {
            boolean brickHit = false;
            for (Brick a : bricksSync) {
                Brick current = a;
                if ((gameStats.getBall().ballAsEllipse()).intersects(current.brickAsRect())) {
                    brickHit = true;
                    current.destroyBrick();
                    int[] ids = current.getIds();
                    gameStats.brickHit(ids[0], ids[1]);

                    bricksToRemove.add(a);
                }
            }
            if (brickHit) {
                gameStats.getBall().changeVerticalDirection();
            }
            bricksSync.removeAll(bricksToRemove);
        }
        checkForVictory();
    }

    /**
     * Determine if player is dead by checking
     * the number of lives left, and deduce a
     * life when ball hits bottom of screen.
     */
    public void checkIfPlayerIsDead()
    {
        if (gameStats.getLives() == 0)
        {
            if (!gameStats.isSimulationMode()) {
                gameStats.setInGame(false);
            }
            gameStats.setPlayerIsDead();
            gameStats.setGameLost(true);
            if (gameStats.isSimulationMode()) {
                startNewGame();
            }
            return;
        }
        if (gameStats.getBall().getY() + (2 * gameStats.getBall().getRadius()) == commons.getHeight())
        {
            if (!gameStats.isSimulationMode())
                gameStats.setInGame(false);

            if (gameStats.getLives() != 0 && !gameStats.isSimulationMode())
                gameStats.setGamePaused(true);

            gameStats.playerDied();

            if (gameStats.getLives() == 0) {
                gameStats.setPlayerIsDead();
                if (gameStats.isSimulationMode()) {
                    startNewGame();
                }
            }
        }
    }

    public void startNewGame() {
        gameInit();
        initStats();
    }

    /**
     * Determine if game must proceed
     * to next level.
     */
    public void checkForVictory()
    {
        List<Brick> bricksSync = Collections.synchronizedList(bricks);
        synchronized (bricksSync) {
            if (bricksSync.size() == 0) {
                gameStats.setGameWon(true);
                newLevel();
            }
        }
    }

    /**
     * The paddle is split into 4 positions
     * which determine the resulting horizontal
     * speed of the ball when it hits each one.
     * This allows for better player control over
     * the ball and enables better gameplay.
     */
    public void adjustBallSpeedRelativeToPaddleIntersection()
    {
        double pLeft = gameStats.getPaddle().getXLeft();
        double bLeft = gameStats.getBall().getX();

        double newSpeed = -1 * gameStats.MAX_BALL_SPEED + ((bLeft - pLeft) / commons.getPWidth()) * gameStats.MAX_BALL_SPEED * 2;


        // if we're in the middle, get random speed
        if (bLeft - (pLeft + commons.getPWidth() * 0.5) < 0.1 && bLeft - (pLeft + commons.getPWidth() * 0.5) > -0.1) {
            newSpeed = Math.random() * 4 - 2;
        }

        gameStats.getBall().setHorizontalSpeed(newSpeed);

    }


    /**
     * Key Listener.
     */
    private class TAdapter extends KeyAdapter
    {
        public void keyReleased(KeyEvent e)
        {
            gameStats.getPaddle().keyReleased(e);
        }

        public void keyPressed(KeyEvent e)
        {
            if (!gameStats.isSimulationMode()) {
                gameStats.getPaddle().keyPressed(e);
            }
            keyToStart(e);

            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !gameStats.isSimulationMode()) {
                pauseGame();
            }
        }
    }

    class ScheduleTask extends TimerTask
    {
        public void run()
        {
            if (gameStats.isInGame())
            {
                gameStats.getPaddle().move();
                gameStats.getBall().move();
                gameStats.movementOccured();
                collisionCheck();
            }

            repaint();
        }
    }
}
