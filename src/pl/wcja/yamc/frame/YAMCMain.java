package pl.wcja.yamc.frame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pl.wcja.yamc.gui.MainFrame;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class YAMCMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			private MainFrame mf = null;

			public void run() {				
				WindowsLookAndFeel wlaf = new WindowsLookAndFeel();
				try{
					UIManager.setLookAndFeel(wlaf);
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
				mf = new MainFrame();
				mf.setVisible(true);
			}
		});
	}

}
