//Pacotes utilizados
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import JGames2D.JGColorIndex;
import JGames2D.JGImage;
import JGames2D.JGImageManager;
import JGames2D.JGLevel;
import JGames2D.JGTopDownLayer;
import JGames2D.JGVector2D;

public class CenaCidadeIlhas extends JGLevel
{
	//LADO DO BLOCO E TAMANHO DO MAPA, EM BLOCOS
	private static final int LADO_BLOCO = 64;
	private static final int COLUNAS = 160;
	private static final int LINHAS = 224;

	//INDICE DO TILE USADO NAS PAREDES
	private static final int TILE_PAREDE = 8;

	//VELOCIDADES DE PASSEIO E DE CORRIDA
	private static final double VELOCIDADE = 8.0;
	private static final double VELOCIDADE_RAPIDA = 26.0;

	private JGTopDownLayer cidade = null;
	private JGImage minimapa = null;
	private int nivelDeRetorno = -1;

	public CenaCidadeIlhas()
	{
		this(-1);
	}

	public CenaCidadeIlhas(int nivelDeRetorno)
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
		//FORA DA CIDADE SO EXISTE MAR, ENTAO O FUNDO ACOMPANHA A AGUA
		gameManager.windowManager.setBackgroundColor(new Color(30, 66, 84));
		gameManager.graphics.setFont(new Font("verdana", Font.BOLD, 13));

		//CORES DO MAPA DO CHAO
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
		cores[11] = new JGColorIndex(11, new Color(0x8B4513));  //AREIA

		cidade = createTopDownLayer(getURL("/Images/gta_city_tiles.png"),
		                            getURL("/Images/city_ground.png"),
		                            cores,
		                            new JGVector2D(LADO_BLOCO, LADO_BLOCO),
		                            true);

		//NO MAPA DE ALTURAS O INDICE DA COR E O NUMERO DE ANDARES
		JGColorIndex[] alturas = new JGColorIndex[6];
		for (int indice = 0; indice < alturas.length; indice++)
		{
			int tom = indice * 0x111111;
			alturas[indice] = new JGColorIndex(indice, new Color(tom));
		}

		cidade.createHeightMap(getURL("/Images/city_height.png"), alturas);
		cidade.setWallFrameIndex(TILE_PAREDE);
		cidade.setPerspective(0.11);

		//COMECA SOBRE O CENTRO DA ILHA OESTE
		centralizaEm(40, 120);

		//O MINIMAPA E A PREVIA COLORIDA DO MAPA INTEIRO
		minimapa = JGImageManager.loadImage(getURL("/Images/city_preview.png"));
	}

	//COLOCA UMA CELULA DO MAPA NO CENTRO DA TELA
	private void centralizaEm(int coluna, int linha)
	{
		cidade.offset.setXY(gameManager.windowManager.getResolutionWidth() / 2.0 - coluna * LADO_BLOCO,
		                    gameManager.windowManager.getResolutionHeight() / 2.0 - linha * LADO_BLOCO);
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

		//SHIFT ACELERA, PORQUE A CIDADE E GRANDE
		double velocidade = gameManager.inputManager.keyPressed(KeyEvent.VK_SHIFT)
		                    ? VELOCIDADE_RAPIDA : VELOCIDADE;
		double deslocaX = 0;
		double deslocaY = 0;

		if (gameManager.inputManager.keyPressed(KeyEvent.VK_LEFT))  deslocaX += velocidade;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_RIGHT)) deslocaX -= velocidade;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_UP))    deslocaY += velocidade;
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_DOWN))  deslocaY -= velocidade;

		cidade.offset.setXY(cidade.offset.getX() + deslocaX,
		                    cidade.offset.getY() + deslocaY);

		//A E Z ABREM E FECHAM AS PAREDES
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_A))
		{
			cidade.setPerspective(Math.min(0.30, cidade.getPerspective() + 0.004));
		}
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_Z))
		{
			cidade.setPerspective(Math.max(0.0, cidade.getPerspective() - 0.004));
		}

		//AS TECLAS 1 A 4 SALTAM PARA CADA ILHA
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_1)) centralizaEm(54, 34);
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_2)) centralizaEm(40, 120);
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_3)) centralizaEm(118, 134);
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_4)) centralizaEm(131, 208);
	}

	@Override
	public void render()
	{
		//DESENHA A CIDADE
		super.render();

		int centroX = gameManager.windowManager.getResolutionWidth() / 2;
		int centroY = gameManager.windowManager.getResolutionHeight() / 2;
		JGVector2D bloco = cidade.screenToCell(centroX, centroY);

		desenhaMinimapa(bloco);

		//MIRA NO PONTO PARA ONDE A CAMERA OLHA
		gameManager.graphics.setColor(new Color(255, 255, 255, 130));
		gameManager.graphics.drawLine(centroX - 10, centroY, centroX + 10, centroY);
		gameManager.graphics.drawLine(centroX, centroY - 10, centroX, centroY + 10);

		gameManager.graphics.setColor(Color.white);
		gameManager.graphics.drawString("SETAS: ANDAR   SHIFT: CORRER   1-4: ILHAS   A/Z: PERSPECTIVA   ESC: " +
		                                (nivelDeRetorno >= 0 ? "MENU" : "SAIR"), 16, 24);
		gameManager.graphics.drawString("BLOCO " + (int)bloco.getX() + ", " + (int)bloco.getY() +
		                                "   ANDARES: " + cidade.getHeightAt(centroX, centroY) +
		                                (cidade.isWallAt(centroX, centroY) ? "   (PREDIO)" : "   (LIVRE)"), 16, 44);
	}

	//DESENHA A PREVIA DO MAPA NO CANTO, COM A JANELA DA CAMERA
	private void desenhaMinimapa(JGVector2D blocoCentral)
	{
		if (minimapa == null || minimapa.getImage() == null)
		{
			return;
		}

		BufferedImage imagem = minimapa.getImage();
		int largura = 128;
		int altura = largura * imagem.getHeight() / imagem.getWidth();
		int x = gameManager.windowManager.getResolutionWidth() - largura - 16;
		int y = 16;

		gameManager.graphics.setColor(new Color(0, 0, 0, 150));
		gameManager.graphics.fillRect(x - 3, y - 3, largura + 6, altura + 6);
		gameManager.graphics.drawImage(imagem, x, y, largura, altura, null);

		//RETANGULO DA AREA VISIVEL, EM BLOCOS
		double blocosNaTela = gameManager.windowManager.getResolutionWidth() / (double)LADO_BLOCO;
		double blocosNaAltura = gameManager.windowManager.getResolutionHeight() / (double)LADO_BLOCO;
		int janelaLargura = Math.max(3, (int)(blocosNaTela * largura / COLUNAS));
		int janelaAltura = Math.max(3, (int)(blocosNaAltura * altura / LINHAS));

		//O MAPA SE REPETE, ENTAO A POSICAO E TRAZIDA DE VOLTA PARA DENTRO DELE
		int coluna = ((int)blocoCentral.getX() % COLUNAS + COLUNAS) % COLUNAS;
		int linha = ((int)blocoCentral.getY() % LINHAS + LINHAS) % LINHAS;

		gameManager.graphics.setColor(Color.white);
		gameManager.graphics.drawRect(x + coluna * largura / COLUNAS - janelaLargura/2,
		                              y + linha * altura / LINHAS - janelaAltura/2,
		                              janelaLargura, janelaAltura);
	}
}
