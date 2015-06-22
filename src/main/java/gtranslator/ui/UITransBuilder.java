package gtranslator.ui;

import gtranslator.Actions;
import gtranslator.Actions.ClearHistoryAction;
import gtranslator.Actions.ModeTClipboardAction;
import gtranslator.Actions.PlayEngWordWithLoadAction;
import gtranslator.Actions.StartStopTClipboardAction;
import gtranslator.Actions.TranslateWordAction;
import gtranslator.ClipboardObserver.MODE;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class UITransBuilder extends UIBuilder implements PropertyChangeListener {
	private JCheckBoxMenuItem activityClipboardMenuItem;
	private JTextArea sourceArea;
	private JTextArea targetArea;
	private Map<MODE, JRadioButtonMenuItem> modeWidgets = new HashMap<>();

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
				Actions.findAction(ClearHistoryAction.class).execute(
						UIOutput.getInstance().getSourceText());
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

		activityClipboardMenuItem = new JCheckBoxMenuItem("Start");
		activityClipboardMenuItem
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean b = ((JCheckBoxMenuItem) e.getSource()).isSelected();
						Actions.findAction(StartStopTClipboardAction.class)
								.execute(!b);
						firePropertyChange(Constants.PROPERTY_CHANGE_ACTIVITY_CLIPBOARD, !b, b);
					}
				});
		sourcePopupMenu.add(activityClipboardMenuItem);
		
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MODE mode = MODE.valueOf(e.getActionCommand());
				Actions.findAction(ModeTClipboardAction.class).execute(mode);
				firePropertyChange(Constants.PROPERTY_CHANGE_MODE_CLIPBOARD,
						null, mode);
			}
		};		
		
		ButtonGroup group = new ButtonGroup();
		
		JMenu menu = new JMenu("Clipboard modes");	
	    menu.setMnemonic(KeyEvent.VK_O);

	    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem("Select without lost focus");
	    menuItem.setActionCommand(MODE.TEXT.name());
	    menuItem.addActionListener(actionListener);
	    group.add(menuItem);
	    menu.add(menuItem);
	    modeWidgets.put(MODE.TEXT, menuItem);	    

	    menuItem = new JRadioButtonMenuItem("Select with lost focus");
	    menuItem.setActionCommand(MODE.SELECT.name());
	    menuItem.addActionListener(actionListener);
	    group.add(menuItem);
	    menu.add(menuItem);
	    modeWidgets.put(MODE.SELECT, menuItem);

	    menuItem = new JRadioButtonMenuItem("Copy");
	    menuItem.setActionCommand(MODE.COPY.name());
	    menuItem.addActionListener(actionListener);
	    group.add(menuItem);
	    menu.add(menuItem);
	    modeWidgets.put(MODE.COPY, menuItem);	   
	    
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
			activityClipboardMenuItem.setSelected(b);
			break;
		case Constants.PROPERTY_CHANGE_MODE_CLIPBOARD:
			JRadioButtonMenuItem button = modeWidgets.get((MODE) evt.getNewValue());
			button.setSelected(true);
			break;
		}
	}

	private class MouseAdapterExt extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			String[] ss = { sourceArea.getText(), sourceArea.getText() };
			Actions.findAction(TranslateWordAction.class).execute(ss);
			targetArea.setText(ss[1]);
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
