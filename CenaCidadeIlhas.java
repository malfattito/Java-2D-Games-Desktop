//Pacotes utilizados
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Random;

import JGames2D.JGColorIndex;
import JGames2D.JGImage;
import JGames2D.JGImageManager;
import JGames2D.JGLevel;
import JGames2D.JGTopDownLayer;
import JGames2D.JGVector2D;

public class CenaCidadeIlhas extends JGLevel
{
	//LADO DO BLOCO E TAMANHO DOS MAPAS, EM BLOCOS
	private static final int LADO_BLOCO = 64;
	private static final int COLUNAS = 160;
	private static final int LINHAS = 224;

	//INDICES DOS TILES, NA MESMA ORDEM DO GERADOR Tools/GenCityPack.java
	private static final int RUA_V = 1, RUA_H = 2, CRUZAMENTO = 3;
	private static final int TELHADO_A = 10, TELHADO_B = 11, TELHADO_C = 12, TELHADO_CASA = 13;
	private static final int PAREDE_A = 14, PAREDE_B = 15, PAREDE_C = 16, PAREDE_CASA = 17;
	private static final int TELHADO_TOPO = 22;

	//UMA COR DO MAPA DO CHAO PARA CADA TILE
	private static final int[] PALETA = {
		0x404040,0xFFD700,0xFF8C00,0xFF0000,0xC0C0C0,0x2E8B57,
		0x228B22,0x1E90FF,0xF4A460,0xB0B0B0,
		0x9E9E9E,0x8B0000,0x4682B4,0xD2691E,
		0x7E786F,0x6B5B4F,0x556677,0xDEB887,
		0x9370DB,0xBA55D3,0xFF69B4,0xADFF2F,0x708090,0x8B4513 };

	private static final String[] NOMES = {"METROPOLE", "LITORANEA", "INDUSTRIAL"};

	//VELOCIDADES DE PASSEIO E DE CORRIDA
	private static final double VELOCIDADE = 8.0;
	private static final double VELOCIDADE_RAPIDA = 26.0;

	private JGTopDownLayer cidade = null;
	private JGImage minimapa = null;
	private int cidadeAtual = 1;
	private int nivelDeRetorno = -1;
	private Random sorteio = new Random();

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
		gameManager.graphics.setFont(new Font("verdana", Font.BOLD, 13));
		carregaCidade(cidadeAtual);
	}

	//TROCA O CENARIO INTEIRO: TILES, MAPA DO CHAO, ALTURAS E MINIMAPA
	private void carregaCidade(int numero)
	{
		//LIBERA O CENARIO ANTERIOR ANTES DE MONTAR O NOVO
		for (int indice = 0; indice < vetLayers.size(); indice++)
		{
			vetLayers.get(indice).free();
		}
		vetLayers.clear();

		if (minimapa != null)
		{
			JGImageManager.free(minimapa);
			minimapa = null;
		}

		cidadeAtual = numero;
		String prefixo = "/Images/city" + numero + "_";

		//FORA DA CIDADE SO EXISTE MAR
		gameManager.windowManager.setBackgroundColor(new Color(24, 54, 74));

		JGColorIndex[] cores = new JGColorIndex[PALETA.length];
		for (int indice = 0; indice < PALETA.length; indice++)
		{
			cores[indice] = new JGColorIndex(indice, new Color(PALETA[indice]));
		}

		cidade = createTopDownLayer(getURL(prefixo + "tiles.png"),
		                            getURL(prefixo + "ground.png"),
		                            cores,
		                            new JGVector2D(LADO_BLOCO, LADO_BLOCO),
		                            true);

		//NO MAPA DE ALTURAS O INDICE DA COR E O NUMERO DE ANDARES
		JGColorIndex[] alturas = new JGColorIndex[6];
		for (int indice = 0; indice < alturas.length; indice++)
		{
			alturas[indice] = new JGColorIndex(indice, new Color(indice * 0x111111));
		}
		cidade.createHeightMap(getURL(prefixo + "height.png"), alturas);

		//CADA TIPO DE TELHADO TEM A SUA PAREDE: E O QUE DA PREDIOS DE CORES E
		//JANELAS DIFERENTES SEM PRECISAR DE OUTRO MAPA
		cidade.setWallFrameIndex(PAREDE_A);
		cidade.setWallFrameIndex(TELHADO_A, PAREDE_A);
		cidade.setWallFrameIndex(TELHADO_B, PAREDE_B);
		cidade.setWallFrameIndex(TELHADO_C, PAREDE_C);
		cidade.setWallFrameIndex(TELHADO_CASA, PAREDE_CASA);
		cidade.setWallFrameIndex(TELHADO_TOPO, PAREDE_B);

		cidade.setPerspective(0.11);

		minimapa = JGImageManager.loadImage(getURL(prefixo + "preview.png"));

		vaiParaARua();
	}

	//COLOCA UMA CELULA DO MAPA NO CENTRO DA TELA
	private void centralizaEm(int coluna, int linha)
	{
		cidade.offset.setXY(gameManager.windowManager.getResolutionWidth() / 2.0 - coluna * LADO_BLOCO,
		                    gameManager.windowManager.getResolutionHeight() / 2.0 - linha * LADO_BLOCO);
	}

	//SORTEIA UM PONTO DE RUA, PARA A CAMERA NUNCA COMECAR NO MEIO DO MAR
	private void vaiParaARua()
	{
		for (int tentativa = 0; tentativa < 4000; tentativa++)
		{
			int coluna = sorteio.nextInt(COLUNAS);
			int linha = sorteio.nextInt(LINHAS);
			int tile = cidade.getFrameIndexByCell(coluna, linha);

			if (tile == RUA_V || tile == RUA_H || tile == CRUZAMENTO)
			{
				centralizaEm(coluna, linha);
				return;
			}
		}
		centralizaEm(COLUNAS / 2, LINHAS / 2);
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

		//AS TECLAS 1, 2 E 3 TROCAM DE CIDADE
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_1) && cidadeAtual != 1) { carregaCidade(1); return; }
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_2) && cidadeAtual != 2) { carregaCidade(2); return; }
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_3) && cidadeAtual != 3) { carregaCidade(3); return; }

		//ESPACO SALTA PARA OUTRO PONTO DA CIDADE
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_SPACE))
		{
			vaiParaARua();
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
		gameManager.graphics.drawString("1 2 3: TROCAR CIDADE    SETAS: ANDAR    SHIFT: CORRER    " +
		                                "ESPACO: OUTRO PONTO    A/Z: PERSPECTIVA    ESC: " +
		                                (nivelDeRetorno >= 0 ? "MENU" : "SAIR"), 16, 24);
		gameManager.graphics.drawString("CIDADE " + cidadeAtual + " - " + NOMES[cidadeAtual - 1] +
		                                "    BLOCO " + (int)bloco.getX() + ", " + (int)bloco.getY() +
		                                "    ANDARES: " + cidade.getHeightAt(centroX, centroY), 16, 44);
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
