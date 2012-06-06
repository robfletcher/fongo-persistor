package co.freeside.gradle

import org.gradle.api.*
import org.gradle.api.tasks.*

class CasperTask extends DefaultTask implements VerificationTask {

	File testSrcDir = new File('src/test/casper')
	File testResultsDir = project.testResultsDir
	File testReportDir = project.testReportDir
	boolean ignoreFailures = false

	@TaskAction
	def casperjs() {
		def resultFile = new File(testResultsDir, 'TEST-casper-suite.xml')
		def result = project.exec {
			ignoreExitValue = true
			workingDir = testSrcDir
			commandLine 'casperjs', 'test', '.', "--xunit=$resultFile", '--ignore-ssl-errors=yes'
		}

		cleanUpTestResult(resultFile)
		generateReport()

		if (!ignoreFailures) result.assertNormalExitValue()
	}

	private void cleanUpTestResult(File resultFile) {
		// hack timing info into report XML otherwise Gradle's DefaultTestReport class will throw NPE
		def testsuite = new XmlParser().parse(resultFile)
		for (testcase in testsuite.testcase) {
			testcase.@time = '0'
		}
		resultFile.withWriter { writer ->
			new XmlNodePrinter(new PrintWriter(writer)).print(testsuite)
		}
	}

	private void generateReport() {
		def reporter = new org.gradle.api.internal.tasks.testing.junit.report.DefaultTestReport()
		reporter.testResultsDir = testResultsDir
		reporter.testReportDir = testReportDir
		reporter.generateReport()
	}
}

