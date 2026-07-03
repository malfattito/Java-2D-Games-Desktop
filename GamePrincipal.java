import JGames2D.JGEngine;

public class GamePrincipal extends JGEngine
{
	public static void main(String[] args) 
	{
		//INSTANCIA A CLASSE GERENCIADORA DO MOTOR
		JGEngine engine = new JGEngine();
		
		//Configura os parametros da janela
		engine.windowManager.setResolution(800, 600, 32);
		engine.windowManager.setfullScreen(false);
		
		//Cria as cenas do jogo
		CenaAbertura cenaAbertura = new CenaAbertura();
		CenaMenu cenamenu = new CenaMenu();
		CenaGame cenaGame = new CenaGame();
		CenaCreditos cenaCreditos = new CenaCreditos();
		CenaControles cenaControles = new CenaControles();
	
		//Adiciona as cenas ao gerente de jogo
		engine.addLevel(cenaAbertura);
		engine.addLevel(cenamenu);
		engine.addLevel(cenaGame);
		engine.addLevel(cenaCreditos);
		engine.addLevel(cenaGame);
		engine.addLevel(cenaControles);
		
		//Inizializa o motor
		engine.start();
	}
}
