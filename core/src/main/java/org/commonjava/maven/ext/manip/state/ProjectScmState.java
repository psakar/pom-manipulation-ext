/**
 * Copyright (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.ext.manip.state;

import java.util.Properties;

import org.commonjava.maven.ext.manip.impl.ProjectSourcesInjectingManipulator;

/**
 * Captures configuration parameters for use with {@link ProjectSourcesInjectingManipulator}. This state implementation captures two properties:
 *
 * <ul>
 *   <li><b>project.scm.read</b> - If true, try to inject SCM info</li>
 * </ul>
 */
public class ProjectScmState
    implements State
{

    /** Set this property to true using <code>-DupdateProjectScmConnection=true</code> in order to turn on injection of the project scm connection. */
    public static final String UPDATE_PROJECT_SCM_CONNECTION_PROPERTY = "updateProjectScmConnection";

    /** Set this property to true using <code>-removeDomainFromScmConnectionHostname=true</code> in order to remove domain from hostname used in project scm connection. */
    public static final String REMOVE_DOMAIN_FROM_SCM_CONNECTION_HOSTNAME = "removeDomainFromScmConnectionHostname";

    private final boolean updateProjectScmConnectionEnabled;

    private final boolean removeDomainFromScmConnectionHostname;


    static
    {
        State.activeByDefault.add( ProjectScmState.class );
    }

    /**
     * Detects the project.src.skip and project.src.version user properties. Sets the projectsourcesEnabled flag and the plugin version accordingly.
     *
     * @param userProperties the properties for the manipulator
     */
    public ProjectScmState( final Properties userProperties )
    {
        updateProjectScmConnectionEnabled = Boolean.parseBoolean(
                userProperties.getProperty(UPDATE_PROJECT_SCM_CONNECTION_PROPERTY, "false"));
        removeDomainFromScmConnectionHostname = Boolean.parseBoolean(
                userProperties.getProperty(REMOVE_DOMAIN_FROM_SCM_CONNECTION_HOSTNAME, "false"));
    }

    /**
     * @see ProjectScmState#UPDATE_PROJECT_SCM_CONNECTION_PROPERTY
     *
     * @return true if {@link #isUpdateProjectScmConnectionEnabled()}.
     */
    @Override
    public boolean isEnabled()
    {
        return isUpdateProjectScmConnectionEnabled();
    }

    /**
     * @see ProjectScmState#UPDATE_PROJECT_SCM_CONNECTION_PROPERTY
     * 
     * @return whether update project scm connection.
     */
    public boolean isUpdateProjectScmConnectionEnabled()
    {
        return updateProjectScmConnectionEnabled;
    }

    /**
     * @see ProjectScmState#REMOVE_DOMAIN_FROM_SCM_CONNECTION_HOSTNAME
     * 
     * @return whether remove domain from project scm connection hostname.
     */
    public boolean isRemoveDomainFromScmConnectionHostname() {
        return removeDomainFromScmConnectionHostname;
    }

}
