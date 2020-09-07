package ru.ifmo.rain.shaldin.i18n;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.ResourceBundle;


public class StatisticsTest {
    @Before
    public void printTestInfo() {
        System.out.println("--------------------");
        System.out.print("Test ");
    }

    @Test
    public void test01_testConstructor() {
        System.out.print("1: ");
        Locale input = new Locale("ru", "RU");
        Locale output = new Locale("ru");
        String inputFile = "inputTest.txt";
        String outputFile = "output.html";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        Assert.assertEquals(inputFile, statistics.getInputFile());
        Assert.assertEquals(outputFile, statistics.getOutputFile());
        Assert.assertEquals(input, statistics.getInputLocale());
        Assert.assertEquals(output, statistics.getOutputLocale());
        System.out.println("OK");
    }

    @Test
    public void test02_testExtract() {
        System.out.print("2: ");
        Locale input = new Locale("ru", "RU");
        Locale output = new Locale("ru");
        String inputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\inputTest.txt";
        String outputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\output.html";
        String text = "Привет. 10.10.2002\n";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        statistics.extractText(inputFile);
        Assert.assertEquals(text, statistics.getText());
        System.out.println("OK");
    }

    @Test
    public void test03_testLocalize() {
        System.out.print("3: ");
        Locale input = new Locale("ru", "RU");
        Locale output = new Locale("ru");
        String inputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\inputTest.txt";
        String outputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\output.html";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        ResourceBundle bundle = ResourceBundle.getBundle("HtmlBundle", output);
        String[] array = {"sentence", "line", "word", "number", "currency", "date"};
        for(String elem : array) {
            Assert.assertEquals(statistics.localize(bundle, elem), bundle.getString(elem));
            Assert.assertEquals(statistics.localize(bundle, elem + "2"), bundle.getString(elem + "2"));
            Assert.assertEquals(statistics.localize(bundle, "print_" + elem), bundle.getString("print_" + elem));
        }
        System.out.println("OK");
    }

    @Test
    public void test04_testHtmlLine() {
        System.out.print("4: ");
        Locale input = new Locale("ru", "RU");
        Locale output = new Locale("ru");
        String inputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\inputTest.txt";
        String outputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\output.html";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        ResourceBundle bundle = ResourceBundle.getBundle("HtmlBundle", output);
        String[] array = {"sentence", "line", "word", "number", "currency", "date"};
        for(String elem : array) {
            Assert.assertNotNull(statistics.htmlLine(bundle, elem, "number_of", 1, 1), bundle.getString(elem));
        }
        System.out.println("OK");
    }

    @Test
    public void test05_testMakeStatistics() {
        System.out.print("5: ");
        Locale input = new Locale("ru", "RU");
        Locale output = new Locale("ru");
        String inputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\inputTest.txt";
        String outputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\output1.html";
        String newOutputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\outputTest.html";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        ResourceBundle bundle = ResourceBundle.getBundle("HtmlBundle", output);
        statistics.makeStatistics();
        TextStatistics statistics1 = new TextStatistics(input, output, outputFile, outputFile);
        TextStatistics statistics2 = new TextStatistics(input, output, newOutputFile, outputFile);
        statistics1.extractText(outputFile);
        statistics2.extractText(newOutputFile);
        Assert.assertEquals(statistics1.getText(), statistics2.getText());
        System.out.println("OK");
    }

    @Test
    public void test06_testEng() {
        System.out.print("6: ");
        Locale input = new Locale("en", "US");
        Locale output = new Locale("ru");
        String inputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\inputTest2.txt";
        String outputFile = "C:\\Users\\shald\\IdeaProjects\\JavaAdvanced\\NewRepos\\java-advanced-2020-solutions\\java-solutions\\ru\\ifmo\\rain\\shaldin\\i18n\\output1.html";
        TextStatistics statistics = new TextStatistics(input, output, inputFile, outputFile);
        statistics.makeStatistics();
        ResourceBundle bundle = ResourceBundle.getBundle("HtmlBundle", output);
        TextStatistics.Statistics statistic = statistics.calculate(BreakIterator.getLineInstance(input), "Date");
        Assert.assertNotNull(statistic);
        Assert.assertEquals("05.06.2020", statistic.max);
        Assert.assertEquals("05.06.2020", statistic.min);
        System.out.println("OK");
    }
}
