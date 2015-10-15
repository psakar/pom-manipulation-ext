/**
 * Copyr ight (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
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

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.maven.ext.manip.ManipulationException;
import org.commonjava.maven.ext.manip.ManipulationSession;
import org.commonjava.maven.ext.manip.model.Project;
import org.commonjava.maven.ext.manip.state.ProjectScmState;
import org.commonjava.maven.ext.manip.state.ProjectSourcesInjectingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple manipulator that fills in project scm url if it's not present. 
 * 
 * in {@link ProjectSourcesInjectingState}.
 */
@Component( role = Manipulator.class, hint = "project-sources" )
public class ProjectScmManipulator
    implements Manipulator
{

    static final String SCM_GIT = "scm:git:";

    // private static final String VALIDATE_PHASE = "validate";

    // private static final String INITIALIZE_PHASE = "initialize";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final ScmConnectionLookup scmInfoLookup;
    
    
    public ProjectScmManipulator() {
        this(new GitScmConnectionLookup());
    }

    // @VisibleForTesting
    ProjectScmManipulator(ScmConnectionLookup scmInfoLookup) {
        this.scmInfoLookup = scmInfoLookup;
    }
    
    @Override
    public void init( final ManipulationSession session )
        throws ManipulationException
    {
        session.setState(new ProjectScmState(session.getUserProperties()));
    }

    @Override
    public void scan( final List<Project> projects, final ManipulationSession session )
        throws ManipulationException
    {
    }

    /**
     * If enabled, grab the execution root pom (which will be the topmost POM in terms of directory structure). Check for the
     * presence of the project-sources-maven-plugin in the base build (/project/build/plugins/). Inject a new plugin execution for creating project
     * sources if this plugin has not already been declared in the base build section.
     */
    @Override
    public Set<Project> applyChanges( final List<Project> projects, final ManipulationSession session )
        throws ManipulationException
    {
        final ProjectScmState state = session.getState(ProjectScmState.class);

        // This manipulator will only run if its enabled
        if (state.isEnabled())
        {
            Set<Project> modifiedProjects = new HashSet<Project>();
            for ( final Project project : projects )
            {
                // if ( project.isExecutionRoot() )
                // {
                logger.info("Examining {} to apply sources/metadata plugins.", project);

                String scmConnection = null;
                try {
                    scmConnection = scmInfoLookup.getConnection(new File("."));
                    if (scmConnection != null && state.isRemoveDomainFromScmConnectionHostname()) {
                        String hostname = extractHostnameFromScmConnection(scmConnection);
                        System.err.println("hostname " + hostname);
                        String hostnameWithoutDomain = stripDomainName(hostname);
                        System.err.println("hostnameWithoutDomain " + hostnameWithoutDomain);
                        scmConnection = scmConnection.replace(hostname, hostnameWithoutDomain);
                    }
                } catch (Exception e) {
                    logger.error("Can not find scm connection for project " + project + " - "
                            + e.getMessage());
                }
                if (state.isUpdateProjectScmConnectionEnabled())
                {

                    final Model model = project.getModel();
                    Scm scm = model.getScm();
                    if (scm == null) {
                        scm = new Scm();
                        model.setScm(scm);
                    }

                    String currentScmConnection = scm.getConnection();
                    if (scmConnection != null && (currentScmConnection == null
                            || !currentScmConnection.equals(scmConnection))) {
                        scm.setConnection(scmConnection);
                        modifiedProjects.add(project);
                    }
                }

                // }
            }
            return modifiedProjects;
        }

        return Collections.emptySet();
    }

    // @VisibleForTesting
    String stripDomainName(String hostname) {
        if (isBlank(hostname)) {
            return null;
        }
        int index = hostname.indexOf(".");
        String beforeDot = index < 0 ? hostname : hostname.substring(0, index);
        return isBlank(beforeDot) ? null : beforeDot.trim();
    }

    // inlined commons StringUtils.isBlank
    private boolean isBlank(String string) {
        int strLen;
        if (string == null || (strLen = string.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // @VisibleForTesting
    String extractHostnameFromScmConnection(String scmConnection) {
        if (isBlank(scmConnection)) {
            return null;
        }
        if (!scmConnection.startsWith(SCM_GIT)) {
            return null;
        }
        scmConnection = scmConnection.substring(SCM_GIT.length());
        List<String> prefixes = asList("git://", "http://", "https://");
        for (String prefix : prefixes) {
            if (scmConnection.startsWith(prefix)) {
                return stripTypeAndAfterHostname(scmConnection, prefix);
            }
        }

        int index = scmConnection.indexOf(":");
        if (index > 0) {
            String string = scmConnection.substring(0, index);
            index = string.indexOf("@");
            return index >= 0 && index + 1 < string.length() ? string.substring(index + 1) : string;
        }

        return null;
    }

    private String stripTypeAndAfterHostname(String scmConnection, String prefix) {
        String withoutType = scmConnection.substring(prefix.length());
        int index = withoutType.indexOf("/");
        return index < 0 ? withoutType : withoutType.substring(0, index);
    }

    @Override
    public int getExecutionIndex()
    {
        return 65;
    }

    
    static interface ScmConnectionLookup {
        public String getConnection(File localGitRepoFolder);
    }

    static class GitScmConnectionLookup implements ScmConnectionLookup {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final String remoteName = "origin";

        public String getConnection(File localGitRepoFolder) {
            logger.debug("Find connection url of local git repo "
                    + localGitRepoFolder.getAbsolutePath());
            String connection = null;
            File gitConfigFile = new File(new File(localGitRepoFolder, ".git"), "config");
            try {
                List<String> lines = readTextFileToList(gitConfigFile);
                int startOfRemoteSection = findIndexOfLineStartingWith(lines,
                        "[remote \"" + remoteName + "\"]");
                if (startOfRemoteSection < 0) {
                    throw new IllegalArgumentException("Remote " + remoteName + " was not found in "
                            + gitConfigFile.getAbsolutePath());
                }
                if (startOfRemoteSection + 1 >= lines.size()) {
                    throw new IllegalArgumentException("Url of remote " + remoteName
                            + " was not found in " + gitConfigFile.getAbsolutePath());
                }
                String lineWithUrl = lines.get(startOfRemoteSection + 1);
                int index = lineWithUrl.indexOf("url = ");
                if (index < 0 || lineWithUrl.length() <= index + 6) {
                    throw new IllegalArgumentException("Url of remote " + remoteName
                            + " was not found in " + gitConfigFile.getAbsolutePath());
                }
                connection = lineWithUrl.substring(index + 6).trim();
                if (connection.length() == 0) {
                    throw new IllegalArgumentException("Url of remote " + remoteName
                            + " was not specified in " + gitConfigFile.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Can not read url of remote " + remoteName + " from git config "
                        + gitConfigFile.getAbsolutePath() + " - " + e.getMessage(), e);
            }
            return connection == null ? null : SCM_GIT + connection;
        }

        /* inlined from commons-io FileUtils.readLines */
        private List<String> readTextFileToList(File file) throws IOException {
            InputStream in = null;
            try {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        throw new IOException(
                                "File '" + file.getAbsolutePath() + "' exists but is a directory");
                    }
                    if (file.canRead() == false) {
                        throw new IOException(
                                "File '" + file.getAbsolutePath() + "' cannot be read");
                    }
                } else {
                    throw new FileNotFoundException(
                            "File '" + file.getAbsolutePath() + "' does not exist");
                }
                in = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in, Charset.forName("utf-8")));
                List<String> list = new ArrayList<String>();
                String line = reader.readLine();
                while (line != null) {
                    list.add(line);
                    line = reader.readLine();
                }
                return list;
            } finally {
                Closeable closeable = (Closeable)in;
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }

        private int findIndexOfLineStartingWith(List<String> lines, String string) {
            int index = 0;
            for (String line : lines) {
                if (line != null && line.startsWith(string)) {
                    return index;
                }
                index++;
            }
            return -1;
        }
    }
}
