/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2015
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
 * Created on Jul 16, 2015 by pop210958
 */
package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JPanel;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.TablePositionEvent;

/**
 *
 * @author pop210958
 */
public class TableOverviewPanel extends ViewerComponent {

    private EventService m_eventService;

    private final JPanel m_canvas;

    private  int m_width = 0;

    private  int m_height = 0;

    private  int m_x = 0;

    private  int m_y = 0;

    /**
     * @param title
     * @param isBorderHidden
     */
    public TableOverviewPanel() {
        super("", true);

        setLayout(new BorderLayout());

        m_canvas = new JPanel() {

            private void drawArrow(final int x1, final int y1, final int x2, final int y2, final String label, final Graphics2D g, final int dir){
                Point2D base = new Point2D.Double(x1,y1);
                Point2D tip = new Point2D.Double(x2,y2);
                Point2D mid = new Point2D.Double(x1 + (x2-x1)*0.4, y1 + (y2-y1)*0.4);
                g.draw(new Line2D.Double(base.getX(), base.getY(),tip.getX(), tip.getY()));
                Font f = g.getFont();
                TextLayout t = new TextLayout(label, f, g.getFontRenderContext());
                Rectangle2D b = t.getBounds();
                int r;
                if(dir == 0) {
                    r= (int)(b.getHeight() + 8);
                } else{
                    r =(int)(b.getWidth() +5);
                }
                g.setColor(Color.GRAY);
                g.fillOval((int)mid.getX()-r/2, (int)mid.getY()-r/2, r, r);
                g.setColor(Color.BLACK);


                g.drawString(label, (int)(mid.getX()-b.getWidth()/2), (int)(mid.getY() +b.getHeight()/2));
                double angle = Math.atan2(y2-y1, x2-x1) + Math.toRadians(30);
                for(int i = 0; i < 2; i++)
                {
                    double x = tip.getX() - 20 * Math.cos(angle);
                    double y = tip.getY() - 20 * Math.sin(angle);
                    g.draw(new Line2D.Double(tip.getX(), tip.getY(), x, y));
                    angle -= 2*Math.toRadians(30);
                }


            }

            @Override
            public void paint(final Graphics g) {
                super.paint(g);
                int boxwidth = 100;
                int boxheight = 50;

                int w = getWidth();
                int h = getHeight();

                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke( new BasicStroke(3.0f));
                g2.fill(new RoundRectangle2D.Double(w/2 - boxwidth/2, h/2 - boxheight/2, boxwidth, boxheight,20,20));
                g2.setColor(Color.BLACK);

                Font f = g.getFont();
                TextLayout t = new TextLayout("X: "+ m_x, f, g2.getFontRenderContext());
                Rectangle2D b = t.getBounds();

                g2.drawString("X: "+m_x, (int)(w/2 - 25-b.getWidth()/2), (int)(h/2 + b.getHeight()/2));

                t = new TextLayout("Y: "+ m_y, f, g2.getFontRenderContext());
                b = t.getBounds();

                g2.drawString("Y: "+m_y, (int)(w/2 + 25-b.getWidth()/2), (int)(h/2 + b.getHeight()/2));

                drawArrow(w/2, h/2 - boxheight/2 - 5, w/2, 20,""  + (m_y), g2, 0);

                drawArrow(w/2 + boxwidth/2 + 5, h/2 , w - 20, h/2 , "" + (m_width-m_x -1), g2, 1);
//
                drawArrow(w/2 - boxwidth/2 - 5, h/2 , 20, h/2 , "" + (m_x), g2, 1);
//
                drawArrow(w/2, h/2 + boxheight/2 + 5, w/2, h - 20, "" + (m_height-m_y -1), g2, 0);
            }
        };
       m_canvas.setBackground(Color.GRAY);
        m_canvas.setMinimumSize(new Dimension(200, 200));

        add(m_canvas, BorderLayout.CENTER);
        setMaximumSize(new Dimension(300, 200));
        setMinimumSize(new Dimension(200, 200));
        setPreferredSize(new Dimension(250, 200));
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventService(final EventService eventService) {
        m_eventService = eventService;
        m_eventService.subscribe(this);

    }

    @EventListener
    public void onTablePositionEvent(final TablePositionEvent e)
    {
        m_height = e.getheight();
        m_width = e.getwidth();
        m_x = e.getx();
        m_y = e.gety();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Position getPosition() {
        // TODO Auto-generated method stub
        return Position.ADDITIONAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }

}
