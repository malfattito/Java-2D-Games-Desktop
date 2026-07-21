/***********************************************************************
*Name: JGAnimation
*Description: represents the animation sequence of image frames
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

public class JGAnimation 
{
	//Class attibutes
	private int currentFrame;
	private int[] vetFrames = null;
	private int fps;
	private boolean loop;
	private JGTimer timer = null;
		
	/***********************************************************
	*Name: JGAnimation
	*Description: parameterized constructors
	*Parameters: JGFrameIndex[]
	*Return: None
	************************************************************/
	public JGAnimation(int[] vetFrames)
	{
		fps = 1;
		currentFrame=0;
		this.vetFrames = vetFrames;
		loop = true;
		timer = new JGTimer();
	}
	
	/*******************************************
	* Name: setLoop()
	* Description: set the animation loop 
	* Parameters: boolean
	* Returns: none
	******************************************/
	public void setLoop(boolean loop)
	{
		this.loop = loop;
	}
	
	/*******************************************
	* Name: getTotalFrames()
	* Description: returns a count with total frames
	* Parameters: none
	* Returns: int
	******************************************/
	public int getTotalFrames()
	{
		return vetFrames.length;
	}
	
	/***********************************************************
	*Name: isEnded
	*Description: determines if the  current animation has ended or not.
	*             Always true to animations in loop
	*Parameters: None
	*Return: boolean
	************************************************************/
	public boolean isEnded()
	{
		if (loop)
		{
			return false;
		}
		
		return (currentFrame >= vetFrames.length);
	}
	
	/***********************************************************
	*Name: getCurrentFrame
	*Description: current frame getter
	*Parameters: None
	*Return: JGQuadAnim
	************************************************************/
	public int getCurrentFrame()
	{
		return getCurrentFrameIndex();
	}
	
	/***********************************************************
	*Name: getCurrentFrameIndex
	*Description: current frame getter integer
	*Parameters: None
	*Return: JGQuadAnim
	************************************************************/
	public int getCurrentFrameIndex()
	{
		if (currentFrame >= 0 && currentFrame < vetFrames.length)
		{
			return vetFrames[currentFrame];
		}
		
		return vetFrames[vetFrames.length-1];
	}
	
	/***********************************************************
	*Name: restart
	*Description: begin the animation again from first quad
	*Parameters: None
	*Return: None
	************************************************************/
	public void restart()
	{
		timer.restart(getFrameInterval());
		currentFrame = 0;
	}

	/***********************************************************
	*Name: getFrameInterval
	*Description: milliseconds each frame stays on screen. Never zero,
	*             which would make the animation loop spin forever.
	*Parameters: None
	*Return: long
	************************************************************/
	private long getFrameInterval()
	{
		return (fps > 0) ? Math.max(1, 1000 / fps) : 1000;
	}
	
	/***********************************************************
	*Name: setFps
	*Description: set the animation speed 
	*Parameter: int
	*Return: None
	************************************************************/
	public void setFPS(int fps)
	{
		
		this.fps = fps;
		timer.restart(getFrameInterval());
	}
	
	/***********************************************************
	*Name: update
	*Description: updates the animation frame with time 
	*Parameters: None
	*Return: None
	************************************************************/
	public void update()
	{
		timer.update();

		//Avanca quantos quadros couberem no tempo decorrido: com um laco
		//simples uma animacao mais rapida que a taxa de quadros ficaria lenta
		while (timer.isTimeEnded())
		{
			if(loop)
			{
				currentFrame++;
				currentFrame %= vetFrames.length;
			}
			else
			{
				currentFrame += (currentFrame < vetFrames.length)? 1: 0;

				if (currentFrame >= vetFrames.length)
				{
					timer.restart();
					return;
				}
			}

			//Desconta o intervalo em vez de zerar, preservando o tempo excedente
			timer.consume();
		}
	}
	
	/***********************************************************
	*Name: free
	*Description: free animation resources
	*Parameters: None
	*Return: None
	************************************************************/
	public void free()
	{
		vetFrames = null;
		timer = null;
	}
}
