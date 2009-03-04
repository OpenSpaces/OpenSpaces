/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.maven.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.springframework.util.FileCopyUtils;

/**
 * Installs the GigaSpaces license file in the local repository.
 *
 * @goal install-license
 * @requiresProject false
 */
public class InstallLicenseMojo extends AbstractOpenSpacesMojo {

    /**
     * The license file.
     *
     * @parameter expression="${file}"
     * @required
     */
    private File file;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * GigaSpaces version.
     *
     * @parameter expression="${version}"
     * @required
     */
    protected String version;


    public void executeMojo() throws MojoExecutionException {
        if (!file.exists()) {
            throw new MojoExecutionException("GigaSpaces license file not found at " + file.getAbsolutePath());
        }

        if (version == null || version.length() == 0) {
            throw new MojoExecutionException("GigaSpaces version is not specified");
        }

        File baseDir = new File(localRepository.getBasedir());
        if (!baseDir.exists()) {
            throw new MojoExecutionException("The local repository was not found at " + baseDir.getAbsolutePath());
        }
        File targetDir = new File(localRepository.getBasedir() + "/com/gigaspaces/gs-runtime/" + version + "/");
        if (!targetDir.exists()) {
            throw new MojoExecutionException("The gs-boot artifact is not installed at [" + targetDir.getAbsolutePath() + "]");
        }

        try {
            File target = new File(targetDir, "gslicense.xml");
            PluginLog.getLog().info("Copying license file to: " + target.getAbsolutePath());
            FileCopyUtils.copy(file, target);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy GigaSpaces license file.", e);
        }
    }
}
