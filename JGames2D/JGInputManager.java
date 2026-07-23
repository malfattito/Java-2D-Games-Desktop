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

	//Estado continuo, lido agora: verdadeiro enquanto a tecla ou o botao esta
	//pressionado
	private boolean[] keyStates = null;
	private boolean mouseState = false;

	//Estado de borda: quantas vezes a tecla ou o botao foi solto. Dois
	//buffers - o do quadro, que a cena le, e o pendente, que a thread da AWT
	//preenche. O motor promove o pendente para o do quadro uma vez, no inicio
	//de cada quadro. Assim um evento que chega no meio do quadro e entregue no
	//quadro seguinte em vez de ser apagado antes de alguem ler, e dois toques
	//no mesmo quadro sao contados, e nao fundidos num unico bit.
	private int[] keyTypedFrame = null;
	private int[] keyTypedPending = null;
	private int mouseClickedFrame = 0;
	private int mouseClickedPending = 0;

	private JGVector2D mousePosition = null;
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
		keyTypedFrame = new int[KEYS_NUMBER];
		keyTypedPending = new int[KEYS_NUMBER];

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
				keyTypedFrame[index] = 0;
				keyTypedPending[index] = 0;
			}
			mouseState = false;
			mouseClickedFrame = 0;
			mouseClickedPending = 0;
		}
	}

	/*******************************************
   	* Name: beginFrame()
   	* Description: opens the input frame. Promotes the releases the AWT thread
   	*              gathered since the last frame into the buffer the scene
   	*              reads, and empties the pending one. Called once by the
   	*              engine at the start of every frame, before the scene reads:
   	*              a release that lands after this point waits in the pending
   	*              buffer and is delivered on the next frame instead of being
   	*              cleared before anyone sees it. Reading never consumes the
   	*              event, so every object can ask about the same click during
   	*              the whole frame.
   	* Parameters: none
   	* Returns: none
   	******************************************/
	void beginFrame()
	{
		synchronized (inputLock)
		{
			for (int index=0; index < keyTypedFrame.length; index++)
			{
				keyTypedFrame[index] = keyTypedPending[index];
				keyTypedPending[index] = 0;
			}
			mouseClickedFrame = mouseClickedPending;
			mouseClickedPending = 0;
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
			return keyTypedFrame[keyCode] > 0;
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
			return mouseClickedFrame > 0;
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
		//mesmo quadro. A troca de buffer acontece uma vez so, em beginFrame().
		synchronized (inputLock)
		{
			return keyTypedFrame[keyCode] > 0;
		}
	}

	/*******************************************
   	* Name: keyTypedCount
   	* Description: how many times the key was released during this frame. The
   	*              boolean keyTyped/keyReleased answer whether it happened at
   	*              all; this one preserves the count, so a scene that cares can
   	*              react to each of two quick taps in the same frame instead of
   	*              treating them as one.
   	* Parameters: int
   	* Returns: int
   	******************************************/
	public int keyTypedCount(int keyCode)
	{
		if (!isValidKey(keyCode))
		{
			return 0;
		}

		synchronized (inputLock)
		{
			return keyTypedFrame[keyCode];
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
		//clique no mesmo quadro. A troca de buffer acontece em beginFrame().
		synchronized (inputLock)
		{
			return mouseClickedFrame > 0;
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
			//Conta o evento no buffer pendente, mesmo que a tecla tenha sido
			//pressionada e solta entre dois quadros: o proximo beginFrame o
			//entrega, e um toque rapido nunca se perde
			synchronized (inputLock)
			{
				keyTypedPending[e.getKeyCode()]++;
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
			mouseClickedPending++;
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
		keyTypedFrame = null;
		keyTypedPending = null;
		mousePosition.free();
		mousePosition = null;
	}
}
