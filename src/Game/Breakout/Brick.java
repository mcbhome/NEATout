package Game.Breakout; /**
The Brick class includes variables which determine
the coordinates of each brick and whether it is
destroyed or not. Bricks are not implemented as
rectangles to allow for them to be used a sprites
in subsequent refactorings.
 */
import java.awt.*;

public class Brick
{
    private int xLeft;
    private int yTop;
    boolean isDestroyed;
    private int[] ids;

    /**
     * Constructor
     *
     * @param  x
     * @param  y
     * @return
     */
    public Brick(int x, int y, int[] ids)
    {
        xLeft = x;
        yTop = y;
        isDestroyed = false;
        this.ids = ids;
    }

    public int getX()
    {
        return xLeft;
    }

    public int getY()
    {
        return yTop;
    }

    public boolean isDestroyed()
    {
        return isDestroyed;
    }

    public void destroyBrick()
    {
        isDestroyed = true;
    }

    /**
     * Return brick as a rectangle.
     *
     * @return Rectangle
     */
    public Rectangle brickAsRect()
    {
        Commons commons = new Commons();
        Rectangle brick
            = new Rectangle(xLeft, yTop,
            commons.getBWidth(),
            commons.getBHeight());

        return brick;
    }

    public int[] getIds() {
        return ids;
    }
}
