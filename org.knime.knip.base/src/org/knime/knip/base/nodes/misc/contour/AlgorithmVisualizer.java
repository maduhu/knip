/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2014
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
 * ---------------------------------------------------------------------
 *
 * Created on Feb 14, 2014 by squareys
 */
package org.knime.knip.base.nodes.misc.contour;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.AWTImageTools;
import org.knime.knip.core.awt.Real2GreyRenderer;

/**
 *
 * @author Jonathan Hale (University of Konstanz)
 */
public class AlgorithmVisualizer<T extends RealType<T>> extends JFrame {

    RandomAccessibleInterval<T> m_interval;
    BufferedImage m_img;

    int[] m_pos;

    public AlgorithmVisualizer(final RandomAccessibleInterval<T> i) {
        setImage(i);

        m_pos = new int[]{0, 0};

        this.setVisible(true);
    }

    public void setImage(final RandomAccessibleInterval<T> i) {
        m_interval = i;
        m_img = AWTImageTools.renderScaledStandardColorImg(m_interval, new Real2GreyRenderer(), 1.0, new long[]{0, 0});

        this.setPreferredSize(new Dimension(m_img.getWidth(), m_img.getHeight()));
        this.setSize(new Dimension(m_img.getWidth(), m_img.getHeight()));
    }

    @Override
    public void paint(final Graphics g) {
        g.drawImage(m_img, 0, 0, this);

        g.setColor(Color.RED);
        g.drawRect(m_pos[0], m_pos[1], 1, 1);
    }

    public void setPosition(final int[] pos){
        m_pos = pos;
    }

    public void setPosition(final int x, final int y) {
        m_pos[0] = x;
        m_pos[1] = y;
    }

    public void update() {
        this.repaint();
    }
}
