package ch.ifocusit.tools.copydrag;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class FileTransferable implements Transferable {
	private static final Logger log = Logger.getLogger(FileTransferable.class.getName());

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hhmmss");

	private final Image image;
	private File imageFile;
	private final List<File> transferData;

	public FileTransferable(final Image image) {
		this.image = image;
		transferData = new LinkedList<File>();
		try {
			imageFile = createImageFile();
		} catch (final IOException e) {
			FileTransferable.log.log(Level.SEVERE, e.getMessage(), e);
		}
		FileTransferable.log.fine("getTransferData:: Created imageFile: " + imageFile);
		transferData.add(imageFile);
	}

	public void deleteTempFile() {
		if (imageFile != null && imageFile.exists()) {
			final boolean deleteSucceeded = imageFile.delete();
			if (!deleteSucceeded) {
				FileTransferable.log.warning("Failed to delete " + imageFile);
			} else {
				FileTransferable.log.warning("Deleted " + imageFile);
			}
		}
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return transferData;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.javaFileListFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return flavor.equals(DataFlavor.javaFileListFlavor);
	}

	private File createImageFile() throws IOException {
		final File tempFile = getTempFile();
		tempFile.createNewFile();
		tempFile.deleteOnExit();
		ImageIO.write((BufferedImage) image, "PNG", tempFile);
		return tempFile;
	}

	private File getTempFile() {
		String tempdir = System.getProperty("java.io.tmpdir");
		if (!tempdir.endsWith("/") && !tempdir.endsWith("\\")) {
			tempdir = tempdir + System.getProperty("file.separator");
		}
		String baseTempFileName = System.getProperty("baseTempFileName");
		if (baseTempFileName == null || "".equals(baseTempFileName.trim())) {
			baseTempFileName = "ClipImg";
		}
		final File tempFile = new File(tempdir, baseTempFileName + DATE_FORMAT.format(new Date(System.currentTimeMillis()))
				+ ".png");

		return tempFile;
	}
}
