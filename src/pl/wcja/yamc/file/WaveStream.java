package pl.wcja.yamc.file;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.media.sound.WaveFileReader;

/**
 * Wave stream implementation.
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		10-02-2012
 *
 */
public class WaveStream extends AbstractAudioStream {
	
	private WaveFileReader wfr = new WaveFileReader();
	private AudioInputStream audioInputStream = null;
	
	private byte[] streamBuffer = null;
	private int bytesPerChannel = 0;
	
	/**
	 * 
	 * @param waveFile
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public WaveStream(File waveFile) throws UnsupportedAudioFileException, IOException {
		super(waveFile);
		if(waveFile.length() > 0) {
			audioFileFormat = wfr.getAudioFileFormat(waveFile);
			audioInputStream = wfr.getAudioInputStream(waveFile);
			//precalc some usable values
			bytesPerChannel = audioFileFormat.getFormat().getFrameSize() / audioFileFormat.getFormat().getChannels();
			readStream();
		}
	}
	
	/**
	 * Reads the stream audio data into internal buffer.
	 * 
	 * @throws IOException
	 */
	private void readStream() throws IOException {
		int readed = 0, tmpBufSize = 512 * 1024, dstIdx = 0;
		byte [] tmpBuf = new byte[tmpBufSize];
		streamBuffer = new byte[(int)(audioFileFormat.getFrameLength() * audioFileFormat.getFormat().getFrameSize())];
		while((readed = audioInputStream.read(tmpBuf, 0, tmpBufSize)) != -1) {
			System.arraycopy(tmpBuf, 0, streamBuffer, dstIdx, (tmpBufSize != readed ? readed : tmpBufSize));
			dstIdx += readed;
		}
	}
	
	/**
	 * <p>
	 * Returns the sample in an array of doubles. The array size is equal to channel number.
	 * double[chan1][chan2]...[chanN]
	 * @param sampleNo
	 * @return
	 */
	public double[] getSample(long sampleNo) {
		double[] sample = new double[audioFileFormat.getFormat().getChannels()];
		byte[] frame = getSampleBytes(sampleNo);
		int idx = 0;
		for(int c = 0; c < audioFileFormat.getFormat().getChannels(); c++) {
			for(int b = 0; b < bytesPerChannel; b++) {
				sample[c] += (int)frame[idx++] << (b * 8);
			}
		}
		return sample;
	}
	
	/**
	 * <p> 
	 * Returns a frame/sample at given index (sampleNo) in a form of byte array. 
	 * @param sampleNo
	 * @return raw data of specified frame(sampleNo)
	 */
	private byte[] getSampleBytes(double sampleNo) {
		byte[] frame = new byte[audioFileFormat.getFormat().getFrameSize()];
		System.arraycopy(
				streamBuffer, 
				(int)sampleNo * audioFileFormat.getFormat().getFrameSize(), 
				frame, 
				0, 
				audioFileFormat.getFormat().getFrameSize());
		return frame;
	}

	//TODO maybe change to system.arraycopy?
	@Override 
	public byte[] getRawData(int offset, int length) {
		byte[] frame = new byte[audioFileFormat.getFormat().getFrameSize() * length];
		int idx = 0;
		int from = offset * audioFileFormat.getFormat().getFrameSize();
		int to = from + (audioFileFormat.getFormat().getFrameSize() * length);
		for(int i = from; i < to; i++) {
			frame[idx] = streamBuffer[i];
			idx++;
		}
		return frame;
	}

	@Override
	public double[][] getData(int offset, int length) {
		// TODO Auto-generated method stub
		return null;
	}
}
