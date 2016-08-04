package Game.Breakout; /**
The paddle class contains the coordinates
of the paddle as well as its horizontal speed.
 */
import java.awt.*;
import java.awt.event.*;

public class Paddle
{
    public enum Direction {
        LEFT, RIGHT
    }

    private double xLeft;
    private int pWidth;
    private int pHeight;
    private double dx;

    public Paddle(int x, int y)
    {
        xLeft = x;
        pHeight = y;
        pWidth = 60;
    }

    public double getXLeft()
    {
        return xLeft;
    }

    public int getPHeight()
    {
        return pHeight;
    }

    public int getPWidth()
    {
        return pWidth;
    }

    /**
     * Move left or right until
     * a wall is reached.
     */
    public void move()
    {
        Commons commons = new Commons();
        xLeft += dx;
        if (xLeft <= 2)
            xLeft = 2;
        if (xLeft >= commons.getWidth()-pWidth)
            xLeft = commons.getWidth()-pWidth-2;
    }

    public Rectangle paddleAsRect()
    {
        Rectangle paddle
            = new Rectangle((int)xLeft, pHeight, pWidth, 3);

        return paddle;
    }

    /**
     * Event listeners for controlling
     * the paddle.
     * @param e
     */
    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT)
        {
            changeDir(Direction.LEFT, 1.5);
        }

        if (key == KeyEvent.VK_RIGHT)
        {
            changeDir(Direction.RIGHT, 1.5);

        }
    }

    public void changeDir(Direction dir, double speed) {
        if (dir == Direction.LEFT) {
            dx = -2 * speed;
        } else if (dir == Direction.RIGHT) {
            dx = 2 * speed;
        }
    }

    public void keyReleased(KeyEvent e)
    {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT)
        {
            dx = 0;
        }

        if (key == KeyEvent.VK_RIGHT)
        {
            dx = 0;
        }
    }
}
