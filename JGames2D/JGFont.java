/***********************************************************************
*Name: JGFont
*Description: a piece of text on the screen. Holds the typeface, the size,
*             the colour and where it sits, so a scene stops carrying that
*             state in loose variables and stops measuring by hand to centre
*             a line.
*
*             The level renders its texts after the layers and the sprites,
*             so a text is always over the scene, never under a block that
*             happens to be drawn later.
*Author: Silvano Malfatti
*Date: 22/07/26
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class JGFont
{
	//How the text sits against its position
	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;

	//Class attributes
	public JGVector2D position = null;
	public String text = "";
	public Color color = Color.white;
	public int alignment = LEFT;
	public boolean visible = true;
	public boolean autoRender = true;

	private JGEngine gameManager = null;
	private Font font = null;

	/***********************************************************
	*Name: JGFont
	*Description: constructor
	*Parameters: JGEngine, String, int, int
	*Return: none
	************************************************************/
	JGFont(JGEngine manager, String family, int style, int size)
	{
		gameManager = manager;
		position = new JGVector2D(0, 0);
		font = new Font(family, style, size);
	}

	/***********************************************************
	*Name: setFont
	*Description: changes the typeface, the style and the size at once
	*Parameters: String, int, int
	*Return: none
	************************************************************/
	public void setFont(String family, int style, int size)
	{
		font = new Font(family, style, size);
	}

	/***********************************************************
	*Name: setSize
	*Description: changes the size, keeping the typeface and the style
	*Parameters: int
	*Return: none
	************************************************************/
	public void setSize(int size)
	{
		font = font.deriveFont((float)size);
	}

	/***********************************************************
	*Name: setStyle
	*Description: changes the style (Font.PLAIN, Font.BOLD, Font.ITALIC),
	*             keeping the typeface and the size
	*Parameters: int
	*Return: none
	************************************************************/
	public void setStyle(int style)
	{
		font = font.deriveFont(style);
	}

	/***********************************************************
	*Name: getFont
	*Description: the typeface as the AWT knows it
	*Parameters: none
	*Return: Font
	************************************************************/
	public Font getFont()
	{
		return font;
	}

	/***********************************************************
	*Name: getMetrics
	*Description: the measurements of the typeface. Null before the window
	*             exists, since there is nothing to measure against yet.
	*Parameters: none
	*Return: FontMetrics
	************************************************************/
	private FontMetrics getMetrics()
	{
		if (gameManager == null || gameManager.graphics == null)
		{
			return null;
		}

		return gameManager.graphics.getFontMetrics(font);
	}

	/***********************************************************
	*Name: getWidth
	*Description: how wide the text is on screen
	*Parameters: none
	*Return: int
	************************************************************/
	public int getWidth()
	{
		FontMetrics metrics = getMetrics();

		if (metrics == null || text == null)
		{
			return 0;
		}

		return metrics.stringWidth(text);
	}

	/***********************************************************
	*Name: getHeight
	*Description: how tall the text is, from the top of the tallest letter
	*             to the bottom of the ones that hang below the line
	*Parameters: none
	*Return: int
	************************************************************/
	public int getHeight()
	{
		FontMetrics metrics = getMetrics();

		if (metrics == null)
		{
			return 0;
		}

		return metrics.getAscent() + metrics.getDescent();
	}

	/***********************************************************
	*Name: getLeft
	*Description: where the first letter starts, which is the position
	*             itself only when the text is aligned to the left
	*Parameters: none
	*Return: int
	************************************************************/
	public int getLeft()
	{
		double x = position.getX();

		//Rounded down, not by integer division: half a pixel of an odd width
		//would land the text one pixel off from where measuring by hand puts
		//it, and the difference shows against art drawn beside it
		if (alignment == CENTER)
		{
			return (int)Math.floor(x - getWidth() / 2.0);
		}

		if (alignment == RIGHT)
		{
			return (int)Math.floor(x - getWidth());
		}

		return (int)Math.floor(x);
	}

	/***********************************************************
	*Name: getRight
	*Description: where the last letter ends
	*Parameters: none
	*Return: int
	************************************************************/
	public int getRight()
	{
		return getLeft() + getWidth();
	}

	/***********************************************************
	*Name: setPosition
	*Description: where the text sits. The y is the line the letters rest
	*             on, the way the AWT draws text, and the x depends on the
	*             alignment: the left edge, the middle or the right edge.
	*Parameters: double, double
	*Return: none
	************************************************************/
	public void setPosition(double x, double y)
	{
		position.setXY(x, y);
	}

	/***********************************************************
	*Name: centerOnScreen
	*Description: puts the text in the middle of the drawing area, keeping
	*             the line it rests on
	*Parameters: double
	*Return: none
	************************************************************/
	public void centerOnScreen(double y)
	{
		alignment = CENTER;

		setPosition(gameManager.windowManager.getResolutionWidth() / 2.0, y);
	}

	/***********************************************************
	*Name: render
	*Description: draws the text
	*Parameters: none
	*Return: none
	************************************************************/
	public void render()
	{
		if (!visible || text == null || text.length() == 0)
		{
			return;
		}

		Graphics2D graphics = gameManager.graphics;

		if (graphics == null)
		{
			return;
		}

		//The font of the context is left as it was found: scenes still draw
		//by hand around this, and a changed font would surprise them
		Font previous = graphics.getFont();

		graphics.setFont(font);
		graphics.setColor(color);
		graphics.drawString(text, getLeft(), (int)position.getY());

		graphics.setFont(previous);
	}

	/*******************************************
	* Name: free
	* Description: free resources
	* Parameters: none
	* Returns: none
	******************************************/
	public void free()
	{
		gameManager = null;
		position = null;
		font = null;
		text = null;
		color = null;
	}
}
