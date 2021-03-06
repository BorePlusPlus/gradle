/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.integtests.fixtures

import org.gradle.util.TestFile
import org.junit.Rule
import spock.lang.Specification

/**
 * Spockified version of AbstractIntegrationTest.
 * 
 * Plan is to bring features over as needed.
 */
class AbstractIntegrationSpec extends Specification {
    
    @Rule final GradleDistribution distribution = new GradleDistribution()
    @Rule final GradleDistributionExecuter executer = new GradleDistributionExecuter()

    ExecutionResult result
    ExecutionFailure failure
    private MavenFileRepository mavenRepo
    private IvyFileRepository ivyRepo

    protected TestFile getBuildFile() {
        testDir.file('build.gradle')
    }

    protected TestFile getSettingsFile() {
        testDir.file('settings.gradle')
    }

    protected TestFile getTestDir() {
        distribution.getTestDir();
    }

    protected TestFile file(Object... path) {
        getTestDir().file(path);
    }

    protected GradleExecuter sample(Sample sample) {
        inDirectory(sample.dir)
    }

    protected GradleExecuter inDirectory(String path) {
        inDirectory(file(path))
    }

    protected GradleExecuter inDirectory(File directory) {
        executer.inDirectory(directory);
    }

    protected GradleExecuter projectDir(path) {
        executer.usingProjectDirectory(file(path))
    }

    protected GradleDistribution requireOwnUserHomeDir() {
        distribution.requireOwnUserHomeDir()
        distribution
    }

    /**
     * Synonym for succeeds()
     */
    protected ExecutionResult run(String... tasks) {
        succeeds(*tasks)
    }

    protected GradleExecuter args(String... args) {
        executer.withArguments(args)
    }

    protected GradleExecuter withDebugLogging() {
        executer.withArguments("-d")
    }

    protected ExecutionResult succeeds(String... tasks) {
        result = executer.withTasks(*tasks).run()
    }

    protected ExecutionFailure runAndFail(String... tasks) {
        fails(*tasks)
    }
    
    protected ExecutionFailure fails(String... tasks) {
        failure = executer.withTasks(*tasks).runWithFailure()
        result = failure
    }
    
    protected List<String> getExecutedTasks() {
        assertHasResult()
        result.executedTasks
    }
    
    protected Set<String> getSkippedTasks() {
        assertHasResult()
        result.skippedTasks
    }
    
    protected List<String> getNonSkippedTasks() {
        executedTasks - skippedTasks
    }
    
    protected void executedAndNotSkipped(String... tasks) {
        tasks.each {
            assert it in executedTasks
            assert !skippedTasks.contains(it)
        }
    }
    
    protected void failureHasCause(String cause) {
        failure.assertHasCause(cause)
    }
    
    private assertHasResult() {
        assert result != null : "result is null, you haven't run succeeds()"
    }

    String getOutput() {
        result.output
    }

    String getErrorOutput() {
        result.error
    }

    ArtifactBuilder artifactBuilder() {
        def executer = distribution.executer()
        executer.withGradleUserHomeDir(distribution.getUserHomeDir())
        return new GradleBackedArtifactBuilder(executer, getTestDir().file("artifacts"))
    }

    public MavenFileRepository maven(TestFile repo) {
        return new MavenFileRepository(repo)
    }

    public MavenFileRepository maven(Object repo) {
        return new MavenFileRepository(file(repo))
    }

    public MavenFileRepository getMavenRepo() {
        if (mavenRepo == null) {
            mavenRepo = new MavenFileRepository(file("maven-repo"))
        }
        return mavenRepo
    }

    public IvyFileRepository ivy(TestFile repo) {
        return new IvyFileRepository(repo)
    }

    public IvyFileRepository ivy(Object repo) {
        return new IvyFileRepository(file(repo))
    }

    public IvyFileRepository getIvyRepo() {
        if (ivyRepo == null) {
            ivyRepo = new IvyFileRepository(file("ivy-repo"))
        }
        return ivyRepo
    }

    def createZip(String name, Closure cl) {
        TestFile zipRoot = file("${name}.root")
        TestFile zip = file(name)
        zipRoot.create(cl)
        zipRoot.zipTo(zip)
    }

    def createDir(String name, Closure cl) {
        TestFile root = file(name)
        root.create(cl)
    }
}