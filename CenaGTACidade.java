//Pacotes utilizados
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.net.URL;

import JGames2D.JGColorIndex;
import JGames2D.JGTopDownLayer;
import JGames2D.JGLevel;
import JGames2D.JGVector2D;

public class CenaGTACidade extends JGLevel
{
	//VELOCIDADE DA ROLAGEM, EM PIXELS POR QUADRO
	private static final double VELOCIDADE = 7.0;

	//LADO DO BLOCO, EM PIXELS
	private static final int LADO_BLOCO = 64;

	//INDICE DO TILE USADO NAS PAREDES
	private static final int TILE_PAREDE = 8;

	private JGTopDownLayer cidade = null;

	//NIVEL PARA ONDE O ESC VOLTA. NEGATIVO ENCERRA O JOGO.
	private int nivelDeRetorno = -1;

	/***********************************************************
	*CONSTRUTOR PADRAO: O ESC ENCERRA. USADO PELA DEMONSTRACAO ISOLADA.
	************************************************************/
	public CenaGTACidade()
	{
		this(-1);
	}

	/***********************************************************
	*CONSTRUTOR QUE RECEBE O NIVEL DE RETORNO DO ESC, PARA A CENA
	*PODER SER USADA DENTRO DO JOGO, VOLTANDO AO MENU.
	************************************************************/
	public CenaGTACidade(int nivelDeRetorno)
	{
		this.nivelDeRetorno = nivelDeRetorno;
	}

	private URL getURL(String arquivo)
	{
		return getClass().getResource(arquivo);
	}

	@Override
	public void init()
	{
		gameManager.windowManager.setBackgroundColor(new Color(20, 22, 26));
		gameManager.graphics.setFont(new Font("verdana", Font.BOLD, 14));

		//CORES DO MAPA DO CHAO: CADA UMA APONTA PARA UM TILE DA FOLHA
		JGColorIndex[] cores = new JGColorIndex[12];
		cores[0]  = new JGColorIndex(0,  new Color(0x404040));  //ASFALTO
		cores[1]  = new JGColorIndex(1,  new Color(0xFFD700));  //RUA VERTICAL
		cores[2]  = new JGColorIndex(2,  new Color(0xFF8C00));  //RUA HORIZONTAL
		cores[3]  = new JGColorIndex(3,  new Color(0xFF0000));  //CRUZAMENTO
		cores[4]  = new JGColorIndex(4,  new Color(0xC0C0C0));  //CALCADA
		cores[5]  = new JGColorIndex(5,  new Color(0x2E8B57));  //GRAMA
		cores[6]  = new JGColorIndex(6,  new Color(0x9E9E9E));  //TELHADO CLARO
		cores[7]  = new JGColorIndex(7,  new Color(0x5A5A5A));  //TELHADO ESCURO
		cores[8]  = new JGColorIndex(8,  new Color(0x7E786F));  //PAREDE
		cores[9]  = new JGColorIndex(9,  new Color(0x1E90FF));  //AGUA
		cores[10] = new JGColorIndex(10, new Color(0xB0B0B0));  //ESTACIONAMENTO
		cores[11] = new JGColorIndex(11, new Color(0x8B4513));  //TERRA

		//CRIA A CAMADA COM O MAPA DO CHAO
		cidade = createTopDownLayer(getURL("/Images/gta_city_tiles.png"),
		                        getURL("/Images/gta_city_map.png"),
		                        cores,
		                        new JGVector2D(LADO_BLOCO, LADO_BLOCO),
		                        true);

		//NO MAPA DE ALTURAS O INDICE DA COR E O NUMERO DE ANDARES
		JGColorIndex[] alturas = new JGColorIndex[6];
		alturas[0] = new JGColorIndex(0, new Color(0x000000));
		alturas[1] = new JGColorIndex(1, new Color(0x111111));
		alturas[2] = new JGColorIndex(2, new Color(0x222222));
		alturas[3] = new JGColorIndex(3, new Color(0x333333));
		alturas[4] = new JGColorIndex(4, new Color(0x444444));
		alturas[5] = new JGColorIndex(5, new Color(0x555555));

		cidade.createHeightMap(getURL("/Images/gta_city_height.png"), alturas);
		cidade.setWallFrameIndex(TILE_PAREDE);
		cidade.setPerspective(0.12);

		cidade.offset.setXY(-600, -600);
	}

	@Override
	public void execute()
	{
		//ESC VOLTA AO MENU, OU ENCERRA SE NAO HOUVER NIVEL DE RETORNO
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_ESCAPE))
		{
			if (nivelDeRetorno >= 0)
			{
				gameManager.setCurrentLevel(nivelDeRetorno);
			}
			else
			{
				gameManager.finish();
			}
			return;
		}

		//AS SETAS MOVEM A CAMERA PELA CIDADE
		double deslocaX = 0;
		double deslocaY = 0;

		if (gameManager.inputManager.keyPressed(KeyEvent.VK_LEFT))  deslocaX += VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_RIGHT)) deslocaX -= VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_UP))    deslocaY += VELOCIDADE;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_DOWN))  deslocaY -= VELOCIDADE;

		cidade.offset.setXY(cidade.offset.getX() + deslocaX,
		                    cidade.offset.getY() + deslocaY);

		//A E Z ABREM E FECHAM AS PAREDES, PARA VER O EFEITO DA PERSPECTIVA
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_A))
		{
			cidade.setPerspective(Math.min(0.30, cidade.getPerspective() + 0.004));
		}
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_Z))
		{
			cidade.setPerspective(Math.max(0.0, cidade.getPerspective() - 0.004));
		}
	}

	@Override
	public void render()
	{
		//DESENHA O CHAO E OS PREDIOS
		super.render();

		int centroX = gameManager.windowManager.getResolutionWidth() / 2;
		int centroY = gameManager.windowManager.getResolutionHeight() / 2;

		//A CAMERA OLHA PARA O CENTRO DA TELA: ALI SO APARECEM OS TELHADOS
		gameManager.graphics.setColor(new Color(255, 255, 255, 130));
		gameManager.graphics.drawLine(centroX - 10, centroY, centroX + 10, centroY);
		gameManager.graphics.drawLine(centroX, centroY - 10, centroX, centroY + 10);

		gameManager.graphics.setColor(Color.white);
		gameManager.graphics.drawString("SETAS: ANDAR PELA CIDADE     A / Z: PERSPECTIVA     ESC: " +
		                                (nivelDeRetorno >= 0 ? "MENU" : "SAIR"), 20, 30);
		gameManager.graphics.drawString("PERSPECTIVA " + String.format("%.3f", cidade.getPerspective()) +
		                                "     ANDARES SOB A MIRA: " + cidade.getHeightAt(centroX, centroY) +
		                                (cidade.isWallAt(centroX, centroY) ? "  (PREDIO)" : "  (LIVRE)"), 20, 52);
	}
}
