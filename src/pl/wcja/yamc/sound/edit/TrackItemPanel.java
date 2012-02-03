package pl.wcja.yamc.sound.edit;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import pl.wcja.yamc.gui.MFProgressDialog;
import pl.wcja.yamc.sound.Track;
import pl.wcja.yamc.sound.TrackItem;
import pl.wcja.yamc.sound.file.DecoderManager;
import pl.wcja.yamc.utils.DialogUtils;
import pl.wcja.yamc.utils.RandomGenerator;

import com.sun.media.sound.WaveFileReader;

/**
 * Track item panel to be viewed and operated in TuneEditorGrid
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public class TrackItemPanel extends WaveEditorPanel implements DropTargetListener {

	private TuneEditorGrid editor = null;
	private TrackItem trackItem = null;
	private DropTarget dropTarget = null;
	private DataFlavor dataFlavor = new DataFlavor("audio/x-wav", "WAVE sound");
	private DataFlavor fileListDF = new DataFlavor("application/x-java-file-list; class=java.util.List","application/x-java-file-list");
	private Track lastTrack = null;
	private int edgeMargin = 4;		//4px edge margin for resizing in tuneEditor
	
	protected enum EDGE_MOVING {
		NONE,
		LEFT,
		RIGHT
	}
	
	private EDGE_MOVING edgeMoving = EDGE_MOVING.NONE;
	
	/**
	 * 
	 * @param egp
	 * @param trackItem
	 */
	public TrackItemPanel(TuneEditorGrid egp, TrackItem trackItem) {
		super();
		this.editor = egp;
		this.trackItem = trackItem;
		dropTarget = new DropTarget(this, this);
		setDropTarget(dropTarget);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public void setTrackItem(TrackItem trackItem) {
		this.trackItem = trackItem;
	}
	
	public TrackItem getTrackItem() {
		return trackItem;
	}
	
	/**
	 * TODO - pobraæ 'przeskalowane' aby gra³ szaybciej/wolniej?
	 * @return
	 */
	public byte[] getBytesScaled(double trackSecondStart, int framesCount) {
		double ratio = trackItem.getLenght() / (visibleEnd - visibleStart);
		if(ratio == 1) {
			//pobieramy normalnie
		} else {
			 double endTime = trackSecondStart + sampleToSecond(framesCount);
		}
		//TODO chyba cos tu...
	}
	
	@Override
	public void setWaveFile(File waveFile) throws UnsupportedAudioFileException, IOException {
		audioFileFormat = new WaveFileReader().getAudioFileFormat(waveFile);
		if(!audioFileFormat.getFormat().matches(editor.getMainFrame().getMixer().getMixAudioFormat())) {
			DialogUtils.showError(editor, 
					String.format("For the moment the sample MUST match: %s\r\n" +
							"and you've tried to load: %s",
							editor.getMainFrame().getMixer().getMixAudioFormat().toString(),
							audioFileFormat), "Error");
		} else {
			super.setWaveFile(waveFile);
		}
		if(trackItem != null) {
			trackItem.setLenght(getTotalSecondLength());
		}
		editor.rearrangeTracks();
		editor.repaint();
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
		}
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
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
								File in = fl.get(0);
								MFProgressDialog pd = new MFProgressDialog(editor.getMainFrame(), "Decoding...", "Trying to decode " + in.getAbsolutePath());
								pd.pack();
								DialogUtils.centerDialog((JFrame) editor.getMainFrame(), pd);
								pd.setVisible(true);
								testFile = new File(System.getProperty("user.dir") + "\\" + in.getName().substring(0, in.getName().lastIndexOf('.'))+ RandomGenerator.getRandomString() + ".wav");
								testFile = DecoderManager.getInstance().decode(in, testFile, pd);
								if (testFile != null) {
									setWaveFile(testFile);
								} else {
									setWaveFile(in);
								}
								pd.dispose();
							} catch (UnsupportedFlavorException e) {
								e.printStackTrace();
								DialogUtils.showError(TrackItemPanel.this, e.toString(), "Error");
							} catch (IOException e) {
								e.printStackTrace();
								DialogUtils.showError(TrackItemPanel.this, e.toString(), "Error");
							} catch (UnsupportedAudioFileException e) {
								e.printStackTrace();
								DialogUtils.showError(TrackItemPanel.this, e.toString(), "Error");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
				DialogUtils.showError(TrackItemPanel.this, e.toString(), "Error");
			} catch (IOException e) {
				e.printStackTrace();
				DialogUtils.showError(TrackItemPanel.this, e.toString(), "Error");
			}
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		int onMask = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			if(lastMousePosition == null) {
				lastMousePosition = e.getLocationOnScreen();
			} else {
				Track t = editor.getTrackAt(editor.getMousePosition());
				if(t != lastTrack) {
					trackItem.getTrack().removeItem(trackItem);
					t.addItem(trackItem);
				}
				double offsetx = lastMousePosition.getX() - e.getLocationOnScreen().getX();
				double timeOffset = editor.pixelToSecond(offsetx);
				trackItem.setTimeFrom(trackItem.getTimeFrom() - timeOffset);
				trackItem.setTimeTo(trackItem.getTimeTo() - timeOffset);
				lastMousePosition = e.getLocationOnScreen();
				editor.rearrangeTracks();
				return;
			}
		}
		onMask = MouseEvent.BUTTON1_DOWN_MASK;
		offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask && (e.getX() <= edgeMargin || edgeMoving == EDGE_MOVING.LEFT)) {
			if(lastMousePosition == null) {
				lastMousePosition = e.getLocationOnScreen();
				edgeMoving = EDGE_MOVING.LEFT;
			} else {
				double offsetx = lastMousePosition.getX() - e.getLocationOnScreen().getX();
				double timeOffset = editor.pixelToSecond(offsetx);
				if(getWaveFile() == null || trackItem.getLenght() + timeOffset <= getTotalSecondLength()) {
					trackItem.setTimeFrom(trackItem.getTimeFrom() - timeOffset);
					lastMousePosition = e.getLocationOnScreen();
					editor.rearrangeTracks();
				}
			}
		} else if((e.getModifiersEx() & (onMask | offMask)) == onMask && (getWidth() - e.getX() <= edgeMargin || edgeMoving == EDGE_MOVING.RIGHT)) {
			if(lastMousePosition == null) {
				lastMousePosition = e.getLocationOnScreen();
				edgeMoving = EDGE_MOVING.RIGHT;
			} else {
				double offsetx = lastMousePosition.getX() - e.getLocationOnScreen().getX();
				double timeOffset = editor.pixelToSecond(offsetx);
				if(getWaveFile() == null || trackItem.getLenght() - timeOffset <= getTotalSecondLength()) {
					trackItem.setTimeTo(trackItem.getTimeTo() - timeOffset);
					lastMousePosition = e.getLocationOnScreen();
					editor.rearrangeTracks();
				}
			}
		}
		super.mouseDragged(e);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		editor.setFocusedTrackItemPanel(this);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		if(getWaveFile() != null) {
			setToolTipText(
				String.format("<html>File: %s<br>" +
						"Lenght: %ss (%s samples)<br>" +
						"Location: %ss - %ss<br>" +
						"Viewing: %ss - %ss", 
					getWaveFile().getAbsolutePath(), 
					getTotalSecondLength(), totalSamples,
					trackItem.getTimeFrom(), trackItem.getTimeTo(), 
					sampleToSecond(viewFromFrame), sampleToSecond(viewToFrame)));
		} else {
			setToolTipText("Empty");
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if(e.getX() <= edgeMargin) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else if(getWidth() - e.getX() <= edgeMargin) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public void mouseExit(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void mouseReleased(MouseEvent e) {
		edgeMoving = EDGE_MOVING.NONE;
		super.mouseReleased(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int onMask = MouseEvent.ALT_DOWN_MASK;
		int offMask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;
		if((e.getModifiersEx() & (onMask | offMask)) == onMask) {
			super.mouseWheelMoved(e);
		} else {
			editor.mouseWheelMoved(e);	
		}
	}
}