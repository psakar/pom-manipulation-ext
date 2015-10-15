package org.commonjava.maven.ext.manip.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.commonjava.maven.ext.manip.impl.ProjectScmManipulator.GitScmConnectionLookup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GitScmConnectionLookupTest {

    private File localGitRepoFolder;

    @Before
    public void before() throws Exception {
        localGitRepoFolder = createTemporaryLocalGitRepo();
    }

    @After
    public void after() throws Exception {
        FileUtils.deleteQuietly(localGitRepoFolder);
    }

    @Test
    public void urlIsReadFromGitConfigForRemoteOrigin() throws Exception {
        GitScmConnectionLookup connectionLookup = new GitScmConnectionLookup();

        String connection = connectionLookup.getConnection(localGitRepoFolder);

        assertEquals("scm:git:" + "http://test/url", connection);
    }

    private File createTemporaryLocalGitRepo() throws IOException {
        File localGitRepoFolder = new File(FileUtils.getTempDirectory(),
                System.currentTimeMillis() + "");
        File gitConfigFolder = new File(localGitRepoFolder, ".git");
        gitConfigFolder.mkdirs();
        FileUtils.copyFile(new File("src/test/resources/git-url-test1/git/config"),
                new File(gitConfigFolder, "config"));
        return localGitRepoFolder;
    }

}
