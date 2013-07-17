package pl.wcja.yamc.sound.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import pl.wcja.yamc.file.AbstractAudioStream;

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
	
	private Logger logger = Logger.getLogger(getClass());
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
	private int[] dividers = new int[] {110, 4410, 44100};
		
	/**
	 * 
	 * @param reapeaksFile
	 * @throws IOException 
	 * @throws Exception
	 */
	public Reapeaks(File reapeaksFile) throws IOException {
		rpkf = reapeaksFile;
		if(rpkf.length() > 0) {
			readReapeaksFile();
		}
	}
	
	/**
	 * @throws IOException 
	 * @throws Exception 
	 * 
	 */
	public Reapeaks(AbstractAudioStream aas) throws IOException {
		String name = aas.getFile().getAbsolutePath() + ".reapeaks";
		rpkf = new File(name);
		if(rpkf.exists()) {
			logger.info(String.format("Reading reapeaks file: %s", name));
			readReapeaksFile();
			return;
		}
		logger.info(String.format("Creating reapeaks file: %s", name));
		generateReapeaks(aas);
		//write the file
		writeReapeaksFile();
	}
	
	/**
	 * 
	 * @param rpkf
	 * @throws IOException 
	 * @throws Exception
	 */
	public void setReapeaksFile(File rpkf) throws IOException  {
		this.rpkf = rpkf;
		if(this.rpkf.length() > 0) {
			readReapeaksFile();
		}
	}

	/**
	 * 
	 * @throws IOException 
	 * @throws Exception
	 */
	private void readReapeaksFile() throws IOException {
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
	private void writeReapeaksFile() throws IOException {
		raf = new RandomAccessFile(rpkf, "rw");
		raf.writeBytes(header);
		raf.write((byte)channels);
		raf.write((byte)mipmapsCount);
		byte[] buf = new byte[4 * 3];
		ByteBuffer headerbb = ByteBuffer.wrap(buf);
		headerbb.order(ByteOrder.LITTLE_ENDIAN);
		headerbb.putInt(sourceSampleRate);
		headerbb.putInt(sourceLastModified);
		headerbb.putInt(sourceFileSize);
		raf.write(buf);
		//write mipmaps and its sizes (later on we can calculate reading offset basing on this)
		//buf = new byte[mipmapsCount * mipmapHeaderSize];
		ByteBuffer mipmapHeaderbb = ByteBuffer.allocate(mipmapsCount * mipmapHeaderSize);
		mipmapHeaderbb = mipmapHeaderbb.order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer peaksbb = null;
		Vector<ByteBuffer> buffers = new Vector<>();
		for(Mipmap m : mipmaps) {
			peaksbb = ByteBuffer.allocate(m.getPeaks().length * 2);
			peaksbb = peaksbb.order(ByteOrder.LITTLE_ENDIAN);
			mipmapHeaderbb.putInt(m.getDivisionFactor());
			mipmapHeaderbb.putInt(m.getPeaks().length / (channels * versionMultiplier));
			for(short s : m.getPeaks()) {
				peaksbb.putShort(s);
			}
			buffers.add(peaksbb);
			peaksbb.flip();
		}
		raf.write(mipmapHeaderbb.array());
		for(ByteBuffer b : buffers) {
			raf.getChannel().write(b);
		}
		raf.close();
	}
	
	/**
	 * Generates 
	 * @throws Exception
	 */
	public void generateReapeaks(AbstractAudioStream aas) throws IOException {
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
		mipmapsCount = mipmaps.size();
	}
	
	/**
	 * 
	 * @param aas
	 * @param divisionFactor in frames (samples)
	 * @return
	 */
	private Mipmap generateMipmap(AbstractAudioStream aas, int divisionFactor) {
		logger.info(String.format("Generating mipmap for file %s at %s division factor", aas.getFile().getName(), divisionFactor));
		int frameLength = aas.getAudioFileFormat().getFrameLength();
		short[] data = new short[(int)(((frameLength / divisionFactor) + 1) * channels * versionMultiplier)];
		int mipmapIdx = 0, srcIdx = 0, dstIdx = 0;
		boolean bigEndian = aas.getAudioFileFormat().getFormat().isBigEndian();
		for(int i = 0; i < frameLength; i += divisionFactor) {
			int toGet = frameLength - i > divisionFactor ?
					divisionFactor :
					frameLength - i;	
			byte[] piece = aas.getRawData(i, toGet);
			ShortBuffer shortBuffer;
			if(bigEndian) {
				shortBuffer = ByteBuffer.wrap(piece).asShortBuffer();
			} else {
				shortBuffer = ByteBuffer.wrap(piece).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();	
			}
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
		logger.info(String.format("Generated %s peaks (%s shorts)", (data.length/channels)/versionMultiplier, data.length));
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
		Mipmap toReturn = null;
		for(Mipmap m : mipmaps) {
			if(samplesPerPixel > m.getDivisionFactor()) {
				toReturn = m;
			}
		}
		return toReturn;
	}
	
	/**
	 * 
	 * @param divider
	 * @return
	 */
	private Mipmap getMipmapFor(int divider) {
		for(Mipmap m : mipmaps) {
			if(m.divisionFactor == divider) {
				return m;
			}
		}
		return null;
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
		 * @throws IOException 
		 * @throws Exception
		 */
		public Mipmap(Reapeaks rpk, RandomAccessFile rpkf, long headerStart, long dataStart) throws IOException {
			this.rpk = rpk;
			readMipmap(rpkf, headerStart, dataStart);
		}
		
		/**
		 * 
		 * @param rpkf
		 * @param headerStart
		 * @param dataStart
		 * @throws IOException 
		 * @throws Exception
		 */
		private void readMipmap(RandomAccessFile rpkf, long headerStart, long dataStart) throws IOException {
			byte[] buf = new byte[mipmapHeaderSize];
			ByteBuffer bb = ByteBuffer.wrap(buf);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			rpkf.seek(headerStart);
			rpkf.read(buf);
			divisionFactor = bb.getInt();
			countOfPeakSamples = bb.getInt();
			buf = new byte[countOfPeakSamples * rpk.getChannels() * rpk.getVersionMultiplier() * 2];
			bb = ByteBuffer.wrap(buf);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			rpkf.seek(dataStart);
			rpkf.read(buf);
			int i = 0;
			peaks = new short[buf.length / 2];
			while(bb.position() < buf.length) {
				peaks[i++] = bb.getShort();
			}
			getClass();
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
