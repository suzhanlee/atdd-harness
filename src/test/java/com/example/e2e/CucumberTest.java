package com.example.e2e;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber E2E 테스트 Runner.
 *
 * <p>Apple IAP 구독 기능에 대한 모든 E2E 테스트를 실행한다.
 * 이 클래스는 cucumber 태스크에서만 실행되어야 한다.
 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty")
@ConfigurationParameter(key = "cucumber.plugin", value = "html:build/reports/cucumber/cucumber-report.html")
@ConfigurationParameter(key = "cucumber.plugin", value = "json:build/reports/cucumber/cucumber.json")
@ConfigurationParameter(key = "cucumber.features", value = "src/test/resources/features")
@ConfigurationParameter(key = "cucumber.glue", value = "com.example.e2e")
@ConfigurationParameter(key = "cucumber.filter.tags", value = "not @wip")
public class CucumberTest {
}
