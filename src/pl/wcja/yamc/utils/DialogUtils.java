package pl.wcja.yamc.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.gui.MFOkCancelDialog;
import pl.wcja.yamc.sound.edit.TrackItemPanel;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class DialogUtils {

	public static void centerScreenDialog(Component comp) {
		Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(ss.getWidth() / 2) - (comp.getWidth() / 2); 
		int y = (int)(ss.getHeight() / 2) - (comp.getHeight() / 2);
		comp.setLocation(x, y);
	}
	
	public static void centerDialog(Component parent, Component comp) {
		Rectangle pb = parent.getBounds();
		int x = (int)(pb.getWidth() / 2) - (comp.getWidth() / 2) + (int)pb.getX(); 
		int y = (int)(pb.getHeight() / 2) - (comp.getHeight() / 2) + (int)pb.getY();
		comp.setLocation(x, y);
	}
	
	public static JPanel getFlowLayoutPanelRow(int align, Component ... components) {
		JPanel p = new JPanel(new FlowLayout(align));
		for(Component c : components) {
			p.add(c);
		}
		return p;
	}	
	
	public static JPanel getBoxLayoutPanelRow(int align, Component ... components) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, align));
		for(Component c : components) {
			p.add(c);
		}
		return p;
	}
	
	public static boolean showYesNoDialog(Component parent, String message, String title) {
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}
	
	public static void showError(Component parent, String message, String title) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public static File selectSaveFile(Component parent) {
		JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));		
		if(jfc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile();
		}
		return null;
	}
	
	public static File selectFile(Component parent) {
		JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));		
		if(jfc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			return jfc.getSelectedFile();
		}
		return null;
	}
	
	public static void showTrackItemPanelDialog(IMainFrame mf, TrackItemPanel tip) {
		MFOkCancelDialog d = new MFOkCancelDialog(mf) {
			
			@Override
			protected void okClicked() {
			}
			
			@Override
			protected void cancelCliked() {
			}
		};
		centerDialog((Component)mf, d);
		d.setVisible(true);
	}
}
