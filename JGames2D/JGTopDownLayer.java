/***********************************************************************
*Name: JGTopDownLayer
*Description: tile layer seen from above with perspective, in the style of
*             the first GTA. The floor is a grid aligned with the screen, and
*             each cell may carry a stack of blocks. The camera looks straight
*             down at one point of the screen: over that point only the roofs
*             show, and the further a building is from it the more its walls
*             open outwards.
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used Packages
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class JGTopDownLayer extends JGLayer
{
	//Class attributes
	private int[] vetHeights = null;
	private int maxHeight = 0;
	private int wallFrameIndex = -1;
	private double perspective = 0.12;
	private JGVector2D cameraCenter = null;
	private AffineTransform faceTransform = null;

	//Lista dos predios visiveis, reaproveitada a cada quadro para nao
	//alocar nada dentro do laco de desenho
	private int[] vetVisibleColumn = new int[256];
	private int[] vetVisibleLine = new int[256];
	private double[] vetVisibleKey = new double[256];
	private int visibleCount = 0;

	/***********************************************************
	*Name: JGTopDownLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D
	*Return: none
	************************************************************/
	public JGTopDownLayer(JGEngine manager, JGVector2D blockSize)
	{
		super(manager, blockSize);
		faceTransform = new AffineTransform();
	}

	/***********************************************************
	*Name: JGTopDownLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D, JGVector2D
	*Return: none
	************************************************************/
	public JGTopDownLayer(JGEngine manager, JGVector2D layerSize, JGVector2D blockSize)
	{
		super(manager, layerSize, blockSize);
		faceTransform = new AffineTransform();
	}

	/***********************************************************
	*Name: cellToScreen
	*Description: top left corner of a cell on the floor
	*Parameters: int, int
	*Return: JGVector2D
	************************************************************/
	public JGVector2D cellToScreen(int column, int line)
	{
		return new JGVector2D(offset.getX() + column * blockSize.getX(),
				              offset.getY() + line * blockSize.getY());
	}

	/***********************************************************
	*Name: screenToCell
	*Description: cell of the floor under a screen position
	*Parameters: double, double
	*Return: JGVector2D
	************************************************************/
	public JGVector2D screenToCell(double screenX, double screenY)
	{
		return new JGVector2D(Math.floor((screenX - offset.getX()) / blockSize.getX()),
				              Math.floor((screenY - offset.getY()) / blockSize.getY()));
	}

	/***********************************************************
	*Name: setPerspective
	*Description: how much the walls open outwards. Zero gives a flat top
	*             view with no walls at all; the usual range is 0.05 to 0.25.
	*Parameters: double
	*Return: none
	************************************************************/
	public void setPerspective(double perspective)
	{
		this.perspective = perspective;
	}

	/***********************************************************
	*Name: getPerspective
	*Description: current opening of the walls
	*Parameters: none
	*Return: double
	************************************************************/
	public double getPerspective()
	{
		return perspective;
	}

	/***********************************************************
	*Name: setCameraCenter
	*Description: point of the screen the camera looks straight down at.
	*             Pass null to keep it at the middle of the screen.
	*Parameters: JGVector2D
	*Return: none
	************************************************************/
	public void setCameraCenter(JGVector2D cameraCenter)
	{
		this.cameraCenter = cameraCenter;
	}

	/***********************************************************
	*Name: setWallFrameIndex
	*Description: tile used on the walls of every building. The roof keeps
	*             using the tile the cell has on the map.
	*Parameters: int
	*Return: none
	************************************************************/
	public void setWallFrameIndex(int wallFrameIndex)
	{
		this.wallFrameIndex = wallFrameIndex;
	}

	/***********************************************************
	*Name: createHeightMap
	*Description: reads the height of each cell from a color image, the same
	*             way createLayer() reads the floor. Here the index of the
	*             color is the number of blocks stacked on the cell.
	*Parameters: URL, JGColorIndex[]
	*Return: none
	************************************************************/
	public void createHeightMap(URL fileName, JGColorIndex[] heightColors)
	{
		JGImage heightImage = JGImageManager.loadImage(fileName);

		if (heightImage == null || heightImage.getImage() == null)
		{
			throw new IllegalArgumentException("JGTopDownLayer: nao foi possivel carregar o mapa de alturas " + fileName);
		}

		BufferedImage pixelImage = heightImage.getImage();
		int imageWidth = pixelImage.getWidth();
		int imageHeight = pixelImage.getHeight();

		if (imageWidth != (int)layerSize.getX() || imageHeight != (int)layerSize.getY())
		{
			throw new IllegalArgumentException("JGTopDownLayer: o mapa de alturas e " + imageWidth + "x" + imageHeight +
			                                   " e o mapa do chao e " + (int)layerSize.getX() + "x" + (int)layerSize.getY() +
			                                   ". Os dois precisam ter o mesmo tamanho.");
		}

		HashMap<Integer, Integer> colorTable = new HashMap<Integer, Integer>();
		for (int index = 0; index < heightColors.length; index++)
		{
			colorTable.put(heightColors[index].getColor().getRGB() & 0x00FFFFFF,
					       heightColors[index].getFrameIndex());
		}

		vetHeights = new int[imageWidth * imageHeight];

		for (int column = 0; column < imageWidth; column++)
		{
			for (int line = 0; line < imageHeight; line++)
			{
				Integer altura = colorTable.get(pixelImage.getRGB(column, line) & 0x00FFFFFF);
				vetHeights[column + line * imageWidth] = (altura == null) ? 0 : altura.intValue();
			}
		}

		updateMaxHeight();

		//O mapa so e necessario durante a construcao
		JGImageManager.free(heightImage);
	}

	/***********************************************************
	*Name: setHeightByCell
	*Description: sets how many blocks are stacked on a cell
	*Parameters: int, int, int
	*Return: none
	************************************************************/
	public void setHeightByCell(int column, int line, int height)
	{
		int columns = (int)layerSize.getX();
		int lines = (int)layerSize.getY();

		if (columns <= 0 || lines <= 0)
		{
			return;
		}

		if (vetHeights == null)
		{
			vetHeights = new int[columns * lines];
		}

		vetHeights[wrap(column, columns) + wrap(line, lines) * columns] = Math.max(0, height);
		maxHeight = Math.max(maxHeight, height);
	}

	/***********************************************************
	*Name: getHeightByCell
	*Description: how many blocks are stacked on a cell, repeating the map
	*Parameters: int, int
	*Return: int
	************************************************************/
	public int getHeightByCell(int column, int line)
	{
		int columns = (int)layerSize.getX();
		int lines = (int)layerSize.getY();

		if (vetHeights == null || columns <= 0 || lines <= 0)
		{
			return 0;
		}

		return vetHeights[wrap(column, columns) + wrap(line, lines) * columns];
	}

	/***********************************************************
	*Name: getHeightAt
	*Description: height of the cell under a screen position
	*Parameters: double, double
	*Return: int
	************************************************************/
	public int getHeightAt(double screenX, double screenY)
	{
		JGVector2D cell = screenToCell(screenX, screenY);

		return getHeightByCell((int)cell.getX(), (int)cell.getY());
	}

	/***********************************************************
	*Name: isWallAt
	*Description: tells if a screen position falls on a cell occupied by a
	*             building. This is the test a car or a person uses to know
	*             that it cannot go through.
	*Parameters: double, double
	*Return: boolean
	************************************************************/
	public boolean isWallAt(double screenX, double screenY)
	{
		return getHeightAt(screenX, screenY) > 0;
	}

	/***********************************************************
	*Name: getCameraX
	*Description: horizontal point the camera looks down at
	*Parameters: none
	*Return: double
	************************************************************/
	private double getCameraX()
	{
		return (cameraCenter != null) ? cameraCenter.getX()
				                      : gameManager.windowManager.getResolutionWidth() / 2.0;
	}

	/***********************************************************
	*Name: getCameraY
	*Description: vertical point the camera looks down at
	*Parameters: none
	*Return: double
	************************************************************/
	private double getCameraY()
	{
		return (cameraCenter != null) ? cameraCenter.getY()
				                      : gameManager.windowManager.getResolutionHeight() / 2.0;
	}

	/***********************************************************
	*Name: render
	*Description: draws the floor and then the buildings. The buildings are
	*             visited from the ones far from the camera to the ones near
	*             it, because a near building may hide a far one.
	*Parameters: none
	*Return: void
	************************************************************/
	public void render()
	{
		if (!isReadyToRender())
		{
			return;
		}

		double blockWidth = blockSize.getX();
		double blockHeight = blockSize.getY();
		int columns = (int)layerSize.getX();
		int lines = (int)layerSize.getY();
		int screenWidth = gameManager.windowManager.getResolutionWidth();
		int screenHeight = gameManager.windowManager.getResolutionHeight();
		Graphics2D graphics = gameManager.graphics;

		//Celulas que tocam a tela
		int firstColumn = (int)Math.floor((0 - offset.getX()) / blockWidth);
		int lastColumn  = (int)Math.ceil((screenWidth - offset.getX()) / blockWidth);
		int firstLine   = (int)Math.floor((0 - offset.getY()) / blockHeight);
		int lastLine    = (int)Math.ceil((screenHeight - offset.getY()) / blockHeight);

		//O CHAO
		for (int line = firstLine; line <= lastLine; line++)
		{
			for (int column = firstColumn; column <= lastColumn; column++)
			{
				int blockIndex = wrap(column, columns) + wrap(line, lines) * columns;

				if (vetBlocks[blockIndex] == null)
				{
					continue;
				}

				drawBlock(vetBlocks[blockIndex].getFrameIndex(),
				          (int)(offset.getX() + column * blockWidth),
				          (int)(offset.getY() + line * blockHeight), graphics);
			}
		}

		if (vetHeights == null)
		{
			return;
		}

		//OS PREDIOS
		//Uma construcao alta se projeta para fora, entao pode aparecer mesmo
		//com a base fora da tela: a margem cresce com a altura possivel.
		int margin = 1 + (int)Math.ceil(maxHeight * perspective *
		                 Math.max(screenWidth, screenHeight) / Math.min(blockWidth, blockHeight));

		double cameraX = getCameraX();
		double cameraY = getCameraY();

		collectBuildings(firstColumn - margin, lastColumn + margin,
		                 firstLine - margin, lastLine + margin,
		                 cameraX, cameraY, blockWidth, blockHeight);

		//Do mais distante da camera para o mais proximo: um predio proximo
		//pode cobrir um distante, nunca o contrario
		sortByKey(0, visibleCount - 1);

		for (int index = 0; index < visibleCount; index++)
		{
			drawBuilding(vetVisibleColumn[index], vetVisibleLine[index],
			             getHeightByCell(vetVisibleColumn[index], vetVisibleLine[index]),
			             cameraX, cameraY, graphics);
		}
	}

	/***********************************************************
	*Name: collectBuildings
	*Description: gathers the cells with buildings inside the visited area,
	*             keeping for each one the exact distance to the camera
	*Parameters: int, int, int, int, double, double, double, double
	*Return: void
	************************************************************/
	private void collectBuildings(int firstColumn, int lastColumn, int firstLine, int lastLine,
	                              double cameraX, double cameraY, double blockWidth, double blockHeight)
	{
		visibleCount = 0;

		for (int line = firstLine; line <= lastLine; line++)
		{
			for (int column = firstColumn; column <= lastColumn; column++)
			{
				if (getHeightByCell(column, line) <= 0)
				{
					continue;
				}

				if (visibleCount == vetVisibleColumn.length)
				{
					growVisibleArrays();
				}

				double centerX = offset.getX() + (column + 0.5) * blockWidth;
				double centerY = offset.getY() + (line + 0.5) * blockHeight;
				double deltaX = centerX - cameraX;
				double deltaY = centerY - cameraY;

				vetVisibleColumn[visibleCount] = column;
				vetVisibleLine[visibleCount] = line;
				//Distancia ao quadrado: ordena igual e evita a raiz quadrada.
				//Guardar o valor exato, sem arredondar para blocos inteiros, e
				//o que impede a ordem de saltar quando a camera anda.
				vetVisibleKey[visibleCount] = deltaX * deltaX + deltaY * deltaY;
				visibleCount++;
			}
		}
	}

	/***********************************************************
	*Name: growVisibleArrays
	*Description: doubles the room of the building list
	*Parameters: none
	*Return: void
	************************************************************/
	private void growVisibleArrays()
	{
		int novo = vetVisibleColumn.length * 2;
		int[] colunas = new int[novo];
		int[] linhas = new int[novo];
		double[] chaves = new double[novo];

		System.arraycopy(vetVisibleColumn, 0, colunas, 0, visibleCount);
		System.arraycopy(vetVisibleLine, 0, linhas, 0, visibleCount);
		System.arraycopy(vetVisibleKey, 0, chaves, 0, visibleCount);

		vetVisibleColumn = colunas;
		vetVisibleLine = linhas;
		vetVisibleKey = chaves;
	}

	/***********************************************************
	*Name: sortByKey
	*Description: orders the building list by distance, farthest first
	*Parameters: int, int
	*Return: void
	************************************************************/
	private void sortByKey(int first, int last)
	{
		if (first >= last)
		{
			return;
		}

		double pivot = vetVisibleKey[(first + last) / 2];
		int i = first;
		int j = last;

		while (i <= j)
		{
			while (vetVisibleKey[i] > pivot) i++;
			while (vetVisibleKey[j] < pivot) j--;

			if (i <= j)
			{
				swapVisible(i, j);
				i++;
				j--;
			}
		}

		sortByKey(first, j);
		sortByKey(i, last);
	}

	/***********************************************************
	*Name: swapVisible
	*Description: exchanges two entries of the building list
	*Parameters: int, int
	*Return: void
	************************************************************/
	private void swapVisible(int a, int b)
	{
		int coluna = vetVisibleColumn[a]; vetVisibleColumn[a] = vetVisibleColumn[b]; vetVisibleColumn[b] = coluna;
		int linha = vetVisibleLine[a];    vetVisibleLine[a] = vetVisibleLine[b];     vetVisibleLine[b] = linha;
		double chave = vetVisibleKey[a];  vetVisibleKey[a] = vetVisibleKey[b];       vetVisibleKey[b] = chave;
	}

	/***********************************************************
	*Name: updateMaxHeight
	*Description: recalculates the tallest stack of the map. The drawing needs
	*             this number every frame to know how far outside the screen a
	*             building may still show up, and varrer o mapa inteiro a cada
	*             quadro custaria caro num mapa grande.
	*Parameters: none
	*Return: void
	************************************************************/
	private void updateMaxHeight()
	{
		maxHeight = 0;

		for (int index = 0; index < vetHeights.length; index++)
		{
			maxHeight = Math.max(maxHeight, vetHeights[index]);
		}
	}

	/***********************************************************
	*Name: drawBuilding
	*Description: draws the walls that face the camera and then the roof
	*Parameters: int, int, int, double, double, Graphics2D
	*Return: void
	************************************************************/
	private void drawBuilding(int column, int line, int height,
	                          double cameraX, double cameraY, Graphics2D graphics)
	{
		double blockWidth = blockSize.getX();
		double blockHeight = blockSize.getY();
		double x = offset.getX() + column * blockWidth;
		double y = offset.getY() + line * blockHeight;

		//Deslocamento de um andar: cresce com a distancia ate a camera
		double stepX = ((x + blockWidth / 2.0) - cameraX) * perspective;
		double stepY = ((y + blockHeight / 2.0) - cameraY) * perspective;

		//A face encostada num vizinho da mesma altura e uma parede interna:
		//desenha-la faria um predio largo parecer varias caixas encostadas.
		//Por andar, porque um vizinho mais baixo so esconde os primeiros.
		int westHeight = getHeightByCell(column - 1, line);
		int eastHeight = getHeightByCell(column + 1, line);
		int northHeight = getHeightByCell(column, line - 1);
		int southHeight = getHeightByCell(column, line + 1);

		//As paredes visiveis sao as viradas para a camera
		for (int floor = 0; floor < height; floor++)
		{
			double baseX = x + stepX * floor;
			double baseY = y + stepY * floor;

			if (stepX > 0)
			{
				if (westHeight <= floor)
				{
					drawFace(baseX, baseY, 0, blockHeight, stepX, stepY, graphics);
				}
			}
			else if (stepX < 0)
			{
				if (eastHeight <= floor)
				{
					drawFace(baseX + blockWidth, baseY, 0, blockHeight, stepX, stepY, graphics);
				}
			}

			if (stepY > 0)
			{
				if (northHeight <= floor)
				{
					drawFace(baseX, baseY, blockWidth, 0, stepX, stepY, graphics);
				}
			}
			else if (stepY < 0)
			{
				if (southHeight <= floor)
				{
					drawFace(baseX, baseY + blockHeight, blockWidth, 0, stepX, stepY, graphics);
				}
			}
		}

		//O telhado usa o tile que a celula tem no mapa
		int roofIndex = getFrameIndexByCell(column, line);

		if (roofIndex >= 0)
		{
			drawBlock(roofIndex, (int)(x + stepX * height), (int)(y + stepY * height), graphics);
		}
	}

	/***********************************************************
	*Name: drawFace
	*Description: paints the wall tile over one face of a floor. The face is
	*             a parallelogram, so an AffineTransform maps the tile onto it
	*             without any deformation of the pixels being needed by hand.
	*Parameters: double, double, double, double, double, double, Graphics2D
	*Return: void
	************************************************************/
	private void drawFace(double originX, double originY, double edgeX, double edgeY,
	                      double riseX, double riseY, Graphics2D graphics)
	{
		if (wallFrameIndex < 0 || vetTiles == null ||
			wallFrameIndex >= vetTiles.length || vetTiles[wallFrameIndex] == null)
		{
			return;
		}

		BufferedImage wall = vetTiles[wallFrameIndex];

		//(s,t) da textura vai para origem + s*aresta + t*subida
		faceTransform.setTransform(edgeX / wall.getWidth(),  edgeY / wall.getWidth(),
		                           riseX / wall.getHeight(), riseY / wall.getHeight(),
		                           originX, originY);

		graphics.drawImage(wall, faceTransform, null);
	}

	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
	public void free()
	{
		super.free();
		vetHeights = null;
		cameraCenter = null;
		faceTransform = null;
	}
}
