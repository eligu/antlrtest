package gr.uom.se.antlrtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
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
      String str = null;

      File resDir = new File(RESOURCES);
      for (File file : resDir.listFiles()) {
         if (file.canRead()) {
            // test(file.getPath());
            System.out.println("Loading file: " + file);
            CommentMapper mapper = new CommentMapper(file);
            int linesOfCode = linesOfCode(mapper);
            int emptyLines = emptyLines(mapper);
            int linesOfComments = linesOfComments(mapper);
            int mixedLines = mixLines(mapper);
            System.out.println("Total Lines: " + mapper.getLinesCount());
            System.out.println("Lines of Code: " + linesOfCode);
            System.out.println("Empty Lines: " + emptyLines);
            System.out.println("Lines of Comments: " + linesOfComments);
            System.out.println("Mix Lines: " + mixedLines);
            /*System.out.println(mapper.getLinesCount());
            if (file.getName().endsWith("test.c")) {
               for (LineRegion region : mapper.getCodeRegions(82)) {
                  System.out.println(region);
               }
            }*/
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

   static int linesOfCode(CommentMapper mapper) {
      int size = mapper.getLinesCount();
      int counter = 0;
      for (int i = 0; i < size; i++) {
         if(containsCode(mapper, i)) {
            counter++;
         }
      }
      return counter;
   }
   
   static boolean containsCode(CommentMapper mapper, int line) {
      for (LineRegion codeRegion : mapper.getCodeRegions(line)) {
         if(!LineMapper.isWS(mapper.getContents(codeRegion))) {
            return true;
         }
      }
      return false;
   }
   static int emptyLines(CommentMapper mapper) {
      int size = mapper.getLinesCount();
      int counter = 0;
      for (int i = 0; i < size; i++) {
         if(mapper.isWS(i)) {
            counter++;
         }
      }
      return counter;
   }
   
   static int linesOfComments(CommentMapper mapper) {
      int size = mapper.getLinesCount();
      int counter = 0;
      for (int i = 0; i < size; i++) {
         if(mapper.containsComments(i)) {
            counter++;
         }
      }
      return counter;
   }
   
   static int mixLines(CommentMapper mapper) {
      int size = mapper.getLinesCount();
      int counter = 0;
      for (int i = 0; i < size; i++) {
         if(mapper.containsComments(i) && containsCode(mapper, i)) {
            counter++;
         }
      }
      return counter;
   }
}
