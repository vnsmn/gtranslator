package gtranslator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class GuiOutput {
	private JFrame frame;
	private JTextArea sourceArea;
	private JTextArea targetArea;
	private JPopupMenu sourcePopupMenu;
	private JSplitPane splitPane;
	private JTabbedPane tabbedPane;
	private JPanel setupPanel;
	JTextField cookieField;
	JTextField dictionaryField;
	private Map<ACTION_TYPE, ActionListener> actionListeners = new HashMap<>();
	private static GuiOutput INSTANCE;
	
	private JCheckBox usingHistoryCheckBox;
	private JCheckBox soundCheckBox;
	private JLabel statisticLabel;

	public enum ACTION_TYPE {
		FIXED, START_STOP, MODE_SELECT, ADDITION_INFO, COOKIE, DISPOSE, 
		REWRITE_HISTORY, CLEAN_HISTORY, USE_HISTORY, STATISTIC, DICTIONARY,
		SOUND, IS_SOUND
	}

	public abstract static class ActionListener {
		public void execute(GuiOutput source) {
		}

		public void execute(String s) {
		}

		public void execute(boolean b) {
		}
	}

	private GuiOutput(int weigth, int height) {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapterExt());
		tabbedPane = new JTabbedPane();
		Font font = new Font("Serif", Font.ITALIC, 10);
		tabbedPane.setFont(font);
		frame.setSize(weigth, height);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		sourceArea = new JTextArea();
		sourceArea.addMouseListener(new MouseAdapterExt());
		sourceArea.setLineWrap(true);
		JScrollPane sourceScrollPane = new JScrollPane();
		sourceScrollPane.add(sourceArea);
		sourceScrollPane.setViewportView(sourceArea);

		targetArea = new JTextArea();
		targetArea.setLineWrap(true);
		JScrollPane targetScrollPane = new JScrollPane();
		targetScrollPane.add(targetArea);
		targetScrollPane.setViewportView(targetArea);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourceScrollPane,
				targetScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		tabbedPane.add(splitPane, "translate");

		sourcePopupMenu = new JPopupMenu("Translate");
		JMenuItem it = new JMenuItem("delete history");
		it.addActionListener(new java.awt.event.ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionListener actionListener = actionListeners
						.get(ACTION_TYPE.CLEAN_HISTORY);
				if (actionListener != null) {
					actionListener.execute(INSTANCE);
				}				
			}
		});		
		sourcePopupMenu.add(it);
		it = new JMenuItem("sound");
		it.addActionListener(new java.awt.event.ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionListener actionListener = actionListeners
						.get(ACTION_TYPE.SOUND);
				if (actionListener != null) {
					actionListener.execute(INSTANCE.getSourceText());
				}				
			}
		});		
		sourcePopupMenu.add(it);
		sourceArea.setComponentPopupMenu(sourcePopupMenu);		

		setupPanel = new JPanel();
		setupPanel.setLayout(new BorderLayout());
		JScrollPane setupScrollPane = new JScrollPane();
		setupScrollPane.add(setupPanel);
		setupScrollPane.setViewportView(setupPanel);		
		tabbedPane.add(setupScrollPane, "setup");

		//----------------------------------------------------------------------------------------//
		Box box = Box.createVerticalBox();
		setupPanel.add(box, BorderLayout.NORTH);
		Border lineBorder = BorderFactory.createLineBorder(Color.GRAY);
		//----------------------------------------------------------------------------------------//
		statisticLabel = new JLabel();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Statistic"));
		panel.add(statisticLabel, BorderLayout.NORTH);
		box.add(panel);		
		//----------------------------------------------------------------------------------------//
		addCheckBox("Stop-Yes/Start-No", ACTION_TYPE.START_STOP, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		addCheckBox("Is selecting mode", ACTION_TYPE.MODE_SELECT, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		addCheckBox("Is addition translate", ACTION_TYPE.ADDITION_INFO, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		addCheckBox("Is rewriting history", ACTION_TYPE.REWRITE_HISTORY, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		usingHistoryCheckBox = addCheckBox("Is using history", ACTION_TYPE.USE_HISTORY, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		soundCheckBox = addCheckBox("Is sound", ACTION_TYPE.IS_SOUND, box, lineBorder);
		//----------------------------------------------------------------------------------------//
		cookieField = addTextField("Cookie", "apply", ACTION_TYPE.COOKIE, box, lineBorder);		
		//----------------------------------------------------------------------------------------//
		dictionaryField = addTextField("Dictionary", "run", ACTION_TYPE.DICTIONARY, box, lineBorder);
		//----------------------------------------------------------------------------------------//		
		// frame.pack(); если размер устанавливается внутренними компонентами
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public void init(ACTION_TYPE type, Object value) {
		switch (type) {
			case COOKIE:
				cookieField.setText((String) value);	
			break;
			case USE_HISTORY:
				usingHistoryCheckBox.setSelected((Boolean) value);
				usingHistoryCheckBox.setText(usingHistoryCheckBox.isSelected() ? "Yes" : "No");
			break;
			case STATISTIC:
				statisticLabel.setText("Count words/phrases: " + value.toString());
			break;
			case DICTIONARY:
				dictionaryField.setText((String) value);
			break;
			case IS_SOUND:
				soundCheckBox.setSelected((Boolean) value);
				soundCheckBox.setText(soundCheckBox.isSelected() ? "Yes" : "No");
				break;
		default:
			break;
		}
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

	public void putActionListener(ACTION_TYPE key, ActionListener actionListener) {
		this.actionListeners.put(key, actionListener);
	}

	public void setSourceText(String txt) {
		sourceArea.setText(txt);
	}

	public String getSourceText() {
		return sourceArea.getText();
	}

	public void setTargetText(String txt) {
		targetArea.setText(txt);
	}

	public void selectTranslatePanel() {
		tabbedPane.setSelectedIndex(0);
	}
	
	public String getDictionaryDirPath() {
		return dictionaryField.getText();
	}

	public static GuiOutput createAndShowGUI() {
		synchronized (GuiOutput.class) {
			if (INSTANCE == null) {
				INSTANCE = new GuiOutput(200, 200);
			}
		}
		INSTANCE.show();
		return INSTANCE;
	}

	public static void dispose() {
		INSTANCE.frame.dispose();
		INSTANCE = null;
	}
	
	private JCheckBox addCheckBox(String title, final ACTION_TYPE type, Box box, Border lineBorder) {
		JCheckBox checkBox = new JCheckBox("No");
		checkBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox)e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
				ActionListener actionListener = actionListeners.get(type);
				if (actionListener != null) {
					actionListener.execute(check.isSelected());
				}
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, title));
		panel.add(checkBox, BorderLayout.WEST);
		box.add(panel);
		return checkBox;
	}
	
	private JTextField addTextField(String title, String buttonText, final ACTION_TYPE type, Box box, Border lineBorder) {
		JTextField field = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, title));
		panel.add(field, BorderLayout.NORTH);
		JButton button = new JButton(buttonText);
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ActionListener actionListener = actionListeners
						.get(type);
				if (actionListener != null) {
					actionListener.execute(field.getText());
				}
			}
		});
		panel.add(button, BorderLayout.WEST);
		box.add(panel);
		return field;
	}

	private class WindowAdapterExt extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			ActionListener actionListener = actionListeners
					.get(ACTION_TYPE.DISPOSE);
			if (actionListener != null) {
				actionListener.execute(INSTANCE);
			}
		}
	}

	private class MouseAdapterExt extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			ActionListener actionListener = actionListeners
					.get(ACTION_TYPE.FIXED);
			if (e.getClickCount() == 2 && actionListener != null) {
				actionListener.execute(INSTANCE);
			}
		}
	}	
}
