package pl.wcja.yamc.sound.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import pl.wcja.yamc.file.AbstractAudioStream;
import pl.wcja.yamc.utils.SoundUtils;

import com.sun.media.sound.WaveFileReader;

/**
 * According to:
 * http://www.reaper.fm/sdk/reapeaks.txt
 * 
 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * @date:		08-02-2012
 *
 */
public class Reapeaks {
	
	private File rpkf = null;
	private RandomAccessFile raf = null;
	private int mipmapHeaderSize = 4 * 2;	//two integers
	private String header = "RPKN";
	private int channels = 0;
	private int mipmapsCount = 0;
	private int sourceSampleRate = 0;
	private int sourceLastModified = 0;
	private int sourceFileSize = 0;
	private List<Mipmap> mipmaps = new LinkedList<Reapeaks.Mipmap>();
	private int versionMultiplier;
	private int[] dividers = new int[] {44100, 4410, 110};
		
	/**
	 * 
	 * @param reapeaksFile
	 * @throws Exception
	 */
	public Reapeaks(File reapeaksFile) throws Exception {
		this.rpkf = reapeaksFile;
		if(this.rpkf.length() > 0) {
			this.readReapeaksFile();
		}
	}
	
	/**
	 * 
	 */
	public Reapeaks(AbstractAudioStream aas) {
		String name = aas.getFile().getName().split(".wav")[0];
		name += ".reapeaks";
		this.rpkf = new File(name);
		try {
			generateReapeaks(aas);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param rpkf
	 * @throws Exception
	 */
	public void setReapeaksFile(File rpkf) throws Exception {
		this.rpkf = rpkf;
		if(this.rpkf.length() > 0) {
			this.readReapeaksFile();
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void readReapeaksFile() throws Exception{
		byte[] buf = new byte[4]; 
		raf = new RandomAccessFile(rpkf, "rw");
		raf.read(buf);
		String s = new String(buf);
		if(!s.equalsIgnoreCase("RPKN") && !s.equalsIgnoreCase("RPKM")) {
			throw new IOException("Not a Reapeaks file!");
		}
		header = s;
		versionMultiplier = getHeader().equalsIgnoreCase("RPKM") ? 1 : 2;

		channels = raf.read();
		mipmapsCount = raf.read();
		
		buf = new byte[4 * 3];
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		raf.read(buf);
		sourceSampleRate = bb.getInt();
		sourceLastModified = bb.getInt();
		sourceFileSize = bb.getInt();
		
		long headerOffset = raf.getFilePointer(), dataOffset = headerOffset + (mipmapHeaderSize * mipmapsCount);
		while(dataOffset < raf.length()) {
			Mipmap mm = new Mipmap(this, raf, headerOffset, dataOffset);
			headerOffset += mipmapHeaderSize;
			dataOffset += mm.getPeaks().length * 2; // * 2 - size of short
			mipmaps.add(mm);
		}
		raf.close();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void writeReapeaksFile() throws Exception {
		raf = new RandomAccessFile(rpkf, "rw");
		raf.writeChars(header);
		raf.write((byte)channels);
		raf.write((byte)mipmapsCount);
		byte[] buf = new byte[4 * 3];
		ByteBuffer bb = ByteBuffer.wrap(buf);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(sourceSampleRate);
		bb.putInt(sourceLastModified);
		bb.putInt(sourceFileSize);
		raf.write(buf);
		
		//write mipmaps...
		
		raf.close();
	}
	
	/**
	 * Generates 
	 * @throws Exception
	 */
	public void generateReapeaks(AbstractAudioStream aas) throws Exception {
		WaveFileReader wfr = new WaveFileReader();
		//setup header
		header = "RPKN"; //-> version multiplier = 2
		versionMultiplier = 2;
		channels = aas.getAudioFileFormat().getFormat().getChannels();
		sourceSampleRate = (int)aas.getAudioFileFormat().getFormat().getSampleRate();
		sourceLastModified = (int)aas.getFile().lastModified();
		sourceFileSize = (int)aas.getFile().length();
		mipmaps.clear();
		for(int i = 0; i < dividers.length; i++) {
			mipmaps.add(generateMipmap(aas, dividers[i]));
		}
		//at last - write the file
//		writeReapeaksFile();
	}
	
	/**
	 * 
	 * @param aas
	 * @param divisionFactor in frames (samples)
	 * @return
	 */
	private Mipmap generateMipmap(AbstractAudioStream aas, int divisionFactor) {
		int frameLength = aas.getAudioFileFormat().getFrameLength();
		short[] data = new short[(int)(((frameLength / divisionFactor) + 1) * channels * versionMultiplier)];
		int mipmapIdx = 0, srcIdx = 0, dstIdx = 0;
		for(int i = 0; i < frameLength; i += divisionFactor) {
			int toGet = frameLength - i > divisionFactor ?
					divisionFactor :
					frameLength - i;	
			byte[] piece = aas.getRawData(i, toGet);
			ShortBuffer shortBuffer = ByteBuffer.wrap(piece).asShortBuffer();
			mipmapIdx = (i / divisionFactor) * channels * versionMultiplier;
			//System.out.println(String.format("frameLenght: %s, i: %s, divFactor: %s, data.length: %s, mipmapidx: %s", frameLength, i, divisionFactor, data.length, mipmapIdx));
			for(int j = 0; j < shortBuffer.limit(); j += channels) {
				for(int c = 0; c < channels; c++) {
					if(versionMultiplier == 1) {
						srcIdx = j + c;
						dstIdx = mipmapIdx + c;
						data[dstIdx] = shortBuffer.get(srcIdx) > data[dstIdx] ? shortBuffer.get(srcIdx) : data[dstIdx];
					} else if (versionMultiplier == 2) {
						srcIdx = j + c;
						dstIdx = mipmapIdx + (c * versionMultiplier);
						data[dstIdx] = shortBuffer.get(srcIdx) > data[dstIdx] ? shortBuffer.get(srcIdx) : data[dstIdx];
						data[dstIdx + 1] = shortBuffer.get(srcIdx) < data[dstIdx + 1] ? shortBuffer.get(srcIdx) : data[dstIdx + 1];
					}
				}
			}
		}
		return new Mipmap(this, divisionFactor, data);
	}
	
	public String getHeader() {
		return header;
	}

	public int getChannels() {
		return channels;
	}

	public int getMipmapsCount() {
		return mipmapsCount;
	}

	public int getSourceSampleRate() {
		return sourceSampleRate;
	}

	public int getSourceLastModified() {
		return sourceLastModified;
	}

	public int getSourceFileSize() {
		return sourceFileSize;
	}

	public int getVersionMultiplier() {
		return versionMultiplier;
	}

	public List<Mipmap> getMipmaps() {
		return mipmaps;
	}
	
	public Mipmap getBestMipmapFor(double samplesPerPixel) {
		return mipmaps.get(mipmaps.size() - 1);//temporaty for tests!
	}

	/**
	 * 
	 * @author:		<a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
	 * @date:		08-02-2012
	 *
	 */
	public class Mipmap {
		private Reapeaks rpk = null;
		private int divisionFactor = 0;
		private int countOfPeakSamples = 0;
		private short[] peaks = null;
		
		public Mipmap(Reapeaks rpk, int divisionFactor, short[] data) {
			this.rpk = rpk;
			this.divisionFactor = divisionFactor;
			this.peaks = data;
			versionMultiplier = rpk.getHeader().equalsIgnoreCase("RPKM") ? 1 : 2;
			countOfPeakSamples = data.length / (rpk.getChannels() * versionMultiplier);
		}
		
		/**
		 * 
		 * @param rpk Reapeaks object that Mipmap belongs to
		 * @param rpkf 
		 * @param headerStart
		 * @param dataStart
		 * @throws Exception
		 */
		public Mipmap(Reapeaks rpk, RandomAccessFile rpkf, long headerStart, long dataStart) throws Exception {
			this.rpk = rpk;
			readMipmap(rpkf, headerStart, dataStart);
		}
		
		/**
		 * 
		 * @param rpkf
		 * @param headerStart
		 * @param dataStart
		 * @throws Exception
		 */
		private void readMipmap(RandomAccessFile rpkf, long headerStart, long dataStart) throws Exception {
			byte[] buf = new byte[mipmapHeaderSize];
			ByteBuffer bb = ByteBuffer.wrap(buf);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			rpkf.seek(headerStart);
			rpkf.read(buf);
			divisionFactor = bb.getInt();
			countOfPeakSamples = bb.getInt();
			buf = new byte[countOfPeakSamples * rpk.getChannels() * rpk.getVersionMultiplier() * 2];
			bb = ByteBuffer.wrap(buf);
			rpkf.seek(dataStart);
			rpkf.read(buf);
			int i = 0;
			peaks = new short[buf.length / 2];
			while(bb.position() < buf.length) {
				peaks[i++] = bb.getShort();
			}
		}

		public int getDivisionFactor() {
			return divisionFactor;
		}

		public int getCountOfPeakSamples() {
			return countOfPeakSamples;
		}

		public short[] getPeaks() {
			return peaks;
		}
		
		public short[] getPeak(int idx) {
			short[] peak = new short[versionMultiplier * channels];
			System.arraycopy(peaks, idx * rpk.getVersionMultiplier() * channels, peak, 0, peak.length);
			return peak;
		}
	}
}
