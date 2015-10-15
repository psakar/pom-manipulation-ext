package org.commonjava.maven.ext.manip.state;

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
    }

    public static Properties setUpdateProjectScmConnectionPropertyTo(Properties properties, String stateValue) {
        if (stateValue != null)
            properties.setProperty(UPDATE_PROJECT_SCM_CONNECTION_PROPERTY, stateValue);
        return properties;
    }

}
