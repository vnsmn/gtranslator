package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.ClipboardObserver;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class UIOutput extends UIBuilder implements PropertyChangeListener {
	private JFrame frame;
	private static UIOutput INSTANCE;
	private JTextArea sourceArea;
	private JTextArea targetArea;
	private JTabbedPane tabbedPane;
	private JPanel glass;
	private int cnt = 0;

	private UIOutput(int weigth, int height) {
		frame = new JFrame();
		frame.setIconImage(
				new ImageIcon(this.getClass().getClassLoader().getResource("fish.png")).getImage());
		frame.setTitle("gtranslator");
		frame.addWindowListener(new WindowAdapterExt());
		tabbedPane = new JTabbedPane();		
		Font font = new Font("Serif", Font.ITALIC, 10);
		tabbedPane.setFont(font);
		frame.setSize(weigth, height);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		sourceArea = new JTextArea();
		sourceArea.setLineWrap(true);
		JScrollPane sourceScrollPane = new JScrollPane();
		sourceScrollPane.add(sourceArea);
		sourceScrollPane.setViewportView(sourceArea);

		targetArea = new JTextArea();
		targetArea.setLineWrap(true);
		JScrollPane targetScrollPane = new JScrollPane();
		targetScrollPane.add(targetArea);
		targetScrollPane.setViewportView(targetArea);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				sourceScrollPane, targetScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		tabbedPane.add(splitPane, "translate");
		JPopupMenu sourcePopupMenu = new JPopupMenu("Translate");
		sourceArea.setComponentPopupMenu(sourcePopupMenu);
		UITransBuilder uiTransBuilder = new UITransBuilder();
		uiTransBuilder.build(sourceArea, targetArea, sourcePopupMenu);

		JPanel setupPanel = new JPanel();
		setupPanel.setLayout(new BorderLayout());
		JScrollPane setupScrollPane = new JScrollPane();
		setupScrollPane.add(setupPanel);
		setupScrollPane.setViewportView(setupPanel);
		tabbedPane.add(setupScrollPane, "setup");
		UISetupBuilder uiSetupBuilder = new UISetupBuilder();
		uiSetupBuilder.build(setupPanel);

		JPanel dicPanel = new JPanel();
		dicPanel.setLayout(new BorderLayout());
		JScrollPane dicScrollPane = new JScrollPane();
		dicScrollPane.add(dicPanel);
		dicScrollPane.setViewportView(dicPanel);
		tabbedPane.add(dicScrollPane, "dict");
		UIDicBuilder uiDicBuilder = new UIDicBuilder();
		uiDicBuilder.build(dicPanel);

		uiTransBuilder.addPropertyChangeListener(this);
		uiSetupBuilder.addPropertyChangeListener(this);
		uiDicBuilder.addPropertyChangeListener(this);

		addPropertyChangeListener(uiTransBuilder);
		addPropertyChangeListener(uiSetupBuilder);
		addPropertyChangeListener(uiDicBuilder);

		// frame.pack(); если размер устанавливается внутренними компонентами
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		ImageIcon loading = new ImageIcon(this.getClass().getClassLoader().getResource("loading.gif"));
		Image img = loading.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT);
		loading.setImage(img);
		glass = (JPanel)frame.getGlassPane();
		JLabel label = new JLabel("loading... ", loading, JLabel.CENTER);
		label.setOpaque(false);
		glass.setLayout(new GridBagLayout());
		glass.add(label);
		glass.setOpaque(false);
		frame.setVisible(true);
	}
	
	public void showWaitCursor() {
		cnt++;		
		glass.setVisible(true);
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void hideWaitCursor() {
		if (--cnt <= 0) {
			closeWaitCursor();			
		}
	}
	
	public void closeWaitCursor() {
		cnt = 0;
		glass.setVisible(false);
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void show() {
		if (frame.getState() != JFrame.NORMAL) {
			frame.setState(JFrame.NORMAL);
		}
		frame.setVisible(true);
	}

	public void hide() {
		frame.setVisible(false);
	}

	public void setSourceText(String s) {
		sourceArea.setText(s);
	}

	public String getSourceText() {
		return sourceArea.getText();
	}

	public void setTargetText(String s) {
		targetArea.setText(s);
	}

	public void selectTranslatePanel() {
		tabbedPane.setSelectedIndex(0);
	}
	
	public static UIOutput getInstance() {
		synchronized (UIOutput.class) {
			if (INSTANCE == null) {
				INSTANCE = new UIOutput(200, 200);
			}
		}
		INSTANCE.show();
		return INSTANCE;
	}

	public static void dispose() {
		INSTANCE.frame.dispose();
		INSTANCE = null;
	}

	private class WindowAdapterExt extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			Actions.findAction(Actions.DisposeAppAction.class).execute(null);
		}
	}

	public void setCookie(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_COOKIE, null, s);
	}

	public void setStatistic(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_STATISTIC, null, s);
	}

	public void setDictionaryDir(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_DICTIONARY_DIR, null, s);
	}

	public void setHistory(boolean b) {
		firePropertyChange(Constants.PROPERTY_CHANGE_HISTORY, null, b);
	}

	public void setSound(boolean b) {
		firePropertyChange(Constants.PROPERTY_CHANGE_SOUND, null, b);
	}
	
	public void setActivityClipboard(boolean b) {
		firePropertyChange(Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD, null, b);
	}
	
	public void setModeClipboard(ClipboardObserver.MODE mode) {
		firePropertyChange(Constants.PROPERTY_CHANGE_MODE_CLIPBOARD, null, mode);
	}
	
	public void setDictionaryResultDir(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_DICTIONARY_RESULT_DIR, null, s);
	}
	
	public void setDictionaryBlockLimit(int i) {
		firePropertyChange(Constants.PROPERTY_CHANGE_DICTIONARY_BLOCK_LIMIT, null, i);
	}
	
	public void setDictionaryPronunciation(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_DICTIONARY_PRONUNCIATION, null, s);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		redirectPropertyChange(evt);
	}
}
