/***********************************************************************
*Name: JGInputManager
*Description: handle the user events like mouse and keyboard
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used Packages
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

public class JGInputManager implements KeyListener, MouseListener, MouseMotionListener
{
	//Class attributes
	private final int KEYS_NUMBER = 256;
	private boolean[] keyStates = null;
	private boolean[] keyReleasedStates = null;
	private JGVector2D mousePosition = null;
	private boolean mouseState = false;
	private boolean mouseReleasedState = false;
	private JGWindowManager windowManager = null;

	//Os eventos chegam pela thread da interface e sao lidos pela thread do jogo
	private final Object inputLock = new Object();

	/*******************************************
   	* Name: JGInputManager
   	* Description: user event handler
   	* Parameters: JFrame
   	* Returns: none
   	******************************************/
	public JGInputManager(JFrame window)
	{
		mousePosition = new JGVector2D();

		keyStates = new boolean[KEYS_NUMBER];
		keyReleasedStates = new boolean[KEYS_NUMBER];

		if (window instanceof JGWindowManager)
		{
			windowManager = (JGWindowManager)window;
		}

		window.addKeyListener(this);
		window.addMouseMotionListener(this);
		window.addMouseListener(this);
	}

	/*******************************************
   	* Name: isValidKey
   	* Description: tells if the key code fits the state table
   	* Parameters: int
   	* Returns: boolean
   	******************************************/
	private boolean isValidKey(int keyCode)
	{
		return (keyCode >= 0 && keyCode < KEYS_NUMBER);
	}

	/*******************************************
   	* Name: updateMousePosition
   	* Description: converts window coordinates into drawing area coordinates
   	* Parameters: MouseEvent
   	* Returns: none
   	******************************************/
	private void updateMousePosition(MouseEvent e)
	{
		if (windowManager == null)
		{
			mousePosition.setXY(e.getX(), e.getY());
			return;
		}

		//e.getX()/getY() estao em coordenadas da janela: descontar a origem da
		//area de desenho (bordas ou tarjas) e desfazer a ampliacao da tela cheia
		java.awt.Point origin = windowManager.getContentOrigin();
		double scale = windowManager.getRenderScale();

		if (scale <= 0)
		{
			scale = 1.0;
		}

		mousePosition.setXY((e.getX() - origin.x) / scale, (e.getY() - origin.y) / scale);
	}
	
	/*******************************************
   	* Name: reset()
   	* Description: reset event handler
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void reset()
	{
		synchronized (inputLock)
		{
			for (int index=0; index < keyStates.length; index++)
			{
				keyStates[index] = false;
				keyReleasedStates[index] = false;
			}
			mouseState = false;
			mouseReleasedState = false;
		}
	}

	/*******************************************
   	* Name: endFrame()
   	* Description: closes the input frame, discarding the events already
   	*              delivered to the scene. Called once per frame by the engine
   	*              so that reading an event never consumes it: every object can
   	*              ask about the same click during the whole frame.
   	* Parameters: none
   	* Returns: none
   	******************************************/
	void endFrame()
	{
		synchronized (inputLock)
		{
			for (int index=0; index < keyReleasedStates.length; index++)
			{
				keyReleasedStates[index] = false;
			}
			mouseReleasedState = false;
		}
	}
	
	/*******************************************
   	* Name: keyReleased
   	* Description: returns if key was released
   	* Parameters: none
   	* Returns: JGVector2D
   	******************************************/
	public boolean keyReleased(int keyCode)
	{
		if (!isValidKey(keyCode))
		{
			return false;
		}

		synchronized (inputLock)
		{
			return keyReleasedStates[keyCode];
		}
	}
	
	/***********************************************************
	*Name: mouseReleased
	*Description: returns if button mouse released
	*Params: none
	*Return: boolean
	************************************************************/
	public boolean mouseReleased()
	{
		synchronized (inputLock)
		{
			return mouseReleasedState;
		}
	}
	
	/*******************************************
   	* Name: keyWasPressed
   	* Description: returns if key was pressed before
   	* Parameters: int
   	* Returns: boolean
   	******************************************/
	public boolean keyTyped(int keyCode)
	{
		if (!isValidKey(keyCode))
		{
			return false;
		}

		//Nao zera o estado: varios objetos podem consultar a mesma tecla no
		//mesmo quadro. A limpeza acontece uma vez so, em endFrame().
		synchronized (inputLock)
		{
			return keyReleasedStates[keyCode];
		}
	}
	
	/***********************************************************
	*Name: mouseClicked
	*Description: returns if button mouse clicked (up / down)
	*Params: none
	*Return: boolean
	************************************************************/
	public boolean mouseClicked()
	{
		//Nao zera o estado: todos os botoes do menu podem testar o mesmo
		//clique no mesmo quadro. A limpeza acontece em endFrame().
		synchronized (inputLock)
		{
			return mouseReleasedState;
		}
	}
	
	/*******************************************
   	* Name:keyIsPressed
   	* Description: returns if key is pressed now
   	* Parameters: int
   	* Returns: boolean
   	******************************************/
	public boolean keyPressed(int keyCode)
	{
		if (!isValidKey(keyCode))
		{
			return false;
		}

		synchronized (inputLock)
		{
			return keyStates[keyCode];
		}
	}

	/***********************************************************
	*Name: mouseEntered()
	*Description: method da interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public boolean mousePressed()
	{
		synchronized (inputLock)
		{
			return mouseState;
		}
	}
	/*******************************************
   	* Name: getMousePosition
   	* Description: returns the last mouse position
   	* Parameters: none
   	* Returns: JGVector2D
   	******************************************/
	public JGVector2D getMousePosition()
	{
		return mousePosition;
	}
	
	/***********************************************************
	*Name: keyPressed()
	*Description: method of interface KeyListener
	*Params: KeyEvent
	*Return: Nenhum
	************************************************************/ 
	public void keyPressed(KeyEvent e)
	{
		if(isValidKey(e.getKeyCode()))
		{
			synchronized (inputLock)
			{
				keyStates[e.getKeyCode()] = true;
			}
		}
	}
	
	/***********************************************************
	*Name: keyRelesead()
	*Description: method of interface KeyListener
	*Params: KeyEvent
	*Return: Nenhum
	************************************************************/
	public void keyReleased(KeyEvent e)
	{
		if(isValidKey(e.getKeyCode()))
		{
			//Marca o evento mesmo que a tecla tenha sido pressionada e solta
			//entre dois quadros: assim um toque rapido nunca se perde
			synchronized (inputLock)
			{
				keyReleasedStates[e.getKeyCode()] = true;
				keyStates[e.getKeyCode()] = false;
			}
		}
	}
	
	/***********************************************************
	*Name: keyTyped()
	*Description: method of interface KeyListener
	*Params: KeyEvent
	*Return: Nenhum
	************************************************************/
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	/***********************************************************
	*Name: mouseMoved()
	*Description: method of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseMoved(MouseEvent e)
	{
		updateMousePosition(e);
	}
	
	/***********************************************************
	*Name: mouseDragged()
	*Description: method of interface MouseMotinListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseDragged(MouseEvent e)
	{
		updateMousePosition(e);
	}
	
	/***********************************************************
	*Name: mouseExited()
	*Description: method of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseExited(MouseEvent e)
	{
	
	}
	
	/***********************************************************
	*Name: mouseReleased()
	*Description: mmethod of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseReleased(MouseEvent e)
	{
		synchronized (inputLock)
		{
			mouseReleasedState = true;
			mouseState = false;
		}
	}
	
	/***********************************************************
	*Name: mouseClicked()
	*Description: method of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseClicked(MouseEvent e)
	{
		
	}
	
	/***********************************************************
	*Name: mouseEntered()
	*Description:method of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mouseEntered(MouseEvent e)
	{
	
	}
	
	/***********************************************************
	*Name: mouseEntered()
	*Description: method of interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public void mousePressed(MouseEvent e)
	{
		synchronized (inputLock)
		{
			mouseState = true;
		}
	}
	
	/***********************************************************
	*Name: free()
	*Description: free resources
	*Params: none
	*Return: none
	************************************************************/
	public void free()
	{
		keyStates = null;
		keyReleasedStates = null;
		mousePosition.free();
		mousePosition = null;
	}
}
