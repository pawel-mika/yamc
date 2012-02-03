package pl.wcja.yamc.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;

public class MarkableScalableComponent extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener{

	protected double viewFrom = 0;
	protected double viewTo = 0;
	protected double markerLocation = 0;
	protected Point lastMousePosition = null;
	private double unitPerPixel = 0;
	
	protected Color colorBackground = Color.white; 
	protected Color colorForeground = Color.blue;
	protected Color colorMarker = Color.red;
	protected Color colorText = Color.black;

	protected void recalculateUnitPexPixel() {
		unitPerPixel = (viewTo - viewFrom) / getWidth();
	}
	
	protected Point pixelToUnit(Point point) {
		return null;
	}
	
	protected Point unitToPixel(Point unit) {
		return null;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	
}
