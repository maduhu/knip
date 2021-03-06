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
package org.knime.knip.base.nodes.misc.splitter;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.KNIMEKNIPPlugin;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.dialog.DialogComponentDimSelection;
import org.knime.knip.base.node.nodesettings.SettingsModelDimSelection;

/**
 * The Dialog for the Image Processing Node.
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class Splitter2NodeDialog extends DefaultNodeSettingsPane {

    private static final String DEFAULT_SUFFIX = "_split";

    private SettingsModelString colCreationMode = Splitter2NodeSettings.createColCreationModeModel();

    private SettingsModelString columnSuffixModel = Splitter2NodeSettings.createColSuffixNodeModel();

    /**
     * Dialog with Column Selection.
     *
     */
    @SuppressWarnings("unchecked")
    public Splitter2NodeDialog() {
        super();

        createNewGroup("Image Column");

        addDialogComponent(new DialogComponentColumnNameSelection(Splitter2NodeSettings.createColumnModel(),
                "Image Column", 0, true, ImgPlusValue.class));

        closeCurrentGroup();

        createNewGroup("Column creation");
        addDialogComponent(new DialogComponentStringSelection(colCreationMode, "Column Creation Mode",
                Splitter2NodeSettings.COL_CREATION_MODES));

        addDialogComponent(new DialogComponentString(columnSuffixModel, "Column suffix"));

        //add append suffix logic
        colCreationMode.addChangeListener(e -> {
            if (colCreationMode.getStringValue().equals(Splitter2NodeSettings.COL_CREATION_MODES[1])) {
                //append
                if (columnSuffixModel.getStringValue().isEmpty()) {
                    columnSuffixModel.setStringValue(DEFAULT_SUFFIX);
                }
            } else {
                if (columnSuffixModel.getStringValue().equals(DEFAULT_SUFFIX)) {
                    columnSuffixModel.setStringValue("");
                }
            }
        });

        closeCurrentGroup();

        createNewGroup("Dimension selection");
        final SettingsModelDimSelection dimSelection = Splitter2NodeSettings.createDimSelectionModel();
        addDialogComponent(new DialogComponentDimSelection(dimSelection, "Dimensions"));
        closeCurrentGroup();

        createNewTab("Advanced");

        final SettingsModelBoolean isAdvanced = Splitter2NodeSettings.createIsAdvancedModel();
        addDialogComponent(new DialogComponentBoolean(isAdvanced, "use advance settings"));
        final SettingsModelIntegerBounded[] models = Splitter2NodeSettings.createAdvancedModels();
        final String[] labels = KNIMEKNIPPlugin.parseDimensionLabels();
        int i = 0;
        for (final SettingsModelIntegerBounded model : models) {
            addDialogComponent(new DialogComponentNumber(model, labels[i++], 1));
        }
        isAdvanced.addChangeListener(e -> {
            for (final SettingsModel sm : models) {
                sm.setEnabled(isAdvanced.getBooleanValue());
            }
            dimSelection.setEnabled(!isAdvanced.getBooleanValue());
        });
        for (final SettingsModel sm : models) {
            sm.setEnabled(isAdvanced.getBooleanValue());
        }
        dimSelection.setEnabled(!isAdvanced.getBooleanValue());
    }
}
