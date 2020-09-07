package ru.ifmo.rain.shaldin.i18n;

import java.io.*;
import java.text.*;
import java.util.*;

import static ru.ifmo.rain.shaldin.i18n.TextUtils.printError;

public class TextStatistics {
    private final Locale textLocale, outputLocale;
    private final String inputFile, outputFile;
    private String text;

    protected String getText() {
        return text;
    }
    protected Locale getInputLocale() {
        return textLocale;
    }
    protected Locale getOutputLocale() {
        return outputLocale;
    }
    protected String getInputFile() {
        return inputFile;
    }
    protected String getOutputFile() {
        return outputFile;
    }

    public TextStatistics(Locale inputLocale, Locale outputLocale, String inputFile, String outputFile) {
        this.textLocale = inputLocale;
        this.outputLocale = outputLocale;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    protected String localize(ResourceBundle bundle, String key) {
        return bundle.getString(key);
    }

    protected String htmlLine(ResourceBundle bundle, String key, String object, Object value, Object add) {
        return "        <p>" +
                localize(bundle, object) + " " +
                localize(bundle, key) + ": " +
                (value == null ? " " : value) +
                (add == null ? "" : " (" + add + ")") + "</p>" + System.lineSeparator();
    }

    protected class Statistics<E> {
        public int count = 0, different = 0;
        public double avgValue = 0;
        public String avg = "", min = "", max = "", minLengthString = "", maxLengthString = "";

        public Statistics() {}

        public Statistics(Set<E> value, List<E> array, String mode) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, outputLocale);
            NumberFormat numberFormat = NumberFormat.getInstance(outputLocale);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(outputLocale);
            this.count = array.size();
            different = value.size();
            for (E elem : value) {
                switch (mode) {
                    case "Date":
                        max = dateFormat.format(elem);
                        break;
                    case "Currency":
                        max = currencyFormat.format(elem);
                        break;
                    case "Number":
                        max = numberFormat.format(elem);
                        break;
                }
                if (min.equals("")) {
                    min = max;
                }
            }
            for (E elem : array) {
                if (!mode.equals("Date")) {
                    avgValue += ((Number)elem).doubleValue();
                }
            }
            if (avgValue != 0) {
                avgValue /= count;
                switch (mode) {
                    case "Currency":
                        avg = currencyFormat.format(avgValue);
                        break;
                    case "Number":
                        avg = numberFormat.format(avgValue);
                        break;
                }
            }
        }

        public Statistics(Set<String> value, Map<Integer, String> length, List<String> array) {
            this.count = array.size();
            different = value.size();
            for (String elem : value) {
                if (min.equals("")) {
                    min = elem;
                }
                max = elem;
            }
            for (Map.Entry<Integer, String> entry : length.entrySet()) {
                if (entry.getKey() < minLengthString.length() || minLengthString.equals("")) {
                    minLengthString = entry.getValue();
                }
                if (entry.getKey() > maxLengthString.length() || maxLengthString.equals("")) {
                    maxLengthString = entry.getValue();
                }
            }
            for (String elem : array) {
                avgValue += elem.length();
            }
            if (avgValue != 0) {
                avgValue /= count;
                Locale.setDefault(outputLocale);
                avg = MessageFormat.format("{0,number}", avgValue);
            }
        }
    }

    public int countLength(String str) {
        int size = 0;
        BreakIterator iterator = BreakIterator.getCharacterInstance(textLocale);
        iterator.setText(str);
        int start = iterator.first();
        int end = iterator.next();
        while (end != BreakIterator.DONE) {
            size++;
            start = end;
            end = iterator.next();
        }
        return size;
    }

    protected Statistics calculate(BreakIterator iterator, String mode) {
        Collator collator = Collator.getInstance(textLocale);
        List<Number> numbers = new ArrayList<>();
        List<Date> dates = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        Set<Number> numberValue = new TreeSet<>(Comparator.comparing(Number::doubleValue));
        Set<Date> dateValue = new TreeSet<>(Comparator.comparing(Date::getTime));
        Set<String> charValue = new TreeSet<>(collator);
        Map<Integer, String> length = new HashMap<>();
        iterator.setText(text);
        int start = iterator.first();
        int end = iterator.next();

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, textLocale);
        NumberFormat numberFormat = NumberFormat.getInstance(textLocale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(textLocale);

        while (end != BreakIterator.DONE) {
            String word = text.substring(start, end);
            try {
                switch (mode) {
                    case "Date":
                        Date date = dateFormat.parse(word);
                        dateValue.add(date);
                        dates.add(date);
                        break;
                    case "Chars":
                        Date date1 = dateFormat.parse(word);
                        Number number1 = numberFormat.parse(word);
                        Number currency1 = currencyFormat.parse(word);
                        break;
                    case "Number":
                        Number number = numberFormat.parse(word);
                        numberValue.add(number);
                        numbers.add(number);
                        break;
                    case "Currency":
                        Number currency = currencyFormat.parse(word);
                        numberValue.add(currency);
                        numbers.add(currency);
                        break;
                }
            } catch (ParseException ignore) {
                if ("Chars".equals(mode)) {
                    if (word.endsWith(" ") || word.endsWith("\n")) {
                        word = word.substring(0, word.length() - 1);
                    }
                    if (word.length() == 0 || word.length() == 1 && !Character.isLetterOrDigit(word.charAt(0))) {
                        start = end;
                        end = iterator.next();
                        continue;
                    }
                    charValue.add(word);
                    int size = countLength(word);
                    if (length.containsKey(size)) {
                        if (collator.compare(word, length.get(size)) < 0) {
                            length.put(size, word);
                        }
                    } else {
                        length.put(size, word);
                    }
                    strings.add(word);
                }
            }
            start = end;
            end = iterator.next();
        }
        switch (mode) {
            case "Number":
            case "Currency":
                return new Statistics<Number>(numberValue, numbers, mode);
            case "Date":
                return new Statistics<Date>(dateValue, dates, mode);
            case "Chars":
                return new Statistics<String>(charValue, length, strings);
            default:
                return new Statistics();
        }
    }

    protected String pattern(ResourceBundle bundle, String key) {
        String keyAdd = "";
        if (key.equals("sentence") || key.equals("word") || key.equals("line")) {
            keyAdd = "2";
        }
        Statistics<?> statistics = null;
        switch (key) {
            case "summary":
                String result = "";
                String[] array = {"sentence2:Chars:1", "word2:Chars:2", "line2:Chars:3", "number2:Number:2", "currency2:Currency:3", "date2:Date:3"};
                for (String elem : array) {
                    String[] args = elem.split(":");
                    BreakIterator breakIterator = null;
                    switch (args[2]) {
                        case "1":
                            breakIterator = BreakIterator.getSentenceInstance(textLocale);
                            break;
                        case "2":
                            breakIterator = BreakIterator.getWordInstance(textLocale);
                            break;
                        case "3":
                            breakIterator = BreakIterator.getLineInstance(textLocale);
                            break;
                    }
                    statistics = calculate(breakIterator, args[1]);
                    result += htmlLine(bundle, args[0], "number_of", statistics.count, null);
                }
                return result;
            case "sentence":
                statistics = calculate(BreakIterator.getSentenceInstance(textLocale), "Chars");
                break;
            case "word":
                statistics = calculate(BreakIterator.getWordInstance(textLocale), "Chars");
                break;
            case "line":
                statistics = calculate(BreakIterator.getLineInstance(textLocale), "Chars");
                break;
            case "number":
                statistics = calculate(BreakIterator.getWordInstance(textLocale), "Number");
                break;
            case "currency":
                statistics = calculate(BreakIterator.getLineInstance(textLocale), "Currency");
                break;
            case "date":
                statistics = calculate(BreakIterator.getLineInstance(textLocale), "Date");
                break;
        }
        return htmlLine(bundle, key + "2", "number_of", statistics.count, null) +
                htmlLine(bundle, key + "2", "different_values", statistics.different, null) +
                htmlLine(bundle, key, "min_value", statistics.min, null) +
                htmlLine(bundle, key, "max_value", statistics.max, null) +
                (keyAdd.equals("2") ?
                        htmlLine(bundle, key, "min_length", statistics.minLengthString.length(), statistics.minLengthString) +
                                htmlLine(bundle, key, "max_length", statistics.maxLengthString.length(), statistics.maxLengthString) : "") +
                (key.equals("date") ? "" : htmlLine(bundle, key + "2", "avg_value" + keyAdd, statistics.avg, null));
    }

    protected String patternLine(ResourceBundle bundle, String key) {
        return "        <h2>" + localize(bundle, "print_" + key) + "</h2>" + System.lineSeparator() +
                pattern(bundle, key) + System.lineSeparator();
    }

    protected void extractText(String fileName) {
        text = "";
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = in.readLine()) != null) {
                text += line + "\n";
            }
        } catch (IOException e) {
            printError("Error occurred while reading input file. " + e.getMessage());
        }
    }

    protected void makeStatistics() {
        extractText(inputFile);
        ResourceBundle bundle = ResourceBundle.getBundle("HtmlBundle", outputLocale);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write("<!DOCTYPE HTML>" + System.lineSeparator() +
                    "<html lang=\"" + (outputLocale.equals(Locale.ENGLISH) ? "en" : "ru") + "\">" + System.lineSeparator() +
                    "   <head>" + System.lineSeparator() +
                    "       <meta charset=\"utf-8\"/>" + System.lineSeparator() +
                    "   </head>" + System.lineSeparator() +
                    "   <body>" + System.lineSeparator() +
                    "       <h1>" + localize(bundle, "analysis") + ": " + inputFile + "</h1>" + System.lineSeparator());
            out.write(
                    patternLine(bundle, "summary") +
                            patternLine(bundle, "sentence") +
                            patternLine(bundle, "line") +
                            patternLine(bundle, "word") +
                            patternLine(bundle, "number") +
                            patternLine(bundle, "currency") +
                            patternLine(bundle, "date"));
            out.write("   </body>" + System.lineSeparator() +
                    "</html>");
        } catch (IOException e) {
            printError("Error occurred while writing to file. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            printError("Arguments cannot be null");
            return;
        }
        if (args.length != 4) {
            printError("Wrong number of arguments. Use TextStatistics <input_locale> <output_locale> <input_file> <output_file>");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                printError("Arguments cannot be null");
                return;
            }
        }
        Locale[] locales = Locale.getAvailableLocales();
        String[] inputLocales = args[0].split("-");
        Locale inputLocale;
        if (inputLocales.length == 1) {
            inputLocale = new Locale(inputLocales[0]);
        } else if (inputLocales.length == 2) {
            inputLocale = new Locale(inputLocales[0], inputLocales[1]);
        } else {
            printError("Input locale does not support");
            return;
        }
        Locale outputLocale = new Locale(args[1]);
        Locale russian = new Locale("ru");
        Locale english = new Locale("en");
        String inputFile = args[2];
        String outputFile = args[3];
        boolean localeContains = false;
        for (Locale locale : locales) {
            if (locale.equals(inputLocale)) {
                localeContains = true;
                break;
            }
        }
        if (!localeContains) {
            printError("System locales do not contain input locale");
            return;
        }
        if (!english.equals(outputLocale) && !russian.equals(outputLocale)) {
            printError("Output locale is not english or russian. Use EN locale or RU locale");
            return;
        }

        TextStatistics textStatistics = new TextStatistics(inputLocale, outputLocale, inputFile, outputFile);
        textStatistics.makeStatistics();
    }
}
