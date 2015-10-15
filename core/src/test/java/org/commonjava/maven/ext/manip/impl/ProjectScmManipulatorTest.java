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
package org.commonjava.maven.ext.manip.impl;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.commonjava.maven.ext.manip.state.ProjectScmStateTest.setUpdateProjectScmConnectionPropertyTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.commonjava.maven.ext.manip.ManipulationException;
import org.commonjava.maven.ext.manip.ManipulationSession;
import org.commonjava.maven.ext.manip.impl.ProjectScmManipulator.ScmConnectionLookup;
import org.commonjava.maven.ext.manip.model.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectScmManipulatorTest
{

    @Mock
    private MavenSession mavenSession;

    @Mock
    private MavenExecutionRequest request;

    @Mock
    private ScmConnectionLookup scmInfoLookup;

    private Project project;

    private ManipulationSession session;

    private ProjectScmManipulator manipulator;

    private List<Project> projects;

    @Before
    public void before() throws Exception {
        session = createManipulationSession();
        manipulator = new ProjectScmManipulator(scmInfoLookup);
        project = createProject();
        projects = singletonList(project);
    }

    @Test
    public void scmIsUpdatedWhenManipulationIsEnablednAndConnectionIsMissing()
        throws Exception
    {
        enablePomManipulation();
        when(scmInfoLookup.getConnection(any(File.class))).thenReturn(createRandomString());

        Set<Project> changes = runMaven();

        assertScmConnectionOfChangedProjects(scmInfoLookup.getConnection(null), changes);
    }

    @Test
    public void scmIsNotUpdatedWhenManipulationIsDisabledAndConnectionIsMissing() throws Exception {
        when(scmInfoLookup.getConnection(any(File.class))).thenReturn(createRandomString());

        Set<Project> changes = runMaven();

        assertEquals(0, changes.size());
    }

    @Test
    public void scmIsNotUpdatedWhenManipulationIsEnabledAndConnectionIsSameAsLookedUp() throws Exception {
        enablePomManipulation();

        String scmConnection = createRandomString();
        setProjectScmConnection(scmConnection);
        when(scmInfoLookup.getConnection(any(File.class))).thenReturn(scmConnection);

        Set<Project> changes = runMaven();

        assertEquals(0, changes.size());
    }

    @Test
    public void scmIsNotUpdatedWhenManipulationIsEnabledAndConnectionLookupThrowsError()
            throws Exception {
        enablePomManipulation();

        String scmConnection = createRandomString();
        setProjectScmConnection(scmConnection);
        when(scmInfoLookup.getConnection(any(File.class))).thenThrow(new RuntimeException("error"));

        Set<Project> changes = runMaven();

        assertEquals(0, changes.size());
    }

    private Set<Project> runMaven() throws ManipulationException {
        manipulator.init(session);
        return manipulator.applyChanges(projects, session);
    }

    private void setProjectScmConnection(String connection) {
        Scm scm = new Scm();
        scm.setConnection(connection);
        project.getModel().setScm(scm);
    }

    private void enablePomManipulation() {
        setUpdateProjectScmConnectionPropertyTo(session.getUserProperties(), TRUE.toString());
    }
    
    private void assertScmConnectionOfChangedProjects(String expectedConnection,
            Set<Project> changes) {
        assertEquals(projects.size(), changes.size());
        for (Project project : changes) {
            assertEquals(expectedConnection,
                    project.getModel().getScm().getConnection());
        }
    }

    private ManipulationSession createManipulationSession() {
        when(mavenSession.getRequest()).thenReturn(request);
        when(request.getUserProperties()).thenReturn(new Properties());
        ManipulationSession session = new ManipulationSession();
        session.setMavenSession(mavenSession);
        return session;
    }

    private Project createProject() throws ManipulationException {
        Model model = new Model();
        model.setGroupId(createRandomString());
        model.setArtifactId(createRandomString());
        model.setVersion(createRandomString());

        return new Project(null, model);
    }

    static String createRandomString() {
        return new Random().nextLong() + "";
    }

}
