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
package org.knime.knip.base.node.nodesettings;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.config.Config;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 * TODO Auto-generated
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 * @param <L>
 */
public class SettingsModelFilterSelection<L extends Comparable<L>> extends SettingsModel {

    private final String m_caseSensitiveMatchCFG = "caseSensitiveMatchCFG";

    private final String m_containsWildCardsCFG = "containsWildCardsCFG";

    private final String m_regularExpressionCFG = "regularExpressionCFG";

    /*
     *
     */
    private final String m_configName;

    /*
     * The operator
     */
    private Operator m_operator = Operator.OR;

    /*
     * The rules
     */
    private String[] m_rules = new String[0];

    private boolean m_caseSensitiveMatch = false;

    private boolean  m_containsWildCards = false;

    private boolean m_regularExpression = false;

    /**
     * Constructor
     *
     * @param configName
     */
    public SettingsModelFilterSelection(final String configName) {
        m_configName = configName;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SettingsModelFilterSelection<L> createClone() {
        final SettingsModelFilterSelection<L> model = new SettingsModelFilterSelection<L>(m_configName);
        model.m_rules = m_rules.clone();
        model.m_operator = m_operator;
        return model;
    }

    @Override
    protected String getConfigName() {
        return m_configName;
    }

    @Override
    protected String getModelTypeID() {
        return "SMID_filterSelection";
    }

    /**
     * @return rulebasedlabelfilter operator
     */
    public RulebasedLabelFilter.Operator getOperator() {
        return m_operator;
    }

    /**
     * @return rule-based label filter
     */
    public RulebasedLabelFilter<L> getRulebasedFilter() {

        final String[] rules = getRules().clone();

        final Pattern[] patterns = new Pattern[rules.length];
        for (int r = 0; r < rules.length; r++) {
            patterns[r] = RulebasedLabelFilter.compileRegularExpression(m_caseSensitiveMatch, m_containsWildCards, m_regularExpression, rules[r]);
        }

        return new RulebasedLabelFilter<L>(patterns, getOperator());
    }

    /**
     * @return rules as string array
     */
    public String[] getRules() {
        return m_rules;
    }

    @Override
    protected void loadSettingsForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        try {
            loadSettingsForModel(settings);
        } catch (final InvalidSettingsException ex) {
            // keep old values
        }
    }

    @Override
    protected void loadSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        final Config lists = settings.getConfig(m_configName);
        m_rules = new String[lists.getInt("numrules")];
        for (int i = 0; i < m_rules.length; i++) {
            m_rules[i] = lists.getString("rule" + i);
        }

        m_operator = RulebasedLabelFilter.Operator.values()[lists.getInt("operator")];

        try {
            m_caseSensitiveMatch = lists.getBoolean(m_caseSensitiveMatchCFG);
            m_containsWildCards = lists.getBoolean(m_containsWildCardsCFG);
            m_regularExpression = lists.getBoolean(m_regularExpressionCFG);
        } catch (InvalidSettingsException e) {
            // introduced in KNIP 1.5.4
            m_caseSensitiveMatch = true;
            m_containsWildCards = true;
            m_regularExpression = false;
        }
    }

    @Override
    protected void saveSettingsForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    @Override
    protected void saveSettingsForModel(final NodeSettingsWO settings) {
        final Config lists = settings.addConfig(m_configName);
        lists.addInt("numrules", m_rules.length);

        for (int i = 0; i < m_rules.length; i++) {
            lists.addString("rule" + i, m_rules[i]);
        }

        lists.addInt("operator", m_operator.ordinal());

        lists.addBoolean(m_caseSensitiveMatchCFG, m_caseSensitiveMatch);
        lists.addBoolean(m_containsWildCardsCFG, m_containsWildCards);
        lists.addBoolean(m_regularExpressionCFG, m_regularExpression);
    }

    /**
     * @param operator
     */
    public void setOperator(final RulebasedLabelFilter.Operator operator) {
        m_operator = operator;
    }

    /**
     * @param rules
     */
    public void setRules(final String[] rules) {
        m_rules = rules.clone();
    }

    @Override
    public String toString() {
        return Arrays.toString(m_rules);
    }

    @Override
    protected void validateSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {

        settings.getStringArray(m_configName);

        final Config lists = settings.getConfig(m_configName);
        final int numRules = lists.getInt("numrules");
        for (int i = 0; i < numRules; i++) {
            lists.getString("rule" + i);
        }

        m_operator = RulebasedLabelFilter.Operator.values()[lists.getInt("operator")];

        try {
            m_caseSensitiveMatch = lists.getBoolean(m_caseSensitiveMatchCFG);
            m_containsWildCards = lists.getBoolean(m_containsWildCardsCFG);
            m_regularExpression = lists.getBoolean(m_regularExpressionCFG);
        } catch (InvalidSettingsException e) {
            // introduced in KNIP 1.5.4
            m_caseSensitiveMatch = true;
            m_containsWildCards = true;
            m_regularExpression = false;
        }
    }

    /**
     * Is caseSensitiveMatch active.
     *
     * @param status
     */
    public void setCaseSensitiveMatch(final boolean status) {
        this.m_caseSensitiveMatch = status;
    }

    /**
     * CaseSensitveMatch status.
     *
     * @return status
     */
    public boolean getCaseSensitiveMatch() {
        return this.m_caseSensitiveMatch;
    }

    /**
     * Is containsWildCards active.
     * Note: if containsWildCards is true, regularExpression has to be false.
     *
     * @param status
     */
    public void setContainsWildCards(final boolean status) {
        if (status) {
            this.m_containsWildCards = true;
            this.m_regularExpression = false;
        } else {
            this.m_containsWildCards = false;
        }
    }

    /**
     * ContainsWildCards status.
     *
     * @return status
     */
    public boolean getContainsWildCards() {
        return this.m_containsWildCards;
    }

    /**
     * Is regularExpression active.
     * Note: if regularExpression is true, containsWildCards has to be false.
     *
     * @param status
     */
    public void setRegularExpression(final boolean status) {
        if (status) {
            this.m_regularExpression = true;
            this.m_containsWildCards = false;
        } else {
            this.m_regularExpression = false;
        }
    }

    /**
     * RegularExpression status.
     *
     * @return status
     */
    public boolean getRegularExpression() {
        return this.m_regularExpression;
    }
}
