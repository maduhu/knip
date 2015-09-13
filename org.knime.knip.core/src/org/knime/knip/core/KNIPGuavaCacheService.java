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
 */

package org.knime.knip.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.cache.CacheService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * {@link CacheService} implementation wrapping a guava {@link Cache}.
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class KNIPGuavaCacheService extends AbstractService implements CacheService {

    @Parameter
    private MemoryService ms;

    private Cache<Object, Object> cache;

    private final Semaphore gate = new Semaphore(1);

    @Override
    public void initialize() {
        //FIXME: Make parameters accessible via image processing config at some point
        cache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.SECONDS).maximumSize((long)(ms.limit() * 0.8))
                .weakKeys().softValues().build();

        ms.register(new MemoryAlertable() {

            @Override
            public void memoryLow() {
                if (gate.tryAcquire()) {
                    cache.cleanUp();
                    gate.release();
                }

            }
        });
    }

    @Override
    public void put(final Object key, final Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(final Object key) {
        return cache.getIfPresent(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V get(final Object key, final Callable<V> valueLoader) throws ExecutionException {
        return (V)cache.get(key, valueLoader);
    }
}