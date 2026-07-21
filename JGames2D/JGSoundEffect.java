/***********************************************************************
*Name: JGSoundEffect
*Description: represents the animation sequence of image frames
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class JGSoundEffect 
{
	//Class Attributes
	private Clip clip = null;
	private String fileName = null;
	
	/***********************************************************
	*Name: JGSoundEffect
	*Description: constructor of a sound object
	*Parameters: URL
	*Return: None
	************************************************************/
	JGSoundEffect(URL file)
	{
		fileName = file.getPath();

		AudioInputStream stream = null;
		try
		{
			stream = AudioSystem.getAudioInputStream(file);
			clip = AudioSystem.getClip();
			clip.open(stream);
		}
		catch(Exception e)
		{
			//Sem som o jogo continua jogavel: registra e segue
			clip = null;
			JGLog.writeLog("ERROR LOAD SOUND " + file + " : " + e + "\n");
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch(Exception e)
				{
				}
			}
		}
	}
	
	/***********************************************************
	*Name: getSoundName
	*Description: returns the name of the sound
	*Parameters: none
	*Return: String
	************************************************************/
	public String getSoundName()
	{
		return fileName;
	}
	
	/***********************************************************
	*Name: setVolume
	*Description: configures the volume of sound reproduction
	*Parameters: float(0 - 100)
	*Return: None
	************************************************************/
	public void setVolume(float volume)
	{
		if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		//Volume zero daria log10(0) = -infinito
		volume = Math.max(0.0001f, Math.min(100.0f, volume)) / 100.0f;

		FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = 20f * (float) Math.log10(volume);

		//O ganho precisa caber no intervalo aceito pela placa de som
		control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), gain)));
	}
	
	/***********************************************************
	*Name: play
	*Description: start sound reproduction
	*Parameters: None
	*Return: None
	************************************************************/
	public void play()
	{
		if (clip == null)
		{
			return;
		}

		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}
	
	/***********************************************************
	*Name: play
	*Description: start sound reproduction in loop
	*Parameters: None
	*Return: None
	************************************************************/
	public void loop()
	{
		if (clip == null)
		{
			return;
		}

		clip.stop();
		clip.setFramePosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	/***********************************************************
	*Name: stop
	*Description: stop sound reproduction
	*Parameters: None
	*Return: None
	************************************************************/
	public void stop()
	{
		if (clip == null)
		{
			return;
		}

		//Nao fecha o clip: o som continua reutilizavel por play()
		clip.stop();
		clip.setFramePosition(0);
	}
	
	/***********************************************************
	*Name: free
	*Description: free resources
	*Parameters: None
	*Return: None
	************************************************************/
	public void free()
	{
		//O clip segura uma linha de audio do sistema: precisa ser fechado
		if (clip != null)
		{
			clip.stop();
			clip.close();
			clip = null;
		}
		fileName = null;
	}
}
