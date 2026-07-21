/***********************************************************************
*Name: JGTimer
*Description: This class represents a lapse of time
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

public class JGTimer 
{
	//Class attributes
	private long iEndTime;
	private long iCurrentTime; 
	
	/***********************************************************
	*Name: JGTimer
	*Description: No parameters constructor
	*Parameters: None
	*Return: None
	************************************************************/
	public JGTimer()
	{
		iCurrentTime = 0;
		iEndTime = 1000;
	}
	
	/***********************************************************
	*Name: JGTimer
	*Description: Parameterized constructor
	*Parameters: long
	*Return: None
	************************************************************/
	public JGTimer(long pInterval)
	{
		iCurrentTime = 0;
		iEndTime = pInterval;
	}
	
	/*******************************************
	* Name: update()
	* Description: performs a update on timer according the last frame time
	* Parameters: none
	* Returns: none
	******************************************/
	public void update()
	{
		iCurrentTime += JGTimeManager.getCurrentFrameTime();
	}
	
	/*******************************************
	* Name: isTimeEnded()
	* Description: used to test if time is ended
	* Parameters: none
	* Returns: boolean
	******************************************/
	public boolean isTimeEnded()
	{
		if (iCurrentTime >= iEndTime)
			return true;

		return false;
	}
	
	/*******************************************
	* Name: restart()
	* Description: restart timer with previous interval
	* Parameters: none
	* Returns: none
	******************************************/
	public void restart()
	{
		iCurrentTime = 0;
	}

	/*******************************************
	* Name: consume()
	* Description: discounts one interval from the elapsed time, keeping the
	*              remainder. Use instead of restart() to avoid drift.
	* Parameters: none
	* Returns: none
	******************************************/
	public void consume()
	{
		if (iEndTime <= 0)
		{
			iCurrentTime = 0;
			return;
		}

		iCurrentTime -= iEndTime;

		if (iCurrentTime < 0)
		{
			iCurrentTime = 0;
		}
	}
	
	/*******************************************
	* Name: restart()
	* Description: restart timer with a new interval
	* Parameters: long
	* Returns: none
	******************************************/
	public void restart(long pNewTime)
	{
		iCurrentTime = 0;
		iEndTime = pNewTime;
	}
	
	/*******************************************
	* Name: getCurrentTime()
	* Description: returns the current time value
	* Parameters: none
	* Returns: long
	******************************************/
	public long getCurrentTime()
	{
		return iCurrentTime;
	}
	
	/*******************************************
	* Name: getEndTime()
	* Description: returns the end time value
	* Parameters: none
	* Returns: long
	******************************************/
	public long getEndTime()
	{
		return iEndTime;
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
