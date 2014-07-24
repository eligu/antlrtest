/**
 * 
 */
package gr.uom.se.antlrtest;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.util.validation.ContainmentCheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class LineMapper {

   /**
    * All known white characters
    */
   static final char[] WHITE_CHARS = new char[] { ' ', '\t', '\r', '\n',
         '\u000C' };

   /**
    * Check if the given character is one of the know white space characters
    * such as \t, \r, \n e.t.c.
    * 
    * @param ch
    *           the character to check
    * @return true if the character is a white space
    */
   public static boolean isWS(char ch) {
      return ContainmentCheck.contains(WHITE_CHARS, ch);
   }

   /**
    * Check if the given array has only known white space characters such as \t,
    * \r, \n e.t.c.
    * 
    * @param array
    *           the array to check
    * @return true if the array contains only white space characters
    */
   public static boolean isWS(char[] array) {
      return isWS(array, 0, array.length);
   }

   /**
    * Check if the given array chars from offset to length -1 are only known
    * white space characters such as \t, \r, \n e.t.c.
    * 
    * @param array
    *           the array to check
    * @param start
    *           the start of subarray to check (inclusive)
    * @param end
    *           the end of chars to check (exclusive)
    * @return true if the array contains only white space characters
    */
   public static boolean isWS(char[] array, int start, int end) {
      return ContainmentCheck.containsOnly(array, start, end, WHITE_CHARS);
   }

   /**
    * Contents of file based on lines. Empty lines have null char array.
    */
   protected char[][] contents;

   /**
    * Create a new line mapper based on the given input.
    * <p>
    * 
    * @param input
    *           the source of contents
    * @throws IOException
    *            if a I/O occurs
    */
   public LineMapper(InputStream input) throws IOException {
      this(new InputStreamReader(input));
   }

   /**
    * Create a new line mapper based on the given file.
    * <p>
    * 
    * @param file
    *           with contents
    * @throws IOException
    *            if a I/O occurs
    */
   public LineMapper(File file) throws IOException {
      this(new FileReader(file));
   }

   /**
    * Create a new line mapper based on the given reader.
    * <p>
    * 
    * @param file
    *           with contents
    * @throws IOException
    *            if a I/O occurs
    */
   public LineMapper(Reader reader) throws IOException {
      BufferedReader breader = null;
      try {
         if (reader instanceof BufferedReader) {
            breader = (BufferedReader) reader;
         } else {
            breader = new BufferedReader(reader);
         }
         this.init(breader);
      } finally {
         if (breader != null) {
            breader.close();
         }
      }

   }

   /**
    * Create a new line mapper based on the given contents.
    * <p>
    * 
    * @param contents
    *           the string which contain the contents to be mapped
    * @throws IOException
    *            if a I/O occurs. Generally speaking a I/O exception will never
    *            occur with a String content however, this is due to buffered
    *            reader.
    */
   public LineMapper(String contents) throws IOException {
      this(new StringReader(contents));
   }

   protected LineMapper() {
   }

   /**
    * Create a new line mapper based on the given reader.
    * <p>
    * 
    * For each line that is not an empty one (contains printable chars) a char
    * array is created with the content of the line.
    * 
    * @param reader
    *           the source of contents
    * @throws IOException
    *            if a I/O occurs
    */
   protected void init(BufferedReader reader) throws IOException {
      // Because the file can get large enough we
      // use a two pass strategy to count the lines first
      // and then to add each line
      String line = null;
      List<char[]> list = new ArrayList<char[]>(1024);
      while ((line = reader.readLine()) != null) {
         list.add(line.toCharArray());
      }
      // Close here to release resources
      reader.close();
      line = null;

      // Init the contents array
      contents = new char[list.size()][];

      // For each non line create a char array
      // with the contents of the current line at specified
      // position
      for (int i = 0; i < list.size(); i++) {
         char[] larray = list.get(i);
         this.contents[i] = larray;
      }
   }

   /**
    * Get the contents of the specified line.
    * <p>
    * 
    * @param line
    *           the number of line (0-based)
    * @return the contents of the given line
    * @throws ArrayIndexOutOfBoundsException
    *            if line is not within the range of the lines (0..n-1 where n is
    *            the lines length)
    * @see #getLinesCount()
    * @see #getContents(int)
    * @see #getLineBuffer(int)
    */
   public String getText(int line) {
      char[] chars = contents[line];
      return new String(chars);
   }

   /**
    * Get the chars for the specified line region.
    * <p>
    * 
    * This method is generally faster as it avoids the creating of a new string
    * object each time, however the returned array is a copy of the region of
    * the specified line.
    * 
    * @param line
    *           which specify the contents within this map. The {@code line}
    *           must be within the range of this map.
    * @return the chars for the specified region. The returned array is a copy
    *         of the original one.
    */
   public char[] getContents(int line) {
      return getRegion(line, 0, contents[line].length);
   }

   /**
    * Get the chars for the specified line region.
    * <p>
    * 
    * <b>WARNING:</b>The array returned is the internal buffer contained within
    * this mapper, and its contents should not be changed otherwise the results
    * of this mapper will be unpredictable. Use cautiously and only for speed.
    * 
    * @param line
    *           which specify the contents within this map. The {@code line}
    *           must be within the range of this map.
    * @return the chars for the specified region. The returned array is the
    *         original buffer of this line. Its contents are read only and
    *         should not be changed.
    */
   public final char[] getLineBuffer(int line) {
      return this.contents[line];
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
    * @param line
    *           the number of line (0-based)
    * @return true if the given line doesn't contain any printable character
    */
   public boolean isWS(int line) {
      return isWS(getLineBuffer(line));
   }

   /**
    * Check if the given line doesn't contain any printable character.
    * <p>
    * 
    * @param line
    *           the number of line (0-based)
    * @return true if the given line doesn't contain any printable character
    */
   public boolean isWSRegion(int line, int start, int end) {
      return isWS(getLineBuffer(line), start, end);
   }

   /**
    * Check if the given line doesn't contain any character.
    * <p>
    * 
    * @param line
    *           the number of line (0-based)
    * @return true if the given line doesn't contain any character
    */
   public boolean isEmpty(int line) {
      return contents[line].length == 0;
   }

   /**
    * Get a copy of the region of the specified line.
    * <p>
    * 
    * @param line
    *           the line of region
    * @param start
    *           the start of the region (inclusive)
    * @param end
    *           the end of the region (exclusive)
    * @return the region of the specified line
    */
   public char[] getRegion(int line, int start, int end) {
      ArgsCheck.greaterThan("start", "0", start, 0);
      ArgsCheck.greaterThan("end", "1", end, 1);
      ArgsCheck.greaterThan("end", "start", end, start);
      ArgsCheck.greaterThanOrEqual("line", "0", line, 0);
      ArgsCheck.lessThan("line", Integer.toString(contents.length), line,
            contents.length);

      char[] original = contents[line];
      int len = end - start;
      char[] copy = new char[len];
      System.arraycopy(original, start, copy, 0, len);
      return copy;
   }
}
