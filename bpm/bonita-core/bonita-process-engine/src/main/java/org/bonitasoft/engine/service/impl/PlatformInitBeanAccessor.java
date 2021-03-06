/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 */
public class PlatformInitBeanAccessor extends SpringBeanAccessor {

    public PlatformInitBeanAccessor() {
        super(null);
    }

    @Override
    protected void init() {
        try {
            getPlatformLicensesSetup().extractLicenses();
        } catch (IOException | NamingException | PlatformException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    PlatformLicensesSetup getPlatformLicensesSetup() {
        return new PlatformLicensesSetup();
    }

    @Override
    protected Properties getProperties() throws IOException {
        return BONITA_HOME_SERVER.getPlatformInitProperties();
    }

    @Override
    protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
        return BONITA_HOME_SERVER.getPlatformInitConfiguration();
    }

    @Override
    protected List<String> getSpringFileFromClassPath(boolean cluster) {
        return Collections.singletonList("bonita-platform-init-community.xml");
    }
}
