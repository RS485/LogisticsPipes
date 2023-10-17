package logisticspipes.routing.debug;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class DebugWindow extends JFrame {

	private int width;
	private int height;
	private JTextPane textArea;

	public DebugWindow(String title, int width, int height) {
		super(title);
		setSize(width, height);
		textArea = new JTextPane();
		JScrollPane pane = new JScrollPane(textArea);
		getContentPane().add(pane);
		setVisible(true);
	}

	public void showInfo(String data, Color color) {
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setFontFamily(attr, "SansSerif");
		StyleConstants.setFontSize(attr, 12);
		StyleConstants.setForeground(attr, color);
		Document document = textArea.getDocument();
		if (document != null) {
			try {
				document.insertString(document.getLength(), data, attr);
			} catch (BadLocationException ignored) {}
		}
		getContentPane().validate();
	}

	public void clear() {
		textArea.setText("");
		getContentPane().validate();
	}
}
