package gr.uom.se.antlrtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Hello world!
 *
 */
public class App 
{
   public static final String RESOURCES = "src/test/resources/";
   
    public static void main( String[] args ) throws IOException
    {
      File resDir = new File(RESOURCES);
      for(File file : resDir.listFiles()) {
         if(file.canRead()) {
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
       
       for(Token token : tokens.getTokens()) {
          String msg = null;
          if(token.getType() == CPPCommentsParser.LINE_COMMENT) {
             msg = "Single line";
             //System.out.println("" + token.getLine());
          } else if (token.getType() == CPPCommentsParser.COMMENT) {
             msg = "Multi line";
             //System.out.println("multiline comment found at line " + token.getLine());
          }
          if(msg != null) {
             msg += " comment found\n";
             msg += "Line: " + token.getLine() + "\n";
             msg += "Char position: " + token.getCharPositionInLine() + "\n";
             msg += "Start: " + token.getStartIndex() + "\n";
             msg += "End: " + token.getStopIndex() + "\n";
             
             if(path.endsWith("test.c") && token.getType() == CPPCommentsParser.LINE_COMMENT) {
                CharStream stream = token.getInputStream();
                
             }
             
             if(token.getType() == CPPCommentsParser.LINE_COMMENT) {
                if(token.getCharPositionInLine() > 0) {
                   CharStream stream = token.getInputStream();
                   Interval interval = new Interval(0, token.getCharPositionInLine()-1);
                   String code = stream.getText(interval);
                   code = code.trim();
                   if(!code.isEmpty()) {
                      System.out.println("Line code: " + code);
                   }
                   
                }
             } else if (token.getType() == CPPCommentsParser.LINE_COMMENT) {
                if(token.getCharPositionInLine() > 0) {
                   // Find the code before comment
                   CharStream stream = token.getInputStream();
                   Interval interval = new Interval(0, token.getCharPositionInLine()-1);
                }
             }
             
             //token.getInputStream().
             System.out.println(msg);
          }
       }
       System.out.println((System.currentTimeMillis() - start));
       String str = "a test \r\n test";
       System.out.println(str.replaceAll("\r\n", "\n"));
    }
    
    static String getCodeBeforeComment(Token token) {
       if(token.getCharPositionInLine() == 0) {
          return null;
       }
       CharStream stream = token.getInputStream();
       Interval interval = new Interval(0, token.getCharPositionInLine()-1);
       String code = stream.getText(interval);
       code = code.trim();
       if(!code.isEmpty()) {
          return code;
       }
       return null;
    }
    
    static String getCodeAfterComment(Token token) {
       //token.getInputStream().
       return null;
    }
    
    static class LineMapper {
       // For non empty lines
       char[][] contents;
       // The mapping of lines with characters
       int[] lines;
       
       public LineMapper(InputStream input) throws IOException {
          
          BufferedReader reader = new BufferedReader(new InputStreamReader(input));
          int counter = 0;
          List<String> linesList = new ArrayList<String>();
          String line = null;
          StringBuilder sb = null;
          
          while((line = reader.readLine()) != null) {
             line = line.trim();
             if(!line.trim().isEmpty()) {
                linesList.add(line);
             }
          }
       }
       
       //static StringBuilder 
    }
}
