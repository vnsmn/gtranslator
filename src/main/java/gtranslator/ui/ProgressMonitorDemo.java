package gtranslator.ui;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public class ProgressMonitorDemo implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private ProgressMonitor progressMonitor;

	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			String message = String.format("Completed %d%%.\n", progress);
			progressMonitor.setNote(message);
		}
	}

	public static ProgressMonitorDemo createAndShowGUI(String title, int max) {
		ProgressMonitorDemo progressMonitorDemo = new ProgressMonitorDemo(
				title, max);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return progressMonitorDemo;
	}

	public ProgressMonitorDemo(String title, int max) {
		progressMonitor = new ProgressMonitor(null, title, "", 0, max);
		progressMonitor.setProgress(0);
	}

	public synchronized void nextProgress(int progress) {
		progressMonitor.setProgress(progress);
		progressMonitor.setNote(String.format("Progress: %d-%d-%d%s", progress,
				progressMonitor.getMaximum(),
				Math.round((100d / progressMonitor.getMaximum()) * progress), "%"));
	}

	public synchronized void close() {
		progressMonitor.close();
	}

	public synchronized boolean isCanceled() {
		return progressMonitor.isCanceled();
	}
}