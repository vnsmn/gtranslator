package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.Actions.CookieAction;
import gtranslator.Actions.DetailTranslateAction;
import gtranslator.Actions.ModeTClipboardAction;
import gtranslator.Actions.RewriteHistoryAction;
import gtranslator.Actions.StartStopTClipboardAction;
import gtranslator.Actions.UseHistoryAction;
import gtranslator.Actions.WordPlayOfClipboardAction;
import gtranslator.ClipboardObserver;
import gtranslator.ClipboardObserver.MODE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class UISetupBuilder extends UIBuilder implements PropertyChangeListener {
	Box box;
	Border lineBorder;

	private JCheckBox activityClipboardCheckBox;
	private JTextField cookieField;
	private JTextField dictionaryDirField;
	private JCheckBox historyCheckBox;
	private JCheckBox soundCheckBox;
	private JLabel statisticLabel;
	private Map<MODE, JRadioButton> modeWidgets = new HashMap<>();

	public void build(JPanel panel) {
		box = Box.createVerticalBox();
		panel.add(box, BorderLayout.NORTH);
		lineBorder = BorderFactory.createLineBorder(Color.GRAY);

		createWidgetOfStatistic();
		createWidgetOfDictionaryDir();
		createWidgetsOfDetailTranslate();
		createWidgetsOfRewriteHistory();
		createWidgetsOfUseHistory();
		createWidgetsOfWordPlayOfClipboardHistory();
		createWidgetsOfActivityClipboardTranslate();
		createWidgetsOfModeClipboard();
		createWidgetOfCookie();
	}

	private void createWidgetOfStatistic() {
		statisticLabel = new JLabel();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Statistic"));
		panel.add(statisticLabel, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfDetailTranslate() {
		JCheckBox checkBox = new JCheckBox("No");
		checkBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
				Actions.findAction(DetailTranslateAction.class).execute(
						check.isSelected());
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Is detail translation?"));
		panel.add(checkBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfRewriteHistory() {
		JCheckBox checkBox = new JCheckBox("No");
		checkBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
				Actions.findAction(RewriteHistoryAction.class).execute(
						check.isSelected());
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do rewrite history?"));
		panel.add(checkBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfUseHistory() {
		historyCheckBox = new JCheckBox("No");
		historyCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
				Actions.findAction(UseHistoryAction.class).execute(
						check.isSelected());
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do use history?"));
		panel.add(historyCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfWordPlayOfClipboardHistory() {
		soundCheckBox = new JCheckBox("No");
		soundCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
				Actions.findAction(WordPlayOfClipboardAction.class).execute(
						check.isSelected());
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do word sound?"));
		panel.add(soundCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetOfCookie() {
		cookieField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Cookie"));
		panel.add(cookieField, BorderLayout.NORTH);
		final JButton button = new JButton("apply");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(CookieAction.class).execute(
						cookieField.getText());
			}
		});
		panel.add(button, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetOfDictionaryDir() {
		dictionaryDirField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Dictionary dir"));
		panel.add(dictionaryDirField, BorderLayout.NORTH);
		final JButton button = new JButton("apply");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(CookieAction.class).execute(
						dictionaryDirField.getText());
			}
		});
		panel.add(button, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfActivityClipboardTranslate() {
		activityClipboardCheckBox = new JCheckBox("Start");
		activityClipboardCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Actions.findAction(StartStopTClipboardAction.class)
								.execute(!activityClipboardCheckBox.isSelected());
						firePropertyChange(
								Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD,
								!activityClipboardCheckBox.isSelected(),
								activityClipboardCheckBox.isSelected());
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do start/stop clipboard?"));
		panel.add(activityClipboardCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfModeClipboard() {
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MODE mode = MODE.valueOf(e.getActionCommand());
				Actions.findAction(ModeTClipboardAction.class).execute(mode);
				UISetupBuilder.this.firePropertyChange(Constants.PROPERTY_CHANGE_MODE_CLIPBOARD,
						null, mode);
			}
		};

		JRadioButton textButton = new JRadioButton("Select without lost focus");
		textButton.setMnemonic(KeyEvent.VK_C);
		textButton.setActionCommand(ClipboardObserver.MODE.TEXT.name());
		textButton.addActionListener(actionListener);
		modeWidgets.put(MODE.TEXT, textButton);		
		
		JRadioButton selButton = new JRadioButton("Select with lost focus");
		selButton.setMnemonic(KeyEvent.VK_B);
		selButton.setActionCommand(ClipboardObserver.MODE.SELECT.name());
		selButton.setSelected(true);
		selButton.addActionListener(actionListener);
		modeWidgets.put(MODE.SELECT, selButton);


		JRadioButton copyButton = new JRadioButton("Copy");
		copyButton.setMnemonic(KeyEvent.VK_D);
		copyButton.setActionCommand(ClipboardObserver.MODE.COPY.name());
		copyButton.addActionListener(actionListener);
		modeWidgets.put(MODE.COPY, copyButton);

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(selButton);
		group.add(textButton);
		group.add(copyButton);

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Mode clipboard"));
		
		panel.add(textButton);
		panel.add(selButton);		
		panel.add(copyButton);
		box.add(panel);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this) {
			return;
		}
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD:
			activityClipboardCheckBox.setSelected((Boolean) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD:
			JRadioButton button = modeWidgets.get((MODE) evt.getNewValue());
			button.setSelected(true);
			break;
		case Constants.PROPERTY_CHANGE_COOKIE:
			cookieField.setText((String) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_DIR:
			dictionaryDirField.setText((String) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_HISTORY:
			historyCheckBox.setSelected((Boolean) evt.getNewValue());
			historyCheckBox
					.setText(historyCheckBox.isSelected() ? "Yes" : "No");
			break;
		case Constants.PROPERTY_CHANGE_SOUND:
			soundCheckBox.setSelected((Boolean) evt.getNewValue());
			soundCheckBox.setText(soundCheckBox.isSelected() ? "Yes" : "No");
			break;
		case Constants.PROPERTY_CHANGE_STATISTIC:
			statisticLabel.setText((String) evt.getNewValue());
			break;
		}
	}
}
