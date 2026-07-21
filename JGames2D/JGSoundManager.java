/***********************************************************************
*Name: JGSoundManager
*Description: represents the animation sequence of image frames
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/
package JGames2D;

import java.net.URL;
import java.util.ArrayList;

public class JGSoundManager 
{
	//Class Attributes
	private static ArrayList<JGSoundEffect> vetSoundEffects = null;
	private static ArrayList<JGMusic> vetMusics = null;
		
	/***********************************************************
	*Name: JGImageManager
	*Description: private constructor
	*Parameters: None
	*Return: None
	************************************************************/
	private JGSoundManager()
	{}
		
	/***********************************************************
	*Name: init
	*Description:prepare the imageManager to the use
	*Parameters: None
	*Return: None
	************************************************************/
	public static void init()
	{
		vetSoundEffects = new ArrayList<JGSoundEffect>();
		vetMusics= new ArrayList<JGMusic>();
	}
	
	/***********************************************************
	*Name: loadSoundEffect
	*Description: load a image or reclycle your reference
	*Parameters: URL
	*Return: JGImage
	************************************************************/
	public static JGSoundEffect loadSoundEffect(URL pName)
	{
		//try to recycle a sound effect
		for (JGSoundEffect sound : vetSoundEffects)
		{
			if (pName.getPath().equals(sound.getSoundName()))
			{
				return sound;
			}
		}
		
		//Creates a new sound effect
		JGSoundEffect sound = new JGSoundEffect(pName);
		vetSoundEffects.add(sound);

		return sound;
	}
	
	/***********************************************************
	*Name: loadMusic
	*Description: load a music or reclycle your reference
	*Parameters: URL
	*Return: JGImage
	************************************************************/
	public static JGMusic loadMusic(URL pName)
	{
		//try to recycle a sound effect
		for (JGMusic music : vetMusics)
		{
			if (pName.getPath().equals(music.getMusicName()))
			{
				return music;
			}
		}
		
		//Creates a new image
		JGMusic music = JGMusic.initWithContentsOfURL(pName);
		if (music != null)
		{
			vetMusics.add(music);
			return music;
		}
		
		return null;
	}
	
	/*******************************************
   	* Name: freeMusic
   	* Description: free a musice from manager
   	* Parameters: JGMusic
   	* Returns: none
   	******************************************/
    public static void freeMusic(JGMusic music) 
    {
    	vetMusics.remove(music);
    }
	
	/*******************************************
   	* Name: freeSound
   	* Description: free a image from manager
   	* Parameters: JGImage
   	* Returns: none
   	******************************************/
    public static void freeSound(JGSoundEffect sound) 
    {
    	vetSoundEffects.remove(sound);
    }
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public static void free() 
    {
    	for (JGMusic music : vetMusics)
		{
			music.free();
		}
		vetMusics.clear();
		vetMusics = null;
		
		for (JGSoundEffect sound : vetSoundEffects)
		{
			sound.free();
		}
		vetSoundEffects.clear();
		vetSoundEffects = null;
	}
}
