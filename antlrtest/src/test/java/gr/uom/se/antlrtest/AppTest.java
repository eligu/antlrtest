package gr.uom.se.antlrtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.TreeSet;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest {
   public static final String RESOURCES = "src/test/resources/";

   @Test
   public void checkRebuild() throws IOException {
      File resDir = new File(RESOURCES);
      for (File file : resDir.listFiles()) {
         if (file.canRead()) {
            testRebuildForFile(file);
         }
      }
   }

   private void testRebuildForFile(File file) throws IOException {

      FileInputStream mfis = new FileInputStream(file);
      CommentMapper mapper = new CommentMapper(file);
      mfis.close();
      System.out.println("Loading file: " + file);
      //System.out.println(mapper.getCommentRegions(1));
      
      FileInputStream bfis = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(bfis));

      String expectedLine = null;
      int lineIndex = 0;
      while ((expectedLine = reader.readLine()) != null) {

         testLineContent(lineIndex, expectedLine, mapper, file);
         testLineRebuild(lineIndex, expectedLine, mapper, file);
         lineIndex++;
      }
      // Check lines count
      assertEquals("file lines for: " + file.getName(), lineIndex, mapper.getLinesCount());
      
      reader.close();
   }

   private void testLineContent(int lineIndex, String expectedLine,
         CommentMapper mapper, File file) {
      // Test the line mapper
      String line = mapper.getText(lineIndex);

      if (line == null) {
         line = "";
         expectedLine = expectedLine.trim();
      } else if (line.isEmpty()) {
         expectedLine = expectedLine.trim();
      }
      assertEquals("line " + (lineIndex + 1) + " at file: " + file.getName(),
            expectedLine, line);
   }

   private void testLineRebuild(int lineIndex, String expectedLine,
         CommentMapper mapper, File file) {
      
      String line = buildLineFromRegions(mapper, lineIndex);
      if (line == null) {
         line = "";
         expectedLine = expectedLine.trim();
      } else if (line.isEmpty()) {
         expectedLine = expectedLine.trim();
      }
      line = buildLineFromRegions(mapper, lineIndex);
      assertEquals("line " + lineIndex + " at file: " + file.getName(),
            expectedLine, line);
      lineIndex++;
   }

   static String buildLineFromRegions(CommentMapper mapper, int line) {
      Collection<LineRegion> codeRegions = mapper.getCodeRegions(line);
      Collection<LineRegion> commentRegions = mapper.getCommentRegions(line);
      TreeSet<LineRegion> regions = new TreeSet<LineRegion>();
      regions.addAll(commentRegions);
      regions.addAll(codeRegions);
      StringBuilder sb = new StringBuilder();
      for (LineRegion region : regions) {
         sb.append(mapper.getContents(region));
      }
      return sb.toString();
   }
}
