/***********************************************************************
*Name: JGAudioMixer
*Description: one line of the sound card, opened and started once, fed by
*             a thread that adds up every sound playing at the moment. The
*             sounds are arrays of samples in memory: starting one is
*             putting it on the list and stopping it is taking it off,
*             which costs nothing to whoever asks. Measured on macOS,
*             starting or stopping a Clip of javax.sound costs the calling
*             thread 120 to 250 ms, every time and for every clip, and the
*             game thread froze at every flourish, every music coming in
*             and every change of screen. A looped sound is written back
*             to back, sample after sample, so the join is seamless.
*Author: Silvano Malfatti
*Date: 14/09/26
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

final class JGAudioMixer implements Runnable
{
	//The format every sound is converted to when it is loaded
	static final int RATE = 44100;
	static final int CHANNELS = 2;
	static final AudioFormat FORMAT = new AudioFormat(RATE, 16, CHANNELS, true, false);

	//How far ahead the line is fed, which is the most a sound waits between
	//play() and being heard, and the block the thread mixes at a time
	private static final int LINE_FRAMES = 2048;
	private static final int BLOCK_FRAMES = 512;

	//How many sounds can play at once, all effects and tracks together
	private static final int MOST_VOICES = 32;

	//One mixer for the whole game, made on the first sound loaded
	private static JGAudioMixer instance = null;

	//Class attributes
	private SourceDataLine line = null;
	private Thread thread = null;
	private volatile boolean running = false;

	//The voices playing now, guarded by the mixer itself
	private final Voice[] voices = new Voice[MOST_VOICES];
	private int voiceCount = 0;
	private long serial = 0;

	//The block being mixed, summed wide and then packed for the line
	private final int[] sum = new int[BLOCK_FRAMES * CHANNELS];
	private final byte[] block = new byte[BLOCK_FRAMES * CHANNELS * 2];

	/***********************************************************
	*Name: Voice
	*Description: one sound playing: whose it is, where it is in its samples
	*             and whether it starts over at the end
	************************************************************/
	static final class Voice
	{
		JGSoundEffect owner = null;
		short[] samples = null;

		//onde a voz esta nas amostras, em quadros e com casa decimal: o passo
		//de leitura e o tom, e um tom que nao seja o do arquivo cai entre dois
		//quadros
		double position = 0;
		boolean loop = false;
		long started = 0;

		//Uma voz comum segue o volume e o tom do som que a criou: e o caso de
		//quase tudo, e e o que deixa um som inteiro subir ou descer de uma vez.
		//Uma voz livre carrega os seus, e e o que permite o mesmo arquivo soar
		//varias vezes ao mesmo tempo em volumes, lados e tons diferentes - os
		//carros da rua, cada um na sua distancia.
		boolean own = true;
		volatile float gainLeft = 1.0f;
		volatile float gainRight = 1.0f;
		volatile float rate = 1.0f;
	}

	/***********************************************************
	*Name: get
	*Description: the mixer, opened on first use
	*Parameters: none
	*Return: JGAudioMixer
	************************************************************/
	static synchronized JGAudioMixer get()
	{
		if (instance == null)
		{
			instance = new JGAudioMixer();
		}

		return instance;
	}

	/***********************************************************
	*Name: JGAudioMixer
	*Description: constructor. Opens and starts the line once - the one
	*             start that costs - and starts the thread that feeds it.
	*Parameters: none
	*Return: none
	************************************************************/
	private JGAudioMixer()
	{
		try
		{
			line = AudioSystem.getSourceDataLine(FORMAT);
			line.open(FORMAT, LINE_FRAMES * FORMAT.getFrameSize());
			line.start();
			running = true;
			thread = new Thread(this, "JGAudioMixer");
			thread.setDaemon(true);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		catch (Exception e)
		{
			//Sem som o jogo continua jogavel: registra e segue
			line = null;
			JGLog.writeLog("ERROR AUDIO MIXER: " + e + "\n");
		}
	}

	/***********************************************************
	*Name: isOpen
	*Description: whether the sound card answered
	*Parameters: none
	*Return: boolean
	************************************************************/
	boolean isOpen()
	{
		return line != null;
	}

	/***********************************************************
	*Name: start
	*Description: starts a sound playing. A sound is allowed a number of
	*             voices at once; past that, its oldest voice gives way, and
	*             with the mixer full the oldest of all does.
	*Parameters: JGSoundEffect, short[], boolean, int
	*Return: none
	************************************************************/
	synchronized Voice start(JGSoundEffect owner, short[] samples, boolean loop, int mostOfOwner)
	{
		if (line == null || samples == null || samples.length == 0)
		{
			return null;
		}

		int mine = 0;
		int oldest = -1;

		for (int index = 0; index < voiceCount; index++)
		{
			if (voices[index].owner == owner)
			{
				mine++;

				if (oldest < 0 || voices[index].started < voices[oldest].started)
				{
					oldest = index;
				}
			}
		}

		if (mine >= mostOfOwner)
		{
			remove(oldest);
		}
		else if (voiceCount == MOST_VOICES)
		{
			oldest = 0;

			for (int index = 1; index < voiceCount; index++)
			{
				if (voices[index].started < voices[oldest].started)
				{
					oldest = index;
				}
			}

			remove(oldest);
		}

		Voice voice = new Voice();
		voice.owner = owner;
		voice.samples = samples;
		voice.position = 0;
		voice.loop = loop;
		voice.started = serial++;
		voices[voiceCount++] = voice;

		return voice;
	}

	/***********************************************************
	*Name: free
	*Description: poe uma voz solta a tocar - com volume, lado e tom proprios,
	*             fora do comando do som que a criou -, sem o limite por som:
	*             varias copias do mesmo arquivo soando ao mesmo tempo sao
	*             justamente o caso de uso, e o teto de vozes do misturador
	*             continua valendo
	*Parameters: JGSoundEffect, short[], boolean
	*Return: Voice, ou null
	************************************************************/
	synchronized Voice free(JGSoundEffect owner, short[] samples, boolean loop,
	                        float gainLeft, float gainRight, float rate)
	{
		//tudo dentro do mesmo trinco em que a mistura entra: a voz nasce ja no
		//volume e no tom que lhe cabem, e nao ha o quadro em que ela soaria no
		//volume de outra pessoa
		Voice voice = start(owner, samples, loop, MOST_VOICES);

		if (voice != null)
		{
			voice.own = false;
			voice.gainLeft = gainLeft;
			voice.gainRight = gainRight;
			voice.rate = rate;
		}

		return voice;
	}

	/***********************************************************
	*Name: silence
	*Description: cala uma voz sozinha, sem mexer nas outras do mesmo som
	*Parameters: Voice
	*Return: none
	************************************************************/
	synchronized void silence(Voice voice)
	{
		for (int index = voiceCount - 1; index >= 0; index--)
		{
			if (voices[index] == voice)
			{
				remove(index);
				return;
			}
		}
	}

	/***********************************************************
	*Name: sounding
	*Description: diz se uma voz ainda esta na lista
	*Parameters: Voice
	*Return: boolean
	************************************************************/
	synchronized boolean sounding(Voice voice)
	{
		for (int index = 0; index < voiceCount; index++)
		{
			if (voices[index] == voice)
			{
				return true;
			}
		}

		return false;
	}

	/***********************************************************
	*Name: stop
	*Description: silences every voice of a sound
	*Parameters: JGSoundEffect
	*Return: none
	************************************************************/
	synchronized void stop(JGSoundEffect owner)
	{
		for (int index = voiceCount - 1; index >= 0; index--)
		{
			if (voices[index].owner == owner)
			{
				remove(index);
			}
		}
	}

	/***********************************************************
	*Name: isPlaying
	*Description: whether any voice of a sound is playing
	*Parameters: JGSoundEffect
	*Return: boolean
	************************************************************/
	synchronized boolean isPlaying(JGSoundEffect owner)
	{
		for (int index = 0; index < voiceCount; index++)
		{
			if (voices[index].owner == owner)
			{
				return true;
			}
		}

		return false;
	}

	private void remove(int index)
	{
		voices[index] = voices[voiceCount - 1];
		voices[voiceCount - 1] = null;
		voiceCount--;
	}

	/***********************************************************
	*Name: run
	*Description: the thread: mixes a block and writes it to the line, which
	*             only takes it when there is room - that is what paces it
	*Parameters: none
	*Return: none
	************************************************************/
	public void run()
	{
		while (running)
		{
			mix();
			line.write(block, 0, block.length);
		}
	}

	/***********************************************************
	*Name: mix
	*Description: adds every voice into the block at its volume and at its
	*             pitch, moves each one on, drops the ones that ended, and packs
	*             the sum into 16-bit samples, clipped
	*Parameters: none
	*Return: none
	************************************************************/
	private void mix()
	{
		for (int index = 0; index < sum.length; index++)
		{
			sum[index] = 0;
		}

		synchronized (this)
		{
			for (int index = voiceCount - 1; index >= 0; index--)
			{
				Voice voice = voices[index];
				float left = voice.own ? voice.owner.gain() : voice.gainLeft;
				float right = voice.own ? voice.owner.gain() : voice.gainRight;
				double rate = voice.own ? voice.owner.rate() : voice.rate;
				short[] samples = voice.samples;
				int frames = samples.length / CHANNELS;
				double at = voice.position;
				boolean ended = false;

				for (int frame = 0; frame < BLOCK_FRAMES; frame++)
				{
					if (at >= frames)
					{
						if (!voice.loop)
						{
							ended = true;
							break;
						}

						at -= frames * Math.floor(at / frames);
					}

					//entre dois quadros: a amostra sai da reta entre eles, que e
					//o que deixa o tom variar sem o serrilhado de repetir e
					//pular amostras
					int first = (int)at;
					double part = at - first;
					int second = first + 1 < frames ? first + 1 : (voice.loop ? 0 : first);

					for (int channel = 0; channel < CHANNELS; channel++)
					{
						double sample = samples[first * CHANNELS + channel] * (1.0 - part)
						                + samples[second * CHANNELS + channel] * part;

						sum[frame * CHANNELS + channel] += (int)(sample * (channel == 0 ? left : right));
					}

					at += rate;
				}

				voice.position = at;

				if (ended)
				{
					remove(index);
				}
			}
		}

		for (int index = 0; index < sum.length; index++)
		{
			int value = Math.max(-32768, Math.min(32767, sum[index]));
			block[index * 2] = (byte)value;
			block[index * 2 + 1] = (byte)(value >> 8);
		}
	}

	/***********************************************************
	*Name: close
	*Description: stops the thread and gives the line back. Called when the
	*             game shuts its sounds down.
	*Parameters: none
	*Return: none
	************************************************************/
	static synchronized void close()
	{
		if (instance == null)
		{
			return;
		}

		JGAudioMixer mixer = instance;
		instance = null;
		mixer.running = false;

		if (mixer.line != null)
		{
			mixer.line.stop();
			mixer.line.close();
		}
	}
}
