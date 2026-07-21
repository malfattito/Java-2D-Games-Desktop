import JGames2D.JGEngine;

//JOGO EM TELA CHEIA COM A CIDADE EM ILHAS APOS O MENU
public class CidadePrincipal
{
	public static void main(String[] args)
	{
		JGEngine engine = new JGEngine();

		//DESENHA NA RESOLUCAO REAL DO MONITOR. COM UM BUFFER MENOR, A TELA
		//CHEIA AMPLIARIA A IMAGEM E JOGARIA FORA O DETALHE DOS TILES.
		java.awt.DisplayMode modo = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
		                                .getDefaultScreenDevice().getDisplayMode();

		engine.windowManager.setResolution(modo.getWidth(), modo.getHeight(), 32);
		engine.windowManager.setWindowTitle("JGames2D - Cidade");
		engine.windowManager.setfullScreen(true);

		//A ORDEM DOS NIVEIS SEGUE A DO GamePrincipal, PORQUE AS CENAS USAM
		//INDICES FIXOS: JOGAR VAI PARA O 2, SOBRE PARA O 3, CONTROLES PARA O 5
		CenaAbertura cenaAbertura = new CenaAbertura();
		CenaMenu cenaMenu = new CenaMenu();
		CenaCidadeIlhas cenaCidade = new CenaCidadeIlhas(1);
		CenaCreditos cenaCreditos = new CenaCreditos();
		CenaControles cenaControles = new CenaControles();

		engine.addLevel(cenaAbertura);    //0
		engine.addLevel(cenaMenu);        //1
		engine.addLevel(cenaCidade);      //2  <- JOGAR
		engine.addLevel(cenaCreditos);    //3  <- SOBRE
		engine.addLevel(cenaCidade);      //4
		engine.addLevel(cenaControles);   //5  <- CONTROLES

		engine.start();
	}
}
