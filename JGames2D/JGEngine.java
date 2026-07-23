/***********************************************************************
*Name: JGGameManager
*Description: class that controlls all resources of the engine
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package Declaration
package JGames2D;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class JGEngine implements Runnable
{
	//Constants of the class
	private final int FRAME_TIME = 33;
	
	//Class attributes
	public JGWindowManager windowManager = null;
	public JGInputManager inputManager = null;
	public Graphics2D graphics = null;
	public JGLevel currentLevel = null;
	private ArrayList<JGLevel> vetLevels = null;
	private boolean executing = true;
	
	/***********************************************************
	*Name: JGGameManager
	*Description: constructor
	*Parameters: none
	*Return: none
	************************************************************/
	public JGEngine()
	{
		loadResources();
	}
	
	/***********************************************************
	*Name: loadResources
	*Description: load the engine resources
	*Parameters: none
	*Return: none
	************************************************************/
	private void loadResources()
	{
		vetLevels = new ArrayList<JGLevel>();
		//JGLog.init();
		JGImageManager.init();
		JGSoundManager.init();
		windowManager = new JGWindowManager(this);
		inputManager = new JGInputManager(windowManager);
	}
	
	/***********************************************************
	*Name: start
	*Description: starts the engine execution
	*Parameters: none
	*Return: none
	************************************************************/
	public void start()
	{
		if (vetLevels.size( ) > 0)
		{
			setCurrentLevel(0);
		}
		windowManager.showWindow();
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/***********************************************************
	*Name: run
	*Description: method defined by the runnable interface
	*Parameters: none
	*Return: none
	************************************************************/
	public void run()
	{
		try
		{
			while (executing)
			{
				long frameStart = System.nanoTime();

				update();
				swapBuffers();
				pause(frameStart);
			}
		}
		catch(RuntimeException e)
		{
			//Sem isto um erro na cena mataria a thread do jogo deixando a
			//janela aberta e o processo vivo para sempre
			JGLog.writeLog("ERRO NAO TRATADO NO LOOP DO JOGO: " + e + "\n");
			e.printStackTrace();
		}
		finally
		{
			free();
		}
	}
	
	/***********************************************************
	*Name: update()
	*Description: method defined by the runnable interface
	*Parameters: none
	*Return: none
	************************************************************/
	private void update()
	{
		JGTimeManager.update();

		//Abre o quadro de entrada antes de a cena ler: promove os eventos que
		//a thread da AWT juntou desde o quadro anterior para o buffer que a
		//cena le. O que chegar daqui em diante espera o proximo quadro, em vez
		//de ser apagado antes de alguem ver.
		inputManager.beginFrame();

		JGLevel level = currentLevel;

		if (level == null)
		{
			return;
		}

		level.execute();

		//A logica da cena pode ter trocado o nivel corrente. Nesse caso o nivel
		//antigo ja foi liberado: o novo so sera desenhado no proximo quadro.
		if (level != currentLevel)
		{
			return;
		}

		level.update();

		windowManager.clearBackBuffer();
		level.render();
	}
	
	/***********************************************************
	*Name: pause
	*Description: pause the game loop
	*Parameters: none
	*Return: none
	************************************************************/
	private void pause(long frameStart)
	{
		//Desconta o tempo ja gasto no quadro para manter a taxa constante
		long elapsed = (System.nanoTime() - frameStart) / 1000000L;
		long remaining = FRAME_TIME - elapsed;

		if (remaining <= 0)
		{
			return;
		}

		try
		{
			Thread.sleep(remaining);
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			executing = false;
		}
	}
	
	/***********************************************************
	*Name: finish
	*Description: ends the engine execution
	*Parameters: none
	*Return: none
	************************************************************/
	public void finish()
	{
		executing = false;
	}
	
	/***********************************************************
	*Name: swapBuffers
	*Description: changes the front buffer by back buffer
	*Parameters: none
	*Return: none
	************************************************************/
	private void swapBuffers()
	{
		windowManager.presentFrame();
	}
	
	/***********************************************************
	*Name: setCurrentLevel
	*Description: define the current level to be executed
	*Parameters: none
	*Return: none
	************************************************************/
	public void setCurrentLevel(int levelIndex)
	{
		if (levelIndex >= 0 && levelIndex < vetLevels.size())
		{
			if (currentLevel != null)
			{
				currentLevel.free();
			}
			
			currentLevel = vetLevels.get(levelIndex);
			currentLevel.init();
			
			JGTimeManager.restart();
			inputManager.reset();
		}
	}
	
	/***********************************************************
	*Name: addLevel
	*Description: add a new level to the engine levels list
	*Parameters: none
	*Return: none
	************************************************************/
	public void addLevel(JGLevel newLevel)
	{
		if (newLevel != null)
		{
			newLevel.setGameManager(this);
			vetLevels.add(newLevel);
		}
	}
	
	/***********************************************************
	*Name: free
	*Description: free the engine resources
	*Parameters: none
	*Return: none
	************************************************************/
	private void free()
	{
		windowManager.removeKeyListener(inputManager);
		windowManager.removeMouseListener(inputManager);
		windowManager.removeMouseMotionListener(inputManager);
		windowManager.free();
		
		currentLevel = null;
		
		for (JGLevel level : vetLevels)
		{
			level.free();
		}
		vetLevels.clear();
		vetLevels = null;
		
		JGImageManager.free();
		JGSoundManager.free();
		
		inputManager.free();
		inputManager = null;
		
		graphics = null;
		
		System.gc();
	}
}