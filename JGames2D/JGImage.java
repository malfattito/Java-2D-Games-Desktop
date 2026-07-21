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
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;

public class JGImage 
{
	//class Attributes
	private String imageName = null;
	private BufferedImage image = null;
	private int referenceCount;
	private int transparency = Transparency.TRANSLUCENT;
	
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
			transparency = detectTransparency(image);
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
   	* Name: detectTransparency
   	* Description: looks at the pixels to find the cheapest transparency that
   	*              still reproduces the image. A PNG with an alpha channel is
   	*              declared TRANSLUCENT even when no pixel is semi transparent,
   	*              and TRANSLUCENT costs a blend on every draw.
   	* Parameters: BufferedImage
   	* Returns: int
   	******************************************/
	private static int detectTransparency(BufferedImage source)
	{
		if (!source.getColorModel().hasAlpha())
		{
			return Transparency.OPAQUE;
		}

		boolean hasHoles = false;

		for (int y = 0; y < source.getHeight(); y++)
		{
			for (int x = 0; x < source.getWidth(); x++)
			{
				int alpha = source.getRGB(x, y) >>> 24;

				//Um unico pixel meio transparente ja exige a mistura completa
				if (alpha != 0 && alpha != 255)
				{
					return Transparency.TRANSLUCENT;
				}

				if (alpha == 0)
				{
					hasHoles = true;
				}
			}
		}

		return hasHoles ? Transparency.BITMASK : Transparency.OPAQUE;
	}

	/*******************************************
   	* Name: getTransparency
   	* Description: transparency actually needed by the image pixels
   	* Parameters: none
   	* Returns: int
   	******************************************/
	public int getTransparency()
	{
		return transparency;
	}

	/*******************************************
   	* Name: createCompatibleImage
   	* Description: creates an image in the same format the screen uses.
   	*              Blitting one of these is a straight copy; a generic format
   	*              like TYPE_4BYTE_ABGR forces a conversion on every draw.
   	* Parameters: int, int, int
   	* Returns: BufferedImage
   	******************************************/
	static BufferedImage createCompatibleImage(int width, int height, int transparency)
	{
		try
		{
			GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsConfiguration configuration = environment.getDefaultScreenDevice().getDefaultConfiguration();

			return configuration.createCompatibleImage(width, height, transparency);
		}
		catch(Throwable t)
		{
			//Sem tela disponivel resta o formato generico
			return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		}
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
