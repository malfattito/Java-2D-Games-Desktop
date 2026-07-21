/***********************************************************************
*Name: JGSprite
*Description: represents a visual object
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

public class JGSprite 
{
	//Class attributes
	private BufferedImage[] vetQuads = null;
	private AffineTransform transform = null;
	private int currentAnimation;
	private boolean mirror = false;
	private int countLines = 0;
	private int countColunms = 0;
	public JGVector2D speed = null;
	public JGVector2D position = null;
	public JGVector2D direction = null;
	public JGVector2D zoom = null;
	public JGVector2D lastPosition = null;
	public JGVector2D initMove = null;
	public JGVector2D endMove = null;
	public JGImage spriteImage = null;
	public ArrayList<JGAnimation> vetAnim = null;
	public JGEngine gameManager = null;
	public JGTimer moveTimer = null;
	public boolean visible;
	public boolean autoRender;
	public int imageWidth = 0;
	public int imageHeight = 0;
	public int frameWidth = 0;
	public int frameHeight = 0;
	public float fAngle = 0.0f;
	
	/***********************************************************
	*Name: JGSprite
	*Description: constructor
	*Parameters: None
	*Return: None
	************************************************************/
	public JGSprite(JGEngine manager, int lin, int col)
	{
		countLines = lin;
		countColunms = col;
		
		speed = new JGVector2D();
		direction = new JGVector2D();
		position = new JGVector2D();
		zoom = new JGVector2D(1,1);
		lastPosition = new JGVector2D();
		initMove = new JGVector2D();
		endMove = new JGVector2D();
		
		vetAnim = new ArrayList<JGAnimation>();
		currentAnimation=-1;
		visible = true;
		autoRender = false;
		gameManager = manager;
		moveTimer = new JGTimer(0);
		
		transform = new AffineTransform();
	}
	
	/***********************************************************
	*Name: getRectangle
	*Description: returns the rectangle with the area used by Sprite
	*Parameters: None
	*Return: None
	************************************************************/
	public Rectangle getRectangle()
	{
		return new Rectangle((int)(position.getX() - (frameWidth / 2.0) * zoom.getX()),
				             (int)(position.getY() - (frameHeight / 2.0) * zoom.getY()),
				             (int)(frameWidth * zoom.getX()), (int)(frameHeight * zoom.getY()));
	}
	
	/***********************************************************
	*Name: isMoveEnded
	*Description: returns if the move has ended
	*Parameters: none
	*Return: boolean
	************************************************************/
	public boolean isMoveEnded()
	{
		return (moveTimer.getEndTime() <= 0);
	}
	
	/***********************************************************
	*Name: moveTo
	*Description: inits a sprite move by a time interval
	*Parameters: int, JGVector2D
	*Return: none
	************************************************************/
	public void moveTo(int timer, JGVector2D newPosition)
	{
		initMove.setXY(position.getX(), position.getY());

		//Copia os valores: guardar a referencia faria o sprite compartilhar
		//o vetor de quem chamou o metodo
		endMove.setXY(newPosition.getX(), newPosition.getY());

		boolean samePosition = (initMove.getX() == endMove.getX()) &&
				               (initMove.getY() == endMove.getY());

		if (timer <= 0 || samePosition)
		{
			position.setXY(endMove.getX(), endMove.getY());
			moveTimer.restart(0);
		}
		else
		{
			moveTimer.restart(timer);
		}
	}
	
	/***********************************************************
	*Name: updateMove
	*Description: update the move position by the time
	*Parameters: none
	*Return: boolean
	************************************************************/
	void updateMove()
	{
		moveTimer.update();

		if (moveTimer.getEndTime() == 0)
		{
			return;
		}

		if (moveTimer.isTimeEnded())
		{
			position.setXY(endMove.getX(), endMove.getY());
			moveTimer.restart(0);
			return;
		}

		//Interpolacao linear entre a posicao inicial e a final
		double progress = (double)moveTimer.getCurrentTime() / (double)moveTimer.getEndTime();

		position.setXY(initMove.getX() + (endMove.getX() - initMove.getX()) * progress,
				       initMove.getY() + (endMove.getY() - initMove.getY()) * progress);
	}
	
	/***********************************************************
	*Name: setSpriteImage
	*Description: set the sprite reference image
	*Parameters: URL
	*Return: none
	************************************************************/
	public void setSpriteImage(URL imageName)
	{
		spriteImage = JGImageManager.loadImage(imageName);

		if (spriteImage == null)
		{
			throw new IllegalArgumentException("JGSprite: nao foi possivel carregar a imagem " + imageName);
		}

		if (countLines <= 0 || countColunms <= 0)
		{
			throw new IllegalArgumentException("JGSprite: numero de linhas e colunas deve ser maior que zero");
		}

		imageWidth = spriteImage.getImageWidth();
		imageHeight = spriteImage.getImageHeight();
		frameWidth = imageWidth / countColunms;
		frameHeight = imageHeight / countLines;

		//Mantem a transparencia da imagem original: usar BITMASK aqui
		//destruiria as bordas suavizadas dos sprites
		int transparency = spriteImage.getTransparency();

		vetQuads = new BufferedImage[countLines * countColunms];
		for (int iIndex=0; iIndex < vetQuads.length; iIndex++)
		{
			vetQuads[iIndex] = JGImage.createCompatibleImage(frameWidth, frameHeight, transparency);
			Graphics2D graphics = vetQuads[iIndex].createGraphics();
			drawFrame(iIndex, 0, 0, graphics);
			graphics.dispose();
		}
	}
	
	/***********************************************************
	*Name: setMirror
	*Description: sets the mirror mode
	*Parameters:boolean 
	*Return: none
	************************************************************/
	public void setMirror(boolean mirror)
	{
		this.mirror = mirror;
	}
	
	/***********************************************************
	*Name: setCurrentAnimation
	*Description: sets the current animation sprite
	*Parameters: int
	*Return: none
	************************************************************/
	public void setCurrentAnimation(int animationIndex)
	{
		if (animationIndex < 0 || animationIndex > vetAnim.size() - 1)
		{
			return;
		}
		
		if(animationIndex != currentAnimation)
		{
			currentAnimation = animationIndex;
			restartAnimation();
		}
	}
	
	/***********************************************************
	*Name: getCurrentAnimationFrame
	*Description: returns the current frame animation index
	*Parameters: none
	*Return: int
	************************************************************/
	public int getCurrentAnimationFrame()
	{
		JGAnimation animation = getCurrentAnimation();

		return (animation != null) ? animation.getCurrentFrameIndex() : -1;
	}
	
	/***********************************************************
	*Name: GetCurrentAnimationIndex
	*Description: returns the index of current animation
	*Parameters: none
	*Return: int
	************************************************************/
	public int getCurrentAnimationIndex()
	{
		return currentAnimation;
	}
	
	/***********************************************************
	*Name: getCurrentAnimation
	*Description: returns the current animation object
	*Parameters: none
	*Return: JGAnimation
	************************************************************/
	public JGAnimation getCurrentAnimation()
	{
		if (currentAnimation < 0 || currentAnimation >= vetAnim.size())
		{
			return null;
		}

		return vetAnim.get(currentAnimation);
	}
	
	/***********************************************************
	*Name: getMirror
	*Description: returns the mirror mode
	*Parameters: none
	*Return: boolean
	************************************************************/
	public boolean getMirror()
	{
		return mirror;
	}
	
	/***********************************************************
	*Name: restartAnimation
	*Description: restart the current animation
	*Parameters: none
	*Return: none
	************************************************************/
	public void restartAnimation()
	{
		if(currentAnimation >= 0 && currentAnimation < vetAnim.size())
		{
			vetAnim.get(currentAnimation).restart();
		}
	}
	
	/***********************************************************
	*Name: updateSprite
	*Description: updates the sprite states
	*Parameters: none
	*Return: none
	************************************************************/
	public void updateSprite()
	{
		updateMove();
		
		if((currentAnimation >= 0) && (currentAnimation < vetAnim.size()))
		{
			vetAnim.get(currentAnimation).update();
		}
	}
	
	/***********************************************************
	*Name: addAnimation
	*Description: creates and add a new animation to Sprite
	*Parameters: none
	*Return: boolean
	************************************************************/
	public void addAnimation(int iFPS, boolean repeat, int...frames)
	{
		JGAnimation animation= new JGAnimation(frames);

		animation.setLoop(repeat);
		animation.setFPS(iFPS);

		vetAnim.add(animation);
		
		if (vetAnim.size() == 1)
		{
			currentAnimation = 0;
		}
	}
	
	
	/***********************************************************
	*Name: addAnimation
	*Description: creates and add a new animation to Sprite
	*Parameters: none
	*Return: boolean
	************************************************************/
	public void addAnimation(int iFPS, boolean repeat, int initFrame, int endFrame)
	{
		
		//Try the correct sequency
		if (endFrame <= initFrame)
			return;
		
		int[] frames = new int[1 + endFrame - initFrame];
		
		for (int iIndex = 0; iIndex < frames.length; iIndex++)
		{
			frames[iIndex] = initFrame + iIndex;
		}
			
		JGAnimation animation= new JGAnimation(frames);
		animation.setLoop(repeat);
		animation.setFPS(iFPS);

		vetAnim.add(animation);
		
		if (vetAnim.size() == 1)
		{
			currentAnimation = 0;
		}
	}
	
	/***********************************************************
	*Name:render
	*Description: render the sprite into the window
	*Parameters: none
	*Return: none
	************************************************************/
	public void render()
	{
		render(position);
	}

	/***********************************************************
	*Name:render
	*Description: render the sprite into the window using a especific position
	*Parameters: JGVector2D
	*Return: none
	************************************************************/
	public void render(JGVector2D position)
	{
		if (!visible || vetQuads == null)
		{
			return;
		}

		//Nao adianta enviar para o Java2D o que cai fora da tela: o recorte
		//custa mais que o teste
		if (isOffScreen(position))
		{
			return;
		}

		transform.setToIdentity();
		transform.translate(position.getX(), position.getY());
		transform.rotate(fAngle);
		//O espelhamento e aplicado aqui, e nao no recorte dos quadros,
		//para que setMirror() funcione a qualquer momento
		transform.scale(mirror ? -zoom.getX() : zoom.getX(), zoom.getY());
		transform.translate(-frameWidth / 2.0, -frameHeight / 2.0);

		gameManager.graphics.drawImage(vetQuads[getRenderFrameIndex()], transform, null);
	}

	/***********************************************************
	*Name:isOffScreen
	*Description: tells if the sprite falls completely outside the drawing
	*             area, and therefore does not need to be drawn
	*Parameters: JGVector2D
	*Return: boolean
	************************************************************/
	private boolean isOffScreen(JGVector2D position)
	{
		if (gameManager == null || gameManager.windowManager == null)
		{
			return false;
		}

		double halfWidth = Math.abs(frameWidth * zoom.getX()) / 2.0;
		double halfHeight = Math.abs(frameHeight * zoom.getY()) / 2.0;

		//Girado, o sprite ocupa ate a propria diagonal em cada eixo
		if (fAngle != 0.0f)
		{
			double radius = Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);
			halfWidth = radius;
			halfHeight = radius;
		}

		return (position.getX() + halfWidth < 0) ||
			   (position.getY() + halfHeight < 0) ||
			   (position.getX() - halfWidth > gameManager.windowManager.getResolutionWidth()) ||
			   (position.getY() - halfHeight > gameManager.windowManager.getResolutionHeight());
	}

	/***********************************************************
	*Name:getRenderFrameIndex
	*Description: returns the frame index that must be drawn
	*Parameters: none
	*Return: int
	************************************************************/
	private int getRenderFrameIndex()
	{
		if (vetAnim.size() == 0 || currentAnimation < 0)
		{
			return 0;
		}

		int frameIndex = vetAnim.get(currentAnimation).getCurrentFrameIndex();

		return (frameIndex >= 0 && frameIndex < vetQuads.length) ? frameIndex : 0;
	}
	
	/***********************************************************
	*Name:drawFrame
	*Description: draw a frame selected in the image
	*Parameters: none
	*Return: none
	************************************************************/
	private void drawFrame(int frameIndex, int x, int y, Graphics2D graphics)
	{
		int sizeX = frameWidth;
		int sizeY = frameHeight;

		int fx = (frameIndex % countColunms) * sizeX;
		int fy = (frameIndex / countColunms) * sizeY;

		graphics.drawImage(spriteImage.getImage() ,x, y, (x + sizeX), (y + sizeY) ,fx, fy, (fx + sizeX), (fy + sizeY), null);
	}
	
	/***********************************************************
	*Name:collide
	*Description: try if exists collision between sprite areas
	*Parameters: none
	*Return: none
	************************************************************/
	public boolean collide(JGSprite sprite)
	{
		if (sprite == null)
		{
			return false;
		}

		//Testa a sobreposicao direto nas coordenadas. Passar por getRectangle()
		//alocaria dois Rectangle a cada par testado, e um jogo compara todos
		//os tiros contra todos os inimigos a cada quadro.
		double halfWidth = Math.abs(frameWidth * zoom.getX()) / 2.0;
		double halfHeight = Math.abs(frameHeight * zoom.getY()) / 2.0;
		double otherHalfWidth = Math.abs(sprite.frameWidth * sprite.zoom.getX()) / 2.0;
		double otherHalfHeight = Math.abs(sprite.frameHeight * sprite.zoom.getY()) / 2.0;

		return Math.abs(position.getX() - sprite.position.getX()) < (halfWidth + otherHalfWidth) &&
			   Math.abs(position.getY() - sprite.position.getY()) < (halfHeight + otherHalfHeight);
	}
	
	/***********************************************************
	*Name:free
	*Description: free sprite resources
	*Parameters: none
	*Return: none
	************************************************************/
	public void free()
	{
		speed = null;
		position = null;
		direction = null;
		zoom = null;
		lastPosition = null;
		initMove = null;
		endMove = null;
		gameManager = null;
		transform = null;

		JGImageManager.free(spriteImage);
		spriteImage = null;

		if (vetAnim != null)
		{
			for (JGAnimation anim : vetAnim)
			{
				anim.free();
			}
			vetAnim.clear();
			vetAnim = null;
		}

		if (vetQuads != null)
		{
			for(int index = 0; index < vetQuads.length; index++)
			{
				if (vetQuads[index] != null)
				{
					vetQuads[index].flush();
					vetQuads[index] = null;
				}
			}
			vetQuads = null;
		}

		moveTimer = null;
	}
}
