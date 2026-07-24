import JGames2D.JGEngine;

public class GamePrincipal extends JGEngine
{
	public static void main(String[] args)
	{
		//PEDE AO SISTEMA O PIPELINE GRAFICO ACELERADO. PRECISA SER ANTES
		//DE QUALQUER AWT/SWING - OU SEJA, ANTES DE INSTANCIAR O MOTOR,
		//POIS O windowManager E UM JFrame E JA INICIALIZA O TOOLKIT.
		System.setProperty("sun.java2d.opengl", "true");       //LINUX/WINDOWS: pipeline OpenGL
		System.setProperty("sun.java2d.metal", "true");        //macOS: pipeline Metal (JDK 17+)
		System.setProperty("sun.java2d.d3d", "true");          //WINDOWS: pipeline Direct3D
		System.setProperty("awt.useSystemAAFontSettings", "on"); //ANTIALIAS DE FONTE DO SISTEMA

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
