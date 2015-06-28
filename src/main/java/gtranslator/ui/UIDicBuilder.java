package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.AppProperties;
import gtranslator.Actions.DictionarySynthesizerAction;
import gtranslator.Actions.SetDictionaryPhoneticAction;
import gtranslator.DictionaryHelper;
import gtranslator.DictionaryHelper.DictionaryInput;
import gtranslator.ui.Constants.PHONETICS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class UIDicBuilder extends UIBuilder implements PropertyChangeListener {
	Box box;
	Border lineBorder;

	private JTextField resultDir;
	private JTextField wordsPath;
	private JTextField limitTextField;
	private JComboBox pronunciationLangComboBox;
	private JComboBox sourceTypeComboBox;
	private JCheckBox rusCheckBox;
	private JCheckBox multiRusCheckBox;
	private JCheckBox sortCheckBox;
	private JCheckBox synthesCheckBox;
	private JTextField prefixTextField;
	private JTextField pauseSecondsTextField;
	private JTextField defisSecondsTextField;
	private JCheckBox phonCheckBox;
	private JCheckBox directCheckBox;

	public void build(JPanel panel) {
		box = Box.createVerticalBox();
		panel.add(box, BorderLayout.NORTH);
		lineBorder = BorderFactory.createLineBorder(Color.GRAY);

		createWidgetsOfResultDir();
		createWidgetsOfWordsPath();
		createWidgetsOfLimitPath();
		createWidgetsOfPhonetic();
		createWidgetsOfSourceType();
		createWidgetsOfSort();
		createWidgetsOfPrefix();
		createWidgetsOfPauseSeconds();
		createWidgetsOfDefisSeconds();
		createWidgetsOfSynthes();
		createWidgetsOfPhon();
		createWidgetsOfDirect();
		createWidgetsOfRus();
		createWidgetsOfMultiRus();
		createWidgetsOfPerform();
	}

	private void createWidgetsOfDirect() {
		directCheckBox = new JCheckBox("eng to rus");
		directCheckBox.setSelected(true);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"is first-eng second-rus?"));
		panel.add(directCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfPhon() {
		phonCheckBox = new JCheckBox("Phonetics");
		phonCheckBox.setSelected(false);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do use phonetics?"));
		panel.add(phonCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfSynthes() {
		synthesCheckBox = new JCheckBox("Synthesier");
		synthesCheckBox.setSelected(false);
		synthesCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox check = (JCheckBox) e.getSource();
				Actions.findAction(DictionarySynthesizerAction.class).execute(
						check.isSelected());
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do use synthesier?"));
		panel.add(synthesCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfDefisSeconds() {
		defisSecondsTextField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Defis seconds"));
		panel.add(defisSecondsTextField, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfPauseSeconds() {
		pauseSecondsTextField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Pause seconds"));
		panel.add(pauseSecondsTextField, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfPrefix() {
		prefixTextField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Prefix"));
		panel.add(prefixTextField, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfSort() {
		sortCheckBox = new JCheckBox("Sort");
		sortCheckBox.setSelected(true);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Do dic sort?"));
		panel.add(sortCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfResultDir() {
		resultDir = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Dir of result"));
		panel.add(resultDir, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfWordsPath() {
		wordsPath = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Path of file to words file"));
		panel.add(wordsPath, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfLimitPath() {
		limitTextField = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Block limit"));
		panel.add(limitTextField, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfPerform() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		box.add(panel);
		final JButton button = new JButton("run");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						button.setEnabled(false);
						try {
							DictionaryInput dic = new DictionaryInput();
							dic.path = wordsPath.getText();
							dic.resultDir = resultDir.getText();
							dic.sourceType = DictionaryHelper.SOURCE_TYPE
									.valueOf(sourceTypeComboBox
											.getSelectedItem().toString());
							dic.phonetic = PHONETICS
									.valueOf(pronunciationLangComboBox
											.getSelectedItem().toString());
							dic.isRusTransled = rusCheckBox.isSelected();
							dic.isMultiRusTransled = multiRusCheckBox
									.isSelected();
							dic.isSort = sortCheckBox.isSelected();
							dic.setPrefix(prefixTextField.getText());
							dic.setPauseSeconds(pauseSecondsTextField.getText());
							dic.setDefisSeconds(defisSecondsTextField.getText());
							dic.isPhonetics = phonCheckBox.isSelected();
							dic.isFirstEng = directCheckBox.isSelected();
							Actions.findAction(Actions.DictionaryAction.class)
									.execute(dic);
						} finally {
							button.setEnabled(true);
						}
					}
				});
				th.setDaemon(true);
				th.start();
			}
		});
		panel.add(button, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfPhonetic() {
		String[] phonetic = { PHONETICS.AM.name(), PHONETICS.BR.name() };
		pronunciationLangComboBox = new JComboBox(phonetic);
		pronunciationLangComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JComboBox comboBox = (JComboBox) e.getSource();
						Actions.findAction(SetDictionaryPhoneticAction.class)
								.execute(
										PHONETICS.valueOf(comboBox
												.getSelectedItem().toString()));
					}
				});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory
				.createTitledBorder(lineBorder, "Phonetic"));
		panel.add(pronunciationLangComboBox, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfSourceType() {
		String[] types = { DictionaryHelper.SOURCE_TYPE.HISTORY.name(),
				DictionaryHelper.SOURCE_TYPE.DICTIONARY.name(),
				DictionaryHelper.SOURCE_TYPE.TEXT.name() };
		sourceTypeComboBox = new JComboBox(types);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Type source"));
		panel.add(sourceTypeComboBox, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfRus() {
		rusCheckBox = new JCheckBox("Yes");
		rusCheckBox.setSelected(true);
		rusCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Is rus?"));
		panel.add(rusCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	private void createWidgetsOfMultiRus() {
		multiRusCheckBox = new JCheckBox("No");
		multiRusCheckBox.setSelected(false);
		multiRusCheckBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JCheckBox check = (JCheckBox) e.getSource();
				check.setText(check.isSelected() ? "Yes" : "No");
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Is multi rus?"));
		panel.add(multiRusCheckBox, BorderLayout.WEST);
		box.add(panel);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this) {
			return;
		}
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_DICTIONARY_RESULT_DIR:
			resultDir.setText(evt.getNewValue().toString());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_BLOCK_LIMIT:
			limitTextField.setText(evt.getNewValue().toString());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_PRONUNCIATION:
			PHONETICS ph = (PHONETICS) evt.getNewValue();
			pronunciationLangComboBox.setSelectedItem(ph.name());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_PAUSE_SECONDS:
			pauseSecondsTextField.setText(evt.getNewValue().toString());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_DEFIS_SECONDS:
			defisSecondsTextField.setText(evt.getNewValue().toString());
			break;
		case Constants.PROPERTY_CHANGE_DICTIONARY_SYNTHESIZER:
			synthesCheckBox.setSelected((Boolean) evt.getNewValue());
			break;
		}

	}
}
