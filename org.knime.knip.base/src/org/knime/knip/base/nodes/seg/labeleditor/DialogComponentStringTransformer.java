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
package org.knime.knip.base.nodes.seg.labeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.knime.knip.core.util.StringTransformer;

/**
 * Dialog Component for String Transformations
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class DialogComponentStringTransformer extends DialogComponent {

    /**
     * Renderer that will display the rowindex and rowkey with different background.
     */
    private static class ListRenderer extends DataColumnSpecListCellRenderer {

        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof String) {
                c.setFont(list.getFont().deriveFont(Font.ITALIC));
            }
            return c;
        }
    }

    private static final String DELIM = "$";

    private final JEditorPane m_expEdit;

    @SuppressWarnings("rawtypes")
    private final JList m_varList;

    private final String m_requiredVariable;

    private final boolean m_addColumnAsVariables;

    private final int m_portIdx;

    private DefaultListModel m_listModel;

    private String[] m_variables;

    /**
     * @param expressionModel settings modelF
     * @param addColumnAsVariables if true, the input columns will be added as variables, too
     * @param portIdx input port of the data table (for input column variables)
     * @param requiredVariable the variables the must appear in the expression, if <code>null</code> no required
     *            variable is assumed
     * @param variables all available variables
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DialogComponentStringTransformer(final SettingsModelString expressionModel,
                                            final boolean addColumnAsVariables, final int portIdx,
                                            final String requiredVariable, final String... variables) {
        super(expressionModel);
        m_addColumnAsVariables = addColumnAsVariables;
        m_portIdx = portIdx;
        m_requiredVariable = requiredVariable;

        m_listModel = new DefaultListModel();

        // Add default variables
        for (int i = 0; i < variables.length; i++) {
            m_listModel.addElement(variables[i]);
        }

        m_variables = variables;

        m_varList = new JList(m_listModel);

        m_expEdit = new JEditorPane();

        m_expEdit.setText(expressionModel.getStringValue());

        m_varList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_varList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                final Object selected = m_varList.getSelectedValue();
                if (selected != null) {
                    final String enter = "$" + selected + "$";
                    m_expEdit.replaceSelection(enter);
                    m_varList.clearSelection();
                    m_expEdit.requestFocus();
                }
            }
        });
        m_varList.setCellRenderer(new ListRenderer());

        m_expEdit.setFont(Font.getFont("Monospaced"));

        final JPanel finalPanel = new JPanel(new BorderLayout());
        finalPanel.add(createPanel(), BorderLayout.CENTER);

        final JPanel additionalOptions = new JPanel();
        additionalOptions.setLayout(new BoxLayout(additionalOptions, BoxLayout.Y_AXIS));
        finalPanel.add(additionalOptions, BorderLayout.SOUTH);

        getComponentPanel().setLayout(new BorderLayout());
        getComponentPanel().add(finalPanel, BorderLayout.CENTER);

        getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent arg0) {
                updateComponent();
            }
        });
        updateComponent();

    }

    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // NB nothing to do
    }

    private JPanel createPanel() {
        final JPanel finalPanel = new JPanel(new GridBagLayout());

        final GridBagConstraints gdb = new GridBagConstraints();

        gdb.insets = new Insets(10, 5, 0, 0);
        gdb.fill = GridBagConstraints.BOTH;

        gdb.weighty = 0.0;
        gdb.weightx = 0.2;
        gdb.gridx = 0;
        gdb.gridy = 0;
        finalPanel.add(new JLabel("Variable List"), gdb);

        gdb.gridy++;
        gdb.weighty = 1.0;
        gdb.weightx = 0.2;
        finalPanel.add(new JScrollPane(m_varList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gdb);

        gdb.gridx++;
        gdb.gridy = 0;
        gdb.weighty = 0.0;
        gdb.weightx = 0.8;
        finalPanel.add(new JLabel("Expression"), gdb);

        gdb.gridy++;
        gdb.weighty = 1.0;
        gdb.weightx = 0.8;
        finalPanel.add(new JScrollPane(m_expEdit, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), gdb);

        gdb.gridy++;

        return finalPanel;
    }

    /**
     * @return the expression transformer
     */
    public StringTransformer getStringTransformer() {
        return new StringTransformer(m_expEdit.getText().toString(), DELIM);
    }

    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_expEdit.setEnabled(enabled);
        m_varList.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(final String text) {
        m_expEdit.setToolTipText(text);
        m_varList.setToolTipText(text);
    }

    @Override
    protected void updateComponent() {
        m_listModel.clear();

        // Add default variables
        for (int i = 0; i < m_variables.length; i++) {
            m_listModel.addElement(m_variables[i]);
        }

        //variables from column
        if (m_addColumnAsVariables) {
            PortObjectSpec spec = getLastTableSpec(m_portIdx);
            if (spec != null && spec instanceof DataTableSpec) {

                for (final String col : ((DataTableSpec)spec).getColumnNames()) {
                    ((DefaultListModel)m_varList.getModel()).addElement(col);
                }
            }
        }

        // only update component if values are off
        final SettingsModelString model = (SettingsModelString)getModel();
        setEnabledComponents(model.isEnabled());
        m_expEdit.setText(model.getStringValue());
    }

    /**
     * Transfers the current value from the component into the model.
     *
     * @throws InvalidSettingsException if the string was not accepted.
     */
    private void updateModel() throws InvalidSettingsException {
        // we transfer the value from the field into the model
        ((SettingsModelString)getModel()).setStringValue(m_expEdit.getText());
    }

    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        if (m_requiredVariable != null && !m_expEdit.getText().contains(DELIM + m_requiredVariable + DELIM)) {
            throw new InvalidSettingsException("Label transformation: Required variable " + m_requiredVariable
                    + " is missing.");
        }
        updateModel();
    }

}
