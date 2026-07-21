/***********************************************************************
*Name: JGImageManager
*Description: singleton class that controls the processo of load a image 
*             or returns your reference
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.net.URL;
import java.util.ArrayList;

public class JGImageManager
{
	//Class Attributes
	private static ArrayList<JGImage> vetImages = null;
	
	/***********************************************************
	*Name: JGImageManager
	*Description: private constructor
	*Parameters: None
	*Return: None
	************************************************************/
	private JGImageManager()
	{}
	
	/***********************************************************
	*Name: init
	*Description:prepare the imageManager to the use
	*Parameters: None
	*Return: None
	************************************************************/
	public static void init()
	{
		vetImages = new ArrayList<JGImage>();
	}
	
	/***********************************************************
	*Name: loadImage
	*Description: load a image or reclycle your reference
	*Parameters: URL
	*Return: JGImage
	************************************************************/
	public static JGImage loadImage(URL pName)
	{
		if (pName == null)
		{
			JGLog.writeLog("ERROR LOAD IMAGE: URL nula\n");
			return null;
		}

		//try to recycle a image
		for (JGImage image : vetImages)
		{
			if (pName.getPath().equals(image.getImageName()))
			{
				//Mais um dono para a mesma imagem
				image.incReferenceCount();
				return image;
			}
		}

		//Creates a new image
		JGImage image = new JGImage();
		if (image.load(pName))
		{
			vetImages.add(image);
			return image;
		}

		return null;
	}

	/*******************************************
   	* Name: free
   	* Description: releases one reference of the image. The pixels are only
   	*              discarded when the last owner gives it up.
   	* Parameters: JGImage
   	* Returns: none
   	******************************************/
    public static void free(JGImage image)
    {
    	if (image == null || vetImages == null)
    	{
    		return;
    	}

    	image.decReferenceCount();

    	if (image.getReferenceCount() <= 0)
    	{
    		vetImages.remove(image);
    		image.free();
    	}
    }
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public static void free() 
    {
		for (JGImage image : vetImages)
		{
			image.free();
		}
		vetImages.clear();
		vetImages = null;
	}
}
