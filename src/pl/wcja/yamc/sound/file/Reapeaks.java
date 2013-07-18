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

/**
 * Reapeaks file implementation.
 * According to:
 * http://www.reaper.fm/sdk/reapeaks.txt
 * 
 * (Be careful with byte order!)
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
	 * Creates a reapeaks object from given reapeaks file.
	 * Reads and parses the data in file.
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
	 * Creates a reapeaks file from an *audio* stream.
	 * Parses the stream and generates mipmap files.
	 * After generating is completed it writes the file to the
	 * disk to it can be used (readed) later without parsing again.
	 *  
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
	 * Set differend reapeak file to this object.
	 * (do we really need this?)
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
	 * Parse the reapeaks file.
	 * Open the stream, analyze and read the file.
	 * Read and create mipmaps also.
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
	 * Write the reapeaks file (including mipmaps) to 
	 * the disk and close the file.
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
	 * Generates the peaks/mipmaps from given audio stream
	 * considering the dividers.
	 * 
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
	 * Generate a mipmap from audio stream with a division factor given.
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
	
	/**
	 * 
	 * @return
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Returns number of channels
	 * @return
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * 
	 * @return
	 */
	public int getMipmapsCount() {
		return mipmapsCount;
	}

	/**
	 * 
	 * @return
	 */
	public int getSourceSampleRate() {
		return sourceSampleRate;
	}

	/**
	 * 
	 * @return
	 */
	public int getSourceLastModified() {
		return sourceLastModified;
	}

	/**
	 * 
	 * @return
	 */
	public int getSourceFileSize() {
		return sourceFileSize;
	}

	/**
	 * 
	 * @return
	 */
	public int getVersionMultiplier() {
		return versionMultiplier;
	}

	/**
	 * 
	 * @return
	 */
	public List<Mipmap> getMipmaps() {
		return mipmaps;
	}
	
	/**
	 * 
	 * @param samplesPerPixel
	 * @return
	 */
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
	 * Mipmap data object
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
		
		/**
		 * 
		 * @param rpk
		 * @param divisionFactor
		 * @param data
		 */
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

		/**
		 * 
		 * @return
		 */
		public int getDivisionFactor() {
			return divisionFactor;
		}

		/**
		 * 
		 * @return
		 */
		public int getCountOfPeakSamples() {
			return countOfPeakSamples;
		}

		/**
		 * 
		 * @return
		 */
		public short[] getPeaks() {
			return peaks;
		}
		
		/**
		 * 
		 * @param idx
		 * @return
		 */
		public short[] getPeak(int idx) {
			short[] peak = new short[versionMultiplier * channels];
			System.arraycopy(peaks, idx * rpk.getVersionMultiplier() * channels, peak, 0, peak.length);
			return peak;
		}
	}
}
