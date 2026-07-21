/***********************************************************************
*Name: JGColorIndex
*Description: associates a color of the index image with a tile frame
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Packate declaration
package JGames2D;

//Packages useds
import java.awt.Color;

public class JGColorIndex 
{
	//Class Attributes
	private int frameIndex;
	private Color color;
	
	/***********************************************************
	*Name: JGColorIndex
	*Description: constructor
	*Parameters: int, Color
	*Return: none
	************************************************************/
	public JGColorIndex(int index, Color color)
	{
		frameIndex = index;
		this.color = color;
	}
	
	/***********************************************************
	*Name: getColor
	*Description: color getter
	*Parameters: none
	*Return: Color
	************************************************************/
	public Color getColor()
	{
		return color;
	}
	
	/***********************************************************
	*Name: getFrameIndex
	*Description: frameIndex getter
	*Parameters: none
	*Return:int
	************************************************************/
	public int getFrameIndex()
	{
		return frameIndex;
	}
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void free() 
    {
    	color = null;
    }
}
