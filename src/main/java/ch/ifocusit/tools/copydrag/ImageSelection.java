package ch.ifocusit.tools.copydrag;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

public class ImageSelection extends TransferHandler implements Transferable {
	private static final long serialVersionUID = 1L;

	private static final DataFlavor flavors[] = { DataFlavor.imageFlavor };

	private BufferedImage image;

	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	public boolean canImport(JComponent comp, DataFlavor flavor[]) {
		if (!(comp instanceof JLabel)) {
			return false;
		}
		for (int i = 0, n = flavor.length; i < n; i++) {
			for (int j = 0, m = flavors.length; j < m; j++) {
				if (flavor[i].equals(flavors[j])) {
					return true;
				}
			}
		}
		return false;
	}

	public Transferable createTransferable(JComponent comp) {
		// Clear
		//image = null;

		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			Icon icon = label.getIcon();
			if (icon instanceof ImageIcon) {
				// image = (BufferedImage) ((ImageIcon) icon).getImage();
				return this;
			}
		}
		return null;
	}

	public boolean importData(JComponent comp, Transferable t) {
		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			if (t.isDataFlavorSupported(flavors[0])) {
				try {
					image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
					final Point p = getResizedDimentions(image);
					final ImageIcon imageIcon = new ImageIcon(image.getScaledInstance(p.x, p.y, 16), "image copied");
					label.setIcon(imageIcon);
					return true;
				} catch (UnsupportedFlavorException ignored) {
				} catch (IOException ignored) {
				}
			}
		}
		return false;
	}

	private Point getResizedDimentions(final BufferedImage img) {
		final Point dim = new Point();
		if (img.getHeight() > img.getWidth()) {
			dim.y = CopyDrag.DEFAULT;
			dim.x = img.getWidth() * (CopyDrag.DEFAULT / img.getHeight());
		} else {
			dim.x = CopyDrag.DEFAULT;
			dim.y = img.getHeight() * (CopyDrag.DEFAULT / img.getWidth());
		}
		dim.x = dim.x == 0 ? CopyDrag.DEFAULT : dim.x;
		dim.y = dim.y == 0 ? CopyDrag.DEFAULT : dim.y;
		return dim;
	}

	// Transferable
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported(flavor)) {
			return image;
		}
		return null;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.imageFlavor);
	}
}