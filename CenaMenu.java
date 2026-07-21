
//Pacotes utilizados
import java.awt.Color;
import java.net.URL;
import JGames2D.JGLevel;
import JGames2D.JGSoundEffect;
import JGames2D.JGSoundManager;
import JGames2D.JGSprite;

public class CenaMenu extends JGLevel
{	
	JGSprite mira = null;
	JGSprite title = null;
	String[] names = {"btn_play", "btn_controls", "btn_credits", "btn_exit"};
	JGSprite[] vetButtons = new JGSprite[names.length];
	JGSoundEffect toc = null;
	JGSoundEffect click = null;
	
	public CenaMenu()
	{
		JGSoundManager.loadSoundEffect(getURL("Sounds/toc.wav"));
		JGSoundManager.loadSoundEffect(getURL("Sounds/click.wav"));
	}
	
	private URL getURL(String image)
	{
		return getClass().getResource(image);
	}
	
	@Override
	public void init() 
	{
		//CONFIGURA A COR E FUNDO DA TELA PARA VERDE
		gameManager.windowManager.setBackgroundColor(Color.GREEN);
		
		//CARREGA O EFEITO DE SOM
		click = JGSoundManager.loadSoundEffect(getURL("Sounds/click.wav"));
		toc = JGSoundManager.loadSoundEffect(getURL("Sounds/toc.wav"));
		
		//CENTRO DA AREA DE DESENHO. NAO PODE SER UM VALOR FIXO: A RESOLUCAO
		//MUDA CONFORME A VERSAO DO JOGO, E O MENU SAIA DO EIXO.
		int centroX = gameManager.windowManager.getResolutionWidth() / 2;

		//CRIA O SPRITE DE TITULO SETA O ZOOM E POSICAO
		title = createSprite(getURL("/Images/gametitle.png"), 1, 1);
		title.zoom.setXY(0.5,0.5);
		title.position.setXY(centroX, 96);

		//CRIA O SPRITE DO BOTAO PLAY ADICIONA ANIMACOES, CONFIG ZOOM E POSICAO
		for (int index=0; index < vetButtons.length; index++)
		{
			vetButtons[index] = createSprite(getURL("/Images/" + names[index] + ".png"), 2, 1);
			vetButtons[index].addAnimation(1, true, 0);
			vetButtons[index].addAnimation(1, true, 1);
			vetButtons[index].zoom.setXY(0.6,0.6);
			vetButtons[index].position.setXY(centroX, 180 + 120 * index);
		}
		
		//CRIA O SPRITE DA MIRA
		mira = createSprite(getURL("/Images/mira.png"), 1, 1);
	}

	@Override
	public void execute() 
	{
		//ATUALIZA A POSICAO DO PONTEIRO DO MOUSE
		mira.position.setX(gameManager.inputManager.getMousePosition().getX());
		mira.position.setY(gameManager.inputManager.getMousePosition().getY());
		
		//TESTA EVENTOS COM OS SPRITES DO MENU
		for (int index=0; index < vetButtons.length; index++)
		{
			if (vetButtons[index].collide(mira))
			{
				
				if (vetButtons[index].getCurrentAnimationIndex() != 1)
				{
					vetButtons[index].setCurrentAnimation(1);
					toc.play();
				}
				
				//SE MOUSE CLICADO E CIMA DO PRIMEIRO BOTAO
				if (index == 0 && gameManager.inputManager.mouseClicked())
				{
					click.play();
					gameManager.setCurrentLevel(2);
					return;
				}
				//SE MOUSE CLICADO E CIMA DO TERCEIRO BOTAO
				else if (index == 1 && gameManager.inputManager.mouseClicked())
				{
					click.play();
					gameManager.setCurrentLevel(5);
					return;
				}
				//SE MOUSE CLICADO E CIMA DO TERCEIRO BOTAO
				else if (index == 2 && gameManager.inputManager.mouseClicked())
				{
					click.play();
					gameManager.setCurrentLevel(3);
					return;
				}
				//SE MOUSE CLICADO E CIMA DO TERCEIRO BOTAO
				else if (index == 3 && gameManager.inputManager.mouseClicked())
				{
					click.play();
					gameManager.finish();
				}
			}
			else
			{
				vetButtons[index].setCurrentAnimation(0);
			}
		}
	}
}
