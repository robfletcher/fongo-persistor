package co.freeside.gradle

import org.gradle.api.*
import org.gradle.reporting.*
import org.gradle.api.internal.tasks.testing.junit.report.*
import org.w3c.dom.*
import org.xml.sax.*
import javax.xml.parsers.*
import java.io.*
import java.math.*

class CasperTestReport implements TestReporter {

	private final HtmlReportRenderer htmlRenderer = new HtmlReportRenderer()
    private File resultDir
    private File reportDir

    CasperTestReport() {
        htmlRenderer.requireResource(getClass().getResource("/org/gradle/reporting/report.js"))
        htmlRenderer.requireResource(getClass().getResource("/org/gradle/reporting/base-style.css"))
        htmlRenderer.requireResource(getClass().getResource("/org/gradle/reporting/css3-pie-1.0beta3.htc"))
        htmlRenderer.requireResource(getClass().getResource("/org/gradle/api/internal/tasks/testing/junit/report/style.css"))
    }

    void setTestResultsDir(File resultDir) {
        this.resultDir = resultDir
    }

    void setTestReportDir(File reportDir) {
        this.reportDir = reportDir
    }

    void generateReport() {
        AllTestResults model = loadModel()
        generateFiles(model)
    }

    private AllTestResults loadModel() {
        AllTestResults model = new AllTestResults()
        if (resultDir.exists()) {
            for (File file : resultDir.listFiles()) {
                if (file.getName().startsWith("TEST-") && file.getName().endsWith(".xml")) {
                    mergeFromFile(file, model)
                }
            }
        }
        return model
    }

    private void mergeFromFile(File file, AllTestResults model) {
        try {
            InputStream inputStream = new FileInputStream(file)
            Document document
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(inputStream))
            } finally {
                inputStream.close()
            }
            NodeList testCases = document.getElementsByTagName("testcase")
            for (int i = 0; i < testCases.getLength(); i++) {
                Element testCase = (Element) testCases.item(i)
                String className = testCase.getAttribute("classname")
                String testName = testCase.getAttribute("name")
                LocaleSafeDecimalFormat format = new LocaleSafeDecimalFormat()
                BigDecimal duration = format.parse(testCase.getAttribute("time") ? testCase.getAttribute("time") : '0')
                duration = duration.multiply(BigDecimal.valueOf(1000))
                NodeList failures = testCase.getElementsByTagName("failure")
                TestResult testResult = model.addTest(className, testName, 0L)
                for (int j = 0; j < failures.getLength(); j++) {
                    Element failure = (Element) failures.item(j)
                    testResult.addFailure(failure.getAttribute("message"), failure.getTextContent())
                }
            }
            NodeList ignoredTestCases = document.getElementsByTagName("ignored-testcase")
            for (int i = 0; i < ignoredTestCases.getLength(); i++) {
                Element testCase = (Element) ignoredTestCases.item(i)
                String className = testCase.getAttribute("classname")
                String testName = testCase.getAttribute("name")
                model.addTest(className, testName, 0).ignored()
            }
            String suiteClassName = document.getDocumentElement().getAttribute("name")
            ClassTestResults suiteResults = model.addTestClass(suiteClassName)
            NodeList stdOutElements = document.getElementsByTagName("system-out")
            for (int i = 0; i < stdOutElements.getLength(); i++) {
                suiteResults.addStandardOutput(stdOutElements.item(i).getTextContent())
            }
            NodeList stdErrElements = document.getElementsByTagName("system-err")
            for (int i = 0; i < stdErrElements.getLength(); i++) {
                suiteResults.addStandardError(stdErrElements.item(i).getTextContent())
            }
        } catch (Exception e) {
            throw new GradleException(String.format("Could not load test results from '%s'.", file), e)
        }
    }

    private void generateFiles(AllTestResults model) {
        try {
            generatePage(model, new OverviewPageRenderer(), new File(reportDir, "index.html"))
            for (PackageTestResults packageResults : model.getPackages()) {
                generatePage(packageResults, new PackagePageRenderer(), new File(reportDir, packageResults.getName() + ".html"))
                for (ClassTestResults classResults : packageResults.getClasses()) {
                    generatePage(classResults, new ClassPageRenderer(), new File(reportDir, classResults.getName() + ".html"))
                }
            }
        } catch (Exception e) {
            throw new GradleException(String.format("Could not generate test report to '%s'.", reportDir), e)
        }
    }

    private <T extends CompositeTestResults> void generatePage(T model, PageRenderer<T> renderer, File outputFile) throws Exception {
        htmlRenderer.renderer(renderer).writeTo(model, outputFile)
    }
}
