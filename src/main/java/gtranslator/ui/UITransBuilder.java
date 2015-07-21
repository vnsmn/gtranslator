package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.Actions.ClearHistoryAction;
import gtranslator.Actions.DetailTranslateAction;
import gtranslator.Actions.ModeTClipboardAction;
import gtranslator.Actions.PlayEngWordWithLoadAction;
import gtranslator.Actions.SnapshotAction;
import gtranslator.Actions.StartStopTClipboardAction;
import gtranslator.Actions.TranslateWordAction;
import gtranslator.Actions.WordPlayOfClipboardAction;
import gtranslator.App;
import gtranslator.ClipboardObserver.MODE;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class UITransBuilder extends UIBuilder implements PropertyChangeListener {
	private JCheckBoxMenuItem[] activityClipboardMenuItems = { null, null };
	private JCheckBoxMenuItem[] soundMenuItems = { null, null };
	private JCheckBoxMenuItem[] detailClipboardItems = { null, null };
	private JCheckBoxMenuItem[] fixedLocationFrameItems = { null, null };
	private JTextArea sourceArea;
	private JTextArea targetArea;
	private JFrame frame;
	@SuppressWarnings("unchecked")
	private Map<MODE, JRadioButtonMenuItem>[] modeWidgets = new HashMap[] {
			new HashMap<>(), new HashMap<>() };

	public void build(JFrame frame, JTextArea sourceArea, JTextArea targetArea,
			JPopupMenu sourcePopupMenu, JPopupMenu targetPopupMenu) {
		this.sourceArea = sourceArea;
		this.targetArea = targetArea;
		this.frame = frame;

		createPopupMenu(sourceArea, targetArea, sourcePopupMenu, 0);
		createPopupMenu(sourceArea, targetArea, targetPopupMenu, 1);

		sourcePopupMenu.addPopupMenuListener(new PopupMenuListenerExt());
		sourceArea.addMouseListener(new SourceMouseAdapter());
		targetArea.addMouseListener(new TargetMouseAdapter());
	}

	private void createPopupMenu(JTextArea sourceArea, JTextArea targetArea,
			JPopupMenu sourcePopupMenu, int index) {
		JMenuItem mit = new JMenuItem("Snapshot word");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(SnapshotAction.class).execute(
						App.getUIOutput().getSourceText(), true);
			}
		});
		sourcePopupMenu.add(mit);
		
		mit = new JMenuItem("Unsnapshot word");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(SnapshotAction.class).execute(
						App.getUIOutput().getSourceText(), false);
			}
		});
		sourcePopupMenu.add(mit);

		mit = new JMenuItem("Play word");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(PlayEngWordWithLoadAction.class).execute(
						sourceArea.getText());
			}
		});
		sourcePopupMenu.add(mit);

		mit = new JMenuItem("Delete word from history");
		mit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Actions.findAction(ClearHistoryAction.class).execute(
						App.getUIOutput().getSourceText());
			}
		});
		sourcePopupMenu.add(mit);

		soundMenuItems[index] = new JCheckBoxMenuItem("Sound");
		soundMenuItems[index]
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean b = ((JMenuItem) e.getSource()).isSelected();
						Actions.findAction(WordPlayOfClipboardAction.class)
								.execute(b);
						soundMenuItems[0].setSelected(b);
						soundMenuItems[1].setSelected(b);
						firePropertyChange(Constants.PROPERTY_CHANGE_SOUND,
								null, b);
					}
				});
		sourcePopupMenu.add(soundMenuItems[index]);

		detailClipboardItems[index] = new JCheckBoxMenuItem(
				"Detail translation");
		detailClipboardItems[index]
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean b = ((JMenuItem) e.getSource()).isSelected();
						Actions.findAction(DetailTranslateAction.class)
								.execute(b);
						detailClipboardItems[0].setSelected(b);
						detailClipboardItems[1].setSelected(b);
						firePropertyChange(
								Constants.PROPERTY_CHANGE_DETAIL_CLIPBOARD,
								null, b);
					}
				});
		sourcePopupMenu.add(detailClipboardItems[index]);

		fixedLocationFrameItems[index] = new JCheckBoxMenuItem(
				"Fixed location frame");
		fixedLocationFrameItems[index].setSelected(true);
		fixedLocationFrameItems[index]
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean b = ((JMenuItem) e.getSource()).isSelected();
						fixedLocationFrameItems[0].setSelected(b);
						fixedLocationFrameItems[1].setSelected(b);
						firePropertyChange(
								Constants.PROPERTY_CHANGE_FIXED_LOCATION_FRAME,
								null, b);
					}
				});
		sourcePopupMenu.add(fixedLocationFrameItems[index]);

		activityClipboardMenuItems[index] = new JCheckBoxMenuItem("Start");
		activityClipboardMenuItems[index]
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean b = ((JCheckBoxMenuItem) e.getSource())
								.isSelected();
						Actions.findAction(StartStopTClipboardAction.class)
								.execute(b);
						firePropertyChange(
								Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD,
								!b, b);
						activityClipboardMenuItems[0].setSelected(b);
						activityClipboardMenuItems[1].setSelected(b);
					}
				});
		sourcePopupMenu.add(activityClipboardMenuItems[index]);

		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MODE mode = MODE.valueOf(e.getActionCommand());
				Actions.findAction(ModeTClipboardAction.class).execute(mode);
				firePropertyChange(Constants.PROPERTY_CHANGE_MODE_CLIPBOARD,
						null, mode);
				for (Map<MODE, JRadioButtonMenuItem> map : modeWidgets) {
					for (Entry<MODE, JRadioButtonMenuItem> ent : map.entrySet()) {
						ent.getValue().setSelected(ent.getKey() == mode);
					}
				}
			}
		};

		ButtonGroup group = new ButtonGroup();

		JMenu menu = new JMenu("Clipboard modes");
		menu.setMnemonic(KeyEvent.VK_O);

		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(
				"Select without lost focus");
		menuItem.setActionCommand(MODE.TEXT.name());
		menuItem.addActionListener(actionListener);
		group.add(menuItem);
		menu.add(menuItem);
		modeWidgets[index].put(MODE.TEXT, menuItem);

		menuItem = new JRadioButtonMenuItem("Select with lost focus");
		menuItem.setActionCommand(MODE.SELECT.name());
		menuItem.addActionListener(actionListener);
		group.add(menuItem);
		menu.add(menuItem);
		modeWidgets[index].put(MODE.SELECT, menuItem);

		menuItem = new JRadioButtonMenuItem("Copy");
		menuItem.setActionCommand(MODE.COPY.name());
		menuItem.addActionListener(actionListener);
		group.add(menuItem);
		menu.add(menuItem);
		modeWidgets[index].put(MODE.COPY, menuItem);

		sourcePopupMenu.add(menu);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this) {
			return;
		}
		switch (evt.getPropertyName()) {
		case Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD:
			boolean b = (boolean) evt.getNewValue();
			for (JMenuItem it : activityClipboardMenuItems) {
				it.setSelected(b);
			}
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD:
			MODE mode = (MODE) evt.getNewValue();
			for (Map<MODE, JRadioButtonMenuItem> map : modeWidgets) {
				for (Entry<MODE, JRadioButtonMenuItem> ent : map.entrySet()) {
					ent.getValue().setSelected(ent.getKey() == mode);
				}
			}
			break;
		case Constants.PROPERTY_CHANGE_SOUND:
			boolean sb = (boolean) evt.getNewValue();
			for (JMenuItem it : soundMenuItems) {
				it.setSelected(sb);
			}
			break;
		case Constants.PROPERTY_CHANGE_DETAIL_CLIPBOARD:
			boolean db = (boolean) evt.getNewValue();
			for (JMenuItem it : detailClipboardItems) {
				it.setSelected(db);
			}
			break;
		}
	}

	private class SourceMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				String[] ss = { sourceArea.getText(), sourceArea.getText() };
				Actions.findAction(TranslateWordAction.class).execute(ss);
				targetArea.setText(ss[1]);
			}
		}
	}

	private class TargetMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				JComponent component = (JComponent) frame.getContentPane()
						.getComponent(0);
				if (frame.isAlwaysOnTop()) {
					component.setBorder(null);
				} else {
					component.setBorder(new LineBorder(Color.lightGray, 2));
				}
				frame.setAlwaysOnTop(!frame.isAlwaysOnTop());
			}
		}
	}

	private class PopupMenuListenerExt implements PopupMenuListener {
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	}
}
