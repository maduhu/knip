
import io.scif.Format;
import io.scif.FormatException;
import io.scif.Metadata;
import io.scif.Parser;
import io.scif.Reader;
import io.scif.config.SCIFIOConfig;
import io.scif.filters.ChannelFiller;
import io.scif.filters.PlaneSeparator;
import io.scif.filters.ReaderFilter;
import io.scif.img.ImgOpener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.log4j.Level;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.knime.base.filehandling.remote.connectioninformation.port.ConnectionInformation;
import org.knime.cloud.core.util.port.CloudConnectionInformation;
import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.core.KNIPGateway;
import org.knime.knip.core.KNIPLogService;
import org.scijava.handles.http.HTTPLocation;
import org.scijava.handles.s3.S3LocationBuilder;
import org.scijava.io.DataHandle;
import org.scijava.io.FileLocation;
import org.scijava.io.Location;

import loci.formats.FormatHandler;
import ome.xml.model.OMEModelImpl;
import ome.xml.model.enums.handlers.DetectorTypeEnumHandler;

public class ScifioImgSource3<T extends RealType<T> & NativeType<T>> {

	private UnclosableReaderFilter reader;
	private Object currentLocation;
	private boolean checkFileFormat;
	private SCIFIOConfig scifioConfig;
	private boolean usedDifferentReaders;
	private ImgOpener imgOpener;
	private ImgFactory imgFactory;
	private Level rootLvl;

	/**
	 * Creates a new ScifioImgSource.
	 * 
	 * @param imgFactory
	 *            the image factory to be used
	 * @param checkFileFormat
	 *            if for each new file to be read a new reader should be created
	 *            or the old one can be re-used
	 * @param config
	 *            additional scifio-specific settings
	 * 
	 * 
	 */
	public ScifioImgSource3(@SuppressWarnings("rawtypes") final ImgFactory imgFactory, final boolean checkFileFormat,
			final SCIFIOConfig config) {
		scifioConfig = config;
		this.checkFileFormat = checkFileFormat;
		imgOpener = new ImgOpener(ScifioGateway.getSCIFIO().getContext());
		this.imgFactory = imgFactory;
		usedDifferentReaders = false;

		// TODO Workaround to suppress BioFormats info message flooding
		org.apache.log4j.Logger.getLogger(OMEModelImpl.class).setLevel(Level.ERROR);
		org.apache.log4j.Logger.getLogger(FormatHandler.class).setLevel(Level.ERROR);
		org.apache.log4j.Logger.getLogger(DetectorTypeEnumHandler.class).setLevel(Level.ERROR);

		rootLvl = org.apache.log4j.Logger.getLogger(KNIPLogService.class.getSimpleName()).getLevel();
	}

	public Img<T> getImages(URI uri, ConnectionInformation con)
			throws MalformedURLException, URISyntaxException, FormatException {

		SourceType s = getSourceFromScheme(con.getProtocol());

		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.knime.knip.io.ScijavaLocationProvider");
		
		IExtension[] ex = point.getExtensions();

		Location loc;
		switch (s) {
		case FILE:
			loc = new FileLocation(uri);
			break;
		case HTTP:
			loc = new HTTPLocation(uri.toString(), con.getUser(), con.getPassword());
			break;
		case S3:
			loc = createS3Location(uri, con);
			break;
		case OMERO:
			loc = createOMEROLocation(uri, con);
			break;
		default:
			throw new IllegalArgumentException("Location type " + s.toString() + " is not supported yet!");
		}

		// ScifioGateway.format().getFormat(source);
		DataHandle<Location> handle = KNIPGateway.handles().create(loc);

		return null;
	}

	private Location createOMEROLocation(URI uri, ConnectionInformation con) {

		// OMEROSession session = new OMEROSession()

		// TODO Auto-generated method stub
		return null;
	}

	private ReaderFilter getUnclosableReader(DataHandle<Location> handle) {
		if (reader == null || (!currentLocation.equals(handle.get()) && checkFileFormat)) {
			final Format format = ScifioGateway.getSCIFIO().format().getFormat(handle,
					new SCIFIOConfig().checkerSetOpen(true));

			final UnclosableReaderFilter r = new UnclosableReaderFilter(format.createReader());

			final Parser p = format.createParser();

			r.setMetadata(p.parse(handle, scifioConfig));

			// check if the current file really contains images
			if (r.getMetadata().getImageCount() == 0) {
				throw new KNIPRuntimeException("No images available at the location " + handle.get());
			}

			// without the "separate"-stuff the images will not be split
			// correctly for some types. This fixes the bug if, for instance,
			// only Channel 1 is desired and Channel 0 was returned every time.
			r.enable(ChannelFiller.class);
			r.enable(PlaneSeparator.class).separate(axesToSplit(r));

			if (reader != null && !(reader.getFormat().getClass().equals(r.getFormat().getClass()))) {
				// more than one reader (class) has been used
				usedDifferentReaders = true;
			}
			if (reader != null) {
				reader.closeNow();
			}
			reader = r;
		} else if (!currentLocation.equals(handle.get()) && !checkFileFormat) {
			// re-use the last reader, set the new image reference (i.e. id) and
			// parse the metadata
			reader.closeNow();

			final Parser p = reader.getFormat().createParser();

			reader.setMetadata(p.parse(handle, scifioConfig));

			// TODO maybe this is a bug and we shouldn't have to do this.
			reader.enable(ChannelFiller.class);
			reader.enable(PlaneSeparator.class).separate(axesToSplit(reader));
		}

		// sets the file the reader currently points to
		currentLocation = handle.get();

		// set the logging level back
		org.apache.log4j.Logger.getLogger(KNIPLogService.class.getSimpleName()).setLevel(rootLvl);
		return reader;
	}

	private Location createS3Location(URI uri, ConnectionInformation con) {
		Location loc;
		CloudConnectionInformation con2 = (CloudConnectionInformation) con;

		String path = uri.getPath();

		String region = uri.getHost();
		String bucket = path.substring(path.indexOf('@'), path.indexOf('/'));
		String file = path.substring(path.indexOf('/') + 1);

		S3LocationBuilder builder = new S3LocationBuilder(bucket, file);
		builder.withBasicRegion(region);
		if (!con2.useKeyChain()) {
			// need to set auth
			builder.withBasicCredentials(con.getUser(), con.getPassword());
		}
		loc = builder.build();
		return loc;
	}

	private SourceType getSourceFromScheme(String scheme) {
		for (SourceType s : SourceType.values()) {
			if (s.scheme.contains(scheme)) {
				return s;
			}
		}
		throw new NoSuchElementException();
	}

	/*
	 * Returns a list of all AxisTypes that should be split out. This is a list
	 * of all non-X,Y planar axes. Always tries to split {@link Axes#CHANNEL}.
	 * 
	 * Code taken from ImgOpener!
	 */
	private AxisType[] axesToSplit(final ReaderFilter r) {
		final Set<AxisType> axes = new HashSet<>();
		final Metadata meta = r.getTail().getMetadata();
		// Split any non-X,Y axis
		for (final CalibratedAxis t : meta.get(0).getAxesPlanar()) {
			final AxisType type = t.type();
			if (!(type == Axes.X || type == Axes.Y)) {
				axes.add(type);
			}
		}
		// Ensure we attempt to split Channel
		axes.add(Axes.CHANNEL);
		return axes.toArray(new AxisType[axes.size()]);
	}

	enum SourceType {
		FILE("file"), HTTP("http", "https"), S3("s3"), OMERO("omero");
		// TODO the following
		// FTP("ftp, sftp"), SSH("ssh", "scp"), ABS("abs");

		private List<String> scheme;

		SourceType(String... schemes) {
			scheme = Arrays.asList(schemes);
		}

		public List<String> getScheme() {
			return scheme;
		}
	}

	/* Helper class to prevent a reader from being closed. */
	private class UnclosableReaderFilter extends ReaderFilter {

		public UnclosableReaderFilter(final Reader r) {
			super(r);
		}

		@Override
		public void close() throws IOException {
			// do nothing here to prevent the reader from being closed
		}

		@Override
		public void close(final boolean fileOnly) throws IOException {
			// do nothing here to prevent the reader from being closed
		}

		/*
		 * Closes the underlying reader, called by the
		 * ScifioImgsource.close()-method.
		 */
		private void closeNow() throws IOException {
			super.close(false);
		}
	}
}
