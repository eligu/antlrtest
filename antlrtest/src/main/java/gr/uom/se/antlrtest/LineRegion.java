/**
 * 
 */
package gr.uom.se.antlrtest;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class LineRegion implements Comparable<LineRegion> {
   
   public static enum Type {
      START_OF_MULTILINE_COMMENT,
      START_OF_SINGLELINE_COMMENT,
      PART_OF_MULTILINE_COMMENT,
      END_OF_MULTILINE_COMMENT,
      CODE,
      NONE
   }
   
   public final int line;
   public final int start;
   public final int end;
   public final Type type;
   
   public LineRegion(int line, int start, int end, Type type) {
      this.line = line;
      this.start = start;
      this.end = end;
      this.type = type;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + end;
      result = prime * result + line;
      result = prime * result + start;
      return result;
   }

   public int length() {
      return end - start + 1;
   }
   
   
   @Override
   public String toString() {
      return "[line=" + line + ", start=" + start + ", end=" + end
            + ", type=" + type + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      LineRegion other = (LineRegion) obj;
      if (line != other.line)
         return false;
      if (start != other.start)
         return false;
      if (end != other.end)
         return false;
      return true;
   }

   @Override
   public int compareTo(LineRegion other) {
      int comp = compare(this.line, other.line);
      if(comp != 0) {
         return comp;
      }
      return compare(this.start, other.start);
   }
   
   private static int compare(int i1, int i2) {
      if(i1 == i2) {
         return 0;
      }
      if(i1 < i2) {
         return -1;
      }
      return 1;
   }
}
