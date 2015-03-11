package ch.ifocusit.tools.copydrag;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class FromTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(FromTransferHandler.class.getName());

	private Image image;
	private FileTransferable fileTransferable;

	public FromTransferHandler() {
	}

	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	public int getSourceActions(final JComponent comp) {
		return 1;
	}

	@Override
	public synchronized Transferable createTransferable(final JComponent comp) {
		FromTransferHandler.log.fine("Create Transferable");
		if (fileTransferable == null) {
			fileTransferable = new FileTransferable(image);
		}
		return fileTransferable;
	}

	@Override
	protected void exportDone(final JComponent source, final Transferable data, final int action) {
		super.exportDone(source, data, action);
		FromTransferHandler.log.fine("Export Done");

		fileTransferable = null;
	}
}
