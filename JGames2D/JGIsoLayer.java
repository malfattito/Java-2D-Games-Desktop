/***********************************************************************
*Name: JGIsoLayer
*Description: tile layer in isometric projection. The blocks are diamonds
*             drawn inside rectangles of blockSize, arranged so that walking
*             one column goes down and to the right, and one line goes down
*             and to the left.
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

public class JGIsoLayer extends JGLayer
{
	/***********************************************************
	*Name: JGIsoLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D
	*Return: none
	************************************************************/
	public JGIsoLayer(JGEngine manager, JGVector2D blockSize)
	{
		super(manager, blockSize);
	}

	/***********************************************************
	*Name: JGIsoLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D, JGVector2D
	*Return: none
	************************************************************/
	public JGIsoLayer(JGEngine manager, JGVector2D layerSize, JGVector2D blockSize)
	{
		super(manager, layerSize, blockSize);
	}

	/***********************************************************
	*Name: cellToScreen
	*Description: top left corner, on the screen, of the rectangle that holds
	*             the diamond of a block
	*Parameters: int, int
	*Return: JGVector2D
	************************************************************/
	public JGVector2D cellToScreen(int column, int line)
	{
		double halfWidth = blockSize.getX() / 2.0;
		double halfHeight = blockSize.getY() / 2.0;

		return new JGVector2D(offset.getX() + (column - line) * halfWidth,
				              offset.getY() + (column + line) * halfHeight);
	}

	/***********************************************************
	*Name: screenToCell
	*Description: block whose diamond contains a screen position.
	*             In map coordinates the diamond becomes a square centred on
	*             the block, so rounding is enough to find it.
	*Parameters: double, double
	*Return: JGVector2D
	************************************************************/
	public JGVector2D screenToCell(double screenX, double screenY)
	{
		double halfWidth = blockSize.getX() / 2.0;
		double halfHeight = blockSize.getY() / 2.0;

		//Coordenadas relativas ao centro do bloco (0,0)
		double u = ((screenX - offset.getX()) - halfWidth) / halfWidth;
		double v = ((screenY - offset.getY()) - halfHeight) / halfHeight;

		return new JGVector2D(Math.round((u + v) / 2.0), Math.round((v - u) / 2.0));
	}

	/***********************************************************
	*Name: render
	*Description: draws the diamonds that fall inside the screen, repeating
	*             the map in both directions.
	*             The blocks are visited by growing column+line, which is the
	*             same as growing screen y: that is the painter order, so a
	*             block never covers one that should be in front of it.
	*Parameters: none
	*Return: void
	************************************************************/
	public void render()
	{
		if (!isReadyToRender())
		{
			return;
		}

		double halfWidth = blockSize.getX() / 2.0;
		double halfHeight = blockSize.getY() / 2.0;
		double blockWidth = blockSize.getX();
		double blockHeight = blockSize.getY();
		double offsetX = offset.getX();
		double offsetY = offset.getY();

		int columns = (int)layerSize.getX();
		int lines = (int)layerSize.getY();

		int screenWidth = gameManager.windowManager.getResolutionWidth();
		int screenHeight = gameManager.windowManager.getResolutionHeight();

		//A soma coluna+linha define o y da faixa: y = offsetY + soma * halfHeight.
		//Só interessam as faixas cujo losango toca a tela.
		int firstDepth = (int)Math.floor((-blockHeight - offsetY) / halfHeight);
		int lastDepth  = (int)Math.ceil((screenHeight - offsetY) / halfHeight);

		for (int depth = firstDepth; depth <= lastDepth; depth++)
		{
			//Dentro da faixa, x = offsetX + (2*coluna - soma) * halfWidth
			double firstColumn = (depth + (-blockWidth - offsetX) / halfWidth) / 2.0;
			double lastColumn  = (depth + (screenWidth - offsetX) / halfWidth) / 2.0;

			int y = (int)(offsetY + depth * halfHeight);

			for (int column = (int)Math.floor(firstColumn); column <= (int)Math.ceil(lastColumn); column++)
			{
				int line = depth - column;
				int blockIndex = wrap(column, columns) + wrap(line, lines) * columns;

				if (vetBlocks[blockIndex] == null)
				{
					continue;
				}

				int x = (int)(offsetX + (column - line) * halfWidth);

				drawBlock(vetBlocks[blockIndex].getFrameIndex(), x, y, gameManager.graphics);
			}
		}
	}
}
