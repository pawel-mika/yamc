package pl.wcja.yamc.sound.edit.tools;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 *
 */
public abstract class BaseMouseTool implements MouseMotionListener, MouseListener, MouseWheelListener{

	protected IMainFrame mf = null;
	
	public BaseMouseTool(IMainFrame mf) {
		this.mf = mf;
	}
	
	public void activate() {
		mf.getTuneEditor().addMouseListener(this);
		mf.getTuneEditor().addMouseMotionListener(this);
		mf.getTuneEditor().addMouseWheelListener(this);
	}
	
	public void unactivate() {
		mf.getTuneEditor().removeMouseListener(this);
		mf.getTuneEditor().removeMouseMotionListener(this);
		mf.getTuneEditor().removeMouseWheelListener(this);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}
	
}
