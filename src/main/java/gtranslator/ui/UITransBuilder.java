package gtranslator.ui;

import gtranslator.Actions;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class UITransBuilder extends UIBuilder implements PropertyChangeListener {
	private JMenuItem activityClipboardMenuItem;
	private JMenuItem modeClipboardMenuItem;
	private JTextArea sourceArea;
	private JTextArea targetArea;

	public void build(JTextArea sourceArea, JTextArea targetArea,
			JPopupMenu sourcePopupMenu) {
		this.sourceArea = sourceArea;
		this.targetArea = targetArea;

		createPopupMenu(sourceArea, targetArea, sourcePopupMenu);

		sourcePopupMenu.addPopupMenuListener(new PopupMenuListenerExt());
		sourceArea.addMouseListener(new MouseAdapterExt());
	}

	private void createPopupMenu(JTextArea sourceArea, JTextArea targetArea,
			JPopupMenu sourcePopupMenu) {
		JMenuItem mit = new JMenuItem("Delete word from history");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Actions.ClearHistoryAction().execute(UIOutput.getInstance()
						.getSourceText());
			}
		});
		sourcePopupMenu.add(mit);

		mit = new JMenuItem("Play word");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Actions.PlayEngWordAction().execute(sourceArea.getText());
			}
		});
		sourcePopupMenu.add(mit);

		activityClipboardMenuItem = new JMenuItem("Start/Stop clipboard");
		activityClipboardMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						firePropertyChange(
								Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD_SETUP,
								null, null);
					}
				});
		sourcePopupMenu.add(activityClipboardMenuItem);

		modeClipboardMenuItem = new JMenuItem("Mode clipboard");
		modeClipboardMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						firePropertyChange(
								Constants.PROPERTY_CHANGE_MODE_CLIPBOARD_SETUP,
								null, null);
					}
				});
		sourcePopupMenu.add(modeClipboardMenuItem);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD_TRANS:
			boolean b = (boolean) evt.getNewValue();
			activityClipboardMenuItem.setText(b ? "Start" : "Stop");
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD_TRANS:
			b = (boolean) evt.getNewValue();
			modeClipboardMenuItem.setText(b ? "Copy" : "Select");
			break;
		}
	}

	private class MouseAdapterExt extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			String[] ss = { sourceArea.getText(), sourceArea.getText() };
			new Actions.TranslateWordAction().execute(ss);
			targetArea.setText(ss[1]);
		}
	}

	private class PopupMenuListenerExt implements PopupMenuListener {
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			firePropertyChange(Constants.PROPERTY_CHANGE_INIT_CLIPBOARD_SETUP,
					null, null);
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	}
}
