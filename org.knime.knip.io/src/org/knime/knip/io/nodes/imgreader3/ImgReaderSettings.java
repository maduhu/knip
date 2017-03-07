package org.knime.knip.io.nodes.imgreader3;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.node.nodesettings.SettingsModelSubsetSelection2;

import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;

/**
 * Settings for the Image Reader nodes
 */
public class ImgReaderSettings {

	/**
	 * Enum that stores the supported image factories
	 *
	 */
	public enum ImgFactoryMode {

		ARRAY_IMG("Array Image Factory"), PLANAR_IMG("Planar Image Factory"), CELL_IMG("Cell Image Factory");

		private final String name;

		private ImgFactoryMode(String name) {
			this.name = name;
		}


		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Enum that stores the metadata modes.
	 * 
	 */
	public enum MetadataMode {
		NO_METADATA("No metadata"), APPEND_METADATA("Append a metadata column"), METADATA_ONLY("Only metadata");

		private final String name;

		MetadataMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Enum that stores teh Column creation modes
	 */
	public enum ColumnCreationMode {
		NEW_TABLE("New Table"), APPEND("Append"), REPLACE("Replace");

		private String name;

		ColumnCreationMode(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public static SettingsModelBoolean createCheckFileFormatModel() {
		return new SettingsModelBoolean("Check File format for each file", true);
	}

	public static SettingsModelBoolean createIsGroupFilesModel() {
		return new SettingsModelBoolean("Group Files", true);
	}

	public static SettingsModelBoolean createAppendOMEXMLColModel() {
		return new SettingsModelBoolean("Append OME-XML column", false);
	}

	/**
	 * @return Model for the settings holding selected image planes.
	 */
	public static final SettingsModelSubsetSelection2 createPlaneSelectionModel() {
		return new SettingsModelSubsetSelection2("Plane selection");
	}

	/**
	 * @return Model to store whether all series should be read
	 */
	public static final SettingsModelBoolean createReadAllSeriesModel() {
		return new SettingsModelBoolean("Read all series", true);
	}

	public static SettingsModelDoubleRange createSeriesSelectionRangeModel() {
		return new SettingsModelDoubleRange("Series range selection", 0, Short.MAX_VALUE);
	}

	/**
	 * @return Model to store the factory used to create the images
	 */
	public static SettingsModelString createImgFactoryModel() {
		return new SettingsModelString("Image Factory", ImgFactoryMode.ARRAY_IMG.toString());
	}

	/**
	 * @return Model to store the metadata mode.
	 */
	public static SettingsModelString createMetaDataModeModel() {
		return new SettingsModelString("Metadata Mode", MetadataMode.NO_METADATA.toString());
	}

	/**
	 * @return Model to store whether to read all meta data or not.
	 */
	public static SettingsModelBoolean createReadAllMetaDataModel() {
		return new SettingsModelBoolean("read all metadata", false);
	}

	public static SettingsModelColumnName createFileURIColumnModel() {
		return new SettingsModelColumnName("File URI column", "");
	}

	public static SettingsModelString createColumnCreationModeModel() {
		return new SettingsModelColumnName("Column creation mode", ColumnCreationMode.NEW_TABLE.toString());
	}

	public static SettingsModelString createColumnSuffixNodeModel() {
		return new SettingsModelString("Column suffix ", "_read");
	}
}
