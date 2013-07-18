package pl.wcja.yamc.file;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		15-07-2013
 *
 */
public class MP3Stream extends AbstractAudioStream {
	
	private MpegAudioFileReader reader = null;
	private AudioInputStream audioInputStream = null;
	
	private byte[] streamBuffer = null;
	private int bytesPerChannel = 0;

	public MP3Stream(File file) throws UnsupportedAudioFileException, IOException {
		super(file);
		if(file != null && file.length() > 0) {
			AudioFileFormat aff = AudioSystem.getAudioFileFormat(file);
			reader = new MpegAudioFileReader();
			audioFileFormat = reader.getAudioFileFormat(file);
			audioInputStream = reader.getAudioInputStream(file);
			bytesPerChannel = audioFileFormat.getFormat().getFrameSize() / audioFileFormat.getFormat().getChannels();
//			readStream();
		}
	}

	@Override
	public double[] getSample(long index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getRMSSample(long fromIndex, long toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getRawData(int sampleOffset, int sampleLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] getData(int offset, int length) {
		// TODO Auto-generated method stub
		return null;
	}

}
