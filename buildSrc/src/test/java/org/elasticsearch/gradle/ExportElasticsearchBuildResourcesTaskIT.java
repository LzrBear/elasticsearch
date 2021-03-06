package org.elasticsearch.gradle;

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.elasticsearch.gradle.test.GradleIntegrationTestCase;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;


public class ExportElasticsearchBuildResourcesTaskIT extends GradleIntegrationTestCase {

    public static final String PROJECT_NAME = "elasticsearch-build-resources";

    public void testUpToDateWithSourcesConfigured() {
        GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("clean", "-s")
            .withPluginClasspath()
            .build();

        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("buildResources", "-s", "-i")
            .withPluginClasspath()
            .build();
        assertTaskSuccessfull(result, ":buildResources");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle.xml");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle_suppressions.xml");

        result = GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("buildResources", "-s", "-i")
            .withPluginClasspath()
            .build();
        assertTaskUpToDate(result, ":buildResources");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle.xml");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle_suppressions.xml");
    }

    public void testImplicitTaskDependencyCopy() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("clean", "sampleCopyAll", "-s", "-i")
            .withPluginClasspath()
            .build();

        assertTaskSuccessfull(result, ":buildResources");
        assertTaskSuccessfull(result, ":sampleCopyAll");
        assertBuildFileExists(result, PROJECT_NAME, "sampleCopyAll/checkstyle.xml");
        // This is a side effect of compile time reference
        assertBuildFileExists(result, PROJECT_NAME, "sampleCopyAll/checkstyle_suppressions.xml");
    }

    public void testImplicitTaskDependencyInputFileOfOther() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("clean", "sample", "-s", "-i")
            .withPluginClasspath()
            .build();

        assertTaskSuccessfull(result, ":sample");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle.xml");
        assertBuildFileExists(result, PROJECT_NAME, "build-tools-exported/checkstyle_suppressions.xml");
    }

    public void testIncorrectUsage() {
        BuildResult result = GradleRunner.create()
            .withProjectDir(getProjectDir(PROJECT_NAME))
            .withArguments("noConfigAfterExecution", "-s", "-i")
            .withPluginClasspath()
            .buildAndFail();
        assertOutputContains("buildResources can't be configured after the task ran");
    }
}
