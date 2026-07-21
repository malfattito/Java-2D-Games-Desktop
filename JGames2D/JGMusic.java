/***********************************************************************
*Name: JGMusic
*Description:singleton class used to reproduce background musics
*Author: Silvano Malfatti
*Date: 01/05/20
************************************************************************/

//Package declaration
package JGames2D;

//Used packages
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

//Interface declaration
interface AVAudioPlayerDelegate 
{
    public void audioPlayerDidFinishPlaying(JGMusic player, boolean successfully);
    public void audioPlayerDecodeErrorDidOccur(JGMusic player);
    public void audioPlayerBeginInterruption(JGMusic player);
    public void audioPlayerEndInterruption(JGMusic player);
}

public class JGMusic 
{
	//Internal Class
	class Listener implements BasicPlayerListener 
	{
		//Class Atributtes
        private JGMusic avAudioPlayer;
        
        /***********************************************************
    	*Name: Listener
    	*Description: implements the events listenner of sounds
    	*Parameters: JGSound
    	*Return: None
    	************************************************************/
        Listener(JGMusic avAudioPlayer) 
        {
            this.avAudioPlayer = avAudioPlayer;
        }
        
        /***********************************************************
    	*Name:opened
    	*Description: sounds is ready to play, properties sound info
    	*Parameters: Object, Map
    	*Return: none
    	************************************************************/
        public void opened(Object stream, Map properties) 
        {
        }

        /***********************************************************
    	*Name:progress
    	*Description: while sound is playing, infos about reprodution
    	*Parameters: int, long, byte[], Map
    	*Return: none
    	************************************************************/
        public void progress(int bytesRead, long microseconds, byte[] pcmData, Map properties) 
        {
        }

        /***********************************************************
    	*Name:setController
    	*Description: a handle to basicplayer, help to control the player
    	*Parameters: BasicController
    	*Return: none
    	************************************************************/
        public void setController(BasicController controller) 
        {
        }

        /***********************************************************
    	*Name:stateUpdated
    	*Description: receive notification about change state of sound
    	*Parameters: BasiPlayerEvent
    	*Return: none
    	************************************************************/
        public void stateUpdated(BasicPlayerEvent event) 
        {
            if (event.getCode() == BasicPlayerEvent.STOPPED)
            {
                try {
                    if (!stopRequested) {
                        if (url != null) {
                            player.open(url);
                        } else {
                            ByteArrayInputStream bis = new ByteArrayInputStream(data);
                            player.open(bis);
                        }
                    }
                    if (!stopRequested && loopsLeft != 0) {
                        if (loopsLeft > 0) {
                            loopsLeft--;
                        }
                        player.play();
                    } else {
                        playing = false;
                        if (delegate != null) {
                            delegate.audioPlayerDidFinishPlaying(avAudioPlayer, true);
                        }
                    }
                } catch (BasicPlayerException exc) {
                    System.err.println("Unable to start next playback loop: " + exc.getMessage());
                    exc.printStackTrace();
                }
            }
        }
    };
	
    //Class attributes
	 private int                   numberOfLoops = 0;
	 private int                   loopsLeft     = 0;
	 private BasicPlayer           player        = null;
	 private boolean               stopRequested = false;
	 private URL                   url           = null;
	 private AVAudioPlayerDelegate delegate      = null;
	 private boolean               playing       = false;
	 private byte[]                data          = null;


	/***********************************************************
	*Name: JGSound
	*Description: private constructor
	*Parameters: URL
	*Return: None
	************************************************************/
    private JGMusic(URL url) throws BasicPlayerException 
    {
        this.url = url;
        player = new BasicPlayer();
        player.addBasicPlayerListener(new Listener(this));

        // If this marker appears in the path, then the resource needs to be
        // loaded out of a JAR file. We expect it in this case to be the JAR
        // file the app is run from.
        final String IN_JAR_MARKER = ".jar!";

        String urlStr = url.toString();
        // If the URL appears to be inside the jar the app is run from, then
        // load it via getResourceAsStream().
        if (urlStr.contains(IN_JAR_MARKER)) {
            int startOfResource = urlStr.indexOf(IN_JAR_MARKER) + IN_JAR_MARKER.length();
            player.open(JGMusic.class.getResourceAsStream(urlStr.substring(startOfResource)));
        } else {
            player.open(this.url);
        }
    }
    
    /***********************************************************
	*Name: JGMusic
	*Description: returns the name of the music
	*Parameters: URL
	*Return: None
	************************************************************/
    public String getMusicName()
    {
       return url.getPath();
    }

    /***********************************************************
	*Name: JGSound
	*Description: private constructor
	*Parameters: FileDescriptior, long, long
	*Return: None
	************************************************************/
    private JGMusic(FileDescriptor fd, long offset, long length) throws BasicPlayerException,
            IOException 
    {

        FileInputStream fis = new FileInputStream(fd);
        if (offset > 0) {
            fis.skip(offset);
        }

        data = new byte[(int) length];
        fis.read(data, 0, (int) length);

        ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, (int) length);
        player = new BasicPlayer();
        player.addBasicPlayerListener(new Listener(this));
        player.open(bis);
        fis.close();
    }

    /*******************************************
	* Name initWithContestsofURL
	* Description: create a new sound object
	* Parameters: URL
	* Returns: JGSound
	******************************************/
    static JGMusic initWithContentsOfURL(URL url) 
    {
        try {
            JGMusic player = new JGMusic(url);
            return player;
        } catch (BasicPlayerException exc) {
           
            return null;
        }
    }

    /*******************************************
	* Name initWithContestsOfFileDescriptor
	* Description: create a new sound object
	* Parameters: FileDescriptor, long, long
	* Returns: JGSound
	******************************************/
    public static JGMusic initWithContentsOfFileDescriptor(FileDescriptor fd, long offset,
            long length)
    {
        try {
            JGMusic player = new JGMusic(fd, offset, length);
            return player;
        } catch (BasicPlayerException exc) {
            return null;
        } catch (IOException exc) {
            return null;
        }
    }

    /*******************************************
   	* Name play
   	* Description: reproduces the current sound
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void play()
    {
        try {
            stopRequested = false;
            if (player.getStatus() == BasicPlayer.PAUSED) {
                player.resume();
            } else {
                loopsLeft = numberOfLoops;
                player.play();
            }
            playing = true;
        } catch (BasicPlayerException exc) {
            System.err.println("Unable to start playback: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /*******************************************
   	* Name stop
   	* Description: stop the sound reprodution
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void stop() {
        try {
            stopRequested = true;
            player.stop();
            playing = false;
        } catch (BasicPlayerException exc) {
            System.err.println("Unable to stop playback: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /*******************************************
   	* Name pause
   	* Description: repause the sound reprodution
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void pause() {
        try {
            player.pause();
            playing = false;
        } catch (BasicPlayerException exc) {
            System.err.println("Unable to pause playback: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /*******************************************
   	* Name getNumberOfLoops
   	* Description: returns the number of repetitions
   	* Parameters: none
   	* Returns: int
   	******************************************/
    public int getNumberOfLoops()
    {
        return this.numberOfLoops;
    }

    /*******************************************
   	* Name setNumberOfLoops
   	* Description: define the number of repetitions
   	* Parameters: int
   	* Returns: none
   	******************************************/
    public void setNumberOfLoops(int numberOfLoops)
    {
        this.loopsLeft = numberOfLoops;
        this.numberOfLoops = numberOfLoops;
    }

    /*******************************************
   	* Name getDelegate
   	* Description: returns the sound delegate
   	* Parameters:none
   	* Returns: oAVAudioPlayerDelegate
   	******************************************/
    public AVAudioPlayerDelegate getDelegate()
    {
        return this.delegate;
    }

    /*******************************************
   	* Name setDelegate
   	* Description: sets the sound delegate
   	* Parameters: AVAudioPlayerDelegate
   	* Returns: none
   	******************************************/
    public void setDelegate(AVAudioPlayerDelegate delegate)
    {
        this.delegate = delegate;
    }

    /*******************************************
   	* Name isPlaying
   	* Description: none
   	* Returns: boolean
   	******************************************/
    public boolean isPlaying() 
    {
        return playing;
    }

    /*******************************************
   	* Name setVolume
   	* Description: sets the sound volume 0.0 <-> 1.0 
   	* Parameters: float
   	* Returns: none
   	******************************************/
    public void setVolume(float volume) 
    {
        try {
            player.setGain(volume);
        } catch (BasicPlayerException exc) {
            System.err.println("Unable to set volume: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    /*******************************************
   	* Name getVolume
   	* Description: returns the sound volume
   	* Parameters: none
   	* Returns: float
   	******************************************/
    public float getVolume() 
    {
        return player.getGainValue();
    }
    
    /*******************************************
   	* Name: free
   	* Description: free resources
   	* Parameters: none
   	* Returns: none
   	******************************************/
    public void free() 
    {
    	//A thread do BasicPlayer continuaria tocando sem o stop
    	if (player != null)
    	{
    		stop();
    	}

    	player        = null;
   	 	url           = null;
   	 	delegate      = null;
   	 	data          = null;
    }
}