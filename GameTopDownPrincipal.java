import JGames2D.JGEngine;

//VERSAO DO JOGO EM TELA CHEIA COM A CENA TOP DOWN NO LUGAR DA PARTIDA.
//A ORDEM DOS NIVEIS E A MESMA DO GamePrincipal, PORQUE AS CENAS CHAMAM
//setCurrentLevel COM INDICES FIXOS: O MENU MANDA JOGAR PARA O 2,
//SOBRE PARA O 3 E CONTROLES PARA O 5.
public class GameTopDownPrincipal
{
	public static void main(String[] args)
	{
		JGEngine engine = new JGEngine();

		//CONFIGURA A JANELA EM TELA CHEIA
		engine.windowManager.setResolution(800, 600, 32);
		engine.windowManager.setWindowTitle("JGames2D - Cidade Top Down");
		engine.windowManager.setfullScreen(true);

		//CRIA AS CENAS. A CIDADE VOLTA AO MENU, QUE E O NIVEL 1.
		CenaAbertura cenaAbertura = new CenaAbertura();
		CenaMenu cenaMenu = new CenaMenu();
		CenaGTACidade cenaCidade = new CenaGTACidade(1);
		CenaCreditos cenaCreditos = new CenaCreditos();
		CenaControles cenaControles = new CenaControles();

		//ADICIONA NA ORDEM QUE OS INDICES DAS CENAS ESPERAM
		engine.addLevel(cenaAbertura);    //0
		engine.addLevel(cenaMenu);        //1
		engine.addLevel(cenaCidade);      //2  <- JOGAR
		engine.addLevel(cenaCreditos);    //3  <- SOBRE
		engine.addLevel(cenaCidade);      //4  (mesma folga do GamePrincipal)
		engine.addLevel(cenaControles);   //5  <- CONTROLES

		engine.start();
	}
}
