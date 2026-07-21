import JGames2D.JGEngine;

//DEMONSTRACAO DA CAMADA ISOMETRICA: UMA CIDADE ROLAVEL PELAS SETAS
public class IsoPrincipal
{
	public static void main(String[] args)
	{
		JGEngine engine = new JGEngine();

		engine.windowManager.setResolution(800, 600, 32);
		engine.windowManager.setWindowTitle("JGames2D - Cena Isometrica");
		engine.windowManager.setfullScreen(false);

		engine.addLevel(new CenaIsoCidade());

		engine.start();
	}
}
