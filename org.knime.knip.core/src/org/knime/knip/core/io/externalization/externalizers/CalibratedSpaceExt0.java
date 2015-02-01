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
package org.knime.knip.core.io.externalization.externalizers;


import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;

import org.knime.knip.core.data.img.CalibratedAxisSpace;
import org.knime.knip.core.data.img.DefaultCalibratedAxisSpace;
import org.knime.knip.core.io.externalization.BufferedDataInputStream;
import org.knime.knip.core.io.externalization.BufferedDataOutputStream;
import org.knime.knip.core.io.externalization.Externalizer;

/**
 *
 * Former serialization of CalibratedSpace. Only supporting de-serialization of DefaultLinearSpace. Use proper
 * implementations for CalibratedSpaces (see e.g. LinearSpaceExt0.java)
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class CalibratedSpaceExt0 implements Externalizer<CalibratedAxisSpace> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CalibratedAxisSpace> getType() {
        return CalibratedAxisSpace.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CalibratedAxisSpace read(final BufferedDataInputStream in) throws Exception {
        final int numDims = in.readInt();
        final DefaultCalibratedAxisSpace res = new DefaultCalibratedAxisSpace(numDims);
        for (int d = 0; d < numDims; d++) {
            final char[] label = new char[in.readInt()];
            in.read(label);
            DefaultLinearAxis axis = new DefaultLinearAxis(Axes.get(String.valueOf(label)));
            axis.setScale(in.readDouble());
            res.setAxis(axis, d);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final BufferedDataOutputStream out, final CalibratedAxisSpace obj) throws Exception {
        out.writeInt(obj.numDimensions());
        for (int d = 0; d < obj.numDimensions(); d++) {
            final char[] label = obj.axis(d).type().getLabel().toCharArray();
            out.writeInt(label.length);
            out.write(label);

            double scale = obj.axis(d).averageScale(0, 1);
            if (Double.isNaN(scale)) {
                out.writeDouble(0.0d);
            } else {
                out.writeDouble(scale);
            }
        }
    }
}
