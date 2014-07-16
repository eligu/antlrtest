package gr.uom.se.antlrtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Hello world!
 * 
 */
public class App {
   public static final String RESOURCES = "src/test/resources/";

   public static void main(String[] args) throws IOException {
      File resDir = new File(RESOURCES);
      for (File file : resDir.listFiles()) {
         if (file.canRead()) {
            test(file.getPath());
         }
      }

   }

   static void test(String path) throws IOException {
      System.out.println("Testing for path: " + path);
      long start = System.currentTimeMillis();

      CPPCommentsLexer lexer = new CPPCommentsLexer(new ANTLRFileStream(path));
      CommonTokenStream tokens = new CommonTokenStream(lexer);

      tokens.fill();
      System.out.println(tokens.size());
      System.out.println(tokens.index());

      for (Token token : tokens.getTokens()) {
         String msg = null;
         if (token.getType() == CPPCommentsParser.LINE_COMMENT) {
            msg = "Single line";
            // System.out.println("" + token.getLine());
         } else if (token.getType() == CPPCommentsParser.COMMENT) {
            msg = "Multi line";
            // System.out.println("multiline comment found at line " +
            // token.getLine());
         }
         if (msg != null) {
            msg += " comment found\n";
            msg += "Line: " + token.getLine() + "\n";
            msg += "Char position: " + token.getCharPositionInLine() + "\n";
            msg += "Start: " + token.getStartIndex() + "\n";
            msg += "End: " + token.getStopIndex() + "\n";

            if (path.endsWith("test.c")
                  && token.getType() == CPPCommentsParser.LINE_COMMENT) {
               CharStream stream = token.getInputStream();

            }

            if (token.getType() == CPPCommentsParser.LINE_COMMENT) {
               if (token.getCharPositionInLine() > 0) {
                  CharStream stream = token.getInputStream();
                  Interval interval = new Interval(0,
                        token.getCharPositionInLine() - 1);
                  String code = stream.getText(interval);
                  code = code.trim();
                  if (!code.isEmpty()) {
                     System.out.println("Line code: " + code);
                  }

               }
            } else if (token.getType() == CPPCommentsParser.LINE_COMMENT) {
               if (token.getCharPositionInLine() > 0) {
                  // Find the code before comment
                  CharStream stream = token.getInputStream();
                  Interval interval = new Interval(0,
                        token.getCharPositionInLine() - 1);
               }
            }

            // token.getInputStream().
            System.out.println(msg);
         }
      }
      System.out.println((System.currentTimeMillis() - start));
      String str = "a test \r\n test";
      System.out.println(str.replaceAll("\r\n", "\n"));
   }

   static String getCodeBeforeComment(Token token) {
      if (token.getCharPositionInLine() == 0) {
         return null;
      }
      CharStream stream = token.getInputStream();
      Interval interval = new Interval(0, token.getCharPositionInLine() - 1);
      String code = stream.getText(interval);
      code = code.trim();
      if (!code.isEmpty()) {
         return code;
      }
      return null;
   }

   static String getCodeAfterComment(Token token) {
      // token.getInputStream().
      return null;
   }

   static class LineMapper {
      /**
       * Contents of file based on lines. Empty lines have null char array.
       */
      protected char[][] contents;

      public LineMapper(InputStream input) throws IOException {

         BufferedReader reader = new BufferedReader(
               new InputStreamReader(input));
         init(reader);
      }

      private void init(BufferedReader reader) throws IOException {
         List<String> contentsList = new ArrayList<String>();
         String line = null;

         while ((line = reader.readLine()) != null) {
            contentsList.add(line);
         }

         contents = new char[contentsList.size()][];

         for (int i = 0; i < contentsList.size(); i++) {
            line = contentsList.get(i);
            if (!line.trim().isEmpty()) {
               this.contents[i] = line.toCharArray();
            }
         }
      }

      private void init(Reader reader) throws IOException {
         BufferedReader br = null;
         if (reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
         } else {
            br = new BufferedReader(reader);
         }
         this.init(br);
      }

      /**
       * Called during initialization when a non empty line is added to
       * {@link #contents} array.
       * <p>
       * 
       * When this method is called the {@link #contents} array is already
       * created. The default implementation does nothing.
       * 
       * @param line
       *           the contents of the line
       * @param i
       *           the number of line (0-based)
       */
      protected void monEmptyLine(char[] line, int i) {
      }

      public LineMapper(String contents) throws IOException {
         this.init(new StringReader(contents));
      }

      /**
       * Get the contents of the line i.
       * <p>
       * 
       * @param i
       *           the number of line (0-based)
       * @return the contents of the given line or null if the line is an empty
       *         one or contains only whitespace characters.
       * @throws ArrayIndexOutOfBoundsException
       *            if i is not within the range of the lines (0..n-1 where n is
       *            the lines length).r
       */
      public String getLine(int i) {
         return new String(contents[i]);
      }

      /**
       * Get the number of lines.
       * <p>
       * 
       * @return the number of lines, included those with empty contents
       */
      public int getLinesCount() {
         return contents.length;
      }

      /**
       * Check if the given line doesn't contain any printable character.
       * <p>
       * 
       * @param i
       *           the number of line (0-based)
       * @return true if the given line doesn't contain any printable character
       */
      public boolean isEmpty(int i) {
         return contents[i] == null;
      }
   }

   static class CommentMapper extends LineMapper {

      private static int COMMENT = 0;
      private static int CODE = 1;
      private static int MIX = 2;

      private int[] lineMap;

      public CommentMapper(InputStream input) throws IOException {
         super(input);
         init(getCharStream(input));
      }

      public CommentMapper(String content) throws IOException {
         super(content);
         init(getCharStream(content));
      }

      private CharStream getCharStream(String content) {
         return new ANTLRInputStream(content);
      }

      private CharStream getCharStream(InputStream content) throws IOException {
         return new ANTLRInputStream(content);
      }

      private void init(CharStream stream) {
         CPPCommentsLexer lexer = new CPPCommentsLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         tokens.fill();
         lineMap = new int[contents.length];
         for (Token token : tokens.getTokens()) {
            processToken(token);
         }
      }

      private void processToken(Token token) {
         if (token.getType() == CPPCommentsLexer.LINE_COMMENT) {
            processSingleLineToken(token);
         } else if (token.getType() == CPPCommentsLexer.COMMENT) {
            processMultiLineToken(token);
         }
      }

      /**
       * Process a token that corresponds to a single line comment.
       * <p>
       * When we have a single line that means a code can only be before
       * comment. By default everything after the comment token is comment, so
       * we must check to see if there are characters before the comment token.
       * If there are characters before token they probably are code characters
       * however there are cases when those characters are part of a multiline
       * comment so we must check the previous token (if any).
       * 
       * @param token
       *           to be processed
       */
      private void processSingleLineToken(Token token) {

      }

      private void processMultiLineToken(Token token) {

      }
   }
   
   //static class TokensList extends LinkedList
}
