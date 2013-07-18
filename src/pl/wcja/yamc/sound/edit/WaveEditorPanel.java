package pl.wcja.yamc.sound.edit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import pl.wcja.yamc.file.AbstractAudioStream;
import pl.wcja.yamc.file.WaveStream;
import pl.wcja.yamc.jcommon.Unit;
import pl.wcja.yamc.sound.file.Reapeaks;
import pl.wcja.yamc.sound.file.Reapeaks.Mipmap;

/**
 * 
 * extends jpanel or jcomponent? co lepiej?
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class WaveEditorPanel extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, WaveEditor {
	
	private Logger logger = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 366586339790323573L;
	private boolean debug = false;
	private boolean info = true;
	private AbstractAudioStream audioStream = null;
	protected Reapeaks reapeaks = null;
	
	protected int channels = 0, frameSize = 0, bytesPerChannel = 0, sampleResolution = 0;
	protected long totalSamples = 0;
	protected double visibleStart = 0, visibleEnd = 0;
	protected double viewFromFrame = 0, viewToFrame = 0;
	protected double markerLocationSample = 0;
	private double playbackMarkerLocation = 0;
	protected Selection selection = null;
	private Selection lastSelection = null;
	private int lastWidth = 0, lastHeight = 0;
	private double samplesPerPixel = 0;
	protected Point lastMousePosition = null;
	private Color colorBackground = Color.white; 
	private Color colorForeground = Color.blue;
	private Color colorMarker = Color.red;
	private Color colorPlaybackMarker = Color.green;
	private Color colorGrid = Color.lightGray;
	private Color colorSelection = Color.darkGray;
	private Color colorText = new Color(128,64,64);
	
	private List<WaveEditorPanelListener> waveformPanelListeners = new LinkedList<WaveEditorPanelListener>();

	private enum SELECTION_EDIT_MODE {
		EDIT_NONE,
		EDIT_BEGINING,
		EDIT_ENDING
	}
	
	private SELECTION_EDIT_MODE selectionEditMode = SELECTION_EDIT_MODE.EDIT_NONE;
	
	/**
	 * 
	 */
	public WaveEditorPanel() {
		super();
		initialize();
	}

	protected void initialize() {
		setBackground(colorBackground);
		setFont(new Font("Tahoma", Font.PLAIN, 9));
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#addWaveformPanelListener(pl.wcja.sound.gui.WaveEditorPanelListener)
	 */
	@Override
	public void addWaveformPanelListener(WaveEditorPanelListener l) {
		synchronized (waveformPanelListeners) {
			waveformPanelListeners.add(l);
		}
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#removeWaveformPanelListener(pl.wcja.sound.gui.WaveEditorPanelListener)
	 */
	@Override
	public void removeWaveformPanelListener(WaveEditorPanelListener l) {
		synchronized (waveformPanelListeners) {
			waveformPanelListeners.remove(l);
		}
	}
	
	private void fireMarkerLocationChangedEvent(MarkerLocationChangedEvent e) {
		synchronized (waveformPanelListeners) {
			for(WaveEditorPanelListener l : waveformPanelListeners) {
				l.markerLocationChanged(e);
			}
		}
	}
	
	private void fireVisibleAreaChangedEvent(VisibleAreaChangedEvent e) {
		synchronized (waveformPanelListeners) {
			for(WaveEditorPanelListener l : waveformPanelListeners) {
				l.visibleAreaChanged(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#setWave(java.io.File)
	 */
	@Override
	public void setWaveFile(File waveFile) throws UnsupportedAudioFileException, IOException {
		//remove listeners so if we crash somewhere in the middle we're going not to listen...
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
		
		audioStream = new WaveStream(waveFile);
		
		//resetujemy poprzednie informacje...
		selection = null;
		viewFromFrame = 0;
		totalSamples = (long)(viewToFrame = audioStream.getAudioFileFormat().getFrameLength());
		sampleResolution = (int) Math.pow(2, audioStream.getAudioFileFormat().getFormat().getSampleSizeInBits());
		channels = audioStream.getAudioFileFormat().getFormat().getChannels();
		frameSize = audioStream.getAudioFileFormat().getFormat().getFrameSize();
		bytesPerChannel = frameSize / channels;
		recalculateSamplesPerPixel();
		
		initReapeaks();
		
		//all ok - start listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		//reset marker location
		setMarkerLocation(0);
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getWave()
	 */
	@Override
	public File getWaveFile() {
		return audioStream != null ? audioStream.getFile() : null;
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getAudioFileFormat()
	 */
	@Override
	public AudioFileFormat getAudioFileFormat() {
		return audioStream.getAudioFileFormat();
	}
	
	/**
	 * <p>
	 * After changing locations data (viewFromSample and viewSampleEnd) this method must be called
	 * to recalculate samplesPerPixel rate.
	 */
	protected void recalculateSamplesPerPixel() {
		samplesPerPixel = (viewToFrame - viewFromFrame) / getWidth();
	}
	
	/**
	 * <p>
	 * Translates specified sample number to location from the left edge of component.
	 * 
	 * @param sampleNo
	 * @return
	 */
	protected double sampleToPixel(double sampleNo) {
		return (sampleNo / samplesPerPixel) - (viewFromFrame / samplesPerPixel);
	} 
	
	/**
	 * <p>
	 * Returns sample that corresponds to specified pixel location based upon the components left edge.
	 * So if the sample is moved that it's beggining is not visible the result will be calculated accordingly.
	 * 
	 * @param xLocation
	 * @return
	 */
	protected double pixelToSample(double xLocation) {
		return viewFromFrame + (xLocation * samplesPerPixel);
	}

	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#sampleToSecond(double)
	 */
	@Override
	public double sampleToSecond(double sample) {
		return sample / audioStream.getAudioFileFormat().getFormat().getSampleRate();
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getBytes(int, int)
	 */
	@Override
	public byte[] getBytes(int sampleOffset, int samplesLen) {
		return audioStream.getRawData(sampleOffset, samplesLen);
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getBytesInto(byte[], int, int)
	 */
	@Override
	public void getBytesInto(byte[] buf, int sampleOffset, int samplesLen) {
		byte[] tmp = getBytes(sampleOffset, samplesLen);
		System.arraycopy(tmp, 0, buf, 0, buf.length);
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		return getBytes((int)viewFromFrame, (int)(viewToFrame - viewFromFrame));
	}	
	
	/**
	 * 
	 * @return
	 */
	private Mipmap getBestMipmap() {
		Mipmap m = reapeaks.getBestMipmapFor(samplesPerPixel);
		return m;
	}
	
	@Override
	public synchronized void paint(Graphics g) {
		long paintTime = System.nanoTime();
		//background
		g.setColor(colorBackground);
		g.fillRect(0, 0, getWidth(), getHeight());
		//childs + border etc
		super.paint(g);
		//wave i jego pierdolki
		if(audioStream != null && audioStream.getFile().length() > 0) {
			recalculateSamplesPerPixel();
			if(g instanceof Graphics2D) {
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			//selection
			if(selection != null) {
				g.setColor(colorSelection);
				double x1 = selection.getSelectionStart() < viewFromFrame ? 0 : (long)sampleToPixel(selection.getSelectionStart());
				double x2 = selection.getSelectionEnd() > viewToFrame ? getWidth() : (long)sampleToPixel(selection.getSelectionEnd());
				int w = (int)(x2 - x1);
				if(w == 0) { w = 1; }
				g.fillRect((int)x1, 0, w, getHeight());
			}
			//sample
			paintAudioStreamRMS(g);
			//marker
			g.setColor(colorMarker);
			g.drawLine((int)sampleToPixel(markerLocationSample), 0, (int)sampleToPixel(markerLocationSample), getHeight());
			//draw info strings
			if(info) {
				drawInfo(g);
			}
		}
		if(debug) {
			long l = System.nanoTime() - paintTime;
			logger.info(String.format("drawing time: %sns (%s\u00B5s); clip: %s", l, l / 1000, g.getClipBounds()));
		}
	}
	
	/**
	 * <p>
	 * Draw some info strings in panel corners...
	 * 
	 * @param g
	 */
	private void drawInfo(Graphics g) {
		String txt = "";
		g.setColor(colorText);
		FontMetrics fm = g.getFontMetrics();
		g.drawString("" + audioStream.getFile().getName(), 2, fm.getHeight());
		txt = String.format("SamplePerPixel: %1.4f", samplesPerPixel);
		g.drawString(txt, (int)(getWidth() - fm.getStringBounds(txt, g).getWidth() - 2), getHeight() - 2);
	}

	/**
	 * 
	 * @param g
	 */
	private void paintAudioStream(Graphics g) {
		lastWidth = getWidth();
		lastHeight = getHeight();
		double channelHeight = (lastHeight / channels);
		Rectangle b = getBounds();
		//in case of this component bounds extends parent bounds we're
		//going to draw only the visible region to speed up drawing!
		Rectangle clipBounds = g.getClipBounds();
		int from = (int)clipBounds.getX();
		int to = from + (int)clipBounds.getWidth();
		if(debug) {
			logger.info(String.format("%s: %s, drawing: %s - %s (=%s)px; clip: %s", audioStream.getFile().getName(), b.toString(), from, to, to - from, clipBounds));
		}
		for(int x = from; x < to; x++) {
			double lts = pixelToSample(x), value;
			double[] sample = audioStream.getSample((long)lts);
			for(int channel = 0; channel < channels; channel++) {
				double chnX = (channel * channelHeight) + (channelHeight / 2);
				g.setColor(colorGrid);
				g.drawLine(x, (int)chnX, x, (int)chnX);
				if(selection != null && lts >= selection.getSelectionStart() && lts <= selection.getSelectionEnd()) {
					g.setColor(colorBackground);
				} else {
					g.setColor(colorForeground);
				}
				value = ((sample[channel] * channelHeight) / sampleResolution);
				g.drawLine(x, (int)(chnX), x, (int)(chnX - value));
				g.drawLine(x, (int)(chnX), x, (int)(chnX + value));
			}
		}
	}
	
	//TODO: add a prerender of wave to a bitmap/png and then draw it properly on the panel instead of dynamic rendering
	//Maybe use REAPEAKS? (http://www.reaper.fm/sdk/reapeaks.txt)
	
	/**
	 * 
	 * @param g
	 */
	private void paintAudioStreamRMS(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		lastWidth = getWidth();
		lastHeight = getHeight();
		double channelHeight = (lastHeight / channels);
		Rectangle b = getBounds();
		//in case of this component bounds extends parent bounds we're
		//going to draw only the visible region to speed up drawing!
		Rectangle clipBounds = g.getClipBounds();
		int from = (int)clipBounds.getX();
		int to = from + (int)clipBounds.getWidth();
		if(debug) {
			logger.info(String.format("%s: %s, drawing: %s - %s (=%s)px; clip: %s", audioStream.getFile().getName(), b.toString(), from, to, to - from, clipBounds));
		}
		double[] sample;
		for(int x = from; x < to; x++) {
			double lts = pixelToSample(x), value;
			double ltsn = pixelToSample(x+1), valuen;
			if((int)lts != (int)ltsn) {
				if(getBestMipmap() != null) {
					sample = getRMSMipmapSample((long)lts, (long)ltsn);
				} else {
					sample = audioStream.getRMSSample((long)lts, (long)ltsn);	
				}
			} else {
				sample = audioStream.getSample((long)lts);
			}			
			for(int channel = 0; channel < channels; channel++) {
				double chnX = (channel * channelHeight) + (channelHeight / 2);
				g.setColor(colorGrid);
				g.drawLine(x, (int)chnX, x, (int)chnX);
				if(selection != null && lts >= selection.getSelectionStart() && lts <= selection.getSelectionEnd()) {
					g.setColor(colorBackground);
				} else {
					g.setColor(colorForeground);
				}
				value = ((sample[channel] * channelHeight) / sampleResolution);
				g.drawLine(x, (int)(chnX), x, (int)(chnX - value));
				g.drawLine(x, (int)(chnX), x, (int)(chnX + value));

			}
		}
	}
	
	/**
	 * TODO - implement it.. but how?:P
	 * Paint the waveform using pre-calculated data from reapacks or 
	 * real data depending of samplesPerPixel ratio.
	 * @param g
	 */
	@Deprecated
	private void paintWaveform(Graphics g) {
		lastWidth = getWidth();
		lastHeight = getHeight();
		double channelHeight = (lastHeight / channels);
		Rectangle b = getBounds();
		//in case of this component bounds extends parent bounds we're
		//going to draw only the visible region to speed up drawing!
		Rectangle clipBounds = g.getClipBounds();
		int from = (int)clipBounds.getX();
		int to = from + (int)clipBounds.getWidth();
		
		if(samplesPerPixel > 128) {
			//draw using accumulated data
			for(int x = from; x < to; x++) {
				double lts = pixelToSample(x), valuemin, valuemax;
				double[] sample = getReapeaksSample((long)lts);
				for(int channel = 0; channel < channels; channel++) {
					double chnX = (channel * channelHeight) + (channelHeight / 2);
					g.setColor(colorGrid);
					g.drawLine(x, (int)chnX, x, (int)chnX);
					if(selection != null && lts >= selection.getSelectionStart() && lts <= selection.getSelectionEnd()) {
						g.setColor(colorBackground);
					} else {
						g.setColor(colorForeground);
					}
					valuemin = ((sample[channel * 2] * channelHeight) / sampleResolution);
					valuemax = ((sample[channel * 2 + 1] * channelHeight) / sampleResolution);
					g.drawLine(x, (int)(chnX), x, (int)(chnX + valuemax));
				}
			}			
		} else {
			//draw from real data
			paintAudioStream(g);
		}
	}
	
	private void initReapeaks() throws IOException  {
		reapeaks = new Reapeaks(audioStream);
	}
		
	/**
	 * Get a sample from reapeak mipmap
	 * @param sampleNo
	 * @return
	 */
	private double[] getReapeaksSample(double sampleNo) {
		Mipmap mm = getBestMipmap();
		double[] sample = new double[reapeaks.getChannels() * reapeaks.getVersionMultiplier()];
		//translate sampleNo to reapeak mipmap sample number
		int rsn = (int)(sampleNo / mm.getDivisionFactor());
		short[] peak = mm.getPeak(rsn);
		for(int i = 0; i < peak.length; i++) {
			sample[i] = peak[i];
		}
		return sample;
	}

	/**
	 * Get a RMS samples from a generated mipmap 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public double[] getRMSMipmapSample(long fromIndex, long toIndex) {
		double[] RMSSample = new double[audioStream.getAudioFileFormat().getFormat().getChannels()];
		double[] tmpSample;
		Mipmap mm = getBestMipmap();
//		double peakCount = Math.ceil((toIndex - fromIndex) / (double)mm.getDivisionFactor());
		double peakCount = (toIndex - fromIndex) / (double)mm.getDivisionFactor();
//		if(mm != null) {
//			System.out.println(String.format(
//					"samples per pixel: %s, get from %s to %s / mipmap divider: %s = %s", samplesPerPixel, 
//					fromIndex, toIndex, mm.getDivisionFactor(), peakCount));	
//		}
		for(long i = fromIndex; i < toIndex; i+=mm.getDivisionFactor()) {
			tmpSample = getReapeaksSample(i);
			for(int ci = 0; ci < audioStream.getAudioFileFormat().getFormat().getChannels(); ci++) {
				RMSSample[ci] += tmpSample[ci * reapeaks.getVersionMultiplier()] * tmpSample[ci * reapeaks.getVersionMultiplier()];
				RMSSample[ci] += tmpSample[(ci  * reapeaks.getVersionMultiplier()) + 1] * tmpSample[(ci  * reapeaks.getVersionMultiplier()) + 1];
			}
		}
		for(int ci = 0; ci < audioStream.getAudioFileFormat().getFormat().getChannels(); ci++) {
			RMSSample[ci] = (Math.sqrt(RMSSample[ci] / (peakCount * 4)));
		}
		return RMSSample;
	}
	
	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#setMarkerLocation(double)
	 */
	@Override
	public void setMarkerLocation(double sample) {
		this.markerLocationSample = (long)sample;
		fireMarkerLocationChangedEvent(new MarkerLocationChangedEvent(this, markerLocationSample, Unit.SAMPLE));
		repaint();
	}

	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#clearSelection()
	 */
	@Override
	public void clearSelection() {
		setSelection(null);
		lastSelection = null;
		selectionEditMode = SELECTION_EDIT_MODE.EDIT_NONE;
	}

	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#setSelection(pl.wcja.sound.gui.Selection)
	 */
	@Override
	public void setSelection(Selection selection) {
		if(selection == null) {
			this.selection = null;
			repaint();
			return;
		}
		if(selection.getSelectionStart() <= selection.getSelectionEnd() && selection.getSelectionStart() >= 0 && selection.getSelectionEnd() <= totalSamples) {
			this.selection = selection;
			repaint();
		}
	}

	/**
	 * <p>
	 * Moves view to specified sample at current samplePerPixel ratio
	 * if the calculated endSample at the current SPP extends totalSamples 
	 * the endSample is set to lastSample and the SPP is recalculated
	 * 
	 * @param sample
	 */
	public void moveTo(double sample) {
		//TODO to be implemented... or not...
	}

	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#pan(double, double)
	 */
	@Override
	public void pan(double sampleStart, double sampleEnd) {
		if(sampleEnd > sampleStart && sampleStart >= 0 && sampleEnd <= totalSamples) {
			viewFromFrame = sampleStart;
			viewToFrame = sampleEnd;
			recalculateSamplesPerPixel();
			fireVisibleAreaChangedEvent(new VisibleAreaChangedEvent(this, viewFromFrame, viewToFrame, Unit.SAMPLE));
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see pl.wcja.sound.gui.WaveEditor#getTotalSecondLength()
	 */
	@Override
	public double getTotalSecondLength() {
		return audioStream.getAudioFileFormat().getFrameLength() / audioStream.getAudioFileFormat().getFormat().getSampleRate();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int v = e.getWheelRotation();
		double left = viewFromFrame - ((markerLocationSample  - viewFromFrame) * (0.1 * v));
		double right = viewToFrame + ((viewToFrame - markerLocationSample) * (0.1 * v));
		recalculateSamplesPerPixel();
		if(left <= 0) {
			left = 0;
		}
		if(right >= totalSamples) {
			right = totalSamples;
		}
		pan(left, right);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int onMask = MouseEvent.BUTTON1_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			setMarkerLocation(pixelToSample(e.getX()));
		}
		onMask = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
		offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			if(lastMousePosition == null) {
				lastMousePosition = e.getLocationOnScreen();
			} else {
				double offset = lastMousePosition.getX() - e.getLocationOnScreen().getX();
				double samplesOffset = pixelToSample(offset) - viewFromFrame;
				double ovss = viewFromFrame + samplesOffset;
				double ovse = viewToFrame + samplesOffset;
				if(ovss > 0 && ovse < totalSamples) {
					pan(ovss, ovse);
				} else if(ovss <= 0) {
					pan(0, ovse - ovss);
				} else if(ovse >= totalSamples) {
					pan(ovse - (ovse - totalSamples), totalSamples);
				}
				lastMousePosition = e.getLocationOnScreen();
			}
		}
		onMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
		offMask = MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			double l = (long)pixelToSample(e.getX());
			if(selection == null) {
				setSelection(new Selection(l, l + 1));
			} else {
				if(lastSelection == null) {
					lastSelection = selection;
					double dstart = Math.abs(l - selection.getSelectionStart());
					double dend = Math.abs(l - selection.getSelectionEnd());
					if(dend < dstart) {
						selectionEditMode = SELECTION_EDIT_MODE.EDIT_ENDING;
					} else {
						selectionEditMode = SELECTION_EDIT_MODE.EDIT_BEGINING;
					}
				}
				double newstart = selection.getSelectionStart(), newend = selection.getSelectionEnd();
				if(selectionEditMode == SELECTION_EDIT_MODE.EDIT_BEGINING) {
					newstart = l;
					setSelection(new Selection(newstart, newend));	 
				} else if(selectionEditMode == SELECTION_EDIT_MODE.EDIT_ENDING) {
					newend = l;
					setSelection(new Selection(newstart, newend));	 
				}
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int onMask = MouseEvent.BUTTON1_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			setMarkerLocation(pixelToSample(e.getX()));
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastMousePosition = null;
		lastSelection = null;
		selectionEditMode = SELECTION_EDIT_MODE.EDIT_NONE;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}