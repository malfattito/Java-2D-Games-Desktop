/***********************************************************************
*Name: JGSoundEffect
*Description: represents a short sound effect, like a shot or an explosion,
*             or a track meant to loop. The samples live in memory in the
*             format of JGAudioMixer, and playing is asking the mixer for
*             a voice: nothing here touches the sound card, so play(),
*             loop() and stop() return at once.
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.io.ByteArrayOutputStream;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class JGSoundEffect 
{
	//Um mesmo efeito precisa poder soar varias vezes ao mesmo tempo: cada
	//disparo e uma voz a mais no misturador, em vez de cortar o som anterior
	private static final int VOICES = 6;

	//Uma trilha nao se sobrepoe a si mesma
	static final int SINGLE_VOICE = 1;

	//Class Attributes
	private short[] samples = null;
	private int voices = VOICES;
	private volatile float volume = 100.0f;
	private String fileName = null;
	
	/***********************************************************
	*Name: JGSoundEffect
	*Description: constructor of a sound object
	*Parameters: URL
	*Return: None
	************************************************************/
	JGSoundEffect(URL file)
	{
		this(file, VOICES);
	}

	/***********************************************************
	*Name: JGSoundEffect
	*Description: constructor of a sound object with a given number of
	*             copies sounding at the same time
	*Parameters: URL, int
	*Return: None
	************************************************************/
	JGSoundEffect(URL file, int voices)
	{
		this.voices = Math.max(1, voices);

		if (file == null)
		{
			//Fica mudo, mas o jogo continua rodando e o motivo fica no log
			fileName = "";
			JGLog.writeLog("ERROR LOAD SOUND: arquivo nao encontrado (URL nula). " +
			               "Confira se a pasta Sounds esta no classpath.\n");
			return;
		}

		fileName = file.getPath();

		try
		{
			//Le o audio uma unica vez para a memoria, ja no formato do
			//misturador: 16 bits, estereo, 44100 Hz
			AudioInputStream source = AudioSystem.getAudioInputStream(file);
			AudioFormat format = source.getFormat();
			AudioFormat wide = new AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, false);

			if (!wide.matches(format))
			{
				source = AudioSystem.getAudioInputStream(wide, source);
			}

			byte[] data = readAll(source);
			source.close();
			samples = convert(data, format.getChannels(), format.getSampleRate());

			//A linha da placa de som abre com o primeiro som carregado - o unico
			//custo que sobrou, uns 200 ms -, e nao no primeiro play(): a carga
			//fica atras da tela de loading, o play() esta na cena
			JGAudioMixer.get();
		}
		catch(Exception e)
		{
			//Sem som o jogo continua jogavel: registra e segue
			samples = null;
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
	*Name: convert
	*Description: turns 16-bit little-endian samples of any channel count
	*             and rate into the mixer's stereo at its rate: a mono
	*             sound goes to both sides, anything beyond two channels is
	*             cut to the first two, and another rate is resampled by
	*             linear interpolation
	*Parameters: byte[], int, float
	*Return: short[]
	************************************************************/
	private static short[] convert(byte[] data, int channels, float rate)
	{
		int frames = data.length / (channels * 2);
		short[] stereo = new short[frames * JGAudioMixer.CHANNELS];

		for (int frame = 0; frame < frames; frame++)
		{
			int left = data[frame * channels * 2] & 0xFF | data[frame * channels * 2 + 1] << 8;
			int right = channels > 1 ? data[frame * channels * 2 + 2] & 0xFF | data[frame * channels * 2 + 3] << 8 : left;
			stereo[frame * 2] = (short)left;
			stereo[frame * 2 + 1] = (short)right;
		}

		if (Math.abs(rate - JGAudioMixer.RATE) < 1)
		{
			return stereo;
		}

		int outFrames = (int)((long)frames * JGAudioMixer.RATE / rate);
		short[] out = new short[outFrames * 2];

		for (int frame = 0; frame < outFrames; frame++)
		{
			double at = frame * rate / JGAudioMixer.RATE;
			int before = Math.min(frames - 1, (int)at);
			int after = Math.min(frames - 1, before + 1);
			double part = at - before;

			for (int channel = 0; channel < 2; channel++)
			{
				out[frame * 2 + channel] = (short)(stereo[before * 2 + channel] * (1 - part) + stereo[after * 2 + channel] * part);
			}
		}

		return out;
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
	*Description: configures the volume of sound reproduction. It applies
	*             to what is already sounding too.
	*Parameters: float(0 - 100)
	*Return: None
	************************************************************/
	public void setVolume(float volume)
	{
		this.volume = Math.max(0.0f, Math.min(100.0f, volume));
	}

	/***********************************************************
	*Name: gain
	*Description: the volume as the factor the mixer multiplies the samples
	*             by - the same amplitude the old MASTER_GAIN in decibels
	*             gave, 20 log10 of the fraction
	*Parameters: none
	*Return: float
	************************************************************/
	float gain()
	{
		return volume / 100.0f;
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
		if (samples != null)
		{
			JGAudioMixer.get().start(this, samples, false, voices);
		}
	}

	/***********************************************************
	*Name: loop
	*Description: start sound reproduction in loop, from the beginning,
	*             alone: a track never overlaps itself
	*Parameters: None
	*Return: None
	************************************************************/
	public void loop()
	{
		if (samples != null)
		{
			JGAudioMixer mixer = JGAudioMixer.get();
			mixer.stop(this);
			mixer.start(this, samples, true, 1);
		}
	}

	/***********************************************************
	*Name: isPlaying
	*Description: whether the sound is being heard now
	*Parameters: None
	*Return: boolean
	************************************************************/
	public boolean isPlaying()
	{
		return samples != null && JGAudioMixer.get().isPlaying(this);
	}
	
	/***********************************************************
	*Name: stop
	*Description: stop sound reproduction of every copy
	*Parameters: None
	*Return: None
	************************************************************/
	public void stop()
	{
		if (samples != null)
		{
			JGAudioMixer.get().stop(this);
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
		stop();
		samples = null;
		fileName = null;
	}
}
