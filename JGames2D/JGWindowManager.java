/***********************************************************************
*Name: JGWindowManager
*Description: represents the window application and your resources
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

import java.awt.Color;
//Used packages
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

public class JGWindowManager extends JFrame
{
	private static final long serialVersionUID = 1L;

	//Depois disto a janela e dada como pronta mesmo sem aviso do sistema
	private static final long READY_TIMEOUT = 3000;

	//Class Attributes
	private int xPos, yPos;
	private int colorDepth;
	private String windowTitle = null;
	private boolean fullScreen;
	private GraphicsDevice graphDevice = null;

	//Duplo buffer por troca de referencia, e nao por copia: a thread do jogo
	//desenha em buffers[renderIndex] enquanto a EDT pinta buffers[displayIndex].
	//No fim do quadro os papeis se invertem, sem copiar pixel nenhum entre os
	//dois - o que economiza uma passada de tela cheia por quadro.
	//
	//CONTRATO: a cena deve ler gameManager.graphics a cada quadro, nunca guardar
	//a referencia numa variavel e reutiliza-la - ela passa a apontar para o
	//outro buffer a cada troca. Todas as cenas do motor ja fazem assim.
	private final BufferedImage[] buffers = new BufferedImage[2];
	private final Graphics2D[] graphics = new Graphics2D[2];
	private int renderIndex = 0;
	private int displayIndex = 1;
	private final Object bufferLock = new Object();
	private JGEngine gameManager = null;
	private Cursor cursor = null;
	private javax.swing.Timer cursorTimer = null;
	private volatile boolean displayReady = false;
	private volatile boolean waitingForSystem = false;
	private long showTime = 0;
	public Color backgroundColor = Color.black;
	public int width, height;
	
	/***********************************************************
	*Name: JGWindowManager
	*Description: construtor default
	*Parameters: JGGameManager
	*Return: None
	************************************************************/
	JGWindowManager(JGEngine gameManager)
	{
		windowTitle = "JGames2D";
		this.gameManager = gameManager;
		graphDevice = null;
		width = 800;
		height = 600;
		colorDepth = 32;
		fullScreen = false;
		xPos = 0;
		yPos = 0;
		initWindow();
	}
	
	/***********************************************************
	*Name: getGraphicsBackBuffer()
	*Description: returns the Graphics BackBuffer
	*Parameters: void
	*Return: Graphics2D
	************************************************************/
	Graphics2D getGraphicsBackBuffer()
	{
		return buffers[renderIndex].createGraphics();
	}
	
	/***********************************************************
	*Name: setBackgroundColor
	*Description: sets the background color used to clear the screen
	*Parameters:Color
	*Return: none
	************************************************************/
	public void setBackgroundColor(Color color)
	{
		backgroundColor = color;
		gameManager.graphics.setColor(color);
	}

	/***********************************************************
	*Name: clearBackBuffer
	*Description: fills the whole back buffer with the background color
	*Parameters: none
	*Return: none
	************************************************************/
	public void clearBackBuffer()
	{
		Color previousColor = gameManager.graphics.getColor();
		gameManager.graphics.setColor(backgroundColor);
		gameManager.graphics.fillRect(0, 0, width, height);
		gameManager.graphics.setColor(previousColor);
	}

	/***********************************************************
	*Name: getResolutionWidth
	*Description: returns the width of the drawing area, in pixels.
	*             Not to be confused with the inherited getWidth(),
	*             which returns the outer size of the window.
	*Parameters: none
	*Return: int
	************************************************************/
	public int getResolutionWidth()
	{
		return width;
	}

	/***********************************************************
	*Name: getResolutionHeight
	*Description: returns the height of the drawing area, in pixels.
	*             Not to be confused with the inherited getHeight(),
	*             which returns the outer size of the window.
	*Parameters: none
	*Return: int
	************************************************************/
	public int getResolutionHeight()
	{
		return height;
	}
	
	/***********************************************************
	*Name: getFullScreen
	*Description: returns the fullscreen mode
	*Parameters:None
	*Return: boolean
	************************************************************/
	public boolean getFullScreen()
	{
		return fullScreen;
	}
	
	/***********************************************************
	*Name: getGraphicsDevice
	*Description: returns the GraphicsDevice object
	*Parameters:None
	*Return: boolean
	************************************************************/
	public GraphicsDevice getGraphicsDevice()
	{
		return graphDevice;
	}
	
	/***********************************************************
	*Name: getBackBufferImage
	*Description: returns the last frame published by presentFrame. A hand-driven
	*             test that dumps the frame must call presentFrame() before
	*             reading, so this returns the finished frame and not the one
	*             half drawn.
	*Parameters:None
	*Return: BufferedImage
	************************************************************/
	public BufferedImage getBackBufferImage()
	{
		return buffers[displayIndex];
	}
	
	/***********************************************************
	*Name: setWindowsPosition
	*Description: set the posicion of the windows in desktop
	*Parameters:int, int
	*Return: none
	************************************************************/
	public void setWindowPosition(int pPosX, int pPosY)
	{
		xPos = pPosX;
		yPos = pPosY;
	}
	
	/***********************************************************
	*Name: setWindowTitle
	*Description: set the title of the window
	*Parameters: String
	*Return: none
	************************************************************/
	public void setWindowTitle(String pTitle)
	{
		windowTitle = pTitle;
	}
	
	/***********************************************************
	*Name: setFullScreen
	*Description: set the fullscreen mode
	*Parameters: boolean
	*Return: none
	************************************************************/
	public void setfullScreen(boolean fullScreenMode)
	{
		fullScreen = fullScreenMode;
	}
	
	/***********************************************************
	*Name: setResolution
	*Description: set the window resolution
	*Parameters: boolean
	*Return: none
	************************************************************/
	public void setResolution(int width, int height, int depth)
	{
		this.width = width;
		this.height = height;
		colorDepth = depth;
		createBackBuffer();
	}

	/***********************************************************
	*Name: createBackBuffer
	*Description: (re)creates the back buffer and its graphics context,
	*             releasing the previous one
	*Parameters: none
	*Return: none
	************************************************************/
	private void createBackBuffer()
	{
		//Os dois buffers vivem ao mesmo tempo: um sendo desenhado, o outro
		//sendo exibido. A EDT pode estar dentro de paint() lendo o exibido, por
		//isso a troca acontece sob o cadeado que ela tambem usa.
		synchronized (bufferLock)
		{
			for (int index = 0; index < 2; index++)
			{
				if (graphics[index] != null)
				{
					graphics[index].dispose();
				}

				if (buffers[index] != null)
				{
					buffers[index].flush();
				}

				buffers[index] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				graphics[index] = buffers[index].createGraphics();
				applyRenderingHints(graphics[index]);
			}

			renderIndex = 0;
			displayIndex = 1;
			gameManager.graphics = graphics[renderIndex];
		}
	}

	/***********************************************************
	*Name: warmFonts
	*Description: mede e desenha uma vez cada familia de fonte dada, no buffer
	*             de tras, e o limpa. O primeiro uso de uma fonte no Java2D
	*             custa uma fracao de segundo - o subsistema de fontes sobe e a
	*             familia e carregada -, e paga uma vez por processo, nao por
	*             tela: sem isto o primeiro quadro de cada tela que a estreia
	*             levava de 150 a 300 ms. Chamado depois de setResolution e
	*             antes de a janela aparecer, o custo fica no arranque.
	*Parameters: String...
	*Return: none
	************************************************************/
	public void warmFonts(String... families)
	{
		Graphics2D graphics = this.graphics[renderIndex];

		if (graphics == null)
		{
			return;
		}

		java.awt.Font previous = graphics.getFont();

		for (String family : families)
		{
			for (int style : new int[]{ java.awt.Font.PLAIN, java.awt.Font.BOLD })
			{
				graphics.setFont(new java.awt.Font(family, style, 14));
				graphics.getFontMetrics().stringWidth("MakeMeDev 0123456789");
				graphics.drawString("MakeMeDev 0123456789", 0, 20);
			}
		}

		graphics.setFont(previous);
		clearBackBuffer();
	}

	/***********************************************************
	*Name: applyRenderingHints
	*Description: sets the Java2D hints on a graphics context, tuned for
	*             throughput rather than image quality: nearest-neighbour
	*             resampling and speed-biased rendering keep the per-frame
	*             cost down so the loop clears its 33 ms budget with room
	*             to spare. Called on every back buffer (re)creation and on
	*             the final scaled blit; kept in one place so setResolution's
	*             buffer swap never drops the hints.
	*Parameters: Graphics2D
	*Return: none
	************************************************************/
	private void applyRenderingHints(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
	}

	/***********************************************************
	*Name: presentFrame
	*Description: publishes the finished frame. Instead of copying the back
	*             buffer into the front one, it swaps their roles: the buffer
	*             just drawn becomes the one the EDT paints, and the next frame
	*             is drawn into the other. No pixels are copied.
	*Parameters: none
	*Return: none
	************************************************************/
	void presentFrame()
	{
		synchronized (bufferLock)
		{
			if (graphics[renderIndex] == null)
			{
				return;
			}

			//O quadro recem-desenhado passa a ser o exibido; o proximo vai no
			//outro buffer. A EDT pinta o exibido e nunca toca no que sera
			//desenhado, entao nao ha corrida de pixels nem imagem rasgada.
			displayIndex = renderIndex;
			renderIndex = 1 - renderIndex;
			gameManager.graphics = graphics[renderIndex];
		}

		repaint();
	}
	
	/***********************************************************
	*Name: initWindow
	*Description: show the window
	*Parameters: none
	*Return: none
	************************************************************/
	private void initWindow()
	{
		
		if (buffers[0] == null)
		{
			createBackBuffer();
		}

		//Esconde o ponteiro do mouse
		hideCursor();

		//A janela precisa ser focavel para receber os eventos de teclado
		setFocusable(true);

		/*Trata o botão fechar no caso de modo janela*/
		addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{		
				gameManager.finish();
			} 						
		}
		);
	}
	
	/***********************************************************
	*Name: mostraJanela()
	*Description: mostra a janela
	*Parametros: Nenhum
	*Retorno: Nenhum
	************************************************************/
	public void showWindow()
	{
		//Verifica o modo de video
		if (fullScreen)
		{
			//O modo exclusivo (setFullScreenWindow) foi abandonado: em macOS
			//recente ele esconde a barra de menu e o dock, porem a janela fica
			//apenas preta, tanto pintando por paint() quanto por BufferStrategy.
			//O back buffer continua na resolucao pedida e e ampliado no paint().
			GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			graphDevice = environment.getDefaultScreenDevice();

			colorDepth = graphDevice.getDisplayMode().getBitDepth();

			//A permissao tem de ser pedida antes de a janela aparecer: e ela
			//que diz se a tela cheia nativa esta ao alcance. Depois de exibida
			//nao ha volta - setUndecorated recusa uma janela ja mostrada, e
			//descobrir o impedimento so entao deixaria o jogo numa janelinha.
			if (allowNativeFullScreen())
			{
				//macOS: tela cheia nativa, a mesma do botao verde. Esconde o
				//dock e a barra de menu. Exige janela decorada e redimensionavel.
				setTitle(windowTitle);
				setSize(width, height);
				setLocation(xPos, yPos);
				listenToFullScreen();
				setVisible(true);
				requestNativeFullScreen();
			}
			else
			{
				//Demais sistemas: janela sem bordas cobrindo o monitor
				//inteiro. E os limites do monitor, e nao a area util
				//(getMaximumWindowBounds), que exclui a barra de tarefas: uma
				//janela do tamanho da area util deixa a faixa da barra de fora,
				//e ela continua aparecendo por cima. Sem borda, do tamanho do
				//monitor e sempre no topo, o Windows recolhe a barra por baixo.
				Rectangle screenArea = graphDevice.getDefaultConfiguration().getBounds();

				setUndecorated(true);
				setResizable(false);
				setSize(screenArea.width, screenArea.height);
				setLocation(screenArea.x, screenArea.y);
				setAlwaysOnTop(true);
				setVisible(true);

				//Traz a janela para a frente com foco: sem isso o Windows pode
				//manter a barra de tarefas por cima de uma janela sem borda que
				//nasce sem foco.
				toFront();
				requestFocus();

				//Aqui o tamanho ja vale: nao ha animacao a esperar
				displayReady = true;
			}
		}
		else
		{
			//Modo Janela
			setTitle(windowTitle);
			setLocation(xPos,yPos);
			setSize(width,height);
			setVisible(true);
			setResizable(false);

			//As bordas e a barra de titulo so existem depois que a janela e exibida.
			//Cresce a janela para que a area util tenha exatamente a resolucao pedida.
			Insets insets = getInsets();
			setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);

			//Em modo janela nao ha transicao: a janela ja esta no lugar
			displayReady = true;
		}

		showTime = System.currentTimeMillis();

		//O foco so pode ser solicitado depois que a janela esta visivel
		requestFocusInWindow();

		//Reaplica o cursor invisivel: a exibicao e a tela cheia refazem o peer
		hideCursor();
		keepCursorHidden();
	}

	/***********************************************************
	*Name: isDisplayReady
	*Description: tells if the window already reached its final size, so the
	*             game can start what it wants the player to see.
	*
	*             It matters because entering fullscreen on macOS is animated
	*             and only settles about a second after showWindow returns. A
	*             scene that starts an animation right away spends that second
	*             drawing into a window that is still growing, and the player
	*             misses the beginning of it.
	*
	*             In windowed mode, and on the systems that use the borderless
	*             window, this is true as soon as the window is shown.
	*Parameters: none
	*Return: boolean
	************************************************************/
	public boolean isDisplayReady()
	{
		if (displayReady)
		{
			return true;
		}

		//A geometria so serve onde o sistema nao avisa. Medido no macOS, a
		//janela informa o tamanho final aos 233 ms, mas a transicao so termina
		//aos 953 ms: confiar no tamanho daria a largada com a animacao ainda
		//correndo, que e exatamente o que se quer evitar.
		if (!waitingForSystem && isCoveringScreen())
		{
			displayReady = true;
			return true;
		}

		//Um sistema que nunca avise nao pode travar o jogo esperando
		if (showTime > 0 && System.currentTimeMillis() - showTime > READY_TIMEOUT)
		{
			displayReady = true;
			return true;
		}

		return false;
	}

	/***********************************************************
	*Name: isCoveringScreen
	*Description: tells if the window already occupies the whole screen
	*Parameters: none
	*Return: boolean
	************************************************************/
	private boolean isCoveringScreen()
	{
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		return getWidth() >= screen.width && getHeight() >= screen.height;
	}

	/***********************************************************
	*Name: listenToFullScreen
	*Description: asks macOS to tell us when the fullscreen transition ends.
	*             The listener is an interface, so it can be built by a proxy
	*             and the engine keeps compiling and running elsewhere.
	*Parameters: none
	*Return: none
	************************************************************/
	private void listenToFullScreen()
	{
		try
		{
			final Class<?> listenerType = Class.forName("com.apple.eawt.FullScreenListener");
			Class<?> utils = Class.forName("com.apple.eawt.FullScreenUtilities");

			Object listener = java.lang.reflect.Proxy.newProxyInstance(
				listenerType.getClassLoader(), new Class<?>[]{ listenerType },
				new java.lang.reflect.InvocationHandler()
				{
					public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments)
					{
						String name = method.getName();

						//Os metodos herdados de Object tambem chegam aqui
						if ("hashCode".equals(name))
						{
							return Integer.valueOf(System.identityHashCode(proxy));
						}
						if ("equals".equals(name))
						{
							return Boolean.valueOf(proxy == arguments[0]);
						}
						if ("toString".equals(name))
						{
							return "JGWindowManager.fullScreenListener";
						}

						if ("windowEnteredFullScreen".equals(name))
						{
							displayReady = true;
						}

						return null;
					}
				});

			utils.getMethod("addFullScreenListenerTo", java.awt.Window.class, listenerType)
			     .invoke(null, this, listener);

			//A partir daqui quem manda e o aviso do sistema
			waitingForSystem = true;
		}
		catch(Throwable t)
		{
			//Sem o aviso do sistema restam a geometria e o tempo limite
			JGLog.writeLog("AVISO DE TELA CHEIA INDISPONIVEL: " + t + "\n");
		}
	}

	/***********************************************************
	*Name: hideCursor
	*Description: puts a fully transparent cursor over the window, which is
	*             how the system pointer is hidden. Applied to the frame and
	*             to the content pane, and repeated after the window is shown:
	*             entering fullscreen rebuilds the peer, and the pointer of
	*             the system would come back.
	*Parameters: none
	*Return: none
	************************************************************/
	private void hideCursor()
	{
		if (cursor == null)
		{
			//Usa o tamanho que o sistema aceita: uma imagem de tamanho
			//diferente pode ser recusada, e o ponteiro volta a aparecer
			Dimension tamanho = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
			int largura = (tamanho.width > 0) ? tamanho.width : 16;
			int altura = (tamanho.height > 0) ? tamanho.height : 16;

			BufferedImage blank = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
			cursor = Toolkit.getDefaultToolkit().createCustomCursor(blank, new Point(0,0), "invisible");
		}

		//O ponteiro atravessa toda a hierarquia da janela: um componente que
		//nao tenha o cursor definido mostra o do sistema
		setCursor(cursor);

		if (getContentPane() != null)
		{
			getContentPane().setCursor(cursor);
		}
		if (getRootPane() != null)
		{
			getRootPane().setCursor(cursor);
			getRootPane().getLayeredPane().setCursor(cursor);
			getRootPane().getGlassPane().setCursor(cursor);
		}
	}

	/***********************************************************
	*Name: keepCursorHidden
	*Description: keeps repeating the hiding for a while. Entering fullscreen
	*             on macOS is animated and only finishes about a second later,
	*             and the system brings its own pointer back when it does, so
	*             hiding it once, before the animation, is not enough.
	*Parameters: none
	*Return: none
	************************************************************/
	private void keepCursorHidden()
	{
		if (cursorTimer != null)
		{
			cursorTimer.stop();
		}

		cursorTimer = new javax.swing.Timer(400, new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evento)
			{
				hideCursor();
			}
		});
		cursorTimer.start();

		//Trocar de aplicativo e voltar tambem devolve o ponteiro do sistema
		addWindowFocusListener(new java.awt.event.WindowAdapter()
		{
			public void windowGainedFocus(java.awt.event.WindowEvent evento)
			{
				hideCursor();
			}
		});
	}

	/***********************************************************
	*Name: allowNativeFullScreen
	*Description: marks the window as able to go fullscreen on macOS, and by
	*             doing it tells if the native API is really within reach.
	*
	*             Merely finding the classes is not enough: com.apple.eawt
	*             exists inside java.desktop but the module does not export it,
	*             so from Java 9 on the reflection only fails when it is used.
	*             The permission is asked here, before the window is shown,
	*             precisely so a refusal can still be answered with the
	*             borderless window instead of a small one in the corner.
	*
	*             Where it is refused the log says so, and the way out is the
	*             flag --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED
	*             on the java command, or the same as Add-Exports in the
	*             manifest of the jar.
	*Parameters: none
	*Return: boolean
	************************************************************/
	private boolean allowNativeFullScreen()
	{
		try
		{
			Class<?> utils = Class.forName("com.apple.eawt.FullScreenUtilities");
			utils.getMethod("setWindowCanFullScreen", java.awt.Window.class, boolean.class)
			     .invoke(null, this, Boolean.TRUE);

			//A troca em si vem depois, e passa pela mesma classe
			Class.forName("com.apple.eawt.Application").getMethod("getApplication");

			return true;
		}
		catch(Throwable t)
		{
			JGLog.writeLog("TELA CHEIA NATIVA INDISPONIVEL, USANDO JANELA SEM BORDA: " + t + "\n");
			return false;
		}
	}

	/***********************************************************
	*Name: requestNativeFullScreen
	*Description: asks macOS to put this window in fullscreen. Called by
	*             reflection so the engine still compiles and runs elsewhere.
	*             Only makes sense after allowNativeFullScreen said yes.
	*Parameters: none
	*Return: boolean
	************************************************************/
	private boolean requestNativeFullScreen()
	{
		try
		{
			Class<?> application = Class.forName("com.apple.eawt.Application");
			Object instance = application.getMethod("getApplication").invoke(null);
			application.getMethod("requestToggleFullScreen", java.awt.Window.class)
			           .invoke(instance, this);

			return true;
		}
		catch(Throwable t)
		{
			JGLog.writeLog("TELA CHEIA NATIVA RECUSADA: " + t + "\n");
			return false;
		}
	}

	/***********************************************************
	*Name: getRenderScale
	*Description: factor applied to the back buffer when it is blitted.
	*             Always 1 in windowed mode; in fullscreen it is the largest
	*             factor that still fits the screen without distortion.
	*Parameters: none
	*Return: double
	************************************************************/
	double getRenderScale()
	{
		if (!fullScreen || width <= 0 || height <= 0)
		{
			return 1.0;
		}

		return Math.min((double)getWidth() / width, (double)getHeight() / height);
	}

	/***********************************************************
	*Name: getContentOrigin
	*Description: returns the top left corner of the drawing area, in window
	*             coordinates. In fullscreen it centers the scaled image,
	*             leaving black bars when the aspect ratios differ.
	*Parameters: none
	*Return: Point
	************************************************************/
	Point getContentOrigin()
	{
		if (fullScreen)
		{
			double scale = getRenderScale();
			return new Point((int)((getWidth() - width * scale) / 2),
					         (int)((getHeight() - height * scale) / 2));
		}

		Insets insets = getInsets();
		return new Point(insets.left, insets.top);
	}
	
	/***********************************************************
	*Name: paint()
	*Description: método da classe JFrame que repinta a janela
	*Parametros: Graphics2D
	*
	*
	*Retorno: Nenhum
	************************************************************/
	public void paint(Graphics graphics)
	{
		Point origin = getContentOrigin();
		Graphics2D g2d = (Graphics2D)graphics;
		applyRenderingHints(g2d);

		double scale = getRenderScale();
		int destWidth = (int)(width * scale);
		int destHeight = (int)(height * scale);

		//Tarjas pretas quando a proporcao da tela difere da do jogo
		if (fullScreen)
		{
			g2d.setColor(Color.black);
			if (origin.y > 0)
			{
				g2d.fillRect(0, 0, getWidth(), origin.y);
				g2d.fillRect(0, origin.y + destHeight, getWidth(), getHeight() - origin.y - destHeight);
			}
			if (origin.x > 0)
			{
				g2d.fillRect(0, 0, origin.x, getHeight());
				g2d.fillRect(origin.x + destWidth, 0, getWidth() - origin.x - destWidth, getHeight());
			}
		}

		//Le o quadro publicado, nunca o que esta sendo desenhado agora
		synchronized (bufferLock)
		{
			BufferedImage frame = buffers[displayIndex];

			if (frame == null)
			{
				return;
			}

			if (!fullScreen)
			{
				g2d.drawImage(frame,origin.x,origin.y,null);
				return;
			}

			g2d.drawImage(frame, origin.x, origin.y, origin.x + destWidth, origin.y + destHeight,
					             0, 0, width, height, null);
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
		//A tela cheia nao usa mais o modo exclusivo, entao nao ha modo de
		//video a restaurar: basta esconder a janela
		graphDevice = null;

		setAlwaysOnTop(false);
		setVisible(false);

		if (gameManager != null)
		{
			gameManager.graphics = null;
		}

		//A EDT pode estar dentro de paint() lendo o buffer exibido: so libera os
		//dois depois de obter o mesmo cadeado que ela usa
		synchronized (bufferLock)
		{
			for (int index = 0; index < 2; index++)
			{
				if (graphics[index] != null)
				{
					graphics[index].dispose();
					graphics[index] = null;
				}

				if (buffers[index] != null)
				{
					buffers[index].flush();
					buffers[index] = null;
				}
			}
		}

		if (cursorTimer != null)
		{
			cursorTimer.stop();
			cursorTimer = null;
		}

		displayReady = false;
		waitingForSystem = false;
		windowTitle = null;
		cursor = null;
		gameManager = null;
		backgroundColor = null;
		dispose();
	}
}
