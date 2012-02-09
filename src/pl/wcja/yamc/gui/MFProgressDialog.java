package pl.wcja.yamc.gui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import pl.wcja.yamc.event.ProgressEvent;
import pl.wcja.yamc.event.ProgressListener;
import pl.wcja.yamc.frame.IMainFrame;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe� Mika</a>, Geomar SA
 *
 */
public class MFProgressDialog extends MFDialog implements ProgressListener {

	private JProgressBar jpb = new JProgressBar(0, 100);
	
	public MFProgressDialog(IMainFrame mf, String title, String description) {
		super(mf);
		setTitle(title);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		add(new JLabel(description));
		jpb.setStringPainted(true);
		add(jpb);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6103906785499165477L;

	@Override
	public void progressChanged(final ProgressEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(e.getTotalParts() > 0) {
					jpb.setValue(e.getDoneParts() * 100 / e.getTotalParts());					
				} else {
					jpb.setString("" + e.getDoneParts());
				}
				if (e.getDoneParts() == e.getTotalParts()) {
					dispose();
				}
			}
		});
	}
}