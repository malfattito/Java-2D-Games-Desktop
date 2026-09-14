/***********************************************************************
*Name: JGGameManager
*Description: class that controlls all resources of the engine
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package Declaration
package JGames2D;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class JGEngine implements Runnable
{
	//Constants of the class
	private final int FRAME_TIME = 33;

	//Criada uma vez: um new Font por quadro so gera lixo para o coletor
	private static final Font STATS_FONT = new Font("Monospaced", Font.BOLD, 14);
	
	//Class attributes
	public JGWindowManager windowManager = null;
	public JGInputManager inputManager = null;
	public Graphics2D graphics = null;
	public JGLevel currentLevel = null;
	private ArrayList<JGLevel> vetLevels = null;
	private boolean executing = true;

	//On-screen frame-time meter, turned on with -Djg.stats=true (or by
	//setting showStats at runtime). Measures the work done per frame so a
	//weak machine can be checked against the 33 ms budget.
	public boolean showStats = false;
	private long lastWorkNanos = 0;      //work time of the last finished frame
	private long maxWorkNanos = 0;       //worst frame inside the current 1 s window
	private long peakWorkNanos = 0;      //worst frame of the previous window (shown)
	private int framesThisSecond = 0;    //frames counted in the current window
	private int currentFps = 0;          //frames completed in the previous window
	private long statsWindowStart = 0;   //start of the current 1 s window
	
	/***********************************************************
	*Name: JGGameManager
	*Description: constructor
	*Parameters: none
	*Return: none
	************************************************************/
	public JGEngine()
	{
		loadResources();
		showStats = Boolean.getBoolean("jg.stats");
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
			statsWindowStart = System.nanoTime();

			while (executing)
			{
				long frameStart = System.nanoTime();

				update();
				swapBuffers();
				recordFrame(System.nanoTime() - frameStart);
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

		if (showStats)
		{
			renderStats();
		}
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
	*Name: recordFrame
	*Description: feeds the frame-time meter. Keeps the last frame's work
	*             time and, over a rolling one second window, the achieved
	*             frame count and the worst frame, so a spike over the
	*             33 ms budget on a weak machine is visible.
	*Parameters: long (work time of the frame, in nanoseconds)
	*Return: none
	************************************************************/
	private void recordFrame(long workNanos)
	{
		lastWorkNanos = workNanos;

		if (workNanos > maxWorkNanos)
		{
			maxWorkNanos = workNanos;
		}

		framesThisSecond++;

		long now = System.nanoTime();

		if (now - statsWindowStart >= 1000000000L)
		{
			currentFps = framesThisSecond;
			peakWorkNanos = maxWorkNanos;
			framesThisSecond = 0;
			maxWorkNanos = 0;
			statsWindowStart = now;
		}
	}

	/***********************************************************
	*Name: renderStats
	*Description: draws the frame-time meter over the finished frame. Green
	*             while the worst frame of the last second stayed inside the
	*             budget, red once it spilled past it - that is the moment
	*             the 30 FPS floor is at risk.
	*Parameters: none
	*Return: none
	************************************************************/
	private void renderStats()
	{
		if (graphics == null)
		{
			return;
		}

		double workMs = lastWorkNanos / 1000000.0;
		double peakMs = peakWorkNanos / 1000000.0;
		String line = String.format("FPS %d   %.1f ms   pico %.1f / %d ms",
				currentFps, workMs, peakMs, FRAME_TIME);

		Font previousFont = graphics.getFont();
		Color previousColor = graphics.getColor();

		graphics.setFont(STATS_FONT);
		graphics.setColor(new Color(0, 0, 0, 160));
		graphics.fillRect(4, 4, 250, 22);
		graphics.setColor(peakMs > FRAME_TIME ? Color.RED : Color.GREEN);
		graphics.drawString(line, 10, 20);

		graphics.setFont(previousFont);
		graphics.setColor(previousColor);
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