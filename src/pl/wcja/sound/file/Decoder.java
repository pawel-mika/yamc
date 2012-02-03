package pl.wcja.sound.file;

import java.io.File;

import pl.wcja.event.ProgressListener;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public abstract class Decoder {

	public abstract boolean isAcceptableFiletype(File file);
	
	public abstract File decode(File fileIn, File fileOut, ProgressListener pl) throws Exception;
}
