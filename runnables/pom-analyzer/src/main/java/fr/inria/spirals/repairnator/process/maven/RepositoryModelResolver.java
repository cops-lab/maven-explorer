/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2018 INRIA
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
// copied (and slightly adapted) from https://github.com/eclipse/repairnator
package fr.inria.spirals.repairnator.process.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * This class allows to resolve Maven artifact in order to build a Maven model
 * Inspired by code from:
 * https://github.com/rickardoberg/neomvn/
 */

@SuppressWarnings("deprecation")
public class RepositoryModelResolver implements ModelResolver {
    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";
    private static final String MAVEN_CENTRAL_URL_MIRROR_1 = "http://repo.maven.apache.org/maven2";
    private static final String MAVEN_CENTRAL_URL_MIRROR_2 = "http://uk.maven.org/maven2";

    private static final Logger logger = LoggerFactory.getLogger(RepositoryModelResolver.class);
    private File localRepository;

    private List<Repository> repositories = new ArrayList<Repository>();

    public RepositoryModelResolver(File localRepository) {
        this.localRepository = localRepository;
        Repository mainRepo = new Repository();
        mainRepo.setUrl(MAVEN_CENTRAL_URL);
        mainRepo.setId("central");
        repositories.add(mainRepo);

        mainRepo = new Repository();
        mainRepo.setUrl(MAVEN_CENTRAL_URL_MIRROR_1);
        mainRepo.setId("mirror1");
        repositories.add(mainRepo);

        mainRepo = new Repository();
        mainRepo.setUrl(MAVEN_CENTRAL_URL_MIRROR_2);
        mainRepo.setId("mirror2");
        repositories.add(mainRepo);
    }

    private File getLocalFile(String groupId, String artifactId, String versionId) {
        File pom = this.localRepository;
        String[] groupIds = groupId.split("\\.");

        // go through subdirectories
        for (String id : groupIds) {
            pom = new File(pom, id);
        }

        pom = new File(pom, artifactId);

        pom = new File(pom, versionId);

        pom = new File(pom, artifactId + "-" + versionId + ".pom");
        return pom;
    }

    private void download(File localRepoFile) throws IOException {
        for (Repository repository1 : repositories) {
            String repository1Url = repository1.getUrl();
            if (repository1Url.endsWith("/")) {
                repository1Url = repository1Url.substring(0, repository1Url.length() - 1);
            }
            URL url = new URL(repository1Url
                    + localRepoFile.getAbsolutePath().substring(this.localRepository.getAbsolutePath().length()));

            logger.debug("Downloading " + url);

            var client = new OkHttpClient();
            var request = new Request.Builder()
                    .url(url)
                    .build();

            try(var response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    localRepoFile.getParentFile().mkdirs();
                    FileWriter out = new FileWriter(localRepoFile);
                    out.write(response.body().string());
                    out.flush();
                    out.close();
                    return;
                }
            }
        }
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String versionId)
            throws UnresolvableModelException {
        File pom = getLocalFile(groupId, artifactId, versionId);

        if (!pom.exists()) {
            try {
                download(pom);
            } catch (IOException e) {
                throw new UnresolvableModelException("Could not download POM", groupId, artifactId, versionId, e);
            }
        }

        return new FileModelSource(pom);
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
        this.addRepository(repository, false);
    }

    @Override
    public void addRepository(Repository repository, boolean b) throws InvalidRepositoryException {
        for (Repository existingRepository : repositories) {
            if (existingRepository.getId().equals(repository.getId()) && !b) {
                return;
            }
        }

        repositories.add(repository);
    }

    @Override
    public ModelResolver newCopy() {
        return new RepositoryModelResolver(this.localRepository);
    }

    @Override
    public ModelSource resolveModel(Dependency d) throws UnresolvableModelException {
        return resolveModel(d.getGroupId(), d.getArtifactId(), d.getVersion());
    }
}