package logisticspipes.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class OpenGLDebugger {

	private static HashMap<Integer, String> niceToHave = null;
	private static int probeID = 0;
	private Thread probeGUIThread;
	private int cycleCount;
	private boolean started;
	private ExtendedHashMap glStuff;
	private ConcurrentHashMap<Integer, GLTypes> glVariablesToCheck;
	private final Lock debuggerLock;
	private final Condition glVariablesCondition;
	private boolean glVariablesUpdated;
	@Getter
	@Setter
	private int printOnCycle;

	private enum GLTypes {
		BOOLEAN(Boolean.class, "boolean", "GL11.glGetBoolean"),
		DOUBLE(Double.class, "double", "GL11.glGetDouble"),
		FLOAT(Float.class, "float", "GL11.glGetFloat"),
		INTEGER(Integer.class, "int", "GL11.glGetInteger"),
		INTEGER64(Long.class, "long", "GL32.glGetInteger64");

		private Class javaClass;
		private String getterFunction;
		private String niceName;

		GLTypes(Class javaClass, String niceName, String getterFunction) {
			this.javaClass = javaClass;
			this.niceName = niceName;
			this.getterFunction = getterFunction;
		}

		public Class getJavaClass() {
			return javaClass;
		}

		public String getGetterFunction() {
			return getterFunction;
		}

		public String getNiceName() {
			return niceName;
		}
	}

	public static class ExtendedHashMap extends HashMap<Integer, Object> {

		private ArrayList<Integer> orderedKeys;
		private ArrayList<Integer> newKeys;
		private ArrayList<Integer> updatedKeys;
		private boolean sessionStarted;

		public int getStopUpdatedIndex() {
			return stopUpdatedIndex;
		}

		public int getStopNewIndex() {
			return stopNewIndex;
		}

		private int stopUpdatedIndex;
		private int stopNewIndex;

		public ExtendedHashMap() {
			sessionStarted = false;
			orderedKeys = new ArrayList<>();
		}

		@Override
		public void putAll(Map<? extends Integer, ?> m) {
			throw new UnsupportedOperationException();
		}

		public void startSession() {
			newKeys = new ArrayList<>();
			updatedKeys = new ArrayList<>();
			sessionStarted = true;
		}

		@Override
		public Object put(Integer key, Object value) {
			if (!sessionStarted) {
				throw new UnsupportedOperationException("Session not started");
			}
			if (containsKey(key)) {
				if (get(key).equals(value)) {
					return value;
				} else {
					orderedKeys.remove(key);
					updatedKeys.add(key);
				}
			} else {
				newKeys.add(key);
			}
			return super.put(key, value);
		}

		public void stopSession() {
			stopNewIndex = newKeys.size();
			orderedKeys.addAll(0, newKeys);
			newKeys = null;
			stopUpdatedIndex = updatedKeys.size();
			stopNewIndex += stopUpdatedIndex;
			orderedKeys.addAll(0, updatedKeys);
			updatedKeys = null;
			sessionStarted = false;
		}

		public String getName(Integer key) {
			return OpenGLDebugger.niceToHave.get(key);
		}

		public int getKey(int index) {
			return orderedKeys.get(index);
		}
	}

	public class SpecialTableModel extends DefaultTableModel {

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
				case 0:
					return "Key";
				case 1:
					return "Value";
				default:
					return "<NOVALUE>";
			}
		}

		@Override
		public int getRowCount() {
			return glStuff.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try {
				int index = glStuff.getKey(rowIndex);
				switch (columnIndex) {
					case 0:
						return glStuff.getName(index);
					case 1:
						return glStuff.get(index);
					default:
						return "<NOVALUE>";
				}
			} catch (IndexOutOfBoundsException e) {
				return "<EXCEPTION>";
			}
		}
	}

	public class SpecialTableCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (table == null) {
				return this;
			}
			setBackground(null);
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (row < glStuff.getStopUpdatedIndex()) {
				setBackground(Color.YELLOW);
			} else if (row < glStuff.getStopNewIndex()) {
				setBackground(Color.GREEN);
			}

			return this;
		}
	}

	public class ProbeGUI extends JDialog implements Runnable {

		private JPanel mainPanel;
		private JTable variableMonitorTable;
		private JButton closeButton;

		public ProbeGUI() {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					try {
						UIManager.setLookAndFeel(info.getClassName());
					} catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
						e.printStackTrace();
					}
					break;
				}
			}

			setupUI();

			setType(Type.UTILITY);
			setContentPane(mainPanel);
			getRootPane().setDefaultButton(closeButton);

			variableMonitorTable.setModel(new SpecialTableModel());

			TableCellRenderer cellRenderer = new SpecialTableCellRenderer();
			variableMonitorTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);

			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					stop();
				}
			});

			mainPanel.registerKeyboardAction(e -> stop(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		}

		@Override
		public void run() {
			for (Integer key : OpenGLDebugger.niceToHave.keySet()) {
				glVariablesToCheck.put(key, GLTypes.BOOLEAN);
			}
			pack();
			setVisible(true);

			while (started) {
				debuggerLock.lock();
				try {
					while (!glVariablesUpdated) {
						glVariablesCondition.await();
					}
					glVariablesUpdated = false;
					updateVariables();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					debuggerLock.unlock();
				}
			}
		}

		private void updateVariables() {
			DefaultTableModel dtm = (DefaultTableModel) variableMonitorTable.getModel();
			dtm.fireTableDataChanged();
		}

		private void setupUI() {
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
			closeButton = new JButton();
			closeButton.setText("Close");
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 5, 0, 0);
			mainPanel.add(closeButton, gbc);
			JButton addButton = new JButton();
			addButton.setText("Add");
			gbc = new GridBagConstraints();
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 5, 0, 0);
			mainPanel.add(addButton, gbc);
			JTextField addTextField = new JTextField();
			addTextField.setText("");
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1.0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			mainPanel.add(addTextField, gbc);
			JScrollPane monitorTableScrollPane = new JScrollPane();
			monitorTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 3;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(0, 0, 5, 0);
			mainPanel.add(monitorTableScrollPane, gbc);
			variableMonitorTable = new JTable();
			variableMonitorTable.setEnabled(false);
			monitorTableScrollPane.setViewportView(variableMonitorTable);
		}
	}

	public OpenGLDebugger(int printOnCycle) {
		if (printOnCycle < 1) {
			throw new IllegalArgumentException("Print per cycle must be at least 1");
		}

		if (OpenGLDebugger.niceToHave == null) {
			OpenGLDebugger.updateNiceToHave();
		}

		debuggerLock = new ReentrantLock();
		glVariablesCondition = debuggerLock.newCondition();

		this.printOnCycle = printOnCycle;
		glStuff = new ExtendedHashMap();
		glVariablesToCheck = new ConcurrentHashMap<>();

		probeGUIThread = new Thread(new ProbeGUI(), "LogisticsPipes GLDebug Probe #" + OpenGLDebugger.probeID);
		OpenGLDebugger.probeID++;
	}

	public void start() {
		if (!started) {
			started = true;
			cycleCount = 0;
			probeGUIThread.start();
		}
	}

	public void stop() {
		if (started) {
			debuggerLock.lock();
			try {
				started = false;
				glVariablesUpdated = true;
				glVariablesCondition.signal();
			} finally {
				debuggerLock.unlock();
			}
		}
	}

	public void cycle() {
		if (started) {
			++cycleCount;
			if (cycleCount % printOnCycle == 0) {
				saveOpenGLStuff();
				cycleCount = 0;
			}
		}
	}

	private void saveOpenGLStuff() {
		debuggerLock.lock();
		try {
			Iterator<Integer> i = glVariablesToCheck.keySet().iterator();
			glStuff.startSession();
			while (i.hasNext()) {
				Integer key = i.next();
				Object value = GL11.glGetBoolean(key);
				if (GL11.glGetError() == GL11.GL_INVALID_ENUM) {
					i.remove();
				} else {
					glStuff.put(key, value);
				}
			}
			glStuff.stopSession();
			glVariablesUpdated = true;
			glVariablesCondition.signal();
		} finally {
			debuggerLock.unlock();
		}
	}

	private static void updateNiceToHave() {
		OpenGLDebugger.niceToHave = new HashMap<>();
		int crawlerVersion = 11;
		boolean almostEnd = false;
		boolean end = false;
		while (!end) {
			String packageGL = String.format("%s%d", "GL", crawlerVersion);
			String nextGL = String.format("%s.%s", "org.lwjgl.opengl", packageGL);
			try {
				crawlerVersion++;
				Class glClass = GL11.class.getClassLoader().loadClass(nextGL);
				com.google.common.reflect.Reflection.initialize(glClass);
				almostEnd = false;

				for (Field f : glClass.getDeclaredFields()) {
					try {
						if (!f.getType().equals(int.class)) {
							continue;
						}

						int id = f.getInt(null);
						String nice = f.getName();
						if (nice.endsWith("BIT")) {
							continue;
						}

						// All the things that are being replaced are not that bad
						if (OpenGLDebugger.niceToHave.containsKey(id) && !OpenGLDebugger.niceToHave.get(id).equals(nice)) {
							System.out.printf("NiceToHave: ID %d exists. Replacing %s with %s!!%n", id, OpenGLDebugger.niceToHave.remove(id), nice);
						}

						OpenGLDebugger.niceToHave.put(id, String.format("%s.%s", packageGL, nice));
					} catch (IllegalArgumentException e) {
						System.out.printf("NiceToHave: Illegal Argument!%nNiceToHave: %s%n", e);
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						System.out.printf("NiceToHave: Illegal Access!%nNiceToHave: %s%n", e);
						e.printStackTrace();
					}
				}
			} catch (ClassNotFoundException e) {
				if (almostEnd) {
					end = true;
				} else {
					almostEnd = true;
					crawlerVersion = (crawlerVersion / 10 + 1) * 10;
				}
			}
		}
	}
}
