import JGames2D.JGEngine;

//VERSAO EM TELA CHEIA COM BUFFER DE 1024 x 768.
//A IMAGEM E AMPLIADA ATE A TELA MANTENDO A PROPORCAO, COM TARJAS PRETAS
//NAS LATERAIS QUANDO O MONITOR NAO E 4:3.
public class CidadeFullscreen
{
	public static void main(String[] args)
	{
		JGEngine engine = new JGEngine();

		engine.windowManager.setResolution(1024, 768, 32);
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
