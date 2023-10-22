/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.f4sten.pomanalyzer;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.c0ps.franz.Kafka;
import dev.c0ps.maven.PomExtractor;
import eu.f4sten.infra.utils.MavenRepositoryUtils;
import eu.f4sten.infra.utils.TimedExecutor;
import eu.f4sten.pomanalyzer.utils.DatabaseUtils;
import eu.f4sten.pomanalyzer.utils.EffectiveModelBuilder;
import eu.f4sten.pomanalyzer.utils.PackagingFixer;
import eu.f4sten.pomanalyzer.utils.ProgressTracker;
import eu.f4sten.pomanalyzer.utils.ShrinkwrapResolver;

public class MainTest {

    private MavenRepositoryUtils repo;
    private EffectiveModelBuilder modelBuilder;
    private PomExtractor extractor;
    private DatabaseUtils db;
    private ShrinkwrapResolver resolver;
    private Kafka kafka;
    private PomAnalyzerArgs args;
    private PackagingFixer fixer;
    private TimedExecutor exec;

    private Main sut;
    private ProgressTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = mock(ProgressTracker.class);
        repo = mock(MavenRepositoryUtils.class);
        modelBuilder = mock(EffectiveModelBuilder.class);
        extractor = mock(PomExtractor.class);
        db = mock(DatabaseUtils.class);
        resolver = mock(ShrinkwrapResolver.class);
        kafka = mock(Kafka.class);
        args = new PomAnalyzerArgs();
        fixer = mock(PackagingFixer.class);
        exec = mock(TimedExecutor.class);

        sut = new Main(tracker, repo, modelBuilder, extractor, db, resolver, kafka, args, fixer, exec);

//        when(extractor.process(eq(null))).thenReturn(new Pom());
//        when(extractor.process(any(Model.class))).thenReturn(new Pom());
//        when(fixer.checkPackage(any(Pom.class))).thenReturn("jar");
    }

    @Test
    public void basicSmokeTest() {
        sut.hashCode();
        // sut.consume("{\"groupId\":\"log4j\",\"artifactId\":\"log4j\",\"version\":\"1.2.17\"}",
        // NORMAL);
    }

    // TODO extend test suite, right now this is only a stub for easy debugging
}