package pl.wcja.yamc.sound.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;

import pl.wcja.yamc.debug.DebugConfig;
import pl.wcja.yamc.frame.Configurable;
import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.gui.MFOkCancelDialog;
import pl.wcja.yamc.gui.MFProgressDialog;
import pl.wcja.yamc.jcommon.Unit;
import pl.wcja.yamc.sound.Track;
import pl.wcja.yamc.sound.TrackItem;
import pl.wcja.yamc.sound.Tune;
import pl.wcja.yamc.sound.file.DecoderManager;
import pl.wcja.yamc.utils.DialogUtils;
import pl.wcja.yamc.utils.RandomGenerator;

/**
 * <p>
 * Panel holding the tune and allowing editing it
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe� Mika</a>, Geomar SA
 *
 */
public class TuneEditorGrid extends JComponent 
	implements TuneEditor, ComponentListener, DropTargetListener, 
	MouseWheelListener, MouseMotionListener, MouseListener, Configurable, PopupMenuListener {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private JPopupMenu popupMenu = null;

	//view from - to (in seconds)
	private double viewFrom = 0, viewTo = 30;
	
	private DropTarget dropTarget = null;
	private DataFlavor fileListDF = new DataFlavor("application/x-java-file-list; class=java.util.List","application/x-java-file-list");
	
	private IMainFrame mf = null;
	private TrackItemPanel focusedTrackItemPanel = null;
	private Tune tune = null;
	
	private Point lastMousePosition = null;
	
	private double beatPerPixel = 0;
	private double secondPerPixel = 0;
	private double beatPerSecond = 0;
	
	private double beatQuantizer = 1 / 8;
	private double beatPaintQuantizer = Math.pow(2, 8);
//	private double beatSnapQuantizer = Math.pow(2, 8);
	private Double markerPosition = Double.valueOf(0);
	
	private int marginTop = 0, marginBottom = 0;
	
	private Color colorBackground = new Color(232,240,255); 
	private Color colorForeground = Color.blue;
	private Color colorMarker = Color.red;
	private Color colorGridTime = Color.lightGray;
	private Color colorGridBeats = new Color(128, 128, 192);
	private Color colorGridBeatsLight = new Color(192, 192, 224);
	private Color colorSelection = Color.darkGray;
	private Color beatFont = new Color(192, 192, 64);
	private Color timeFont = new Color(80, 192, 192);
	
	private DecimalFormat unitFormat = new DecimalFormat("###0.000");
	
	private List<TuneEditorGridListener> tuneEditorGridListeners = new LinkedList<TuneEditorGridListener>();
	
	/**
	 * Snap mode for moving trackItems
	 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe� Mika</a>, Geomar SA
	 *
	 */
	public enum SnapMode {
		NONE("None", 0),
		CLOSEST_BAR("Closest bar", 1);
		
		private String name = "";
		private int value = 0;
		
		private SnapMode(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;  
		}
		
		public int getValue() {
			return value;
		}
	}
	private SnapMode snapMode = SnapMode.NONE;
	
	/**
	 * Enum for displaying grids
	 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe� Mika</a>, Geomar SA
	 *
	 */
	public enum GridDisplay {
		NONE("None", 0),
		SECONDS("Seconds", 1),
		BEATS("Beats", 2),
		BOTH("Both", 3);
		
		private String name = "";
		private int value = 0;
		
		private GridDisplay(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;  
		}
		
		public int getValue() {
			return value;
		}
	}
	private GridDisplay gridDisplay = GridDisplay.BOTH;	
	
	/**
	 * <p>
	 * 
	 * @param mf
	 */
	public TuneEditorGrid(IMainFrame mf) {
		super();
		this.mf = mf;
		unitFormat.getDecimalFormatSymbols().setDecimalSeparator('.');
		setDoubleBuffered(true);
		setBackground(colorBackground);
		setFont(new Font("Tahoma", Font.PLAIN, 11));
		initialize();
	}
	
	private void initialize() {
		removeComponentListener(this);
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
				
		dropTarget = new DropTarget(this, this);
		setDropTarget(dropTarget);
		
		popupMenu = new JPopupMenu("Menu");
		setComponentPopupMenu(popupMenu);
		popupMenu.addPopupMenuListener(this);
		
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}	
	
	public void addTuneEditorGridListener(TuneEditorGridListener l) {
		synchronized (tuneEditorGridListeners) {
			tuneEditorGridListeners.add(l);
		}
	}
	
	public void removeTuneEditorGridListener(TuneEditorGridListener l) {
		synchronized (tuneEditorGridListeners) {
			tuneEditorGridListeners.remove(l);
		}
	}
	
	private void fireMarkerLocationChangedEvent(MarkerLocationChangedEvent e) {
		synchronized (tuneEditorGridListeners) {
			for(TuneEditorGridListener l : tuneEditorGridListeners) {
				l.markerLocationChanged(e);
			}
		}
	}
	
	private void fireVisibleAreaChangedEvent(VisibleAreaChangedEvent e) {
		synchronized (tuneEditorGridListeners) {
			for(TuneEditorGridListener l : tuneEditorGridListeners) {
				l.visibleAreaChanged(e);
			}
		}
	}
	
	/**
	 * 
	 */
	public void recalculateRatios() {
		secondPerPixel = ((viewTo - viewFrom) / getWidth());
		beatPerSecond = tune.getBPM() / 60;
		beatPerPixel = (secondToBeat(viewTo - viewFrom) / getWidth());
	}
	
	/**
	 * Translates seconds to pixel absolute from the left edge of the component (viewFrom)
	 * @param second
	 * @return
	 */
	public double secondToPixelAbsolute(double second) {
		return (second / secondPerPixel) - (viewFrom / secondPerPixel);
	}
	
	/**
	 * Translates seconds to pixel
	 * @param second
	 * @return
	 */
	public double secondToPixel(double second) {
		return second / secondPerPixel;
	}
	
	/**
	 * Translates pixels to seconds
	 * @param pixel
	 * @return
	 */
	public double pixelToSecond(double pixel) {
		return /*viewFrom + */(pixel * secondPerPixel);
	}
	
	/**
	 * Translates pixels to seconds absolute from the viewFrom (left edge of component)
	 * @param pixel
	 * @return
	 */
	public double pixelToSecondAbsolute(double pixel) {
		return viewFrom + (pixel * secondPerPixel);
	}
	
	/**
	 * Translates beat to second
	 * @param beat
	 * @return
	 */
	public double beatToSecond(double beat) {
		return beat / beatPerSecond;
	}
	
	/**
	 * Translates second to beat
	 * @param second
	 * @return
	 */
	public double secondToBeat(double second) {
		return second * beatPerSecond;
	}
	
	public double secondToBeatAbsolute(double second) {
		return secondToBeat(viewFrom) + secondToBeat(second);
	}
	
	public double beatToPixel(double beat) {
		return beat / beatPerPixel;
	}
	
	/**
	 * 
	 * @param beat
	 * @return
	 */
	public double beatToPixelAbsolute(double beat) {
		return (beat / beatPerPixel) - secondToPixel(viewFrom); 
	}
	
	public IMainFrame getMainFrame() {
		return mf;
	}
	
	@Override
	public Tune getTune() {
		return tune;		
	}

	@Override
	public void setTune(Tune tune) {
		this.tune = tune;
		if(tune != null) {
			this.viewFrom = tune.getViewFrom();
			this.viewTo = tune.getViewTo();
			removeAll();
			for(Track t : tune.getTracks()) {
				for(TrackItem ti : t.getItems()) {
					addTrackItem(ti);
				}
			}
			recalculateRatios();
			rearrangeTracks();
			mf.setTuneTitle(tune.getTuneName());
			repaint();
		}
	}
	
	@Override
	public double getMarkerPosition() {
		synchronized(markerPosition) {
			return markerPosition;
		}
	}
	
	@Override
	public void setMarkerLocation(double markerPosition) {
		synchronized (this.markerPosition) {
			fireMarkerLocationChangedEvent(new MarkerLocationChangedEvent(this, markerPosition, Unit.SECOND));
			//repaint only if we really need it
			double newPx = secondToPixelAbsolute(markerPosition);
			double oldPx = secondToPixelAbsolute(this.markerPosition);
			this.markerPosition = markerPosition;
			if(newPx != oldPx) {
				repaint((int)oldPx - 2, 0, 3, getHeight());
				repaint((int)newPx - 2, 0, 3, getHeight());
			}
		}
	}
	
	public GridDisplay getGridDisplay() {
		return gridDisplay;
	}
	
	public void setGridDisplay(GridDisplay gd) {
		gridDisplay = gd;
//		FontMetrics fm = new FontDesignMetrics(getFont()); 
		
		marginBottom = 0;
		marginTop = 0;
		rearrangeTracks();
		repaint();
	}
	
	public void pan(double timeFrom, double timeTo) {
		viewFrom = timeFrom;
		viewTo = timeTo;
		recalculateRatios();
		fireVisibleAreaChangedEvent(new VisibleAreaChangedEvent(this, viewFrom, viewTo, Unit.SECOND));
		rearrangeTracks();
		repaint(0, 0, getWidth(), getHeight());
	}
		
	public void setFocusedTrackItemPanel(TrackItemPanel tip) {
		this.focusedTrackItemPanel = tip;
		if(tip != null && tip.getWaveFile() != null) {
			mf.getStatusStrip().setStatus(tip.getWaveFile().getAbsolutePath());
		}
	}
	
	public TrackItemPanel getFocusedTrackItemPanel() {
		return focusedTrackItemPanel;
	}

	/**
	 * Return track at specified point or null
	 * @param p
	 * @return
	 */
	public Track getTrackAt(Point p) {
		if(tune != null && p != null) {
			int tracksNo = tune.getTracks().size();
			double trackHeight = getTrackHeight();
			double y = marginTop;
			for(Track track : tune.getTracks()) {
				if(p.getY() >= 0 && p.getY() < y + trackHeight) {
					return track;
				}
				y += trackHeight;
			}
		}
		return null;
	}
	
	/**
	 * Get specified track top location
	 * @param track
	 * @return
	 */
	private double getTrackTop(Track track) {
		if(tune != null) {
			int tracksNo = tune.getTracks().size();
			double trackHeight = getTrackHeight();
			double y = marginTop;
			for(Track t : tune.getTracks()) {
				if(t == track) {
					return y;
				}
				y += trackHeight;
			}
		}
		return -1;
	}
	
	/**
	 * Get a track item panels that belongs to giver track
	 * @param t
	 * @return
	 */
	private Collection<TrackItemPanel> getTrackItemPanelsOn(Track t) {
		List<TrackItemPanel> tipl = new LinkedList<TrackItemPanel>();
		for(TrackItem ti : t.getItems()) {
			tipl.add(getTrackItemPanel(ti));
		}
		return tipl;
	}
	
	/**
	 * Get the track item panels that are 'under' the point
	 * @param p
	 * @return
	 */
	private Collection<TrackItemPanel> getTrackItemPanelsAt(Point p) {
		List<TrackItemPanel> tipl = new LinkedList<TrackItemPanel>();
		for(TrackItemPanel tip : getTrackItemPanelsOn(getTrackAt(p))) {
			if(tip.getBounds().contains(p)) {
				tipl.add(tip);
			}
		}
		return tipl;
	}
	
	/**
	 * Returns single track height
	 * @return
	 */
	private double getTrackHeight() {
		return (getHeight() - marginTop - marginBottom) / tune.getTracks().size();
	}
	
	/**
	 * <p>
	 * Returns trackItemPanel for trackItem contained in this grid or null
	 *  
	 * @param trackItem
	 * @return
	 */
	public TrackItemPanel getTrackItemPanel(TrackItem trackItem) {
		for(Component c : getComponents()) {
			if(c instanceof TrackItemPanel && ((TrackItemPanel)c).getTrackItem() == trackItem) {
				return (TrackItemPanel)c;
			}
		}
		return null;
	}
	
	private void removeTrack(Track track) {
		for(TrackItemPanel tip : getTrackItemPanelsOn(track)) {
			removeTrackItemPanel(tip);
		}
		tune.removeTrack(track);
		rearrangeTracks();
		repaint();
	}
	
	private void removeTrackItemPanel(TrackItemPanel tip) {
		Track t = tip.getTrackItem().getTrack();
		t.removeItem(tip.getTrackItem());
		remove(tip);
		repaint();
	}

	@Override
	public synchronized void paint(Graphics g) {
		long paintTime = System.nanoTime();
		if(tune != null) {
			recalculateRatios();
			if(g instanceof Graphics2D) {
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			//fill background
			g.setColor(colorBackground);
			g.fillRect(0, 0, getWidth(), getHeight());
			//paint the grid - vertical 'beats' and horizontal tracks lines
//			paintBeatGrid(g);
			paintBeatGridNew(g);
			paintTrackLines(g);
		}
		//paint children...
		super.paint(g);
		//marker
		g.setColor(colorMarker);
		g.drawLine((int)secondToPixelAbsolute(markerPosition), 0, (int)secondToPixelAbsolute(markerPosition), getHeight());
		if(DebugConfig.getInstance().isDebugTuneEditorGridDrawing()) {
			long l = System.nanoTime() - paintTime;
//			System.out.println(String.format(this.getClass().getCanonicalName() + " drawing time: %sns (%s\u00B5s); clip: %s", l, l / 1000, g.getClipBounds()));
			logger.debug(String.format("Drawing time: %sns (%s\u00B5s); clip: %s", l, l / 1000, g.getClipBounds()));
		}
	}
	
	/**
	 * 
	 * @param g
	 */
	@Deprecated
	private void paintBeatGrid(Graphics g) {
		if(tune != null) {
			FontMetrics fm = g.getFontMetrics();
			String patVF = "  " + unitFormat.format(viewFrom);
			String patVT = "  " + unitFormat.format(viewTo);
			Rectangle2D bVF = fm.getStringBounds(patVF, g);
			Rectangle2D bVT = fm.getStringBounds(patVT, g);
			Rectangle2D labelBounds = bVF.getWidth() > bVT.getWidth() ? bVF : bVT;
			labelBounds = new Rectangle2D.Double(labelBounds.getX(), labelBounds.getY(), labelBounds.getWidth(), labelBounds.getHeight() * 2);	
			marginTop = (int)labelBounds.getHeight() + 1;
			double x = beatToPixel(1);
			beatPaintQuantizer = Math.pow(2, 6);
			while((x * beatPaintQuantizer) > (labelBounds.getWidth() * 3)) {
				beatPaintQuantizer /= 2;
			}
			double margin = Math.pow(2, beatPaintQuantizer);
			if(margin >= (viewFrom - viewTo)) {
				margin = Math.abs(viewFrom - viewTo);
			}
			double modulovf = (secondToBeat(Math.round(viewFrom - margin)) % beatPaintQuantizer);
			double modulovt = (secondToBeat(Math.round(viewTo + margin)) % beatPaintQuantizer);
			double from = secondToBeat(Math.round(viewFrom - margin)) - modulovf;
			double to = secondToBeat(Math.round(viewTo + margin)) + modulovt;
			
			int lineHeight = getHeight() - marginBottom;
			for(double vf = from; vf <= to; vf += beatPaintQuantizer) {
				x = Math.round(beatToPixelAbsolute(vf));
				g.setColor(vf % 1 == 0 ? colorGridBeats : colorGridBeatsLight);
				g.drawLine((int)x, marginTop, (int)x, lineHeight);
				paintBeatLabel(g, labelBounds, vf, colorGridBeats);
			}
			if(DebugConfig.getInstance().isDebugTuneEditorGridDrawing()) {
				logger.debug(String.format("Orginal view beat grid: %s - %s; viewFrom - viewTo (seconds): %s - %s; margin: %s", secondToBeat(Math.round(viewFrom)), secondToBeat(Math.round(viewTo)), viewFrom, viewTo, margin));
				logger.debug(String.format("Drawing beat grid: %s - %s; modulo [from, to]: %s, %s; step: %s; beatPerPixel: %s", from, to, modulovf, modulovt, beatPaintQuantizer, beatPerPixel));
			}
		}
	}
	
	/**
	 * <p>
	 * Draws a label containing beat and second beneath
	 * @param g
	 * @param beat
	 */
	@Deprecated
	private void paintBeatLabel(Graphics g, Rectangle2D maxRect, double beat, Color bgColor) {
		FontMetrics fm = g.getFontMetrics();
		Graphics tg = g.create();
		int x = (int)Math.round(beatToPixelAbsolute(beat));
		String sb = unitFormat.format(beat) + " b";
		String ss = unitFormat.format(beatToSecond(beat)) + " s";
//		maxRect = fm.getStringBounds(sb, g);
		tg.translate(x, 0);
		
		tg.setColor(bgColor);
		tg.fillRect(0, 0, (int)maxRect.getWidth(), (int)maxRect.getHeight());
		tg.setColor(Color.white);
		tg.drawRect(0, 0, (int)maxRect.getWidth(), (int)maxRect.getHeight());
		tg.setColor(colorForeground);
		tg.setColor(beatFont);
		tg.drawString(sb, 3, ((int)maxRect.getHeight() / 2) - 2);
		tg.setColor(timeFont);
		tg.drawString(ss, 3, (int)maxRect.getHeight() - 2);
				
		tg.finalize();
	}
	
	/**
	 * Paint vertical beat, beat/N lines
	 * @param g
	 */
	private void paintBeatGridNew(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		Graphics tg = g.create();
		
		if(tune != null) {
//			String labels = "", labelb = ""; 
//			if(gridDisplay == GridDisplay.SECONDS) {
//				labels = unitFormat.format(999.999d) + "s";
//			} else if(gridDisplay == GridDisplay.BEATS) {
//				labelb = unitFormat.format(999.999d) + "b";
//			} else if(gridDisplay == GridDisplay.BOTH) {
//				labels = unitFormat.format(999.999d) + "s"; 
//				labelb = unitFormat.format(999.999d) + "b";
//			} else if(gridDisplay == GridDisplay.NONE) {
//				
//			}
			Rectangle2D stringBounds = getMaxStringBounds(viewFrom, viewTo, tg);
			double quantizer = pixelToSecond(stringBounds.getWidth());
			double pixelPerBeat = beatToPixel(1);
			double fraction = pixelPerBeat >= stringBounds.getWidth() ? 1.0d / (int)(Math.round(pixelPerBeat) / stringBounds.getWidth()) : 1.0d;
			quantizer = quantizer < 1 ? fraction : Math.round(quantizer + 0.5d);
//			quantizer = Math.round(quantizer * 1000.0d) / 1000.0d;
			double mt = (int)stringBounds.getHeight() + 1;
			mt = gridDisplay == GridDisplay.BOTH ? mt * 2 : mt;
			setMarginTop((int)mt);

			double from = Math.round(viewFrom - 0.5d);
			double to = Math.round(viewTo + 0.5d);

			//lets even it to pair number to be sure we cross '0' and the
			//grid doesn't flicker 
			from = from % 2 != 0 ? from - 1.0d : from;
			from = quantizer % 2 != 0 ? ((int)(from / quantizer) * quantizer) - quantizer : from;
			
			for(double dt = 0; dt >= from - quantizer; dt -= quantizer) {
				drawTimeLine(dt, stringBounds, tg);
			}
			for(double dt = 0 + quantizer; dt <= to; dt += quantizer) {
				drawTimeLine(dt, stringBounds, tg);
			}
			if(DebugConfig.getInstance().isDebugTuneEditorGridDrawing()) {
				logger.debug(String.format("Orginal view beat grid: %s - %s; viewFrom - viewTo (seconds): %s - %s; ", secondToBeat(Math.round(viewFrom)), secondToBeat(Math.round(viewTo)), viewFrom, viewTo));
				logger.debug(String.format("Drawing beat grid: %s - %s; step: %s; beatPerPixel: %s", from, to, beatPaintQuantizer, beatPerPixel));
			}
		}
		tg.finalize();
	}
	
	/**
	 * 
	 * @param time
	 * @param stringBounds
	 * @param g
	 */
	private void drawTimeLine(double time, Rectangle2D stringBounds, Graphics g) {
		int lineHeight = getHeight();
		int ix = (int)secondToPixelAbsolute(time);
		String labels = "", labelb = "";
		if((Math.round(time * 1000.d) / 1000.d) % 1 == 0) {
			g.setColor(Color.DARK_GRAY);
			g.drawLine(ix, marginTop, ix, lineHeight);	
		} else {
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(ix, marginTop, ix, lineHeight);
		}
		if(gridDisplay == GridDisplay.SECONDS) {
			labels = unitFormat.format(time) + "s";
			g.drawString(labels, ix, 0 + (int)stringBounds.getHeight());
		} else if(gridDisplay == GridDisplay.BEATS) {
			labelb = unitFormat.format(secondToBeat(time)) + "b";
			g.drawString(labelb, ix, 0 + (int)stringBounds.getHeight());
		} else if(gridDisplay == GridDisplay.BOTH) {
			labels = unitFormat.format(time) + "s"; 
			labelb = unitFormat.format(secondToBeat(time)) + "b";
			g.drawString(labels, ix, 0 + (int)stringBounds.getHeight());
			g.drawString(labelb, ix, marginTop);
		}
	}
	
	/**
	 * Get the maximum string bound for display label to be sure
	 * it fits in the space between the grid lines.
	 * @param from
	 * @param to
	 * @param g
	 * @return
	 */
	private Rectangle2D getMaxStringBounds(double from, double to, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		String labelSFrom = "", labelBFrom = "", labelSTo = "", labelBTo = ""; 
		if(gridDisplay == GridDisplay.SECONDS) {
			labelSFrom = unitFormat.format(from) + "s";
			labelSTo = unitFormat.format(to) + "s";
		} else if(gridDisplay == GridDisplay.BEATS) {
			labelBFrom = unitFormat.format(secondToBeat(from)) + "b";
			labelBTo = unitFormat.format(secondToBeat(to)) + "b";
		} else if(gridDisplay == GridDisplay.BOTH) {
			labelSFrom = unitFormat.format(from) + "s";
			labelSTo = unitFormat.format(to) + "s";
			labelBFrom = unitFormat.format(secondToBeat(from)) + "b";
			labelBTo = unitFormat.format(secondToBeat(to)) + "b";
		} else if(gridDisplay == GridDisplay.NONE) {
			
		}
		Rectangle2D max = fm.getStringBounds(labelSFrom, g);
		Rectangle2D sbSTo = fm.getStringBounds(labelSTo, g);
		Rectangle2D sbBFrom = fm.getStringBounds(labelBFrom, g);
		Rectangle2D sbBTo = fm.getStringBounds(labelBTo, g);
		max = max.getWidth() > sbSTo.getWidth() ? max : sbSTo;
		max = max.getWidth() > sbBFrom.getWidth() ? max : sbBFrom;
		max = max.getWidth() > sbBTo.getWidth() ? max : sbBTo;
		return max;
	}
		
	/**
	 * 
	 * @param g
	 */
	private void paintTrackLines(Graphics g) {
		g.setColor(colorGridTime);
		int trackLineY = marginTop;
		int trackHeight = (int)getTrackHeight();
		g.setColor(colorGridTime);
		for(int i = 0; i <= tune.getTracks().size(); i++) {
			g.drawLine(0, trackLineY, getWidth(), trackLineY);
			trackLineY += trackHeight;
		}		
	}

	/**
	 * <p>
	 * Recalculate tracks and trackItems locations according to size and secondPerPixel
	 */
	public void rearrangeTracks() {
		if(tune != null) {
			recalculateRatios();
//			int tracksNo = tune.getTracks().size();
			double trackHeight = getTrackHeight();
			double componenty = marginTop;
			for(Track track : tune.getTracks()) {
				for(TrackItem ti : track.getItems()) {
					TrackItemPanel tip = getTrackItemPanel(ti);
					if(tip != null) {
						double pf = Math.round(secondToPixelAbsolute(ti.getTimeFrom()));
						double pe = Math.round(secondToPixelAbsolute(ti.getTimeTo()));
						tip.setLocation((int)pf, (int)componenty);
						tip.setSize((int)(pe - pf), (int)trackHeight);
					} else {
						tip = new TrackItemPanel(this, ti);
						double pf = Math.round(secondToPixelAbsolute(ti.getTimeFrom()));
						double pe = Math.round(secondToPixelAbsolute(ti.getTimeTo()));
						tip.setLocation((int)pf, (int)componenty);
						tip.setSize((int)(pe - pf), (int)trackHeight);
						tip.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
						add(tip);
					}
				}
				componenty += trackHeight;
			}
			tune.updateTuneLength();
		}
	}
	
	/**
	 * 
	 * @param margin
	 */
	private void setMarginTop(int margin) {
		this.marginTop = margin;
		rearrangeTracks();
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		rearrangeTracks();
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		DataFlavor[] fl = dtde.getTransferable().getTransferDataFlavors();
		if(!dtde.getTransferable().isDataFlavorSupported(fileListDF)) {
			dtde.rejectDrag();
		} else {
//			try {
//				List<File> flist = (List<File>)dtde.getTransferable().getTransferData(fileListDF);
//				if(flist.size() == 1) {
//					Track track = getTrackAt(dtde.getLocation());
//					double second = pixelToSecond((int)dtde.getLocation().getX());
//					if(tempTpi == null) {
//						tempTpi = new TrackItemPanel(this, null);
//						tempTpi.setWave(flist.get(0));
//						TrackItem ti = new TrackItem(second, second + tempTpi.getTotalSecondLength());
//						tempTpi.setTrackItem(ti);
//						track.addItem(ti);
//						add(tempTpi);
//					} else {
//						TrackItem ti = new TrackItem(second, second + tempTpi.getTotalSecondLength());
//						tempTpi.setTrackItem(ti);
//					}
//					double pf = Math.round(secondToPixel(tempTpi.getTrackItem().getTimeFrom()));
//					double pe = Math.round(secondToPixel(tempTpi.getTrackItem().getTimeTo()));
//					tempTpi.setLocation((int)pf, (int)getTrackTop(track));
//					tempTpi.setSize((int)(pe - pf), (int)getTrackHeight());
//					tempTpi.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
//					repaint();
//					redrawTracks();
//				}
//			} catch (UnsupportedFlavorException e) {
//				e.printStackTrace();
//				DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
//			} catch (IOException e) {
//				e.printStackTrace();
//				DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
//			} catch (UnsupportedAudioFileException e) {
//				e.printStackTrace();
//				DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
//			}
		}
	}

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		if(dtde.getTransferable().isDataFlavorSupported(fileListDF)) {
			Transferable t = dtde.getTransferable();
			try {
				dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
				final List<File> fl = (List<File>)t.getTransferData(fileListDF);
				if(fl.size() >= 1) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							File testFile = null;
							try {
								Track track = getTrackAt(dtde.getLocation());
								double second = pixelToSecondAbsolute((int)dtde.getLocation().getX());
								TrackItemPanel tip = addTrackItem(null); 
										//new TrackItemPanel(TuneEditorGrid.this, null);

								File in = fl.get(0);
								MFProgressDialog pd = new MFProgressDialog(getMainFrame(), "Decoding...", "Trying to decode " + in.getAbsolutePath());
								pd.pack();
								DialogUtils.centerDialog((JFrame) getMainFrame(), pd);
								pd.setVisible(true);
								testFile = new File(System.getProperty("user.dir") + "\\" + in.getName().substring(0, in.getName().lastIndexOf('.'))+ RandomGenerator.getRandomString() + ".wav");
								testFile = DecoderManager.getInstance().decode(in, testFile, pd);
								if (testFile != null) {
									tip.setWaveFile(testFile);
								} else {
									tip.setWaveFile(in);
								}
								pd.dispose();
								TrackItem ti = new TrackItem(track, second, second + tip.getTotalSecondLength());
								tip.setTrackItem(ti);
								track.addItem(ti);
								double pf = Math.round(secondToPixelAbsolute(ti.getTimeFrom()));
								double pe = Math.round(secondToPixelAbsolute(ti.getTimeTo()));
								tip.setLocation((int)pf, (int)getTrackTop(track));
								tip.setSize((int)(pe - pf), (int)getTrackHeight());
								tip.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
								add(tip);
								repaint();
							} catch (UnsupportedFlavorException e) {
								e.printStackTrace();
								DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
							} catch (IOException e) {
								e.printStackTrace();
								DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
							} catch (UnsupportedAudioFileException e) {
								e.printStackTrace();
								DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
				DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
			} catch (IOException e) {
				e.printStackTrace();
				DialogUtils.showError(TuneEditorGrid.this, e.toString(), "Error");
			}
		}		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int v = e.getWheelRotation();
		double left = viewFrom - ((markerPosition  - viewFrom) * (0.1 * v));
		double right = viewTo + ((viewTo - markerPosition) * (0.1 * v));
		//wyswietlamy max. powiekszenie 0.25sek lub 4096 sek
		if(Math.abs(left - right) >= 0.25 && Math.abs(left - right) <= (1024 * 4)) {
			recalculateRatios();
			pan(left, right);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int onMask = MouseEvent.BUTTON1_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			setMarkerLocation(pixelToSecondAbsolute(e.getX()));
		}	
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastMousePosition = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int onMask = MouseEvent.BUTTON1_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			setMarkerLocation(pixelToSecondAbsolute(e.getX()));
		}
		onMask = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.ALT_DOWN_MASK ;
		offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			if(lastMousePosition == null) {
				lastMousePosition = e.getLocationOnScreen();
			} else {
				double offset = lastMousePosition.getX() - e.getLocationOnScreen().getX();
				double samplesOffset = pixelToSecond(offset);
				double ovss = viewFrom + samplesOffset;
				double ovse = viewTo + samplesOffset;
				pan(ovss, ovse);
				lastMousePosition = e.getLocationOnScreen();
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void configure() {
		TuneEditorGridConfigDialog d = new TuneEditorGridConfigDialog(this);
		DialogUtils.centerDialog((JFrame)mf, d);
		d.setVisible(true);
	}
	
	public TrackItemPanel addTrackItem(TrackItem ti) {
		TrackItemPanel tip = new TrackItemPanel(this, ti);
		tip.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		tip.setInheritsPopupMenu(true);
		return (TrackItemPanel)add(tip);
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		popupMenu.removeAll();	//move popup menu building to another class?
		final Track t = getTrackAt(getMousePosition());
		final Collection<TrackItemPanel> tipl = getTrackItemPanelsAt(getMousePosition());
		if(tipl != null && !tipl.isEmpty()) {
			popupMenu.add("Items: " + tipl.size());
			if(tipl.size() == 1) {
				popupMenu.add(new AbstractAction("Remove") {
					@Override
					public void actionPerformed(ActionEvent e) {
						removeTrackItemPanel(tipl.iterator().next());
					}
				});
				popupMenu.add(new AbstractAction("Properities") {
					@Override
					public void actionPerformed(ActionEvent paramActionEvent) {
						DialogUtils.showTrackItemPanelDialog(mf, tipl.iterator().next());
					}
				});
				popupMenu.add(new AbstractAction("Edit...") {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						TrackItemPanel item = tipl.iterator().next();
						if(item == null || item.getWaveFile() == null) {
							this.setEnabled(false);
							return;
						}
						MFOkCancelDialog d = new MFOkCancelDialog(mf) {
							@Override
							protected void okClicked() {
							}
							@Override
							protected void cancelCliked() {
							}
						};
						WaveEditorPanel wep = new WaveEditorPanel();
						try {
							wep.setWaveFile(item.getWaveFile());
						} catch (UnsupportedAudioFileException | IOException e) {
							e.printStackTrace();
						}
						d.add(wep);
						d.pack();
						DialogUtils.centerScreenDialog(d);
						d.setVisible(true);
					}
				});
			}
		} else {
			popupMenu.add("Track: " + t.toString());
			popupMenu.add(new AbstractAction("Remove") {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeTrack(t);
				}
			});
		}
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// TODO Auto-generated method stub
		
	}

}