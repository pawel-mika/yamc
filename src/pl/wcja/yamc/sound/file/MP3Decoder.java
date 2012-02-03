package pl.wcja.yamc.sound.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javazoom.jl.converter.Converter;
import javazoom.jl.converter.Converter.ProgressListener;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.Obuffer;
import pl.wcja.yamc.event.ProgressEvent;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MP3Decoder extends Decoder { 
	
	public MP3Decoder() {
//		http://www.rgagnon.com/javadetails/java-0487.html
//		MimetypesFileTypeMap.setDefaultFileTypeMap(filetypemap)
	}
	
	public File decode(final File fileIn, final File fileOut, final pl.wcja.yamc.event.ProgressListener progressListener) throws JavaLayerException, InterruptedException {
		final Object o = new Object();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Converter c = new Converter();
				try {
					final FileInputStream fis = new FileInputStream(fileIn);
					final int total = fis.available(); 
					ProgressListener pl = new ProgressListener() {
						@Override
						public void readFrame(int i, Header header) {
						}
						@Override
						public void parsedFrame(int i, Header header) {
						}
						@Override
						public void decodedFrame(int i, Header header, Obuffer obuffer) {
							try {
								progressListener.progressChanged(new ProgressEvent(this, "", total, total - fis.available()));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						@Override
						public void converterUpdate(int i, int j, int k) {
						}
						@Override
						public boolean converterException(Throwable throwable) {
							return false;
						}
					};
					c.convert(fis, fileOut.getAbsolutePath(), pl, null);
					fis.close();
				} catch (JavaLayerException e) {
					fileOut.delete();
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				progressListener.progressChanged(new ProgressEvent(this, "", -1, -1));
				synchronized(o) {
					o.notifyAll();
				}
			}
		});
		t.start();
		synchronized(o) {
			o.wait();
			if(fileOut.exists()) {
				return fileOut;	
			} else {
				return null;			
			}			
		}
	}

	@Override
	public boolean isAcceptableFiletype(File file) {
		//narazie prosto...
		String substring = file.getName().substring(file.getName().lastIndexOf('.'));
		if(substring.equalsIgnoreCase(".mp3")) {
			return true;
		}
		return false;
	}
}