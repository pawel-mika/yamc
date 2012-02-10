package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import pl.wcja.yamc.event.BufferMixedEvent;
import pl.wcja.yamc.event.MixerListener;
import pl.wcja.yamc.event.PlaybackEvent;
import pl.wcja.yamc.event.PlaybackEvent.State;
import pl.wcja.yamc.event.PlaybackStatusListener;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.frame.ToolBarEntry;
import pl.wcja.yamc.sound.MixerPanel;
import pl.wcja.yamc.sound.Track;
import pl.wcja.yamc.sound.TrackItem;
import pl.wcja.yamc.sound.Tune;
import pl.wcja.yamc.sound.edit.TrackItemPanel;
import pl.wcja.yamc.utils.SoundUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class MFMixer extends MixerPanel implements ToolBarEntry {

	private boolean debugPerTrackFetch = false, debugCompleteFetch = true, debugBufferMix = true;
	private long fetchTime = 0, completeFetchTime = 0, bufferMixTime = 0;
	
	private IMainFrame mf = null;
	private Thread playerThread = null;
	private PlayerRunnable playerRunnable = null;
//	private Map<TrackItem, SourceDataLine> trackItemLines = new HashMap<TrackItem, SourceDataLine>();
	
	private JPanel jpMixer = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JButton jbPlay = new JButton("Play");
	private JButton jbStop = new JButton("Stop");
	
	private TrackItem emptyTrackItem = new TrackItem(new Track("fake empty track"), 0, 0);
	
	private List<PlaybackStatusListener> playbackStatusListeners = new LinkedList<PlaybackStatusListener>();
	private List<MixerListener> mixerListeners = new LinkedList<MixerListener>();
	
	public MFMixer(IMainFrame mf) {
		super();
		this.mf = mf;
		jpMixer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
//		jpMixer.setBackground(Color.white);
		jbPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play();	
			}
		});
		jbStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();	
			}
		});
		jpMixer.add(jbPlay);
		jpMixer.add(jbStop);
	}

	@Override
	public Component getToolbarComponent() {
		return jpMixer; 
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}

	@Override
	public String getMenu() {
		return "Edit";
	}

	@Override
	public String getSubmenu() {
		return "Preferences";
	}

	@Override
	public String getEntryName() {
		return "Mixer";
	}

	@Override
	public void entrySelected() {
		configure();
	}
	
	@Override
	public void play() {
		stop();
		if(!defaultDataLine.isOpen()) {
			playerRunnable = new PlayerRunnable();
			playerThread = new Thread(playerRunnable);
			playerThread.setPriority(Thread.MAX_PRIORITY);
			playerThread.start();
		}
		firePlaybackEvent(new PlaybackEvent(this, State.PLAY));
	}
	
	public void stop() {
		if(playerThread != null && playerRunnable != null && !playerRunnable.isInterrupted()) {
			playerRunnable.interrupt();
			playerThread.interrupt();
			defaultDataLine.drain();
			defaultDataLine.stop();
			defaultDataLine.close();
//			for(SourceDataLine sdl : trackItemLines.values()) {
//				sdl.drain();
//				sdl.stop();
//			}
		} else if(playerThread != null && playerRunnable.isInterrupted()){
			//przy stopie drugi klik spowoduje reset pozycji markera...albo i nie:):P
			mf.getTuneEditor().setMarkerLocation(0);
		}
		firePlaybackEvent(new PlaybackEvent(this, State.STOP));
	}

	/**
	 * 
	 * @param position in seconds
	 * @param frames - samples to get (to get size in bytes have to be multiplied by byte per frame) 
	 * @return
	 */
	private Collection<FetchedSampleData> fetchTune(double position, int frames) {
		completeFetchTime = System.nanoTime();
		Tune tune = mf.getTuneEditor().getTune();
		if(tune != null) {
			synchronized(tune) {
				List<FetchedSampleData> out = new ArrayList<MFMixer.FetchedSampleData>();
				for(Track t : tune.getTracks()) {
					for(TrackItem ti : t.getItems()) {
						TrackItemPanel tip = mf.getTuneEditor().getTrackItemPanel(ti);
						if(includeTrackItemPanelInMix(position, frames, tip)) {
							if(debugPerTrackFetch) {
								fetchTime = System.nanoTime();
								System.out.print(String.format("Fetching %s for %s samples from %s", tip.getWaveFile().getName(), frames, position));
							}
							byte[] buf = new byte[frames * mixAudioFormat.getFrameSize()];
							fetchTrackItemPanel(buf, tip, position, frames, mixAudioFormat.getFrameSize());
							out.add(new FetchedSampleData(ti, tip.getAudioFileFormat().getFormat().getChannels(), buf));
							if(debugPerTrackFetch) {
								long l = System.nanoTime() - fetchTime;
								System.out.println(String.format(" - done in: %sns (%s\u00B5s, %ss)", l, l / 1000, (double)l / 1000000));
							}
						} else {
							//get empty silent buffer...
//							byte[] buf = new byte[frames * mixAudioFormat.getFrameSize()];
//							out.add(new FetchedSampleData(ti, mixAudioFormat.getChannels(), buf));
						}
					}
				}
				if(debugCompleteFetch) {
					long l = System.nanoTime() - completeFetchTime;
					System.out.println(String.format("Complete fetching done in: %sns (%s\u00B5s, %sms, %ss)", l, l / 1000, (double)l / 1000000, (double)l / 1000000000));
				}
				//if no fetched data - add empty track to keep datalines busy;)
				if(out.isEmpty()) {
					out.add(new FetchedSampleData(emptyTrackItem, 2, new byte[frames * mixAudioFormat.getFrameSize()]));
				}
				return out;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param tip
	 * @param startTime
	 * @param frames
	 * @return
	 */
	private void fetchTrackItemPanel(byte[] fetchBuffer, TrackItemPanel tip, double startTime, int frames, int frameSize) {
		//moze przeliczyc wszystko na ramki zamiast czasu?
		double endTime = startTime + samplesToTime(frames);
		double sampleStart = tip.getTrackItem().getTimeFrom();
		double sampleEnd = tip.getTrackItem().getTimeTo();
		if(startTime < sampleStart && endTime >= sampleStart) {
			//dodajemy cisz� z przodu bufora
			int tmpFrames = frames - (int)timeToSamples(sampleStart - startTime);
			byte[] tmpBuff = tip.getBytes(0, tmpFrames);
			System.arraycopy(tmpBuff, 0, fetchBuffer, (frames - tmpFrames) * frameSize, tmpFrames * frameSize);
//			printBuffer(fetchBuffer);
		} else if(startTime >= sampleStart && endTime <= sampleEnd) {
			//bierzemy ca��...
			double s = trackToItemTime(startTime, tip);
			tip.getBytesInto(fetchBuffer, (int)timeToSamples(s), frames);
		} else if(startTime <= sampleEnd && endTime > sampleEnd){
			//dodajemy cisz� na koniec pr�bki TODO - sprawdzic czemu tutaj nie w�azi...? z�y warunek
			int tmpFrames = frames - (int)timeToSamples(sampleEnd - endTime);
			byte[] tmpBuff = tip.getBytes(0, tmpFrames);
			System.arraycopy(tmpBuff, 0, fetchBuffer, 0, tmpFrames);			
		}
	}
	
	/**
	 * 
	 * @param trackTime
	 * @param tip
	 * @return
	 */
	private double trackToItemTime(double trackTime, TrackItemPanel tip) {
		return trackTime - tip.getTrackItem().getTimeFrom();
	}
	
	/**
	 * do we need to include specified TrackItemPanel in current mixBuffer 
	 * @param position
	 * @param frames
	 * @param tpi
	 * @return
	 */
	private boolean includeTrackItemPanelInMix(double position, int frames, TrackItemPanel tpi) {
		if(tpi.getWaveFile() == null) {
			return false;
		}
		double positionEnd = position + tpi.sampleToSecond(frames);
		if(((position - tpi.getTrackItem().getTimeFrom()) >= 0 || (positionEnd - tpi.getTrackItem().getTimeFrom() >=0 )) && 
			((position - tpi.getTrackItem().getTimeTo() <= 0) || (positionEnd - tpi.getTrackItem().getTimeTo() <= 0))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe� Mika</a>, Geomar SA
	 *
	 */
	protected class FetchedSampleData {
		private TrackItem trackItem = null;
		int channelNo = 2;
		byte[] sampleData = null;
		public FetchedSampleData(TrackItem trackItem, int channelNo, byte[] sampleData) {
			super();
			this.trackItem = trackItem;
			this.channelNo = channelNo;
			this.sampleData = sampleData;
		}
		public int getChannelNo() {
			return channelNo;
		}
		public byte[] getSampleData() {
			return sampleData;
		}
		public TrackItem getTrackItem() {
			return trackItem;
		}
	}
	
	/**
	 * <p>
	 * Translate littleendian frame to double[]
	 * @param frame
	 * @param bytesPerChannel
	 * @param channels
	 * @return
	 */
	private double[] convertFrame(byte[] frame, int bytesPerChannel, int channels) {
		double[] sample = new double[channels];
		int idx = 0;
		for(int c = 0; c < channels; c++) {
			double value = 0;
			for(int b = 0; b < bytesPerChannel; b++) {
				value += (int)frame[idx] << (b * 8);
				idx++;
			}
			sample[c] = value;
		}
		return sample;
	}
	

	/**
	 * 
	 * @param buffer
	 * @param frameNo
	 * @param frameSize
	 * @return
	 */
	private byte[] getFrame(byte[] buffer, int frameNo, int frameSize) {
		byte[] frame = new byte[frameSize];
		System.arraycopy(buffer, frameNo * frameSize, frame, 0, frameSize);
		return frame;
	}

	private byte[] mixFetchedData(Collection<FetchedSampleData> fetched) {
		byte[][] test = new byte[fetched.size()][];
		int i = 0; 
		for(FetchedSampleData fsd : fetched) {
			test[i++] = fsd.sampleData;
		}
		return SoundUtils.mixdownBuffers(mixAudioFormat, test);
	}
	/**
	 * TODO poprawic to bo w pytkie nie tak jak powinno by� jest...
	 * 
	 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
	 *
	 */
	protected class PlayerRunnable implements Runnable {
		private boolean interrupted = false;
		@Override
		public void run() {
			try {
				defaultDataLine.open(new AudioFormat(outSamplerate, outBitrate, outChannels, true, false), mixBufferSampleLen * 32);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}

			while(true && !interrupted && defaultDataLine.isOpen()) {
				double d = mf.getTuneEditor().getMarkerPosition();
				double t = samplesToTime(mixBufferSampleLen);						
				mf.getTuneEditor().setMarkerLocation(d + t);

				Collection<FetchedSampleData> fetched = fetchTune(d - t, mixBufferSampleLen);
				if(debugBufferMix) {
					bufferMixTime = System.nanoTime();
				}
				byte[] test = mixFetchedData(fetched);	//test new mixing using floating point mixer method
				if(debugBufferMix) {
					long l = System.nanoTime() - bufferMixTime;
					System.out.println(String.format("Fetched data mix done in: %sns (%s\u00B5s, %sms)", l, l / 1000, (double)l / 1000000));
				}
				fireMixerEvent(new BufferMixedEvent(this, mixAudioFormat, test));

				//
				defaultDataLine.write(test, 0, test.length);
				defaultDataLine.start();
				System.out.println(String.format("----> bufferSize: %s, available to write: %s", defaultDataLine.getBufferSize(), defaultDataLine.available()));
				if(defaultDataLine.getBufferSize() == defaultDataLine.available()) {
					System.out.println(" -- buffer underrun!!");
				} else if(defaultDataLine.available() == 0) {
					System.out.println(" -- buffer overrunn!!");
				}
			}
		}
		
		public void interrupt() {
			this.interrupted = true;
		}
		
		public boolean isInterrupted() {
			return interrupted;
		}
	}
	
	private JMenuItem jmi = null;
	
	@Override
	public JMenuItem getMenuItem() {
		return jmi;
	}

	@Override
	public void setMenuItem(JMenuItem jmi) {
		this.jmi = jmi;
	}
	
	public void addPlaybackStatusListener(PlaybackStatusListener l) {
		synchronized (playbackStatusListeners) {
			playbackStatusListeners.add(l);
		}
	}
	
	public void removePlaybackStatusListener(PlaybackStatusListener l) {
		synchronized (playbackStatusListeners) {
			playbackStatusListeners.remove(l);
		}
	}
	
	private void firePlaybackEvent(PlaybackEvent e) {
		synchronized (playbackStatusListeners) {
			for(PlaybackStatusListener l : playbackStatusListeners) {
				l.playbackStatusChanged(e);
			}
		}
	}
	
	public void addMixerListener(MixerListener l) {
		synchronized (mixerListeners) {
			mixerListeners.add(l);
		}
	}
	
	public void removeMixerListener(MixerListener l) {
		synchronized (mixerListeners) {
			mixerListeners.remove(l);
		}
	}
	
	private void fireMixerEvent(BufferMixedEvent e) {
		synchronized (mixerListeners) {
			for(MixerListener l : mixerListeners) {
				l.bufferMixed(e);
			}
		}
	}
}
