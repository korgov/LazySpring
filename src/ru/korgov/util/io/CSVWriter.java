package ru.korgov.util.io;

import ru.korgov.util.collection.CollectionUtils;
import ru.korgov.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 10.03.12
 */
public class CSVWriter {

    private static final Pattern QUOTE = Pattern.compile("\"");

    private Writer innerWriter;
    private String separator;

    public CSVWriter(final Writer innerWriter, final String separator) {
        this.innerWriter = innerWriter;
        this.separator = separator;
    }

    public CSVWriter(final Writer innerWriter) {
        this(innerWriter, ",");
    }

    public CSVWriter(final String filename) throws FileNotFoundException {
        this(filename, ",");
    }

    public CSVWriter(final String filename, final String separator) throws FileNotFoundException {
        this(IOUtils.getWriter(filename), separator);
    }
    
    public void write(final String[] row) throws IOException {
        write(CollectionUtils.list(row));
    }

    public void write(final Collection<String> row) throws IOException {
        innerWriter.write(StringUtils.join(wrapTexts(escapeQuotes(row)), separator) + '\n');
    }

    private  List<String> wrapTexts(final Iterable<String> texts){
        final List<String> out = new ArrayList<String>();
        for(final String text : texts){
            out.add(wrapText(text));
        }
        return out;
    }
    
    private List<String> escapeQuotes(final Collection<String> strs){
        final List<String> out = new ArrayList<String>(strs.size());
        for(final String str : strs){
            out.add(escapeQuotes(str));
        }
        return out;
    }


    private String escapeQuotes(final String str) {
        if(str == null){
            return "";
        }
        return QUOTE.matcher(str).replaceAll("\"\"");
    }

    private String wrapText(final String text){
        if(!StringUtils.isEmpty(text)){
            return '"' + text + '"';
        }
        return text;
    }
    
    public void close() {
        try {
            innerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
