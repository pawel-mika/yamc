package pl.wcja.yamc.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import pl.wcja.yamc.frame.IMainFrame;
import pl.wcja.yamc.utils.DialogUtils;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public class LoginDialog extends MFDialog {
	protected IMainFrame mf = null;
	private JLabel jlLogin;
	private JLabel jlPass;
	private JTextField jtfLogin;
	private JPasswordField jpfPass;
	private JCheckBox jcbStart;
	private JCheckBox jcbRemember;
	private JButton jbOk;
	private JButton jbCancel;
	private JButton jbRegister;
	private String[] loginAndPass = null;
	
	/**
	 * 
	 * @param mf
	 */
	public LoginDialog(IMainFrame mf) {
		super(mf);
		buildGUI();
		setTitle("Login");
		pack();
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loginAndPass = new String[2];
				loginAndPass[0] = jtfLogin.getText();
				loginAndPass[1] = new String(jpfPass.getPassword());
				dispose();
			}
		});
		
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}	
	
	public String[] showLoginDialog() {
		setVisible(true);
		return loginAndPass; 
	}
	
	public void buildGUI() {
		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
		jp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		jlLogin = new JLabel("Login");
		jp.add(DialogUtils.getFlowLayoutPanelRow(FlowLayout.LEFT, jlLogin));
		jtfLogin = new JTextField();
		jp.add(DialogUtils.getBoxLayoutPanelRow(BoxLayout.X_AXIS, jtfLogin));
		jlPass = new JLabel("Password");
		jp.add(DialogUtils.getFlowLayoutPanelRow(FlowLayout.LEFT, jlPass));
		jpfPass = new JPasswordField();
		jp.add(DialogUtils.getBoxLayoutPanelRow(BoxLayout.X_AXIS, jpfPass));
		jcbStart = new JCheckBox("Start with system");
		jp.add(DialogUtils.getFlowLayoutPanelRow(FlowLayout.LEFT, jcbStart));
		jcbRemember = new JCheckBox("Remember me");
		jp.add(DialogUtils.getFlowLayoutPanelRow(FlowLayout.LEFT, jcbRemember));
		
		jbRegister = new JButton("Register");
		jbOk = new JButton("Login");
		jbCancel = new JButton("Cancel");
		jp.add(DialogUtils.getFlowLayoutPanelRow(FlowLayout.RIGHT, jbOk, jbRegister, jbCancel));
		
		setContentPane(jp);
	}
}
