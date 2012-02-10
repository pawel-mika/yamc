package pl.wcja.yamc.file;

import java.io.File;

import com.sun.media.sound.WaveFileReader;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		10-02-2012
 *
 */
public class WaveStream extends AbstractAudioStream {
	
	WaveFileReader wfr = new WaveFileReader();
	
	public WaveStream(File waveFile) {
		
	}
}
