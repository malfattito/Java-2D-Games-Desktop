
//Pacotes utilizados
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import JGames2D.JGColorIndex;
import JGames2D.JGOrthoLayer;
import JGames2D.JGLevel;
import JGames2D.JGMusic;
import JGames2D.JGSoundEffect;
import JGames2D.JGSoundManager;
import JGames2D.JGSprite;
import JGames2D.JGTimer;
import JGames2D.JGVector2D;

public class CenaGame extends JGLevel
{
	//Declara os objetos visuais da cena
	private JGSprite aviao = null;
	private JGOrthoLayer layer = null;
	private ArrayList<JGSprite> vetInimigos = null;
	private ArrayList<JGSprite> vetTiros = null;
	private ArrayList<JGSprite> vetExplosoes = null;
	private JGTimer tempoNovoInimigo = null;
	private JGTimer tempoTiro = null;
	private JGTimer tempoInvencivel = null;
	private Random sorteio = null;
	private int totalVidas = 3;
	private String pontosStr = "0000";
	private int pontos = 0;
	private int pontosTemporarios = 0;
	private boolean gameOver = false;
	private JGMusic musica = null;
	private JGSoundEffect effect = null;
	private JGSoundEffect shot = null;
	private JGSoundEffect boom = null;
	private JGSoundEffect hit = null;
	
	public CenaGame()
	{
		//LOAD GAME SOUNDS
		JGSoundManager.loadSoundEffect(getURL("Sounds/engine.wav"));
		JGSoundManager.loadSoundEffect(getURL("Sounds/SHOT.wav"));
		JGSoundManager.loadSoundEffect(getURL("Sounds/boom.wav"));
		JGSoundManager.loadSoundEffect(getURL("Sounds/hit.wav"));
		
		//CARREGA A MUSICA DO JOGO
		musica = JGSoundManager.loadMusic(getURL("Sounds/DangerZone.mp3"));
		musica.setNumberOfLoops(-1);  
	}
	
	@Override
	public void init() 
	{
		//REINICIA OS ATRIBUTOS PARA O INICIO DE UM NOVO JOGO
		reiniciaGame();
		
		//CONFIGURA OS PARAMETROS DA FONTE UTILIZADA PARA ESCREVER NA TELA
		gameManager.graphics.setFont(new Font("verdana", Font.BOLD, 24));
		gameManager.graphics.setColor(Color.yellow);
		
		//CARREGA O EFEITO DE SOM
		effect = JGSoundManager.loadSoundEffect(getURL("Sounds/engine.wav"));
		shot =JGSoundManager.loadSoundEffect(getURL("Sounds/SHOT.wav"));
		boom = JGSoundManager.loadSoundEffect(getURL("Sounds/boom.wav"));
		hit = JGSoundManager.loadSoundEffect(getURL("Sounds/hit.wav"));
		
		//REPRODUCAO SONORA
		musica.play();
		effect.setVolume(40);
		effect.loop();
		
		//CRIA O SPRITE, CARREGA A IMAGEM E CONFIGURA COMO FALSE A VISUALIZACAO
		createSprite(getURL("/Images/spr_enemy.png"), 1, 3).visible = false;
		createSprite(getURL("/Images/spr_shot.png"),1,1).visible = false;
		createSprite(getURL("/Images/spr_bigexplosion.png"), 2 , 4).visible = false;
		
		//INSTANCIA O ARRAYLIST DE INIMIGOS
		vetInimigos = new ArrayList<JGSprite>();
		
		//INSTANCIA O ARRAYLIST DE TIROS
		vetTiros = new ArrayList<JGSprite>();
		
		//INSTANCIA O ARRAYLIST EXPLOSOES
		vetExplosoes = new ArrayList<JGSprite>();
		
		//INICIALIZA O TIMER RESPONSAVEL PELA CRIACAO DO NOVO INIMIGO
		tempoNovoInimigo = new JGTimer(1000);
		
		//INICIALIZA O TIMER RESPONSAVEL PELO CONTROLE DO TEMPO DE TIRO
		tempoTiro = new JGTimer(300);
		
		//INICIALIZA O TIMER RESPONSAVEL PELA INVENCIBILIDADE DO JOGADOR
		tempoInvencivel = new JGTimer(0);
		
		//METODO QUE VAI SER CHAMADO SEMPRE QUE UMA CENA FOR APRESENTADA NA JANELA
		gameManager.windowManager.setBackgroundColor(Color.black);
		
		//CRIA O SPRITE DO JOGADOR E ADICIONA A ANIMACAO
		aviao = createSprite(getURL("/Images/spr_airplane.png"),1, 4);
		aviao.addAnimation(15, true, 0, 1, 2);
		aviao.position.setXY(400, 500);
		
		//CRIA A CAMADA DO JOGO
		JGColorIndex[] vetCores = new JGColorIndex[10];
		vetCores[0] = new JGColorIndex(0,new Color(0,0,0));
		vetCores[1] = new JGColorIndex(1,new Color(255,0,0));
		vetCores[2] = new JGColorIndex(2,new Color(0,255,0));
		vetCores[3] = new JGColorIndex(4,new Color(0,0,255));
		vetCores[4] = new JGColorIndex(5,new Color(255,255,0));
		vetCores[5] = new JGColorIndex(6,new Color(0,255,255));
		vetCores[6] = new JGColorIndex(7,new Color(255,0,255));
		vetCores[7] = new JGColorIndex(8,new Color(255,255,255));
		vetCores[8] = new JGColorIndex(9,new Color(175,0,0));
		vetCores[9] = new JGColorIndex(10,new Color(0,175,0));
		
		layer = createOrthoLayer(getClass().getResource("/Images/spr_elements.png"),
									getClass().getResource("/Images/lay_level.bmp"), 
									vetCores, new JGVector2D(32, 32), true);
		
		layer.setSpeed(new JGVector2D(0 , 10));
	}
	
	private void configuraPontos() 
	{
		if (pontosTemporarios > 0)
		{
			pontos++;
			pontosTemporarios--;
		}
		
		pontosStr = (pontos % 10000) / 1000 + "";
		pontosStr += (pontos % 1000) / 100 + "";
		pontosStr += (pontos % 100) / 10 + "";
		pontosStr += (pontos % 10) + "";
	}
	
	//RESCREVE O METODO DE DESENHO DA CENA
	@Override
	public void render()
	{
		super.render();

		gameManager.graphics.setColor(Color.yellow);
		gameManager.graphics.drawString("VIDAS: " + totalVidas, 50, 50);
		gameManager.graphics.drawString(pontosStr, 650, 50);

		//A MENSAGEM PRECISA SER ESCRITA AQUI: O QUE E DESENHADO EM execute()
		//E APAGADO PELA LIMPEZA DA TELA ANTES DA CENA SER DESENHADA
		if (gameOver)
		{
			gameManager.graphics.drawString("GAME OVER! - PRESS ENTER...", 200, 300);
		}
	}
	
	private URL getURL(String image)
	{
		return getClass().getResource(image);
	}
	
	private boolean testaGameOver()
	{
		return gameOver;
	}
	
	private void testaJogadorAtingido()
	{
		//ATUALIZA O TEMPO RESPONSAVEL PELA INVENCIBILIDADE
		tempoInvencivel.update();
		
		//ENQUANTO TEMPO CORRENDO AVIAO PISCA
		if (!tempoInvencivel.isTimeEnded())
		{
			aviao.visible = !aviao.visible;
			return;
		}
		
		//AVIAO SEMPRE VISIVEL
		aviao.visible = true;
		
		//TESTA A COLISAO ENTRE AVIAO E INIMIGO
		for (JGSprite inimigo : vetInimigos)
		{
			if (inimigo.visible != false)
			{
				//QUANDO OCORRER A COLISAO SOME AVIAO INIMIGO, REINICA TEMPO INVENCIBILIDADE E TERMINA O LOOP
				if (inimigo.collide(aviao))
				{
					hit.play();
					totalVidas--;
					tempoInvencivel.restart(2000);
					inimigo.visible = false;
					
					if (totalVidas < 0)
					{
						tempoInvencivel.restart(0);
						totalVidas = 0;
						gameOver = true;
					}
					
					break;
				}
			}
		}
	}
	
	private void testaColisaoTiros()
	{
		for (JGSprite inimigo : vetInimigos)
		{
			for (JGSprite tiro : vetTiros )
			{
				if (inimigo.visible && tiro.visible && tiro.collide(inimigo))
				{
					boom.play();
					pontosTemporarios += 50;
					criaExplosao((int)inimigo.position.getX(),(int)inimigo.position.getY());
					inimigo.visible = false;
					tiro.visible = false;
					break;
				}
			}
		}
	}
	
	private void atualizaInimigos()
	{
		//ATUALIZA AS POSICOES DOS INIMIGOS
		for (JGSprite inimigo : vetInimigos)
		{
			inimigo.position.setX(inimigo.position.getX() + 20 * inimigo.direction.getX());
			inimigo.position.setY(inimigo.position.getY() + 20 * inimigo.direction.getY());
			
			//SE JA SAIU DA TELA, SETA A VISIBILIDADE DO SPRITE COMO FALSO
			if (inimigo.position.getX() < -inimigo.frameWidth ||
			    inimigo.position.getX() > gameManager.windowManager.width + inimigo.frameWidth || 
				inimigo.position.getY() > gameManager.windowManager.height + inimigo.frameHeight)
			{
				inimigo.visible = false;
			}
		}
	}
	
	private void updateTiros()
	{
		//ATUALIZA A POSICAO DE CADA TIRO E VERIFICA SE JA SAIU DA TELA PARA RECICLAR
		for (JGSprite tiro : vetTiros)
		{
			tiro.position.setY(tiro.position.getY() + 10 * tiro.direction.getY());
			
			if (tiro.position.getY() < -tiro.imageHeight)
			{
				tiro.visible = false;
			}
			
		}
	}
	
	private void updateExplosoes()
	{
		for (JGSprite explosao : vetExplosoes)
		{
			if (explosao.getCurrentAnimation().isEnded())
			{
				explosao.visible = false;
			}
		}
	}
	
	private void criaExplosao(int posX, int posY)
	{
		JGSprite referencia = null;
		
		for (JGSprite explosao : vetExplosoes)
		{
			if (explosao.visible == false)
			{
				referencia = explosao;
				explosao.visible = true;
				explosao.position.setXY(posX, posY);
				explosao.getCurrentAnimation().restart();
				break;
			}
		}
		
		if (referencia == null)
		{
			referencia = createSprite(getURL("/Images/spr_bigexplosion.png"), 2 , 4);
			referencia.addAnimation(10, false, 0, 7);
			vetExplosoes.add(referencia);
		}
		
		referencia.position.setXY(posX, posY);
	}
	
	private void criaTiro(int posX, int posY)
	{
		//SE TEMPO DO TIRO NAO ACABOU, RETORNA
		if (!tempoTiro.isTimeEnded())
		{
			return;
		}
		
		//REINICIALIZA O TEMPO DO TIRO
		tempoTiro.restart();
		
		shot.play();
		
		//CRIA UMA REFERENCIA
		JGSprite referencia = null;
		
		//TENTA RECICLAR UM SPRITE DE TIRO
		for (JGSprite tiro : vetTiros)
		{
			if (tiro.visible == false)
			{
				referencia = tiro;
				referencia.visible = true;
				break;
			}
		}
		
		//SE NAO CONSEGUIR RECICLAR, CRIA UM NOVO SPRITE E ADD AO VETOR DE TIRO
		if (referencia == null)
		{
			referencia = createSprite(getURL("/Images/spr_shot.png"), 1 , 1);
			vetTiros.add(referencia);
		}
		
		//CONFIGURA A POSICAO E DIRECAO DO NOVO TIRO
		referencia.position.setXY(posX, posY);
		referencia.direction.setXY(0, -1);
	}
	
	private void criaInimigo()
	{
		//CRIA UM OBJETO PARA RANDOMIZACAO DE VALORES
		sorteio = new Random();
		
		//ATUALIZA O TEMPO CRIACAO DOS INIMIGOS
		tempoNovoInimigo.update();
		
		//VERIFICA SE O INTERVALO DE TEMPO JA ACABOU
		if (tempoNovoInimigo.isTimeEnded())
		{
			//REINICIA O INTERVALO DE TEMPO
			tempoNovoInimigo.restart();
			
			//VARIAVEL DE REFERENCIA PARA O NOVO INIMIGO RECLICADO OU CRIADO
			JGSprite referencia = null;
			
			//VERIFICA SE HA ALGUM SPRITE PARA REAPROVEITAR
			for (JGSprite inimigo : vetInimigos)
			{
				if (inimigo.visible == false)
				{
					referencia = inimigo;
					referencia.visible = true;
					break;
				}
			}
			
			//SE NAO CONSEGUIU REAPROVEITAR NINGUEM, ENTAO CRIA UM NOVO SPRITE E ADD AO VETOR
			if (referencia == null)
			{
				referencia = createSprite(getURL("/Images/spr_enemy.png"), 1, 4);
				referencia.addAnimation(10, true, 0, 2);
				vetInimigos.add(referencia);
			}
			
			//SORTEIA UM VALOR ENTRE 0 E 2
			int valorSorteado = sorteio.nextInt(3);
			
			//REDEFINE OS ATRIBUTOS DO SPRITE
			if (valorSorteado == 0)
			{
				referencia.position.setX(0);
				referencia.position.setY( -referencia.frameHeight);
				referencia.direction.setX(1);
				referencia.direction.setY(1);	
			}
			else if (valorSorteado == 1)
			{
				referencia.position.setX(400);
				referencia.position.setY( -referencia.frameHeight);
				referencia.direction.setX(0);
				referencia.direction.setY(1);	
			}
			else if (valorSorteado == 2)
			{
				referencia.position.setX(800);
				referencia.position.setY(-referencia.frameHeight);
				referencia.direction.setX(-1);
				referencia.direction.setY(1);	
			}
		}
	}
	
	private void reiniciaGame()
	{
		pontos = 0;
		this.pontosTemporarios = 0;
		totalVidas = 3;
		gameOver = false;
	}

	@Override
	public void execute() 
	{
		//VAI SER CHAMADO CONSTANTEMENTE (LOOP) EM UM TAXA DE PELO MENOS 30 QUADROS POR SEGUNDO
		//IMPLEMENTA A LOGICA DA CENA
		if (gameManager.inputManager.keyTyped(KeyEvent.VK_ESCAPE))
		{
				musica.stop();
				effect.stop();
				gameManager.setCurrentLevel(1);
				return;
		}
		atualizaInimigos();
		updateTiros();
		
		//SE GAME OVER OCORREU INTERROME A EXECUCAO DO GAME
		if (testaGameOver())
		{
			if (gameManager.inputManager.keyPressed(KeyEvent.VK_ENTER))
			{
				reiniciaGame();
			}
			return;	
		}
		
		//METODO UTILIZADO PARA ADICIONAR UM NOVO INIMIGO AO JOGO
		criaInimigo();
		testaColisaoTiros();
		updateExplosoes();
		testaJogadorAtingido();
		configuraPontos();
		
		//ATUALIZA O TEMPO DO TIRO
		tempoTiro.update();
		
		//TESTA OS EVENTOS DE TECLA
		trataEventos();	
	}
	
	private void trataEventos()
	{
		//VERIFICA EVENTO DE TECLA E DISPARA UM TIRO
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_SPACE))
		{
			criaTiro((int)aviao.position.getX(), 
					 (int)(aviao.position.getY() - aviao.frameHeight / 2 - 16));
		}
				
		//TESTA OS EVENTOS COM O USUARIO
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_LEFT))
		{
			aviao.position.setX(aviao.position.getX() - 10);
		}
				
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_RIGHT))
		{
			aviao.position.setX(aviao.position.getX() + 10);
		}
				
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_UP))
		{
			aviao.position.setY(aviao.position.getY() - 10);
		}
				
		if (gameManager.inputManager.keyPressed(KeyEvent.VK_DOWN))
		{
			aviao.position.setY(aviao.position.getY() + 10);
		}
				
		//CASO AVIAO SAIA DOS LIMITES DA TELA, RECOLOCA DE VOLTA
		if (aviao.position.getX() < aviao.frameWidth / 2)
		{
			aviao.position.setX(aviao.frameWidth / 2);
		}
		else if (aviao.position.getX() > gameManager.windowManager.width - aviao.frameWidth / 2)
		{
			aviao.position.setX(gameManager.windowManager.width - aviao.frameWidth / 2);
		}
				
		if (aviao.position.getY() < aviao.frameHeight / 2)
		{
			aviao.position.setY(aviao.frameHeight / 2);
		}
		else if (aviao.position.getY() > gameManager.windowManager.height - aviao.frameHeight / 2)
		{
			aviao.position.setY(gameManager.windowManager.height - aviao.frameHeight / 2);
		}
	}
}
