import JGames2D.JGEngine;

//DEMONSTRACAO DA CAMADA COM VISTA DE CIMA EM PERSPECTIVA, ESTILO GTA 1
public class GTAPrincipal
{
	public static void main(String[] args)
	{
		JGEngine engine = new JGEngine();

		engine.windowManager.setResolution(800, 600, 32);
		engine.windowManager.setWindowTitle("JGames2D - Vista GTA");
		engine.windowManager.setfullScreen(false);

		engine.addLevel(new CenaGTACidade());

		engine.start();
	}
}
