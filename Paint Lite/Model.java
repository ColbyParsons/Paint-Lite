
import java.util.*;
import java.awt.Color;
import java.awt.Point;
//THIS CLASS IS PROVIDED BY THE CS349 ASSIGNMENT 2 STARTER CODE
public class Model {

    public class DrawObject {
        public Point S;
        public Point F;
        public Tool type;
        public Color colour;
        public Color fill = null;
        public int thickness;
        public boolean selected = false;


        public DrawObject(Point S, Point F, Tool type, Color c, int t){
            this.S = S;
            this.F = F;
            this.type = type;
            this.colour = c;
            this.thickness = t;
        }
    }

    public enum Tool {
        MOUSE,
        ERASER,
        LINE,
        RECT,
        CIRCLE,
        BUCKET
    }

    private Tool currTool = Tool.MOUSE;
    private Color currColour = Color.red;
    private int currThick = 1;

    /** The observers that are watching this model for changes. */
    private List<Observer> observers;
    private boolean mouseIn = false;
    private boolean mouseDown = false;
    private boolean mouseUp = false;
    private boolean mouseClick = false;
    private ArrayList<DrawObject> shapes = new ArrayList();
    private Point mousePos = new Point(0,0);



    /**
     * Create a new model.
     */
    public Model() {
        this.observers = new ArrayList<Observer>();
    }

    /**
     * Add an observer to be notified when this model changes.
     */
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Remove an observer from this model.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify all observers that the model has changed.
     */
    public void notifyObservers() {
        for (Observer observer: this.observers) {
            observer.update(this);
        }
    }
    public void selectObj(int i){
        for(int j = 0; j < shapes.size();j++){
            if(j == i){
                shapes.get(j).selected = true;
            }else{
                shapes.get(j).selected = false;
            }
        }
    }

    public DrawObject makeObj(Point S, Point F, Tool type, Color c, int t){
        return new DrawObject(S,F,type,c,t);
    }

    public ArrayList<DrawObject> getShapes() {
        return shapes;
    }

    public void clear(){shapes.clear(); notifyObservers();}

    public void addToPaint(DrawObject d){
        shapes.add(d);
    }

    public void removeFromPaint(int i){
        shapes.remove(i);
    }

    public DrawObject getShape(int i){return shapes.get(i);}

    public void setMousePos(int x, int y){
        mousePos.x = x;
        mousePos.y = y;
        notifyObservers();
    }

    public Point getMousePos(){
        return mousePos;
    }

    public void setDrag(){notifyObservers();}

    public void quietClick(){this.mouseClick = false;}
    public void setMouseClick(boolean i){
        this.mouseClick = i;
        notifyObservers();
        this.mouseClick = false;
    }

    public boolean getMouseClick(){
        return this.mouseClick;
    }

    public void setMouseIn(boolean i){
        this.mouseIn = i;
        notifyObservers();
    }

    public boolean getMouseIn(){
        return this.mouseIn;
    }

    /*public void setMouseDown(boolean i){
        this.mouseDown = i;
        notifyObservers();
    }

    public boolean getMouseDown(){
        return this.mouseIn;
    }

    public void setMouseUp(boolean i){
        this.mouseUp = i;
        notifyObservers();
        this.mouseUp = false;
    }

    public boolean getMouseUp(){
        return this.mouseUp;
    }
    */

    public void setThick(int i){
        this.currThick = i;
        notifyObservers();
    }

    public int getThick(){
        return this.currThick;
    }

    public void setColour(Color i){
        this.currColour = i;
        notifyObservers();
    }

    public Color getColour(){
        return this.currColour;
    }

    public void setTool(Tool i){
        this.currTool = i;
        notifyObservers();
    }

    public Tool getTool(){
        return this.currTool;
    }
}
