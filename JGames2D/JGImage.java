/***********************************************************************
*Name: JGImage
*Description: represents a visual image used to characters or scenes
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

public class JGImage 
{
	//class Attributes
	private String imageName = null;
	private BufferedImage image = null;
	private int referenceCount;
	
	/***********************************************************
	*Name:JGImage
	*Description: constructor
	*Parameters:none
	*Return: none
	************************************************************/
	public JGImage()
	{
		imageName = "";
		referenceCount = 1;
	}
	
	/***********************************************************
	*Name:getImageName
	*Description: returns the name of the image
	*Parameters:none
	*Return: String
	************************************************************/
	public String getImageName()
	{
		return imageName;
	}
	
	/***********************************************************
	*Name:getImage
	*Description: returns the image object
	*parameters: none
	*Return: BufferedImage
	************************************************************/
	public BufferedImage getImage()
	{
		return image;
	}
	
	/***********************************************************
	*Name:getImageWidth
	*Description: returns the image width in pixels
	*parameters: none
	*Return: int
	************************************************************/
	public int getImageWidth()
	{
		return image.getWidth();
	}
	
	/***********************************************************
	*Name:getImageHeight
	*Description: returns the image height in pixels
	*parameters: none
	*Return: int
	************************************************************/
	public int getImageHeight()
	{
		return image.getHeight();
	}
	
	/***********************************************************
	*Name:load
	*Description: loads a image from file, returns true if successful 
	*parameters:URL
	*Return: boolean
	************************************************************/
	public boolean load(URL pNome)
	{
		try
		{ 
			//read the file image
			image = ImageIO.read(pNome);
			
			//Creates the model of colors of the image
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
			int transparency = image.getColorModel().getTransparency();
			BufferedImage copy = gc.createCompatibleImage(image.getWidth(),image.getHeight(), transparency);
			Graphics2D g2d = copy.createGraphics();
			g2d.drawImage(image,0,0,null);
			image = copy;
			imageName = pNome.getPath();
			g2d.dispose();
		}
		catch(Exception e)
		{
			JGLog.writeLog("ERROR LOAD IMAGE " + pNome+ "\n");
			return false;
		}
		
		return true;
	}
	
	/*******************************************
   	* Name: incReferenceCount
   	* Description: inc the number of references of image
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void incReferenceCount()
	{
		referenceCount++;
	}
	
	/*******************************************
   	* Name: decReferenceCount
   	* Description: dec the number of references of image
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void decReferenceCount()
	{
		referenceCount--;
	}
	
	/*******************************************
   	* Name: getReferenceCount
   	* Description: dec the number of references of image
   	* Parameters: none
   	* Returns: int
   	******************************************/
	public int getReferenceCount()
	{
		return referenceCount;
	}
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void free()
	{
		if (image != null)
		{
			image.flush();
			image = null;
		}
		imageName = null;
		referenceCount = 0;
	}
}
