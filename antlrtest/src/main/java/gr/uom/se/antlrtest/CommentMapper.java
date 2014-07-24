/**
 * 
 */
package gr.uom.se.antlrtest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommentMapper extends LineMapper {

   protected TreeMap<Integer, TreeSet<LineRegion>> regionMap;

   public CommentMapper(byte[] data) throws IOException {
      // WARNING: this will not allow files larger than
      // the default maximum length as specified in IOReader
      //InputStream in = IOLoader.loadInMemory(input);
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      super.init(new BufferedReader(new InputStreamReader(bais)));
      bais.close();
      bais = new ByteArrayInputStream(data);
      init(getCharStream(bais));
   }

   public CommentMapper(String content) throws IOException {
      init(getCharStream(content));
      BufferedReader reader = new BufferedReader(new StringReader(content));
      super.init(reader);
      reader.close();
   }

   public CommentMapper(File file) throws IOException {
      super(file);
      FileInputStream input = null;
      try {
         input = new FileInputStream(file);
         init(getCharStream(input));
      } finally {
         if (input != null) {
            input.close();
         }
      }
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
      
      // Init line map
      regionMap = new TreeMap<Integer, TreeSet<LineRegion>>();
     
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
    * When we have a single line that means a code can only be before comment.
    * By default everything after the comment token is comment, so we must check
    * to see if there are characters before the comment token. If there are
    * characters before token they probably are code characters however there
    * are cases when those characters are part of a multiline comment so we must
    * check the previous token (if any).
    * 
    * @param token
    *           to be processed
    */
   private void processSingleLineToken(Token token) {
      // Get the line of this token and check it against our
      // mapping
      int lineIndex = token.getLine();
      // We have a 0-based line mapping so decrease the index
      lineIndex--;
      checkLine(lineIndex);

      // The index of the first char of this token within the
      // line
      int linePosition = token.getCharPositionInLine();
      int length = token.getText().length();
      // Create a region for this comment token
      LineRegion region = new LineRegion(lineIndex, linePosition, linePosition + length - 1,
            LineRegion.Type.START_OF_SINGLELINE_COMMENT);
      addRegion(lineIndex, region);
   }
   
   private void addRegion(int lineIndex, LineRegion region) {
      TreeSet<LineRegion> set = regionMap.get(lineIndex);
      if(set == null) {
         set = new TreeSet<LineRegion>();
         regionMap.put(lineIndex, set);
      }
      set.add(region);
   }

   private void checkLine(int lineIndex) {
      if (lineIndex < 0 || lineIndex > (getLinesCount() - 1)) {
         throw new IllegalStateException("token line " + (lineIndex)
               + " is not within contents");
      }
   }

   private void processMultiLineToken(Token token) {
      // Get the line of this token and check it against our
      // mapping
      int lineIndex = token.getLine();
      // We have a 0-based line mapping so decrease the index
      lineIndex--;
      checkLine(lineIndex);

      // The index of the first char of this token within the
      // line
      int linePosition = token.getCharPositionInLine();

      // Here we need to match all lines of this multiline comment
      String[] lines = token.getText().split("\r?\n|\r");

      // Now start adding regions of this comment
      for (int i = 0; i < lines.length; i++) {

         String line = lines[i];
         int lineLength = line.length();
         LineRegion.Type type = null;
         if (i == 0) {
            type = LineRegion.Type.START_OF_MULTILINE_COMMENT;
         } else if (i == lines.length - 1) {
            type = LineRegion.Type.END_OF_MULTILINE_COMMENT;
         } else {
            type = LineRegion.Type.PART_OF_MULTILINE_COMMENT;
         }
         if (!isWS(lineIndex)) {
            LineRegion region = new LineRegion(lineIndex, linePosition,
                  linePosition + lineLength - 1, type);
            addRegion(lineIndex, region);
         }
         lineIndex++;
         // Each new line within this comment will start at position 0
         // except the very first line where this comment starts
         linePosition = 0;
      }
   }

   /**
    * Get the text for the specified line region.
    * <p>
    * 
    * @param region
    *           which specify the contents within this map. The {@code line} of
    *           this region must be within the range of this map. If there are
    *           no contents (for example this is a line with no printable
    *           characters) a null is returned. The {@code start} and
    *           {@code end} of this region must be within 0 - line length,
    *           otherwise an exception is thrown.
    * @return the text for the specified region
    */
   public String getText(LineRegion region) {
      char[] line = getLineBuffer(region.line);
      if (line == null) {
         return null;
      }
      int len = region.length();
      if (line.length < len) {
         throw new IllegalArgumentException("region for line " + region.line
               + " is greater than line contents");
      }
      return new String(line, region.start, len);
   }

   /**
    * Get the chars for the specified line region.
    * <p>
    * 
    * This method is generally faster as it avoids the creating of a new string
    * object each time, however the returned array is a copy of the region of
    * the specified line.
    * 
    * @param region
    *           which specify the contents within this map. The {@code line} of
    *           this region must be within the range of this map. If there are
    *           no contents (for example this is a line with no printable
    *           characters) a null is returned. The {@code start} and
    *           {@code end} of this region must be within 0 - line length,
    *           otherwise an exception is thrown.
    * @return the text for the specified region
    */
   public char[] getContents(LineRegion region) {
      char[] line = getLineBuffer(region.line);
      if (line == null) {
         return null;
      }
      int len = region.length();
      if (line.length < len) {
         throw new IllegalArgumentException("region for line " + region.line
               + " is greater than line contents");
      }
      char[] rcontents = new char[len];
      System.arraycopy(line, region.start, rcontents, 0, len);
      return rcontents;
   }

   /**
    * Return true if the specified line contains only code.
    * <p>
    * 
    * A line to be only code must not contain any comment at all.
    * 
    * @param line
    *           to check if it is code line only
    * @return true if line contains only code.
    */
   public boolean isCodeOnly(int i) {
      checkLine(i);
      // First must contain characters
      if (isWS(i)) {
         return false;
      }
      // If there are no regions that means
      // there are no comments at this line
      // and all printable characters must
      // be code
      Collection<LineRegion> regions = regionMap.get(i);
      if (regions == null || regions.isEmpty()) {
         return true;
      }
      // Regions is not null or emtpy, that means
      // we have regions of comment
      return false;
   }

   /**
    * Check if the given line (0-based) contains comment.
    * <p>
    * 
    * @param i
    *           the line index
    * @return true if the given line contains comments
    */
   public boolean containsComments(int i) {
      checkLine(i);
      // First must contain characters
      if (isWS(i)) {
         return false;
      }
      // If there are no regions that means
      // there are no comments at this line
      // and all printable characters must
      // be code
      Collection<LineRegion> regions = regionMap.get(i);
      if (regions == null || regions.isEmpty()) {
         return false;
      }
      // Regions is not null or empty, that means
      // we have regions of comments
      return true;
   }

   /**
    * Return true if the specified line contains only comment.
    * <p>
    * 
    * @param line
    *           to check if it is comment line only
    * @return true if line contains only comment.
    */
   public boolean isCommentOnly(int i) {
      if (!containsComments(i)) {
         return false;
      }
      int len = 0;
      for (LineRegion region : regionMap.get(i)) {
         len += region.length();
      }
      return len == contents[i].length;
   }

   /**
    * Get the comment regions for the given line.
    * <p>
    * 
    * @param i
    *           the line index (0-based)
    * @return the comment regions of this line
    */
   public Collection<LineRegion> getCommentRegions(int i) {
      checkLine(i);
      // First must contain characters
      if (isWS(i)) {
         return Collections.emptyList();
      }

      // If there are no regions that means
      // there are no comments at this line
      // and all printable characters must
      // be code
      Collection<LineRegion> regions = regionMap.get(i);
      if (regions == null) {
         regions = Collections.emptyList();
      }
      return regions;
   }

   /**
    * Get the code regions for the given line.
    * <p>
    * 
    * @param i
    *           the line index (0-based)
    * @return the code regions of this line
    */
   public Collection<LineRegion> getCodeRegions(int i) {
      return getCodeRegions(i, true);
   }

   public Collection<LineRegion> getCodeRegions(int line, boolean includeEmpty) {
      checkLine(line);
      // First must contain characters
      if (isWS(line)) {
         return Collections.emptyList();
      }
      
      // If there are no regions that means
      // there are no comments at this line
      // and all printable characters must
      // be code
      TreeSet<LineRegion> regions = regionMap.get(line);
      Collection<LineRegion> cregions = new ArrayList<LineRegion>();

      if (regions == null || regions.isEmpty()) {

         LineRegion region = newRegion(line, 0, contents[line].length - 1,
               LineRegion.Type.CODE, includeEmpty);

         if (region != null) {
            cregions.add(region);
         }
         return cregions;
      }

      // At this point we are sure there are comment regions
      // The algorithm used here is:
      // Assume code starts at start = 0
      // Get current comment region and set the end of code
      // current.start
      // If there are chars between start and end then they are code
      Iterator<LineRegion> it = regions.iterator();
      int start = 0;
      int end;
      while (it.hasNext()) {

         LineRegion current = it.next();
         end = current.start;
         // If this comment region starts from 0 that means
         // end - start = 0 so there will be no comment region
         if ((end - start) > 0) {

            LineRegion region = newRegion(line, start, end - 1,
                  LineRegion.Type.CODE, includeEmpty);

            if (region != null) {
               cregions.add(region);
            }
         }
         start = current.end + 1;
      }

      // When finishing if the start is not at the end of the line that means
      // we have a code because the start is pointing at the end of the last
      // comment region so there are no more comments but there are code
      // characters
      // until the end of the line
      if (start < contents[line].length) {

         LineRegion region = newRegion(line, start, contents[line].length - 1,
               LineRegion.Type.CODE, includeEmpty);

         if (region != null) {
            cregions.add(region);
         }
      }
      return cregions;
   }

   /**
    * Return true if the specified region of the given line is empty or contains
    * only white space chars.
    * <p>
    * 
    * @param line
    *           the line where the region is
    * @param start
    *           the start of the region, inclusive
    * @param end
    *           the end of the region exclusive
    * @return true if the range is empty (contains only white space chars or is
    *         null line)
    */
   private boolean isEmptyRegion(int line, int start, int end) {
      char[] array = getLineBuffer(line);
      if (array == null || array.length == 0) {
         return true;
      }
      return isWS(array, start, end);
   }

   private LineRegion newRegion(int line, int start, int end,
         LineRegion.Type type, boolean includeEmpty) {

      LineRegion region = null;

      if (includeEmpty) {
         region = new LineRegion(line, start, end, LineRegion.Type.CODE);
      } else if (!isEmptyRegion(line, start, end + 1)) {
         region = new LineRegion(line, start, end, LineRegion.Type.CODE);
      }
      return region;
   }
}
