package pl.wcja.yamc.sound.file;

import java.io.File;

import pl.wcja.yamc.event.ProgressListener;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public abstract class Decoder {

	public abstract boolean isAcceptableFiletype(File file);
	
	public abstract File decode(File fileIn, File fileOut, ProgressListener pl) throws Exception;
}
