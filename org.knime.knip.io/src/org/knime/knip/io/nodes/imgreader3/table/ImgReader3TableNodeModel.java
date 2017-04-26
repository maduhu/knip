package org.knime.knip.io.nodes.imgreader3.table;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.filehandling.remote.connectioninformation.port.ConnectionInformationPortObject;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.node.NodeUtils;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.io.nodes.imgreader3.AbstractImgReaderNodeModel;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettings;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettings.ColumnCreationMode;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettings.MetadataMode;
import org.knime.knip.io.nodes.imgreader3.ScifioImgReader;
import org.knime.knip.io.nodes.imgreader3.ScifioImgReader.ScifioReaderBuilder;
import org.knime.knip.io.nodes.imgreader3.ScifioReadResult;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImgReader3TableNodeModel<T extends RealType<T> & NativeType<T>> extends AbstractImgReaderNodeModel<T> {

	private static final int CONNECTION_PORT = 0;
	private static final int DATA_PORT = 0;
	protected static final NodeLogger LOGGER = NodeLogger.getLogger(ImgReader3TableNodeModel.class);

	/** Settings Models */
	private final SettingsModelColumnName filenameColumnModel = ImgReaderSettings.createFileURIColumnModel();
	private final SettingsModelString columnCreationModeModel = ImgReaderSettings.createColumnCreationModeModel();
	private final SettingsModelString columnSuffixModel = ImgReaderSettings.createColumnSuffixNodeModel();
	private final SettingsModelBoolean appendSeriesNumberModel = ImgReaderSettings.createAppendSeriesNumberModel();

	private boolean useRemote;

	protected ImgReader3TableNodeModel() {
		super(new PortType[] { ConnectionInformationPortObject.TYPE_OPTIONAL, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE });

		addAdditionalSettingsModels(Arrays.asList(filenameColumnModel, columnCreationModeModel, columnSuffixModel));
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		// check if we are using a remote location
		useRemote = inSpecs[CONNECTION_PORT] != null;

		return createOutSpec(inSpecs);
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

		final AtomicInteger errorCount = new AtomicInteger(0);

		final BufferedDataTable in = (BufferedDataTable) inObjects[DATA_PORT];
		final BufferedDataContainer container = exec.createDataContainer(in.getDataTableSpec());
		final int uriColIdx = getUriColIdx(in.getDataTableSpec());

		ScifioImgReader<T> reader;
		if (useRemote) {
			reader = createRemoteScifioReader(exec, (ConnectionInformationPortObject) inObjects[CONNECTION_PORT],
					uriColIdx);
		} else {
			reader = createLocalScifioReader(exec);
		}

		for (final DataRow row : in) {
			exec.checkCanceled();
			final ScifioReadResult<T> res = reader
					.read(((URIDataValue) row.getCell(uriColIdx)).getURIContent().getURI());
			res.getRows().forEach(container::addRowToTable);

			// errors during execution
			if (!res.getError().isPresent()) {
				handleReadErrors(errorCount, row.getKey(), res.getError().get());
			}
		}

		reader.close();
		container.close();
		return new PortObject[] { container.getTable() };
	}

	private PortObjectSpec[] createOutSpec(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		// ensure there is a valid column
		final int uriColIdx = getUriColIdx(inSpecs[DATA_PORT]);

		// initialze the settings
		final MetadataMode metaDataMode = EnumUtils.valueForName(metadataModeModel.getStringValue(),
				MetadataMode.values());
		final DataTableSpec spec = (DataTableSpec) inSpecs[DATA_PORT];

		final boolean readImage = metaDataMode == MetadataMode.NO_METADATA
				|| metaDataMode == MetadataMode.APPEND_METADATA;
		final boolean readMetadata = metaDataMode == MetadataMode.APPEND_METADATA
				|| metaDataMode == MetadataMode.METADATA_ONLY;

		final ColumnCreationMode columnCreationMode = EnumUtils.valueForName(columnCreationModeModel.getStringValue(),
				ColumnCreationMode.values());

		// Create the outspec

		final DataTableSpec outSpec;
		if (columnCreationMode == ColumnCreationMode.NEW_TABLE) {

			DataTableSpecCreator specBuilder = new DataTableSpecCreator();

			if (readImage) {
				specBuilder.addColumns(new DataColumnSpecCreator("Image", ImgPlusCell.TYPE).createSpec());
			}
			if (readMetadata) {
				specBuilder.addColumns(new DataColumnSpecCreator("OME-XML Metadata", XMLCell.TYPE).createSpec());
			}
			if (readAllSeriesModel.getBooleanValue()) {
				specBuilder.addColumns(new DataColumnSpecCreator("Series Number", StringCell.TYPE).createSpec());
			}

			outSpec = specBuilder.createSpec();

		} else { // Append and replace

			final DataColumnSpec imgSpec = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, "Image" + columnSuffixModel.getStringValue()),
					ImgPlusCell.TYPE).createSpec();
			final DataColumnSpec metaDataSpec = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, "OME-XML Metadata" + columnSuffixModel.getStringValue()),
					XMLCell.TYPE).createSpec();
			final DataColumnSpec seriesNumberSpec = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, "Series Number"), StringCell.TYPE).createSpec();

			DataTableSpecCreator outSpecBuilder = new DataTableSpecCreator(spec);

			if (columnCreationMode == ColumnCreationMode.APPEND) {
				if (readImage) {
					outSpecBuilder.addColumns(imgSpec);
				}
				if (readMetadata) {
					outSpecBuilder.addColumns(metaDataSpec);
				}
				if (appendSeriesNumberModel.getBooleanValue()) {
					outSpecBuilder.addColumns(seriesNumberSpec);
				}

				outSpec = outSpecBuilder.createSpec();

			} else if (columnCreationMode == ColumnCreationMode.REPLACE) {

				// As we can only replace the URI column, we append all
				// additional columns.
				boolean replaced = false;

				if (readImage) {
					// replaced is always false in this case
					outSpecBuilder.replaceColumn(uriColIdx, imgSpec);
					replaced = true;
				}
				if (readMetadata) {
					if (!replaced) {
						outSpecBuilder.replaceColumn(uriColIdx, metaDataSpec);
						replaced = true;
					} else {
						outSpecBuilder.addColumns(metaDataSpec);
					}
				}
				if (appendSeriesNumberModel.getBooleanValue()) {
					if (!replaced) {
						outSpecBuilder.replaceColumn(uriColIdx, seriesNumberSpec);
					} else {
						outSpecBuilder.addColumns(seriesNumberSpec);
					}
				}

				outSpec = outSpecBuilder.createSpec();
			} else {
				// should really not happen
				throw new IllegalStateException("Support for the columncreation mode"
						+ columnCreationModeModel.getStringValue() + " is not implemented!");
			}
		}
		return new PortObjectSpec[] { outSpec };
	}

	private int getUriColIdx(final PortObjectSpec inSpec) throws InvalidSettingsException {
		return NodeUtils.autoColumnSelection((DataTableSpec) inSpec, filenameColumnModel, URIDataValue.class,
				ImgReader3TableNodeModel.class);
	}

	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		return new StreamableOperator() {
			@Override
			public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
					throws Exception {

				final RowInput in = (RowInput) inputs[DATA_PORT];
				final RowOutput out = (RowOutput) outputs[0];

				final AtomicInteger encounteredExceptionsCount = new AtomicInteger(0);

				final ScifioImgReader<T> reader;
				DataRow row;
				final int uriColIdx = getUriColIdx(inSpecs[DATA_PORT]);
				if (useRemote) {
					final ConnectionInformationPortObject connection = (ConnectionInformationPortObject) ((PortObjectInput) inputs[CONNECTION_PORT])
							.getPortObject();
					reader = createRemoteScifioReader(exec, connection, uriColIdx);
				} else {
					reader = createLocalScifioReader(exec);
				}

				// get next row from input
				while ((row = in.poll()) != null) {

					// TODO Make less ugly?
					final ScifioReadResult<T> res = reader
							.read(((URIDataValue) row.getCell(uriColIdx)).getURIContent().getURI());

					for (final DataRow resRow : res.getRows()) {
						out.push(resRow);
					}

					// count number of errors
					if (!res.getError().isPresent()) {
						handleReadErrors(encounteredExceptionsCount, row.getKey(), res.getError().get());
					}
				}

				in.close();
				out.close();
				reader.close();
			}
		};
	}

	private ScifioImgReader<T> createLocalScifioReader(final ExecutionContext exec) {
		// TODO Auto-generated method stub
		return null;
	}

	private ScifioImgReader<T> createRemoteScifioReader(final ExecutionContext exec,
			final ConnectionInformationPortObject connection, int uriColIdx) {

		ScifioReaderBuilder<T> builder = new ScifioReaderBuilder<>();

		// File settings
		builder.checkFileFormat(checkFileFormatModel.getBooleanValue());
		builder.metaDataMode(EnumUtils.valueForName(metadataModeModel.getStringValue(), MetadataMode.values()));
		builder.imgFactory(createImgFactory());
		builder.appendSeriesNumber(appendSeriesNumberModel.getBooleanValue());

		// connection info
		builder.connectionInfo(connection.getConnectionInformation());

		// Table settings
		builder.exec(exec);
		builder.uriColumnIdx(uriColIdx);
		builder.columnCreationMode(
				EnumUtils.valueForName(columnCreationModeModel.getStringValue(), ColumnCreationMode.values()));

		return builder.build();
	}

	@Override
	protected void doLoadInternals(final File nodeInternDir, final ExecutionMonitor exec) {
		// nothing to do
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE, InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED };
	}

	private void handleReadErrors(final AtomicInteger encounteredExceptionsCount, final RowKey rowKey,
			final Throwable throwable) {
		encounteredExceptionsCount.incrementAndGet();

		LOGGER.warn("Encountered exception while reading from source: " + rowKey + " ; view log for more info.");

		LOGGER.debug(throwable);
	}

}
