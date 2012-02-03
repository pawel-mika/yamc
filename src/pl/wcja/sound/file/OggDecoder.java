package pl.wcja.sound.file;

import java.io.File;

import pl.wcja.event.ProgressListener;

public class OggDecoder extends Decoder {

	public OggDecoder() {
//		http://www.rgagnon.com/javadetails/java-0487.html
//		MimetypesFileTypeMap.setDefaultFileTypeMap(filetypemap)
	}

	@Override
	public boolean isAcceptableFiletype(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File decode(File fileIn, File fileOut, ProgressListener pl) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
