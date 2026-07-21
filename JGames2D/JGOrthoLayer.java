/***********************************************************************
*Name: JGOrthoLayer
*Description: tile layer in orthogonal projection: the blocks form a
*             rectangular grid aligned with the screen
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

public class JGOrthoLayer extends JGLayer
{
	/***********************************************************
	*Name: JGOrthoLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D
	*Return: none
	************************************************************/
	public JGOrthoLayer(JGEngine manager, JGVector2D blockSize)
	{
		super(manager, blockSize);
	}

	/***********************************************************
	*Name: JGOrthoLayer
	*Description: constructor
	*Parameters: JGEngine, JGVector2D, JGVector2D
	*Return: none
	************************************************************/
	public JGOrthoLayer(JGEngine manager, JGVector2D layerSize, JGVector2D blockSize)
	{
		super(manager, layerSize, blockSize);
	}

	/***********************************************************
	*Name: cellToScreen
	*Description: top left corner of a block on the screen
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
	*Description: block under a screen position
	*Parameters: double, double
	*Return: JGVector2D
	************************************************************/
	public JGVector2D screenToCell(double screenX, double screenY)
	{
		return new JGVector2D(Math.floor((screenX - offset.getX()) / blockSize.getX()),
				              Math.floor((screenY - offset.getY()) / blockSize.getY()));
	}

	/***********************************************************
	*Name: render
	*Description: draws the blocks that fall inside the screen, repeating the
	*             map in both directions
	*Parameters: none
	*Return: void
	************************************************************/
	public void render()
	{
		if (!isReadyToRender())
		{
			return;
		}

		int xBlock = 0;
		int yBlock = 0;
		double xPosition = 0.0f;
		double offsetX = offset.getX();
		double offsetY = offset.getY();
		double layerSizeX = layerSize.getX();
		double layerSizeY = layerSize.getY();

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
}
