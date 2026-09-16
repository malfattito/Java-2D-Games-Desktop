/***********************************************************************
*Name: JGLog
*Description: singleton that creates a file log to receive the engines messages
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JGLog 
{
	/***********************************************************
	*Name: CLog
	*Description: private constructor
	*Parameters: none
	*Return: None
	************************************************************/
	private JGLog()
	{}
	
	/*******************************************
	* Name:init
	* Description: inits the log system
	* Parameters: none
	* Returns: none
	******************************************/
	public static void init()
	{
		try
		{
			//sem o "true" de acrescentar: o arquivo comeca vazio a cada
			//execucao. Acumulando, uma linha de ontem se le como um erro de
			//agora, e foi exatamente o que aconteceu - um erro ja corrigido
			//continuou aparecendo no alto do arquivo por dias
			FileWriter file = new FileWriter("LOG.txt");
			file.write("*************************************************\n");
			file.write("   LOG  " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "\n");
			file.write("*************************************************\n\n");
			file.flush();
			file.close();
		}
		catch(Exception e)
		{
			//Uma janela modal aqui congelaria o laco do jogo
			System.err.println("OPEN LOG ERROR: " + e);
		}
	}
	
	/*******************************************
	* Name:writeLog
	* Description: write a message log
	* Parameters: String
	* Returns: none
	******************************************/
	public static void writeLog(String logMessage)
	{
		try
		{
			//a hora em cada linha: sem ela nao se sabe se duas queixas iguais
			//sao a mesma repetida ou duas de momentos diferentes
			FileWriter arquivo = new FileWriter("LOG.txt",true);
			arquivo.write(new SimpleDateFormat("HH:mm:ss").format(new Date()) + "  " + logMessage);
			arquivo.flush();
			arquivo.close();
		}
		catch(Exception e)
		{
			System.err.println("LOG WRITE ERROR: " + e);
		}
	}
	
	/*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void free() 
    {
    }
}
