/***********************************************************************
*Name: JGLayer
*Description: base of the tile layers. Holds the tileset, the block map and
*             the scrolling state; each subclass decides how the blocks are
*             placed on the screen.
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

public abstract class JGLayer
{
	//Class attributes
	public JGVector2D offset = null;
	public JGVector2D speed = null;
	protected JGEngine gameManager = null;
	protected JGFrameIndex[] vetBlocks = null;
	protected JGVector2D blockSize = null;
	protected JGVector2D layerSize = null;
	protected JGImage tileImage = null;
	protected JGColorIndex[] vetColorIndex = null;
	protected boolean visible = false;
	protected boolean autoRender = false;
	protected int tileColumns = 0;
	protected BufferedImage[] vetTiles = null;

	/***********************************************************
	*Name: JGLayer
	*Description: constructor used when the map comes from a color image,
	*             which is the one that defines the layer size
	*Parameters: JGEngine, JGVector2D
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
	*Description: constructor used when the map is filled by the game,
	*             block by block
	*Parameters: JGEngine, JGVector2D, JGVector2D
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
	*Name: render
	*Description: draws the visible part of the layer. Each subclass places
	*             the blocks according to its own projection.
	*Parameters: none
	*Return: void
	************************************************************/
	public abstract void render();

	/***********************************************************
	*Name: cellToScreen
	*Description: top left corner, on the screen, of the block at the given
	*             map coordinates
	*Parameters: int, int
	*Return: JGVector2D
	************************************************************/
	public abstract JGVector2D cellToScreen(int column, int line);

	/***********************************************************
	*Name: screenToCell
	*Description: map coordinates of the block under a screen position.
	*             Base of the collision between the game objects and the map.
	*Parameters: double, double
	*Return: JGVector2D
	************************************************************/
	public abstract JGVector2D screenToCell(double screenX, double screenY);

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
	*Description: returns the layer size, in blocks
	*Parameters: none
	*Return: JGVector2D
	************************************************************/
	public JGVector2D getLayerSize()
	{
		return layerSize;
	}

	/***********************************************************
	*Name: getBlockSize
	*Description: returns the block size, in pixels
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
	*Description: sets the frame index by a specific position
	*Parameters: int, int
	*Return: none
	************************************************************/
	public void setFrameIndex(int index, int frameIndex)
	{
		if (vetBlocks == null || index < 0 || index >= vetBlocks.length)
		{
			return;
		}

		if (vetBlocks[index] == null)
		{
			vetBlocks[index] = new JGFrameIndex();
		}

		vetBlocks[index].setFrameIndex(frameIndex);
	}

	/***********************************************************
	*Name: getFrameIndexByPosition
	*Description: returns the frame of a position in the block array
	*Parameters: int
	*Return: int
	************************************************************/
	public int getFrameIndexByPosition(int position)
	{
		if (vetBlocks == null || position < 0 || position >= vetBlocks.length ||
			vetBlocks[position] == null)
		{
			return -1;
		}

		return vetBlocks[position].getFrameIndex();
	}

	/***********************************************************
	*Name: getFrameIndexByCell
	*Description: frame of a block by map coordinates, repeating the map as
	*             many times as needed, exactly as the drawing does
	*Parameters: int, int
	*Return: int
	************************************************************/
	public int getFrameIndexByCell(int column, int line)
	{
		int columns = (int)layerSize.getX();
		int lines = (int)layerSize.getY();

		if (columns <= 0 || lines <= 0)
		{
			return -1;
		}

		return getFrameIndexByPosition(wrap(column, columns) + wrap(line, lines) * columns);
	}

	/***********************************************************
	*Name: getFrameIndexAt
	*Description: frame of the block under a screen position, or -1 when
	*             there is no block there. Used to test collisions with the map.
	*Parameters: double, double
	*Return: int
	************************************************************/
	public int getFrameIndexAt(double screenX, double screenY)
	{
		JGVector2D cell = screenToCell(screenX, screenY);

		return getFrameIndexByCell((int)cell.getX(), (int)cell.getY());
	}

	/***********************************************************
	*Name: isBlockAt
	*Description: tells if there is a block under a screen position
	*Parameters: double, double
	*Return: boolean
	************************************************************/
	public boolean isBlockAt(double screenX, double screenY)
	{
		return getFrameIndexAt(screenX, screenY) >= 0;
	}

	/***********************************************************
	*Name: wrap
	*Description: keeps an index inside the map, repeating it in both
	*             directions. Java gives a negative rest for negative values.
	*Parameters: int, int
	*Return: int
	************************************************************/
	protected static int wrap(int value, int size)
	{
		int rest = value % size;

		return (rest < 0) ? rest + size : rest;
	}

	/***********************************************************
	*Name: getAutoRender
	*Description: tells if the level draws this layer automatically
	*Parameters: none
	*Return: boolean
	************************************************************/
	public boolean getAutoRender()
	{
		return autoRender;
	}

	/***********************************************************
	*Name: setAutoRender
	*Description: defines if the level draws this layer automatically
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

		sliceTiles();
	}

	/***********************************************************
	*Name: sliceTiles
	*Description: cuts the tileset into one image per block, in the format
	*             the screen uses. Recortar na hora do desenho impedia o
	*             Java2D de usar a copia direta, e a layer redesenha
	*             centenas de blocos por quadro.
	*Parameters: none
	*Return: none
	************************************************************/
	private void sliceTiles()
	{
		int blockWidth = (int)blockSize.getX();
		int blockHeight = (int)blockSize.getY();
		int tileRows = tileImage.getImageHeight() / blockHeight;
		int transparency = tileImage.getTransparency();

		vetTiles = new BufferedImage[tileColumns * tileRows];

		for (int index = 0; index < vetTiles.length; index++)
		{
			int fx = (index % tileColumns) * blockWidth;
			int fy = (index / tileColumns) * blockHeight;

			vetTiles[index] = JGImage.createCompatibleImage(blockWidth, blockHeight, transparency);

			Graphics2D graphics = vetTiles[index].createGraphics();
			graphics.drawImage(tileImage.getImage(), 0, 0, blockWidth, blockHeight,
					                                 fx, fy, fx + blockWidth, fy + blockHeight, null);
			graphics.dispose();
		}
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
	*Description: draws one block of the tileset at a screen position
	*Parameters: int, int, int, Graphics2D
	*Return: void
	************************************************************/
	protected void drawBlock(int frameIndex, int x, int y, Graphics2D g2d)
	{
		if (frameIndex < 0 || frameIndex >= vetTiles.length)
		{
			return;
		}

		g2d.drawImage(vetTiles[frameIndex], x, y, null);
	}

	/***********************************************************
	*Name: isReadyToRender
	*Description: tells if the layer has everything it needs to be drawn
	*Parameters: none
	*Return: boolean
	************************************************************/
	protected boolean isReadyToRender()
	{
		if (!visible || vetBlocks == null || tileImage == null || vetTiles == null)
		{
			return false;
		}

		//Sem tamanho de bloco valido o desenho dividiria por zero
		return blockSize.getX() > 0 && blockSize.getY() > 0 &&
			   layerSize.getX() > 0 && layerSize.getY() > 0;
	}

	/***********************************************************
	*Name: createLayer
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

	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void free()
	{
		JGImageManager.free(tileImage);

		if (vetTiles != null)
		{
			for (int index = 0; index < vetTiles.length; index++)
			{
				if (vetTiles[index] != null)
				{
					vetTiles[index].flush();
					vetTiles[index] = null;
				}
			}
			vetTiles = null;
		}

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
