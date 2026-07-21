//Pacotes utilizados
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.net.URL;

import JGames2D.JGColorIndex;
import JGames2D.JGIsoLayer;
import JGames2D.JGLevel;
import JGames2D.JGVector2D;

public class CenaIsoCidade extends JGLevel
{
	//VELOCIDADE DA ROLAGEM, EM PIXELS POR QUADRO
	private static final double VELOCIDADE = 8.0;

	//TAMANHO DO LOSANGO DE CADA BLOCO
	private static final int LARGURA_BLOCO = 64;
	private static final int ALTURA_BLOCO = 32;

	private JGIsoLayer cidade = null;

	private URL getURL(String arquivo)
	{
		return getClass().getResource(arquivo);
	}

	@Override
	public void init()
	{
		//FUNDO ESCURO: APARECE SE A CENA FOR ROLADA PARA ALEM DO MAPA
		gameManager.windowManager.setBackgroundColor(new Color(24, 26, 30));
		gameManager.graphics.setFont(new Font("verdana", Font.BOLD, 14));

		//CADA COR DO MAPA DE INDICES CORRESPONDE A UM TILE DA FOLHA
		JGColorIndex[] cores = new JGColorIndex[8];
		cores[0] = new JGColorIndex(0, new Color(0x2E8B57));   //GRAMA
		cores[1] = new JGColorIndex(1, new Color(0x404040));   //ASFALTO
		cores[2] = new JGColorIndex(2, new Color(0xFFD700));   //VIA NO EIXO DAS COLUNAS
		cores[3] = new JGColorIndex(3, new Color(0xFF8C00));   //VIA NO EIXO DAS LINHAS
		cores[4] = new JGColorIndex(4, new Color(0xFF0000));   //CRUZAMENTO
		cores[5] = new JGColorIndex(5, new Color(0xC0C0C0));   //CALCADA
		cores[6] = new JGColorIndex(6, new Color(0x1E90FF));   //AGUA
		cores[7] = new JGColorIndex(7, new Color(0x8B4513));   //TERRA

		//CRIA A CAMADA ISOMETRICA A PARTIR DA FOLHA DE TILES E DO MAPA DE CORES
		cidade = createIsoLayer(getURL("/Images/iso_city_tiles.png"),
		                        getURL("/Images/iso_city_map.png"),
		                        cores,
		                        new JGVector2D(LARGURA_BLOCO, ALTURA_BLOCO),
		                        true);

		//COMECA COM O CENTRO DA TELA SOBRE UM CRUZAMENTO
		cidade.offset.setXY(gameManager.windowManager.getResolutionWidth() / 2,
		                    gameManager.windowManager.getResolutionHeight() / 2);
	}

	@Override
	public void execute()
	{
		//ESC ENCERRA
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_ESCAPE))
		{
			gameManager.finish();
			return;
		}

		//AS SETAS MOVEM A CAMERA: O MAPA ANDA NO SENTIDO CONTRARIO
		double deslocaX = 0;
		double deslocaY = 0;

		if (gameManager.inputManager.keyPressed(KeyEvent.VK_LEFT))  deslocaX += VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_RIGHT)) deslocaX -= VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_UP))    deslocaY += VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_DOWN))  deslocaY -= VELOCIDADE;

		cidade.offset.setXY(cidade.offset.getX() + deslocaX,
		                    cidade.offset.getY() + deslocaY);
	}

	@Override
	public void render()
	{
		//DESENHA A CAMADA
		super.render();

		//BLOCO SOB O CENTRO DA TELA: MOSTRA A CONVERSAO DE TELA PARA MAPA
		int centroX = gameManager.windowManager.getResolutionWidth() / 2;
		int centroY = gameManager.windowManager.getResolutionHeight() / 2;
		JGVector2D bloco = cidade.screenToCell(centroX, centroY);
		int frame = cidade.getFrameIndexAt(centroX, centroY);

		//MIRA NO CENTRO, PARA AJUDAR A CONFERIR A CELULA APONTADA
		gameManager.graphics.setColor(new Color(255, 255, 255, 160));
		gameManager.graphics.drawLine(centroX - 8, centroY, centroX + 8, centroY);
		gameManager.graphics.drawLine(centroX, centroY - 8, centroX, centroY + 8);

		gameManager.graphics.setColor(Color.white);
		gameManager.graphics.drawString("SETAS: ROLAR A CENA     ESC: SAIR", 20, 30);
		gameManager.graphics.drawString("CENTRO DA TELA -> COLUNA " + (int)bloco.getX() +
		                                "  LINHA " + (int)bloco.getY() +
		                                "  TILE " + frame + " (" + nomeDoTile(frame) + ")", 20, 52);
		gameManager.graphics.drawString("OFFSET " + (int)cidade.offset.getX() +
		                                ", " + (int)cidade.offset.getY(), 20, 74);
	}

	private String nomeDoTile(int frame)
	{
		switch (frame)
		{
			case 0: return "GRAMA";
			case 1: return "ASFALTO";
			case 2: return "VIA COLUNA";
			case 3: return "VIA LINHA";
			case 4: return "CRUZAMENTO";
			case 5: return "CALCADA";
			case 6: return "AGUA";
			case 7: return "TERRA";
			default: return "VAZIO";
		}
	}
}
