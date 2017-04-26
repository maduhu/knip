package org.knime.knip.io.nodes.imgreader3.table;

import org.knime.core.data.uri.URIDataValue;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.io.nodes.imgreader3.AbstractImgReaderNodeDialog;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettings;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettings.ColumnCreationMode;

public class ImgReader3TableNodeDialog extends AbstractImgReaderNodeDialog {

	private final SettingsModelString fileURIColumnModel;

	@SuppressWarnings("unchecked")
	public ImgReader3TableNodeDialog() {
		super();

		createNewGroup("File Input Column");
		fileURIColumnModel = ImgReaderSettings.createFileURIColumnModel();

		addDialogComponent(new DialogComponentColumnNameSelection(fileURIColumnModel, "File URI column in input table",
				1, true, false, URIDataValue.class));
		closeCurrentGroup();

		// insert default gui
		super.buildRemainingGUI();

		createNewTab("Column Settings");
		final SettingsModelString colCreationModeModel = ImgReaderSettings.createColumnCreationModeModel();
		addDialogComponent(new DialogComponentStringSelection(colCreationModeModel, "Column Creation Mode",
				EnumUtils.getStringListFromToString(ColumnCreationMode.values())));

		final SettingsModelString columnSuffixModel = ImgReaderSettings.createColumnSuffixNodeModel();
		addDialogComponent(new DialogComponentString(columnSuffixModel, "Column Suffix"));
	}

}
