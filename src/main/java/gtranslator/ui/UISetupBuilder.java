package gtranslator.ui;

import gtranslator.Actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class UISetupBuilder extends UIBuilder implements PropertyChangeListener {
	Box box;
	Border lineBorder;

	private JCheckBox activityClipboardCheckBox;
	private JCheckBox modeClipboardCheckBox;
	private JTextField cookieField;
	private JTextField dictionaryDirField;
	private JCheckBox historyCheckBox;
	private JCheckBox soundCheckBox;
	private JLabel statisticLabel;

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
		createWidgetsOfModeClipboardTranslate();
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
				new Actions.DetailTranslateAction().execute(check.isSelected());
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
				new Actions.RewriteHistoryAction().execute(check.isSelected());
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
				new Actions.UseHistoryAction().execute(check.isSelected());
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
				new Actions.WordPlayOfClipboardAction().execute(check
						.isSelected());
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
				new Actions.CookieAction().execute(cookieField.getText());
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
				new Actions.CookieAction().execute(dictionaryDirField.getText());
			}
		});
		panel.add(button, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfActivityClipboardTranslate() {
		activityClipboardCheckBox = new JCheckBox("Stop");
		activityClipboardCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						changeActivityClipboard(activityClipboardCheckBox.isSelected());
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do start/stop clipboard?"));
		panel.add(activityClipboardCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfModeClipboardTranslate() {
		modeClipboardCheckBox = new JCheckBox("Select");
		modeClipboardCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						changeModeClipboard(modeClipboardCheckBox.isSelected());
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Is mode of copy/select of clipboard?"));
		panel.add(modeClipboardCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void changeActivityClipboard(Boolean b) {
		activityClipboardCheckBox.setSelected(b == null ? !activityClipboardCheckBox
				.isSelected() : b);
		activityClipboardCheckBox.setText(activityClipboardCheckBox
				.isSelected() ? "Start" : "Stop");
		firePropertyChange(Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD_TRANS,
				!activityClipboardCheckBox.isSelected(),
				activityClipboardCheckBox.isSelected());
		new Actions.StartStopTClipboardAction()
				.execute(activityClipboardCheckBox.isSelected());
	}

	private void changeModeClipboard(Boolean b) {
		modeClipboardCheckBox.setSelected(b == null ? !modeClipboardCheckBox
				.isSelected() : b);
		modeClipboardCheckBox
				.setText(modeClipboardCheckBox.isSelected() ? "Copy" : "Select");
		firePropertyChange(Constants.PROPERTY_CHANGE_MODE_CLIPBOARD_TRANS,
				!modeClipboardCheckBox.isSelected(),
				modeClipboardCheckBox.isSelected());
		new Actions.ModeTClipboardAction().execute(modeClipboardCheckBox
				.isSelected());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD_SETUP:
			changeActivityClipboard(null);
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD_SETUP:
			changeModeClipboard(null);
			break;
		case Constants.PROPERTY_CHANGE_INIT_CLIPBOARD_SETUP:
			firePropertyChange(
					Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD_TRANS,
					null, activityClipboardCheckBox.isSelected());
			firePropertyChange(
					Constants.PROPERTY_CHANGE_MODE_CLIPBOARD_TRANS, null,
					modeClipboardCheckBox.isSelected());
			break;
		case Constants.PROPERTY_CHANGE_INIT_COOKIE_SETUP:
			cookieField.setText((String) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_INIT_DICTIONARY_DIR_SETUP:
			dictionaryDirField.setText((String) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_INIT_HISTORY_SETUP:
			historyCheckBox.setSelected((Boolean) evt.getNewValue());
			historyCheckBox
					.setText(historyCheckBox.isSelected() ? "Yes" : "No");
			break;
		case Constants.PROPERTY_CHANGE_INIT_SOUND_SETUP:
			soundCheckBox.setSelected((Boolean) evt.getNewValue());
			soundCheckBox.setText(soundCheckBox.isSelected() ? "Yes" : "No");
			break;
		case Constants.PROPERTY_CHANGE_INIT_STATISTIC_SETUP:
			statisticLabel.setText((String) evt.getNewValue());
			break;
		}
	}
}
