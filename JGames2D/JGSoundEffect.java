/***********************************************************************
*Name: JGSoundEffect
*Description: represents a short sound effect, like a shot or an explosion
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class JGSoundEffect 
{
	//Um mesmo efeito precisa poder soar varias vezes ao mesmo tempo: cada
	//disparo usa uma das copias, em vez de cortar o som anterior
	private static final int VOICES = 6;

	//Class Attributes
	private Clip[] clips = null;
	private int nextClip = 0;
	private float volume = 100.0f;
	private String fileName = null;
	
	/***********************************************************
	*Name: JGSoundEffect
	*Description: constructor of a sound object
	*Parameters: URL
	*Return: None
	************************************************************/
	JGSoundEffect(URL file)
	{
		if (file == null)
		{
			//Fica mudo, mas o jogo continua rodando e o motivo fica no log
			fileName = "";
			clips = null;
			JGLog.writeLog("ERROR LOAD SOUND: arquivo nao encontrado (URL nula). " +
			               "Confira se a pasta Sounds esta no classpath.\n");
			return;
		}

		fileName = file.getPath();

		try
		{
			//Le o audio uma unica vez para a memoria e abre varias copias a
			//partir dele: reabrir a URL por copia custaria muito mais
			AudioInputStream source = AudioSystem.getAudioInputStream(file);
			AudioFormat format = source.getFormat();
			byte[] data = readAll(source);
			source.close();

			clips = new Clip[VOICES];
			for (int index = 0; index < clips.length; index++)
			{
				clips[index] = AudioSystem.getClip();
				clips[index].open(new AudioInputStream(new ByteArrayInputStream(data),
						                               format, data.length / format.getFrameSize()));
			}
		}
		catch(Exception e)
		{
			//Sem som o jogo continua jogavel: registra e segue
			clips = null;
			JGLog.writeLog("ERROR LOAD SOUND " + file + " : " + e + "\n");
		}
	}

	/***********************************************************
	*Name: readAll
	*Description: reads the whole audio stream into memory
	*Parameters: AudioInputStream
	*Return: byte[]
	************************************************************/
	private byte[] readAll(AudioInputStream stream) throws java.io.IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int read;

		while ((read = stream.read(chunk)) > 0)
		{
			output.write(chunk, 0, read);
		}

		return output.toByteArray();
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
		this.volume = volume;

		if (clips == null)
		{
			return;
		}

		for (int index = 0; index < clips.length; index++)
		{
			applyVolume(clips[index]);
		}
	}

	/***********************************************************
	*Name: applyVolume
	*Description: applies the current volume to one clip
	*Parameters: Clip
	*Return: None
	************************************************************/
	private void applyVolume(Clip clip)
	{
		if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		//Volume zero daria log10(0) = -infinito
		float level = Math.max(0.0001f, Math.min(100.0f, volume)) / 100.0f;

		FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		float gain = 20f * (float) Math.log10(level);

		//O ganho precisa caber no intervalo aceito pela placa de som
		control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), gain)));
	}

	/***********************************************************
	*Name: play
	*Description: start sound reproduction. Overlaps with the previous
	*             reproductions instead of cutting them off.
	*Parameters: None
	*Return: None
	************************************************************/
	public void play()
	{
		Clip clip = takeFreeClip();

		if (clip == null)
		{
			return;
		}

		//Rewinding is not enough: the line still holds what the previous
		//reproduction gave it, and the beginning of the new one goes out
		//underneath that leftover, which is heard as a piece missing
		clip.stop();
		clip.flush();

		clip.setFramePosition(0);
		applyVolume(clip);
		clip.start();
	}

	/***********************************************************
	*Name: takeFreeClip
	*Description: returns the next copy that is not sounding, going round
	*             the voices. If every copy is busy, recycles the oldest one.
	*Parameters: None
	*Return: Clip
	************************************************************/
	private Clip takeFreeClip()
	{
		if (clips == null)
		{
			return null;
		}

		//Goes round instead of always handing back the first copy. A copy
		//that has just finished is still emptying itself into the sound
		//card, and taking it again straight away is what costs the start of
		//the next reproduction. Going round gives it the whole turn to drain.
		for (int step = 0; step < clips.length; step++)
		{
			int index = (nextClip + step) % clips.length;

			if (!clips[index].isRunning())
			{
				nextClip = (index + 1) % clips.length;

				return clips[index];
			}
		}

		//Every copy is busy: the one that started longest ago gives way
		Clip oldest = clips[nextClip];
		nextClip = (nextClip + 1) % clips.length;

		return oldest;
	}
	
	/***********************************************************
	*Name: loop
	*Description: start sound reproduction in loop. Always uses the first
	*             copy, so that stop() can find it again.
	*Parameters: None
	*Return: None
	************************************************************/
	public void loop()
	{
		if (clips == null)
		{
			return;
		}

		clips[0].stop();
		clips[0].setFramePosition(0);
		applyVolume(clips[0]);
		clips[0].loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	/***********************************************************
	*Name: stop
	*Description: stop sound reproduction of every copy
	*Parameters: None
	*Return: None
	************************************************************/
	public void stop()
	{
		if (clips == null)
		{
			return;
		}

		//Nao fecha os clips: o som continua reutilizavel por play()
		for (int index = 0; index < clips.length; index++)
		{
			clips[index].stop();
			clips[index].setFramePosition(0);
		}
	}
	
	/***********************************************************
	*Name: free
	*Description: free resources
	*Parameters: None
	*Return: None
	************************************************************/
	public void free()
	{
		//Cada clip segura uma linha de audio do sistema: precisa ser fechado
		if (clips != null)
		{
			for (int index = 0; index < clips.length; index++)
			{
				if (clips[index] != null)
				{
					clips[index].stop();
					clips[index].close();
					clips[index] = null;
				}
			}
			clips = null;
		}
		fileName = null;
	}
}
