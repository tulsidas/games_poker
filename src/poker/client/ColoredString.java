package poker.client;

public class ColoredString implements Comparable<ColoredString> {
   private String string;

   private int color;

   public ColoredString(String string, int color) {
      this.string = string;
      this.color = color;
   }

   public String getString() {
      return string;
   }

   public int getColor() {
      return color;
   }

   @Override
   public int compareTo(ColoredString o) {
      return string.compareTo(o.string);
   }
}
