/***********************************************************************
*Name: JGTimeManager
*Description: Singleton class that controls and updates all time objects
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

public class JGTimeManager 
{
	//Class attributes
	private static long 	lLastFrameTime = System.nanoTime();
	private static long 	lCurrentFrameTime = 0;
	private static final int MAX_INTERVAL = 250;

	/***********************************************************
	*Name: JGTimeManager
	*Description: Private and no parameters constructor
	*Parameters: None
	*Return: None
	************************************************************/
	private JGTimeManager()
	{
	}
	
	/*******************************************
	* Name: getCurrentFrameTime()
	* Description: returns the current frame time
	* Parameters: none
	* Returns: long
	******************************************/
	public static long getCurrentFrameTime()
	{
		return lCurrentFrameTime;
	}
	
	/*******************************************
	* Name: restart()
	* Description: restart the last frame time
	* Parameters: none
	* Returns: none
	******************************************/
	public static void restart()
	{
		lLastFrameTime = System.nanoTime();
		lCurrentFrameTime = 0;
	}
	
	/*******************************************
	* Name: update()
	* Description: handle time events
	* Parameters: none
	* Returns: none
	******************************************/
	public static void update()
	{
		//nanoTime e monotonico: nao sofre com ajustes do relogio do sistema
		long currentTime = System.nanoTime();

		lCurrentFrameTime = (currentTime - lLastFrameTime) / 1000000L;
		lLastFrameTime = currentTime;

		//Limita o salto apos uma pausa longa (janela minimizada, breakpoint)
		//para que os temporizadores nao avancem varios segundos de uma vez
		if (lCurrentFrameTime > MAX_INTERVAL)
		{
			lCurrentFrameTime = MAX_INTERVAL;
		}
		else if (lCurrentFrameTime < 0)
		{
			lCurrentFrameTime = 0;
		}
	}
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void free() 
    {
    }
}
