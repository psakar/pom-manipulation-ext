package org.commonjava.maven.ext.manip.state;

import static org.commonjava.maven.ext.manip.state.ProjectScmState.REMOVE_DOMAIN_FROM_SCM_CONNECTION_HOSTNAME;
import static org.commonjava.maven.ext.manip.state.ProjectScmState.UPDATE_PROJECT_SCM_CONNECTION_PROPERTY;
import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

public class ProjectScmStateTest {

    @Test
    public void projectScmConnectionManipulationIsEnabledWhenPropertyIsSet() throws Exception {
        Properties userProperties = setUpdateProjectScmConnectionPropertyTo(new Properties(), "true");

        ProjectScmState state = new ProjectScmState(userProperties);

        assertEquals(true, state.isUpdateProjectScmConnectionEnabled());
        assertEquals(false, state.isRemoveDomainFromScmConnectionHostname());
    }

    @Test
    public void setUpdateRemoveDomainFromScmConnectionHostnameTo() throws Exception {
        Properties userProperties = setUpdateRemoveDomainFromScmConnectionHostnameTo(
                new Properties(),
                "true");

        ProjectScmState state = new ProjectScmState(userProperties);

        assertEquals(false, state.isUpdateProjectScmConnectionEnabled());
        assertEquals(true, state.isRemoveDomainFromScmConnectionHostname());
    }

    public static Properties setUpdateProjectScmConnectionPropertyTo(Properties properties, String stateValue) {
        if (stateValue != null)
            properties.setProperty(UPDATE_PROJECT_SCM_CONNECTION_PROPERTY, stateValue);
        return properties;
    }

    public static Properties setUpdateRemoveDomainFromScmConnectionHostnameTo(Properties properties,
            String stateValue) {
        if (stateValue != null)
            properties.setProperty(REMOVE_DOMAIN_FROM_SCM_CONNECTION_HOSTNAME, stateValue);
        return properties;
    }

}
