package logisticspipes.pipes.basic.debug;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@SuppressWarnings("serial")
public class LogWindow extends JPanel {

	private static Map<Integer, LogWindow> map = new HashMap<>();

	public static LogWindow getWindow(int id) {
		LogWindow window = LogWindow.map.get(id);
		if (window == null) {
			window = new LogWindow();
			LogWindow.map.put(id, window);
		}
		return window;
	}

	private JTextPane logArea;
	private DefaultMutableTreeNode baseNode;
	private JTree tree;
	private List<StatusEntry> currentLayout = new ArrayList<>(0);
	private JFrame frame;

	private LogWindow() {
		super(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();

		logArea = new JTextPane();
		JScrollPane logPane = new JScrollPane(logArea);
		tabbedPane.addTab("Console", null, logPane, "");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		baseNode = new DefaultMutableTreeNode("State Information");
		tree = new JTree(baseNode);
		// tree.addTreeExpansionListener(this);
		// tree.addMouseListener(this);
		JScrollPane treeView = new JScrollPane(tree);
		tabbedPane.addTab("Status List", null, treeView, "");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		add(tabbedPane);
		createAndShowGUI();
	}

	private void createAndShowGUI() {
		frame = new JFrame("");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public void newLine(String data) {
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, "SansSerif");
		StyleConstants.setFontSize(attr, 12);
		// StyleConstants.setForeground(attr, color);
		Document document = logArea.getDocument();
		if (document != null) {
			try {
				document.insertString(document.getLength(), data + "\n", attr);
			} catch (BadLocationException ignored) {}
		}
		validate();
	}

	public void clear() {
		logArea.setText("");
		validate();
	}

	public void updateStatus(List<StatusEntry> entries) {
		compareLists(entries, currentLayout, baseNode);
		currentLayout = entries;
		// this.validate();
	}

	private void compareLists(List<StatusEntry> newList, List<StatusEntry> oldList, DefaultMutableTreeNode node) {
		for (int i = 0; i < newList.size() && i < oldList.size(); i++) {
			if (!newList.get(i).equals(oldList.get(i))) {
				StatusEntry entry = newList.get(i);
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
				child.setUserObject(entry.name);
				if (entry.subEntry != null) {
					if (oldList.get(i).subEntry != null) {
						compareLists(entry.subEntry, oldList.get(i).subEntry, child);
					} else {
						compareLists(entry.subEntry, new ArrayList<>(0), child);
					}
				} else if (oldList.get(i).subEntry != null) {
					child.removeAllChildren();
				}
				((DefaultTreeModel) tree.getModel()).reload(child);
			}
		}
		for (int i = newList.size(); i < oldList.size(); i++) {
			node.remove(i);
			((DefaultTreeModel) tree.getModel()).reload(node);
		}
		for (int i = oldList.size(); i < newList.size(); i++) {
			StatusEntry entry = newList.get(i);
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(entry.name);
			node.add(newNode);
			if (entry.subEntry != null) {
				compareLists(entry.subEntry, new ArrayList<>(0), newNode);
			}
			((DefaultTreeModel) tree.getModel()).reload(node);
		}
	}

	public void setTitle(String title) {
		frame.setName(title);
	}
}
