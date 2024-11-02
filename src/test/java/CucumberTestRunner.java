import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources", // Path to your feature files
        glue = "steps",
        plugin = {"pretty", "html:target/cucumber-reports.html"}, // Optional: generates HTML reports
        monochrome = true
)
public class CucumberTestRunner {
}