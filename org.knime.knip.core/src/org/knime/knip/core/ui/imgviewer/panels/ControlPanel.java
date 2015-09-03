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
package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.panels.ViewerScrollEvent.Direction;

/**
 * This class represents a panel with an ArrowButton to click.
 *
 * @author Andreas Burger, University of Konstanz
 */
public class ControlPanel extends ViewerComponent {

    private final Position m_Position;

    private final BasicArrowButton m_button;

    private EventService m_eventService;

    /**
     * Create a new ControlPanel. The position given determines the arrow direction.
     *
     * @param pos The position this panel will be located in.
     */
    public ControlPanel(final Position pos) {
        super("", false);

        //TODO: Move back to img viewer.
        //TODO: Keep tooltip?

        m_Position = pos;
        m_button = new ImageToolTipButton(SwingConstants.SOUTH);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        if (pos == Position.WEST) {
            m_button.setDirection(SwingConstants.WEST);
            m_button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    m_eventService.publish(new ViewerScrollEvent(Direction.WEST));

                }

            });
        } else if (pos == Position.EAST) {
            m_button.setDirection(SwingConstants.EAST);
            m_button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    m_eventService.publish(new ViewerScrollEvent(Direction.EAST));

                }

            });
        } else if (pos == Position.NORTH) {
            m_button.setDirection(SwingConstants.NORTH);
            m_button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    m_eventService.publish(new ViewerScrollEvent(Direction.NORTH));

                }

            });
        } else if (pos == Position.SOUTH) {
            m_button.setDirection(SwingConstants.SOUTH);
            m_button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    m_eventService.publish(new ViewerScrollEvent(Direction.SOUTH));

                }

            });

        }
        add(m_button);

        setPreferredSize(new Dimension(20, 20));
        setMinimumSize(getPreferredSize());

    }

    @Override
    public Position getPosition() {
        return m_Position;
    }

    /**
     * Method used to get the underlying JButton
     *
     * @return The button displayed in this component.
     */
    public JButton getButton() {
        return m_button;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventService(final EventService eventService) {
        m_eventService = eventService;
        eventService.subscribe(this);

    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        // Nothing to do here
    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException {
        // Nothing to do here
    }

}
