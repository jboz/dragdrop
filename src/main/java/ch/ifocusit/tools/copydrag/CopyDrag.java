package ch.ifocusit.tools.copydrag;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class CopyDrag extends TrayIcon implements MouseListener, ClipboardOwner {
	private static final Logger log = Logger.getLogger(CopyDrag.class.getName());

	private static final Image ICON_IMAGE_ON = new ImageIcon(CopyDrag.class.getResource("/WithAttachment.png")).getImage();
	private static final Image ICON_IMAGE_OFF = new ImageIcon(CopyDrag.class.getResource("/NoAttachment.png")).getImage();

	private static final int DEFAULT = 100;

	private static final Dimension DEFAULT_DIMENSION = new Dimension(DEFAULT * 2, DEFAULT * 2);

	private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

	private final JFrame jFrame = new JFrame();
	private DefaultListModel<Icon> model = new DefaultListModel<>();
	private final FromTransferHandler transferHandler = new FromTransferHandler();

	private List<BufferedImage> images = new ArrayList<>();

	public CopyDrag(final boolean imageAvailable) {
		super(ICON_IMAGE_OFF);

		setImageAvailable(imageAvailable);

		addMouseListener(this);

		// create icon menu
		createPopupMenu();

		// create window
		// jFrame.getContentPane().setLayout(new FlowLayout());
		jFrame.requestFocusInWindow();
		jFrame.toFront();

		final JScrollPane scrollPane = new JScrollPane();
		final JList<Icon> jList = new JList<>(model);
		jList.setDragEnabled(true);
		// jList.setTransferHandler(transferHandler);
		scrollPane.setViewportView(jList);
		jList.setLayout(new FlowLayout());
		jFrame.getContentPane().add(scrollPane);
		// jFrame.setLocationByPlatform(true);
		// jFrame.setUndecorated(false);
		jFrame.setPreferredSize(DEFAULT_DIMENSION);
		jFrame.pack();

		// listen clipboard
		regainOwnership(sysClip.getContents(this));
	}

	public void quit() {
		SystemTray.getSystemTray().remove(CopyDrag.this);
		System.exit(0);
	}

	private void createPopupMenu() {
		final PopupMenu popup = new PopupMenu();

		final MenuItem exitItem = new MenuItem("Exit");
		popup.add(exitItem);

		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				quit();
			}
		});
		setPopupMenu(popup);
	}

	public void setImageAvailable(final boolean imageAvailable) {
		if (imageAvailable || !images.isEmpty()) {
			setImage(ICON_IMAGE_ON);
			setToolTip("Click to show image for drag and drop");
		} else {
			setImage(ICON_IMAGE_OFF);
			setToolTip("Copy an image to the clipboard");
		}
		if (imageAvailable) {
			mouseClicked(null);
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (e != null && (e.getModifiers() & 0x10) <= 0) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateFrame();
			}
		});
	}

	private void updateFrame() {
		// v.clear();
		final BufferedImage clipboardImage = getClipboardImage();
		if (clipboardImage != null && !alreadyAdded(clipboardImage)) {
			// read clipboard
			final Point p = getResizedDimentions(clipboardImage);
			final ImageIcon imageIcon = new ImageIcon(clipboardImage.getScaledInstance(p.x, p.y, 16), "desc");

			// put clipboard image
			model.add(0, imageIcon);
			images.add(clipboardImage);

			transferHandler.setImage(clipboardImage);

			// open
			SwingUtilities.updateComponentTreeUI(jFrame);
		}
		jFrame.setVisible(true);
	}

	private boolean alreadyAdded(final BufferedImage img) {
		for (BufferedImage image : images) {
			if (bufferedImagesEqual(img, image)) {
				return true;
			}
		}
		return false;
	}

	boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2) {
		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
			for (int x = 0; x < img1.getWidth(); x++) {
				for (int y = 0; y < img1.getHeight(); y++) {
					if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private Point getResizedDimentions(final BufferedImage img) {
		final Point dim = new Point();
		if (img.getHeight() > img.getWidth()) {
			dim.y = DEFAULT;
			dim.x = img.getWidth() * (DEFAULT / img.getHeight());
		} else {
			dim.x = DEFAULT;
			dim.y = img.getHeight() * (DEFAULT / img.getWidth());
		}
		dim.x = dim.x == 0 ? DEFAULT : dim.x;
		dim.y = dim.y == 0 ? DEFAULT : dim.y;
		return dim;
	}

	private BufferedImage getClipboardImage() {
		final Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				return (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
			}
		} catch (final UnsupportedFlavorException e) {
		} catch (final IOException e) {
		}
		return null;
	}

	@Override
	public void lostOwnership(Clipboard c, Transferable t) {
		try {
			Thread.sleep(200);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		Transferable contents = sysClip.getContents(this);

		setImageAvailable(contents.isDataFlavorSupported(DataFlavor.imageFlavor));
		regainOwnership(contents);
	}

	private void regainOwnership(Transferable t) {
		sysClip.setContents(t, this);
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	public static void main(final String[] args) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (Exception e) {
					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		});
	}

	private static void createAndShowGUI() throws Exception {
		final SystemTray tray = SystemTray.getSystemTray();

		// create tray icon
		final boolean isImage = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null)
				.isDataFlavorSupported(DataFlavor.imageFlavor);
		final CopyDrag copyDragnIcon = new CopyDrag(isImage);

		// register tray icon
		tray.add(copyDragnIcon);
	}
}
