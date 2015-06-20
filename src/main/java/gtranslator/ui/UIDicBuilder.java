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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class UIDicBuilder extends UIBuilder implements PropertyChangeListener {
	Box box;
	Border lineBorder;

	JTextField targetDir;
	JTextField wordsPath;

	public void build(JPanel panel) {
		box = Box.createVerticalBox();
		panel.add(box, BorderLayout.NORTH);
		lineBorder = BorderFactory.createLineBorder(Color.GRAY);

		createWidgetsOfTextTargetDir();
		createWidgetsOfWordsPath();
		createWidgetsOfActionTranslatedHistory();
	}

	private void createWidgetsOfTextTargetDir() {
		targetDir = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Target dir of result"));
		panel.add(targetDir, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfWordsPath() {
		wordsPath = new JTextField();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(lineBorder,
				"Path to words file"));
		panel.add(wordsPath, BorderLayout.NORTH);
		box.add(panel);
	}

	private void createWidgetsOfActionTranslatedHistory() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(wordsPath, BorderLayout.NORTH);
		box.add(panel);
		final JButton button = new JButton("words");
		button.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread th = new Thread(new Runnable() {					
					@Override
					public void run() {
						button.setEnabled(false);
						try {
							Actions.findAction(Actions.HistoryDictionaryAction.class)
									.execute(targetDir.getText());
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}
}
