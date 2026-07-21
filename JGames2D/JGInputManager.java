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
	private boolean[] prevKeyStates = null;
	private JGVector2D mousePosition = null;
	private boolean mouseState = false;
	private boolean mousePrevState = false;
	private JGWindowManager windowManager = null;

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
		prevKeyStates = new boolean[KEYS_NUMBER];

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
		for (int index=0; index < keyStates.length; index++)
		{
			keyStates[index] = false;
			prevKeyStates[index] = false;
			
		}
		mouseState = false;
		mousePrevState = false;
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

		return prevKeyStates[keyCode] && !keyStates[keyCode];
	}
	
	/***********************************************************
	*Name: mouseReleased
	*Description: returns if button mouse released
	*Params: none
	*Return: boolean
	************************************************************/
	public boolean mouseReleased()
	{
		return (mousePrevState && !mouseState) ? true : false;
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

		boolean result = prevKeyStates[keyCode] && !keyStates[keyCode];

		prevKeyStates[keyCode] = false;

		return result;
	}
	
	/***********************************************************
	*Name: mouseClicked
	*Description: returns if button mouse clicked (up / down)
	*Params: none
	*Return: boolean
	************************************************************/
	public boolean mouseClicked()
	{
		boolean result = (mousePrevState && !mouseState);
		
		mousePrevState = false;
		
		return result;
	}
	
	/*******************************************
   	* Name:keyIsPressed
   	* Description: returns if key is pressed now
   	* Parameters: int
   	* Returns: boolean
   	******************************************/
	public boolean keyPressed(int keyCode)
	{
		return isValidKey(keyCode) && keyStates[keyCode];
	}

	/***********************************************************
	*Name: mouseEntered()
	*Description: method da interface MouseMotionListener
	*Params: MouseEvent
	*Return: Nenhum
	************************************************************/
	public boolean mousePressed()
	{
		return mouseState;
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
			keyStates[e.getKeyCode()] = true;
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
			prevKeyStates[e.getKeyCode()] = keyStates[e.getKeyCode()];
			keyStates[e.getKeyCode()] = false;
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
		mousePrevState = mouseState;
		mouseState = false;
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
		mouseState = true;
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
		prevKeyStates = null;
		mousePosition.free();
		mousePosition = null;
	}
}
