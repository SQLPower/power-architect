package ca.sqlpower.architect;

import java.io.PrintWriter;

public class IOUtils {

    public int indent;

    public IOUtils() {
        super();
        // TODO Auto-generated constructor stub
    }

    // ------------------- utility methods -------------------
    /**
     * Prints to the output writer {@link #out} indentation spaces
     * (according to {@link #indent}) followed by the given text.
     * @param out
     */
    public void print(PrintWriter out, String text) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.print(text);
    }

    /**
     * Prints <code>text</code> to the output writer {@link #out} (no
     * indentation).
     */
    public void niprint(PrintWriter out, String text) {
        out.print(text);
    }

    /**
     * Prints <code>text</code> followed by newline to the output
     * writer {@link #out} (no indentation).
     */
    public void niprintln(PrintWriter out, String text) {
        out.println(text);
    }

    /**
     * Prints to the output writer {@link #out} indentation spaces
     * (according to {@link #indent}) followed by the given text
     * followed by a newline.
     */
    public void println(PrintWriter out, String text) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.println(text);
    }
}
