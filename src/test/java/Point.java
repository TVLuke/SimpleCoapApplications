/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 23.05.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class Point {

    public int x;
    public int y;


    public static void change(Point point, int newX, int newY){
        point.x = newX;
        point.y = newY;
    }

    @Override
    public String toString(){
        return "x: " + x + ", y: " + y;
    }
    public static void main(String[] args){
        Point test = new Point();
        test.x = 5;
        test.y = 6;
        System.out.println(test);
        Point.change(test, 10, 11);
        System.out.println(test);
    }
}
