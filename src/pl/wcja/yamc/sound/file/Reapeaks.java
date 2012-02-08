package pl.wcja.yamc.sound.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

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
	private String header = "";
	private int channels = 0;
	private int mipmapsCount = 0;
	private int sourceSampleRate = 0;
	private int sourceLastModified = 0;
	private int sourceFileSize = 0;
	private List<Mipmap> mipmaps = new LinkedList<Reapeaks.Mipmap>();	
	
	public Reapeaks() {
		
	}
	
	public Reapeaks(File reapeaksFile) throws Exception {
		this.readReapeaksFile(reapeaksFile);
	}
	
	public void setReapeaksFile(File rpkf) throws Exception {
		this.readReapeaksFile(rpkf);
	}

	private void readReapeaksFile(File rpkf) throws Exception{
		this.rpkf = rpkf;
		byte[] buf = new byte[4]; 
		raf = new RandomAccessFile(rpkf, "rw");
		raf.read(buf);
		String s = new String(buf);
		if(!s.equalsIgnoreCase("RPKN") && !s.equalsIgnoreCase("RPKM")) {
			throw new IOException("Not a Reapeaks file!");
		}
		header = s;

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
	
	private void writeReapeaksFile(File file) {
		
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

	public List<Mipmap> getMipmaps() {
		return mipmaps;
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
		
//		public Mipmap(int divisionFactor, int countOfPeakSamples, short[] data) {
//			
//		}
//		
//		public Mipmap(ByteBuffer header, ByteBuffer data) {
//			
//		}
		
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
			//there are some differences in v1.0 (RPKM) and v1.1 (RPKN) so...
			int versionMultiplier = rpk.getHeader().equalsIgnoreCase("RPKM") ? 1 : 2;
			buf = new byte[countOfPeakSamples * rpk.getChannels() * versionMultiplier * 2];
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
	}
}
