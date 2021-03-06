package ch.ifocusit.tools.copydrag;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

public class CopyDrag extends TrayIcon implements ClipboardOwner {
	private static final Logger log = Logger.getLogger(CopyDrag.class.getName());

	private static final Image ICON_IMAGE_ON = new ImageIcon(CopyDrag.class.getResource("/WithAttachment.png")).getImage();
	private static final Image ICON_IMAGE_OFF = new ImageIcon(CopyDrag.class.getResource("/NoAttachment.png")).getImage();

	public static final int DEFAULT = 100;

	private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

	private JFrame jFrame;
	private JLabel label;

	// private List<BufferedImage> images = new ArrayList<>();

	public CopyDrag(final boolean imageAvailable) {
		super(ICON_IMAGE_OFF);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e != null && (e.getModifiers() & 0x10) <= 0) {
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						readClipboard();
					}
				});
			}
		});

		// create icon menu
		createPopupMenu();

		// create window
		jFrame = new JFrame();
		// jFrame.getContentPane().setLayout(new FlowLayout());
		jFrame.requestFocusInWindow();
		jFrame.toFront();

		label = new JLabel();
		jFrame.getContentPane().add(label, BorderLayout.CENTER);
		label.setPreferredSize(new Dimension(150, 150));
		label.setTransferHandler(new ImageSelection());
		// draggable
		label.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				JComponent comp = (JComponent) e.getSource();
				TransferHandler handler = comp.getTransferHandler();
				handler.exportAsDrag(comp, e, TransferHandler.COPY);
			}
		});

		// actions
		final JPanel actionsPnl = new JPanel();
		jFrame.getContentPane().add(actionsPnl, BorderLayout.SOUTH);

		final JButton copy = new JButton("Copy");
		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransferHandler handler = label.getTransferHandler();
				handler.exportToClipboard(label, sysClip, TransferHandler.COPY);
			}
		});
		final JButton paste = new JButton("Paste");
		paste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				readClipboard();
			}
		});
		actionsPnl.add(copy);
		actionsPnl.add(paste);

		// jFrame.setLocationByPlatform(true);
		// jFrame.setUndecorated(false);
		// jFrame.setPreferredSize(DEFAULT_DIMENSION);
		jFrame.pack();

		// listen clipboard
		regainOwnership(sysClip.getContents(this));

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				readClipboard();
			}
		});
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

	private void readClipboard() {
		Transferable clipData = sysClip.getContents(sysClip);
		if (clipData != null) {
			final boolean imageAvailable = clipData.isDataFlavorSupported(DataFlavor.imageFlavor);
			if (imageAvailable) {
				TransferHandler handler = label.getTransferHandler();
				handler.importData(label, clipData);
				jFrame.setVisible(true);
			}
			if (imageAvailable/* || !images.isEmpty() */) {
				setImage(ICON_IMAGE_ON);
				setToolTip("Click to show image for drag and drop");
			} else {
				setImage(ICON_IMAGE_OFF);
				setToolTip("Copy an image to the clipboard");
			}
		}
	}

	// private boolean alreadyAdded(final BufferedImage img) {
	// for (BufferedImage image : images) {
	// if (bufferedImagesEqual(img, image)) {
	// return true;
	// }
	// }
	// return false;
	// }
	// boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2) {
	// if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
	// for (int x = 0; x < img1.getWidth(); x++) {
	// for (int y = 0; y < img1.getHeight(); y++) {
	// if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false;
	// }
	// }
	// } else {
	// return false;
	// }
	// return true;
	// }

	@Override
	public void lostOwnership(Clipboard c, Transferable t) {
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		readClipboard();
		regainOwnership(sysClip.getContents(this));
	}

	private void regainOwnership(Transferable t) {
		sysClip.setContents(t, this);
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
