/***********************************************************************
*Name: JGLayer
*Description: represents a tile layer built from a tileset
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/


//Package declaration
package JGames2D;

//Used Packages
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class JGLayer 
{
	//Class attributes
	public JGVector2D offset = null;
	public JGVector2D speed = null;
	private JGEngine gameManager = null;
	private JGFrameIndex[] vetBlocks = null;
	private JGVector2D blockSize = null;
	private JGVector2D layerSize = null;
	private JGImage tileImage = null;
	private JGColorIndex[] vetColorIndex = null;
	private boolean visible = false;
	private boolean autoRender = false;
	private int tileColumns = 0;
	
	/***********************************************************
	*Name: JGLayer
	*Description: constructor
	*Parameters: JGGameManager, JGVector2D
	*Return: none
	************************************************************/
	public JGLayer(JGEngine manager, JGVector2D blockSize)
	{
		vetBlocks = null;
		tileImage = null;
		vetColorIndex = null;
		this.blockSize = blockSize;
		gameManager = manager;
		offset = new JGVector2D();
		speed = new JGVector2D();
		layerSize = new JGVector2D();
		visible = true;
		autoRender = false;
	}
	
	/***********************************************************
	*Name: JGLayer
	*Description: constructor
	*Parameters: JGGameManager, JGVector2D, JGVector2D
	*Return: none
	************************************************************/
	public JGLayer(JGEngine manager, JGVector2D layerSize, JGVector2D blockSize)
	{
		gameManager = manager;
		this.layerSize = layerSize;
		vetColorIndex = null;
		tileImage = null;
		vetBlocks = new JGFrameIndex[(int)(layerSize.getX() * layerSize.getY())];
		for (int iIndex=0; iIndex<vetBlocks.length; iIndex++)
		{
			vetBlocks[iIndex] = new JGFrameIndex();
		}
		this.blockSize = blockSize;
		speed = new JGVector2D();
		offset = new JGVector2D();
		visible = true;
		autoRender = false;
	}
	
	/***********************************************************
	*Name: setColorIndex
	*Description: defines the color index to create the layer
	*Parameters: JGColorIndex[]
	*Return: none
	************************************************************/
	public void setColorIndex(JGColorIndex[] colorIndex)
	{
		vetColorIndex = colorIndex;
	}
	
	/***********************************************************
	*Name: getLayerSize
	*Description: returns the layer size
	*Parameters: none
	*Return: JGVector2D
	************************************************************/
	public JGVector2D getLayerSize()
	{
		return layerSize;
	}
	
	/***********************************************************
	*Name: getBlockSize
	*Description: returns the block size
	*Parameters: none
	*Return: JGVector2D
	************************************************************/
	public JGVector2D getBlockSize()
	{
		return blockSize;
	}
	
	/***********************************************************
	*Name: getOffset
	*Description: returns the offset of the layer
	*Parameters: none
	*Return: JGVector2D
	************************************************************/
	public JGVector2D getOffset()
	{
		return offset;
	}
	
	/***********************************************************
	*Name: setFrameIndex
	*Description:sets the frame index by a specific position
	*Parameters: int, int
	*Return: none
	************************************************************/
	public void setFrameIndex(int index, int frameIndex)
	{
		vetBlocks[index].setFrameIndex(frameIndex);
	}
	
	/***********************************************************
	*Name: getFrameIndexByPosition
	*Description:returns the frame to the position index
	*Parameters: int
	*Return: int
	************************************************************/
	public int getFrameIndexByPosition(int position)
	{
		return vetBlocks[position].getFrameIndex();
	}
	
	/***********************************************************
	*Name: getAutoRender
	*Description:returns the frame to the position index
	*Parameters: none
	*Return: boolean
	************************************************************/
	public boolean getAutoRender()
	{
		return autoRender;
	}
	
	/***********************************************************
	*Name: setAutoRender
	*Description:set the frame to the position index
	*Parameters: boolean
	*Return: none
	************************************************************/
	public void setAutoRender(boolean autoRender)
	{
		this.autoRender = autoRender;
	}
	
	/***********************************************************
	*Name: setVisible
	*Description: configures the visibility of the layer
	*Parameters: boolean
	*Return: void
	************************************************************/
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	
	/***********************************************************
	*Name: getVisible
	*Description: getter of visibility of the layer
	*Parameters: none
	*Return: boolean
	************************************************************/
	boolean getVisible()
	{
		return visible;
	}
	
	/***********************************************************
	*Name: setTileImage
	*Description: set the image used to the tiles
	*Parameters: URL
	*Return: void
	************************************************************/
	public void setTileImage(URL file)
	{
		tileImage = JGImageManager.loadImage(file);

		if (tileImage == null)
		{
			throw new IllegalArgumentException("JGLayer: nao foi possivel carregar a imagem de tiles " + file);
		}

		if (blockSize == null || blockSize.getX() <= 0 || blockSize.getY() <= 0)
		{
			throw new IllegalArgumentException("JGLayer: o tamanho do bloco deve ser maior que zero");
		}

		//O numero de colunas do tileset nao muda: calcula uma unica vez
		tileColumns = (int)(tileImage.getImageWidth() / blockSize.getX());
	}
	
	/***********************************************************
	*Name: setSpeed
	*Description: set the layer speed
	*Parameters: JGVector2D
	*Return: void
	************************************************************/
	public void setSpeed(JGVector2D speed)
	{
		this.speed = speed;
	}
	
	/***********************************************************
	*Name: scrollLayer
	*Description: scroll the layer with the current speed
	*Parameters: none
	*Return: void
	************************************************************/
	public void scrollLayer()
	{
		offset.setX(offset.getX() + speed.getX());
		offset.setY(offset.getY() + speed.getY());
	}
	
	/***********************************************************
	*Name: drawBlock
	*Description: int, int, int, Graphics2D
	*Parameters: none
	*Return: void
	************************************************************/
	private void drawBlock(int frameIndex, int x, int y, Graphics2D g2d)
	{
		int width = (int)blockSize.getX();
		int height = (int)blockSize.getY();

		int fx = (frameIndex % tileColumns) * width;
		int fy = (frameIndex / tileColumns) * height;

		g2d.drawImage(tileImage.getImage() ,x, y, x + width, y + height ,fx, fy, fx + width, fy + height, null);
	}
	
	/***********************************************************
	*Name: createLayer()
	*Description: create a layer based in a color image
	*Parameters: URL
	*Return: void
	************************************************************/
	public void createLayer(URL fileName)
	{
		//Loads the pixel image
		JGImage indexImage = JGImageManager.loadImage(fileName);

		//Testa se a imagem foi carregada
		if (indexImage == null || indexImage.getImage() == null)
		{
			throw new IllegalArgumentException("JGLayer: nao foi possivel carregar o mapa de cores " + fileName);
		}

		if (vetColorIndex == null)
		{
			throw new IllegalStateException("JGLayer: chame setColorIndex() antes de createLayer()");
		}

		BufferedImage pixelImage = indexImage.getImage();
		int imageWidth = pixelImage.getWidth();
		int imageHeight = pixelImage.getHeight();

		//set the layer size
		layerSize.setX(imageWidth);
		layerSize.setY(imageHeight);

		//Cria o vetor de bricks
		vetBlocks = new JGFrameIndex[imageWidth * imageHeight];

		//Indexa as cores por valor RGB: evita uma busca linear por pixel
		HashMap<Integer, Integer> colorTable = new HashMap<Integer, Integer>();
		for (int iIndex = 0; iIndex < vetColorIndex.length; iIndex++)
		{
			colorTable.put(vetColorIndex[iIndex].getColor().getRGB() & 0x00FFFFFF,
					       vetColorIndex[iIndex].getFrameIndex());
		}

		//Configura os bricks da layer conforme a cor da imagem
		for (int iIndex = 0;  iIndex < imageWidth; iIndex++)
		{
			for (int jIndex = 0; jIndex < imageHeight; jIndex++)
			{
				//Descarta o canal alfa: so o RGB identifica o bloco
				Integer index = colorTable.get(pixelImage.getRGB(iIndex, jIndex) & 0x00FFFFFF);

				if (index != null)
				{
					vetBlocks[iIndex + (jIndex * imageWidth)] = new JGFrameIndex();
					vetBlocks[iIndex + (jIndex * imageWidth)].setFrameIndex(index.intValue());
				}
				else
				{
					vetBlocks[iIndex + (jIndex * imageWidth)] = null;
				}
			}
		}

		//O mapa de cores so e necessario durante a construcao da layer
		JGImageManager.free(indexImage);
	}
	
	/***********************************************************
	*Name: render()
	*Description: create a layer based in a color image
	*Parameters: Graphics2D
	*Return: void
	************************************************************/
	public void  render()
	{
		int xBlock = 0;
		int yBlock = 0;
		double xPosition = 0.0f;
		double offsetX = offset.getX();
		double offsetY = offset.getY();
		double layerSizeX = layerSize.getX();
		double layerSizeY = layerSize.getY();

		//Retorna se a layer nao estiver visivel ou ainda nao tiver blocos
		if (!visible || vetBlocks == null || tileImage == null)
		{
			return;
		}

		//Sem tamanho de bloco valido o desenho dividiria por zero
		if (blockSize.getX() <= 0 || blockSize.getY() <= 0 || layerSizeX <= 0 || layerSizeY <= 0)
		{
			return;
		}

		//Calcula o início da layer em x caso offset seja menor que zero
		if (offsetX > 0)
		{	
			int mult = (int)Math.ceil(Math.abs(offsetX) / blockSize.getX());
			xBlock = ((int)layerSizeX - ((mult % (int)layerSizeX))) == (int)layerSizeX ? 0 : ((int)layerSizeX - ((mult % (int)layerSizeX))); 
			offsetX -= mult * blockSize.getX();
		}
		//Guarda o início do offset e o brick inicial em X
		xPosition = offsetX;
		
		//Calcula o início da layer em y caso offset seja menor que zero
		if (offsetY > 0)
		{
			int mult = (int)Math.ceil(Math.abs(offsetY) / blockSize.getY());
			yBlock = ((int)layerSizeY - ((mult % (int)layerSizeY))) == (int)layerSizeY ? 0 : ((int)layerSizeY - ((mult % (int)layerSizeY))); 
			offsetY -= mult * blockSize.getY();
		}
		
		//Desenha todos os bricks da layer.
		//getResolutionWidth/Height sao a area de desenho: getWidth/getHeight
		//da janela incluiriam as bordas e a barra de titulo.
		int screenWidth = gameManager.windowManager.getResolutionWidth();
		int screenHeight = gameManager.windowManager.getResolutionHeight();

		for(int iStartX = xBlock; offsetY < screenHeight;
				                  yBlock = (yBlock+1)%(int)layerSizeY,
				                  offsetY += blockSize.getY(),
				                  xBlock = iStartX,offsetX = xPosition)
		{
			for( ; offsetX < screenWidth; xBlock = (xBlock+1)%(int)layerSizeX, offsetX += blockSize.getX())
			{
				int blockIndex = xBlock + (yBlock * (int)layerSizeX);
				if (vetBlocks[blockIndex] != null)
				{
					drawBlock(vetBlocks[blockIndex].getFrameIndex(), (int) offsetX, (int) offsetY, gameManager.graphics);	
				}
			}
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
		JGImageManager.free(tileImage);
		gameManager = null;
		vetBlocks = null;
		blockSize = null;
		offset = null;
		layerSize = null;
		speed = null;
		tileImage = null;

		//A layer criada por tamanho nao possui indice de cores
		if (vetColorIndex != null)
		{
			for (int index = 0; index < vetColorIndex.length; index++)
			{
				vetColorIndex[index].free();
			}
			vetColorIndex = null;
		}
	}
}
