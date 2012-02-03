package pl.wcja.yamc.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;

/**
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		24-10-2011
 *
 */
public class SoundUtils {

	/**
	 * - Might throw an exception if buffers is null/empty...
	 * - All buffers in buffers[][] have to be the same length
	 * - When mixing for example 3 empty (silent) buffers and one with sound,
	 * 	 the output buffer is 3 x silenced...
	 * @param format
	 * @param buffers
	 * @return
	 */
	public static byte[] mixdownBuffers(AudioFormat format, byte[][] buffers) {
		//allocate output buffer of proper size
		int channelSize = format.getFrameSize() / format.getChannels();
		int sampleMaxValue = (int)(Math.pow(2, format.getSampleSizeInBits()));
		int bufferIndex = 0;
		byte[] sample = new byte[channelSize];
		byte[] outBuffer = new byte[buffers[0].length];
		double[] mixBuffer = new double[buffers[0].length / channelSize];
		double dNumber = 0;
		//iterate through buffers, convert to double, normalize to 1.0 and add into mixBuffer
		for(byte[] buffer : buffers) {
			bufferIndex = 0;
			for(int i = 0; i < buffer.length; i += channelSize) {
				System.arraycopy(buffer, i, sample, 0, channelSize);
				//dNumber = fixedPointToDouble(sample, format.isBigEndian()) / sampleHalfSize;
				double s = byteArrayShortToDouble(sample, format.isBigEndian());
				dNumber = s / sampleMaxValue;
				mixBuffer[bufferIndex] += dNumber;
				bufferIndex++;
			}
		}
		//convert back - multiply by sample size, divide by number of buffers (samples added together).
		//the double type should give us a much room ahead to avoid clipping
		bufferIndex = 0;
		for(double d : mixBuffer) {
			short sn = (short)((d * sampleMaxValue) / (buffers.length));
			byte[] b = shortToByteArray(sn, format.isBigEndian());
			System.arraycopy(b, 0, outBuffer, bufferIndex, b.length);
			bufferIndex+=channelSize;
		}
		return outBuffer;
	}
	
	/**
	 * 
	 * @param data
	 * @param format
	 * @return
	 */
	public static double[] mixdownToMono(double[] data, AudioFormat format) {
		double[] out = new double[data.length / format.getChannels()];
		int sampleMaxValue = (int)(Math.pow(2, format.getSampleSizeInBits()));
		int outIndex = 0;
		for(int i = 0; i < data.length; i += format.getChannels()) {
			//here event with 7.1 channels we should have enough space ahead
			//to do the mixdown without normalizing to 1.0
			for(int c = 0; c < format.getChannels(); c++) {
				out[outIndex] += data[i + c];
			}
			out[outIndex] /= format.getChannels();
			outIndex++;
		}
		return out;
	}
	
	/**
	 * Convert audio data from byte array into double array
	 * @param bytes
	 * @param format
	 * @return
	 */
	public static double[] convertToDoubleArray(byte[] bytes, AudioFormat format) {
		int channelSize = format.getFrameSize() / format.getChannels();
		int outIndex = 0;
		double[] out = new double[bytes.length / channelSize];
		byte[] sample = new byte[channelSize];
		for(int i = 0; i < bytes.length; i += channelSize) {
			System.arraycopy(bytes, i, sample, 0, channelSize);
			double s = byteArrayShortToDouble(sample, format.isBigEndian());
			out[outIndex] = s;
			outIndex++;
		}
		return out;
	}
	
	/**
	 * Converts a short number stored in byte[2] into a double number
	 * WARNING! does not perform a size check!
	 * 
	 * @param number
	 * @param bigEndian
	 * @return
	 */
	public static double byteArrayShortToDouble(byte[] number, boolean bigEndian) {
		return (double)ByteBuffer.
				wrap(number).
				order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).
				asShortBuffer().get();
		
	}

	/**
	 * Converts a short number into a byte array considering endianiness (?)
	 * @param number
	 * @param bigEndian
	 * @return
	 */
	public static byte[] shortToByteArray(short number, boolean bigEndian) {
		return bigEndian ? 
				new byte[] {(byte)(number >> 8), (byte)number} : 
				new byte[] {(byte)number, (byte)(number >> 8)};
	}
	
	/**
	 * 
	 * @param buffer
	 * @param bufferNumberSize size of number in buffer byte array
	 * @param isBigEndian
	 * @return
	 */
	public static double[] byteArrayToDoubleArray(byte[] buffer, int bufferNumberSize, boolean isBigEndian) {
		int bufferIndex = 0;
		byte[] sample = new byte[bufferNumberSize];
		double[] mixBuffer = new double[buffer.length / bufferNumberSize];
		for(int i = 0; i < buffer.length; i += bufferNumberSize) {
			System.arraycopy(buffer, i, sample, 0, bufferNumberSize);
			double s = byteArrayShortToDouble(sample, isBigEndian);
			mixBuffer[bufferIndex] = s;
			bufferIndex++;
		}
		return mixBuffer;
	}
	
	/**
	 * 
	 * @param data
	 * @param channels
	 * @return
	 */
	public static double[][] splitChannels(double[] data, int channels) {
		double[][] splitted = new double[channels][data.length / channels];
		for(int c = 0; c < channels; c++) {
			int idx = 0;
			for(int i = c; i < data.length; i+=channels) {
				splitted[c][idx++] = data[i];
			}
		}
		return splitted;
	}
	
	/**
	 * based on: 
	 * http://www.hackchina.com/en/r/124715/FFT.java__html
	 * 
	 * @param data
	 * @param alpha
	 * @return
	 */
	public static double[] applyHanningWindow(double[] data, double alpha) {
		if (alpha <= 0.0 || alpha >= 1.0) {
			throw new IllegalArgumentException("alpha is invalid. Must be between 0 and 1 exclusive.");
		}
		for (int i = 0; i < data.length; i++) {
			data[i] *= alpha - alpha * Math.cos(2 * Math.PI * i / data.length);
		}
		return data;
	}
	
	/**
	 * Applies a blackmann-harris window to a data that is going to be passed to FFT
	 * @param data
	 * @return
	 */
	public static double[] applyBlackmannHarrisWindow(double[] data) {
		double a0 = 0.3635819, a1 = 0.4891775, a2 = 0.1365995, a3 = 0.0106411;
		for (int i = 0; i < data.length; i++) {
			data[i] *= a0 - (a1 * Math.cos(2 * Math.PI * i / data.length)) 
						  + (a2 * Math.cos(4 * Math.PI * i / data.length))
						  - (a3 * Math.cos(6 * Math.PI * i / data.length));
		}
		return data;
	}
	
}
