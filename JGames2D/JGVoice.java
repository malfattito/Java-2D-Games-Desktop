/***********************************************************************
*Name: JGVoice
*Description: o comando de uma voz solta: uma copia de um som tocando com o
*             volume, o lado e o tom dela, sem depender do som que a criou nem
*             das outras copias dele. E o que faz o som posicional - a mesma
*             batida de motor soando de tres carros ao mesmo tempo, cada um no
*             seu volume e no seu lado, conforme a distancia e o rumo.
*
*             Mexer numa voz que ja acabou nao faz nada e nao quebra: o que se
*             segura aqui e a voz daquele comeco, e nenhuma outra toma o lugar
*             dela.
*Author: Silvano Malfatti
*Date: 15/09/26
************************************************************************/

//Package declaration
package JGames2D;

public final class JGVoice
{
	private final JGAudioMixer.Voice voice;

	JGVoice(JGAudioMixer.Voice voice, float volume, float pan)
	{
		this.voice = voice;
		this.volume = Math.max(0.0f, Math.min(100.0f, volume));
		this.pan = Math.max(-1.0f, Math.min(1.0f, pan));
	}

	/***********************************************************
	*Name: setVolume
	*Description: o quanto esta voz soa, de 0 a 100, sem mexer nas outras
	*Parameters: float
	*Return: None
	************************************************************/
	public void setVolume(float volume)
	{
		apply(volume, pan);
	}

	/***********************************************************
	*Name: setPan
	*Description: de que lado esta voz soa: -1 todo a esquerda, 0 no meio, 1
	*             todo a direita. O que sai de um lado perde do outro, e o
	*             total nao muda - uma voz no meio nao soa mais alta que uma
	*             voz de lado.
	*Parameters: float
	*Return: None
	************************************************************/
	public void setPan(float pan)
	{
		apply(volume, pan);
	}

	/***********************************************************
	*Name: setRate
	*Description: o tom desta voz: 1 e a altura em que o arquivo foi escrito
	*Parameters: float
	*Return: None
	************************************************************/
	public void setRate(float rate)
	{
		voice.rate = Math.max(0.25f, Math.min(4.0f, rate));
	}

	/***********************************************************
	*Name: stop
	*Description: cala esta voz, e so ela
	*Parameters: None
	*Return: None
	************************************************************/
	public void stop()
	{
		JGAudioMixer.get().silence(voice);
	}

	/***********************************************************
	*Name: isPlaying
	*Description: diz se esta voz ainda soa
	*Parameters: None
	*Return: boolean
	************************************************************/
	public boolean isPlaying()
	{
		return JGAudioMixer.get().sounding(voice);
	}

	private float volume = 100.0f;
	private float pan = 0.0f;

	private void apply(float volume, float pan)
	{
		this.volume = Math.max(0.0f, Math.min(100.0f, volume));
		this.pan = Math.max(-1.0f, Math.min(1.0f, pan));

		float level = this.volume / 100.0f;
		float side = (this.pan + 1.0f) / 2.0f;

		//a raiz do quanto cabe a cada lado: assim a voz nao fica mais fraca ao
		//passar pelo meio, que e o que uma divisao simples faria
		voice.gainLeft = level * (float)Math.sqrt(1.0 - side);
		voice.gainRight = level * (float)Math.sqrt(side);
	}
}
