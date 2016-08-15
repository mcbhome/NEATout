package Game.Breakout;

/**
The Commons class contains all
the variables which
are used by several classes. Screen
height and width are not set as constants because
content pane of the JFrame depend on the
Operating System running the program.
 */
public class Commons
{
    public static int brickWidth = 38;
    public static int brickHeight = 7;
    public static int paddleWidth = 60;
    public static int paddleHeight = 8;
    public static int width = 300;
    public static int height = 400;
    public boolean inGame = false;

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    public void setHeight(int newHeight)
    {
        height = newHeight;
    }

    public void setWidth(int newWidth)
    {
        width = newWidth;
    }

    public int getBWidth()
    {
        return brickWidth;
    }

    public void setBWidth(int bWidth_)
    {
        brickWidth = bWidth_;
    }

    public int getBHeight()
    {
        return brickHeight;
    }

    public int getPWidth()
    {
        return paddleWidth;
    }

    public int getPHeight()
    {
        return paddleHeight;
    }

    public boolean getInGame()
    {
        return inGame;
    }

    public void setInGame(boolean a)
    {
        inGame = true;
    }
}
