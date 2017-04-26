/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.io.nodes.imgreader3;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.knip.base.node.dialog.DialogComponentSubsetSelection2;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettingsModels.ImgFactories;
import org.knime.knip.io.nodes.imgreader3.ImgReaderSettingsModels.MetadataMode;

/**
 * Abstract dialog for the ImageReader, specifies common settings. 
 *
 * @author <a href="mailto:gabriel.einsdorf@uni.kn"> Gabriel Einsdorf</a>
 */
public abstract class AbstractImgReaderNodeDialog extends DefaultNodeSettingsPane {


	protected void buildRemainingGUI() {
		createNewGroup("Output");
		addDialogComponent(new DialogComponentStringSelection(ImgReaderSettingsModels.createMetaDataModeModel(),
				"OME-XML-Metadata:", EnumUtils.getStringCollectionFromToString(MetadataMode.values())));
		addDialogComponent(new DialogComponentBoolean(ImgReaderSettingsModels.createReadAllMetaDataModel(),
				"Read non OME-XML Metadata"));

		addDialogComponent(new DialogComponentStringSelection(ImgReaderSettingsModels.createImgFactoryModel(),
				"Image factory", EnumUtils.getStringCollectionFromToString(ImgFactories.values())));
		closeCurrentGroup();

		createNewGroup("File");
		addDialogComponent(new DialogComponentBoolean(ImgReaderSettingsModels.createCheckFileFormatModel(),
				"Check file format for each file (may be slower)"));
		closeCurrentGroup();


		createNewGroup("Series & Groups");
		final SettingsModelBoolean readAllSeriesModel = ImgReaderSettingsModels.createReadAllSeriesModel();
		final SettingsModelDoubleRange seriesSelectionRangeModel = ImgReaderSettingsModels.createSeriesSelectionRangeModel();
		addDialogComponent(new DialogComponentBoolean(readAllSeriesModel, "Read all series"));
		addDialogComponent(new DialogComponentDoubleRange(seriesSelectionRangeModel, 0, Short.MAX_VALUE, 1, "Series index"));

		readAllSeriesModel.addChangeListener(e -> seriesSelectionRangeModel.setEnabled(!readAllSeriesModel.getBooleanValue()));
		seriesSelectionRangeModel.setEnabled(!readAllSeriesModel.getBooleanValue());

		addDialogComponent(
				new DialogComponentBoolean(ImgReaderSettingsModels.createIsGroupFilesModel(), "Load group files?"));
		closeCurrentGroup();

		createNewTab("Subset Selection");
		createNewGroup("Image Subset Selection");
		addDialogComponent(new DialogComponentSubsetSelection2(ImgReaderSettingsModels.createPlaneSelectionModel(),
				true, true, new int[] { 0, 1 }));
		closeCurrentGroup();
	}
}
