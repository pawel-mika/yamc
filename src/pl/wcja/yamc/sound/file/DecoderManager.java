package pl.wcja.yamc.sound.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import pl.wcja.yamc.event.ProgressListener;
import pl.wcja.yamc.file.MP3Stream;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 * 
 */
public class DecoderManager {

	private static List<Decoder> registeredDecoders = new LinkedList<Decoder>();

	private static DecoderManager instance = new DecoderManager();

	private DecoderManager() {
		registeredDecoders = new LinkedList<Decoder>();
		registeredDecoders.add(new MP3Decoder());
	}

	public static DecoderManager getInstance() {
		return instance;
	}

	public File decode(final File in, final File out, final ProgressListener pl) throws Exception {
		for (Decoder d : registeredDecoders) {
			if (d.isAcceptableFiletype(in)) {
//				MP3Stream TEST = new MP3Stream(in);
				return d.decode(in, out, pl);
			}
		}
		return null;
	}

}
