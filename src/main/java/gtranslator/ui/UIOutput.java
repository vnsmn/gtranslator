package gtranslator.ui;

import gtranslator.Actions;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
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

	private UIOutput(int weigth, int height) {
		frame = new JFrame();
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
		frame.setVisible(true);
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

	public void setWaitCursor() {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public void setDefCursor() {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
			new Actions.DisposeAppAction().execute(null);
		}
	}

	public void setCookie(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_INIT_COOKIE_SETUP, null, s);
	}

	public void setStatistic(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_INIT_STATISTIC_SETUP,
				null, s);
	}

	public void setDictionaryDir(String s) {
		firePropertyChange(Constants.PROPERTY_CHANGE_INIT_DICTIONARY_DIR_SETUP,
				null, s);
	}

	public void setHistory(boolean b) {
		firePropertyChange(Constants.PROPERTY_CHANGE_INIT_HISTORY_SETUP, null,
				b);
	}

	public void setSound(boolean b) {
		firePropertyChange(Constants.PROPERTY_CHANGE_INIT_SOUND_SETUP, null, b);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
				evt.getNewValue());
	}
}
