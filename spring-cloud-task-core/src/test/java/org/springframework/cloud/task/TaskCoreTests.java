/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.cloud.task.configuration.SimpleTaskAutoConfiguration;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies core behavior for Tasks.
 *
 * @author Glenn Renfro
 */
@ExtendWith(OutputCaptureExtension.class)
public class TaskCoreTests {

	private static final String TASK_NAME = "taskEventTest";

	private static final String EXCEPTION_MESSAGE = "FOO EXCEPTION";

	private static final String CREATE_TASK_MESSAGE = "Creating: TaskExecution{executionId=";

	private static final String UPDATE_TASK_MESSAGE = "Updating: TaskExecution with executionId=";

	private static final String SUCCESS_EXIT_CODE_MESSAGE = "with the following {exitCode=0";

	private static final String EXCEPTION_EXIT_CODE_MESSAGE = "with the following {exitCode=1";

	private static final String EXCEPTION_INVALID_TASK_EXECUTION_ID = "java.lang.IllegalArgumentException: "
			+ "Invalid TaskExecution, ID 55 not found";

	private static final String ERROR_MESSAGE = "errorMessage='java.lang.IllegalStateException: "
			+ "Failed to execute CommandLineRunner";

	private ConfigurableApplicationContext applicationContext;

	@AfterEach
	public void teardown() {
		if (this.applicationContext != null && this.applicationContext.isActive()) {
			this.applicationContext.close();
		}
	}

	@Test
	public void successfulTaskTest(CapturedOutput capturedOutput) {
		this.applicationContext = SpringApplication.run(TaskConfiguration.class,
				"--spring.cloud.task.closecontext.enable=false", "--spring.cloud.task.name=" + TASK_NAME,
				"--spring.main.web-environment=false");

		String output = capturedOutput.toString();
		assertThat(output.contains(CREATE_TASK_MESSAGE)).as("Test results do not show create task message: " + output)
				.isTrue();
		assertThat(output.contains(UPDATE_TASK_MESSAGE)).as("Test results do not show success message: " + output)
				.isTrue();
		assertThat(output.contains(SUCCESS_EXIT_CODE_MESSAGE)).as("Test results have incorrect exit code: " + output)
				.isTrue();
	}

	/**
	 * Test to verify that deprecated annotation does not affect task execution.
	 */
	@Test
	public void successfulTaskTestWithAnnotation(CapturedOutput capturedOutput) {
		this.applicationContext = SpringApplication.run(TaskConfigurationWithAnotation.class,
				"--spring.cloud.task.closecontext.enable=false", "--spring.cloud.task.name=" + TASK_NAME,
				"--spring.main.web-environment=false");

		String output = capturedOutput.toString();
		assertThat(output.contains(CREATE_TASK_MESSAGE)).as("Test results do not show create task message: " + output)
				.isTrue();
		assertThat(output.contains(UPDATE_TASK_MESSAGE)).as("Test results do not show success message: " + output)
				.isTrue();
		assertThat(output.contains(SUCCESS_EXIT_CODE_MESSAGE)).as("Test results have incorrect exit code: " + output)
				.isTrue();
	}

	@Test
	public void exceptionTaskTest(CapturedOutput capturedOutput) {
		boolean exceptionFired = false;
		try {
			this.applicationContext = SpringApplication.run(TaskExceptionConfiguration.class,
					"--spring.cloud.task.closecontext.enable=false", "--spring.cloud.task.name=" + TASK_NAME,
					"--spring.main.web-environment=false");
		}
		catch (IllegalStateException exception) {
			exceptionFired = true;
		}
		assertThat(exceptionFired).as("An IllegalStateException should have been thrown").isTrue();

		String output = capturedOutput.toString();
		assertThat(output.contains(CREATE_TASK_MESSAGE)).as("Test results do not show create task message: " + output)
				.isTrue();
		assertThat(output.contains(UPDATE_TASK_MESSAGE)).as("Test results do not show success message: " + output)
				.isTrue();
		assertThat(output.contains(EXCEPTION_EXIT_CODE_MESSAGE)).as("Test results have incorrect exit code: " + output)
				.isTrue();
		assertThat(output.contains(ERROR_MESSAGE)).as("Test results have incorrect exit message: " + output).isTrue();
		assertThat(output.contains(EXCEPTION_MESSAGE)).as("Test results have exception message: " + output).isTrue();
	}

	@Test
	public void invalidExecutionId(CapturedOutput capturedOutput) {
		boolean exceptionFired = false;
		try {
			this.applicationContext = SpringApplication.run(TaskExceptionConfiguration.class,
					"--spring.cloud.task.closecontext.enable=false", "--spring.cloud.task.name=" + TASK_NAME,
					"--spring.main.web-environment=false", "--spring.cloud.task.executionid=55");
		}
		catch (ApplicationContextException exception) {
			exceptionFired = true;
		}
		assertThat(exceptionFired).as("An ApplicationContextException should have been thrown").isTrue();

		String output = capturedOutput.toString();
		assertThat(output.contains(EXCEPTION_INVALID_TASK_EXECUTION_ID))
				.as("Test results do not show the correct exception message: " + output).isTrue();
	}

	@EnableTask
	@ImportAutoConfiguration({ SimpleTaskAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class })
	public static class TaskConfiguration {

		@Bean
		public CommandLineRunner commandLineRunner() {
			return strings -> {
			};
		}

	}

	@EnableTask
	@ImportAutoConfiguration({ SimpleTaskAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class })
	public static class TaskConfigurationWithAnotation {

		@Bean
		public CommandLineRunner commandLineRunner() {
			return strings -> {
			};
		}

	}

	@EnableTask
	@ImportAutoConfiguration({ SimpleTaskAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class })
	public static class TaskExceptionConfiguration {

		@Bean
		public CommandLineRunner commandLineRunner() {
			return strings -> {
				throw new IllegalStateException(EXCEPTION_MESSAGE);
			};
		}

	}

}
