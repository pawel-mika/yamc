package pl.wcja.yamc.sound.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JButton;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.plugin.ToolBarEntryPlugin;
import pl.wcja.yamc.utils.DialogUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class SaveProject extends ToolBarEntryPlugin {

	private JButton jbSave = null;
	
	public SaveProject(IMainFrame mf) {
		super(mf);
		jbSave = new JButton("Save");
		jbSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveProject();
			}
		});
	}

	@Override
	public Component getToolbarComponent() {
		return jbSave;
	}

	@Override
	public int getOffsetModifier() {
		return 0;
	}

	@Override
	public String getMenu() {
		return "File";
	}

	@Override
	public String getSubmenu() {
		return null;
	}

	@Override
	public String getEntryName() {
		return "Save proejct...";
	}

	@Override
	public void entrySelected() {
		saveProject();
	}
	
	private void saveProject() {
		File f = DialogUtils.selectSaveFile((Component)mf);
		if(f != null) {
			try{
				FileOutputStream fos = new FileOutputStream(f);
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				ObjectOutputStream o = new ObjectOutputStream(b);
				o.writeObject(mf.getTuneEditor().getTune());
				fos.write(b.toByteArray());
				o.close();
				b.close();
				fos.flush();
				fos.close();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
}
