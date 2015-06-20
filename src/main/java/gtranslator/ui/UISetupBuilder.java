package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.Actions.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

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
		activityClipboardCheckBox = new JCheckBox("Stop");
		activityClipboardCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {						
						changeActivityClipboardCheckBox(activityClipboardCheckBox.isSelected());						
						Actions.findAction(StartStopTClipboardAction.class)
								.execute(!activityClipboardCheckBox.isSelected());
					}
				});
		Actions.findAction(StartStopTClipboardAction.class).getObservable()
				.addObserver(new Observer() {
					@Override
					public void update(Observable o, Object arg) {
						changeActivityClipboardCheckBox((Boolean) arg);
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do start/stop clipboard?"));
		panel.add(activityClipboardCheckBox, BorderLayout.WEST);
		box.add(panel);
	}
	
	private void changeActivityClipboardCheckBox(boolean b) {
		activityClipboardCheckBox.setSelected(b);
		activityClipboardCheckBox.setText(b ? "Start" : "Stop");
		firePropertyChange(
				Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD,
				!b, b);		
	}

	private void createWidgetsOfModeClipboardTranslate() {
		modeClipboardCheckBox = new JCheckBox("Select");
		modeClipboardCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						changeModeClipboardCheckBox(modeClipboardCheckBox.isSelected());
						Actions.findAction(ModeTClipboardAction.class).execute(
								modeClipboardCheckBox.isSelected());
					}
				});
		Actions.findAction(ModeTClipboardAction.class).getObservable()
				.addObserver(new Observer() {
					@Override
					public void update(Observable o, Object arg) {
						changeModeClipboardCheckBox((Boolean) arg);
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Is mode of copy/select of clipboard?"));
		panel.add(modeClipboardCheckBox, BorderLayout.WEST);
		box.add(panel);
	}
	
	private void changeModeClipboardCheckBox(boolean b) {		
		modeClipboardCheckBox.setSelected(b);
		modeClipboardCheckBox.setText(b ? "Select" : "Copy");
		firePropertyChange(
				Constants.PROPERTY_CHANGE_MODE_CLIPBOARD,
				!b, b);		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this) {
			return;
		}
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD:
			changeActivityClipboardCheckBox((Boolean) evt.getNewValue());
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD:
			changeModeClipboardCheckBox((Boolean) evt.getNewValue());
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
