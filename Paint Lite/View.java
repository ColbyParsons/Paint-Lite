
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.*;
import java.lang.*;
import java.awt.geom.*;
import javax.swing.text.*;
import java.io.*;
import java.lang.*;
import java.awt.geom.Line2D.*;
//THE SKELETON OF THIS FILE IS PROVIDED BY THE CS349 ASSIGNMENT 2 STARTER CODE
public class View extends JFrame implements Observer {

    private Model model;
    DrawArea mainDraw;
    int isSelected = -1;
    boolean startDrag = false;
    JFrame mainWindow;

    /**
     * Create a new View.
     */
    public View(Model model) {
        // Set up the window.
        this.setTitle("JSketch");
        this.setMinimumSize(new Dimension(150, 450));
        this.setPreferredSize(new Dimension(800, 600));
        this.setSize(800,600);
        this.setLocation(20, 50);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        JPanel inner = new JPanel();
        inner.setPreferredSize(new Dimension(790,590));
        inner.setBackground(Color.black);
        inner.setLayout(new BorderLayout());
        inner.add(new TopBar(model), BorderLayout.NORTH);
        inner.add(new ToolBar(model), BorderLayout.WEST);
        mainDraw = new DrawArea(model);
        JScrollPane jp = new JScrollPane(mainDraw, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jp.getVerticalScrollBar().setUnitIncrement(15); // speedup scrolling
        inner.add(jp,BorderLayout.CENTER);

        this.setContentPane(inner);
        // Hook up this observer so that it will be notified when the model
        // changes.
        this.model = model;
        model.addObserver(this);

        model.notifyObservers();
        setVisible(true);
        mainDraw.setPreferredSize(mainDraw.getSize());

        mainWindow = this;
    }

    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.

    }


    public class DrawArea extends JPanel implements Observer{

        private Model model;
        private Point dragPoint, startPoint,finishPoint;
        private Color currColour;
        private Model.Tool currTool;
        private boolean mouseIn;
        private boolean mouseDown;
        private boolean mouseClick;
        private boolean mouseUp;
        private boolean drag = false;
        private int thick;
        boolean during = false;
        Point start;
        Model.DrawObject currObj;

        public DrawArea(Model model) {
            this.model = model;
            model.addObserver(this);
            DrawArea copy = this;

            this.setMinimumSize(new Dimension(100, 450));

            this.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent componentEvent) {
                    copy.setPreferredSize(copy.getSize());
                }
            });

            this.addMouseMotionListener(new MouseAdapter(){
                public void mouseMoved(MouseEvent e){
                    model.setMousePos(e.getX(), e.getY());
                }
                public void mouseDragged(MouseEvent e){
                    if(model.getMouseIn()) {
                        drag = true;
                        model.setMousePos(e.getX(), e.getY());
                    }
                }
            });

            this.addMouseListener(new MouseAdapter() {



                public void mouseClicked (MouseEvent mouseEvent) {
                    if(model.getMouseIn()) {
                        model.setMouseClick(true);
                    }
                }

                public void mousePressed(MouseEvent mouseEvent) {

                }

                public void mouseReleased(MouseEvent mouseEvent) {
                    if(model.getMouseIn()) {
                        drag = false;
                        startDrag = false;
                        model.setDrag();

                    }
                }
                public void mouseEntered (MouseEvent mouseEvent) {
                    model.setMouseIn(true);
                }
                public void mouseExited (MouseEvent mouseEvent) {
                    model.setMouseIn(false);
                }

            });

            this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
            this.getActionMap().put("escape", new Escape(model));




            this.setBackground(Color.white);




            setVisible(true);
        }



        public class Escape extends AbstractAction{

            private Model model;
            public Escape(Model model){
                this.model = model;
            }

            public void actionPerformed(ActionEvent a){
                model.selectObj(-1);
                isSelected = -1;
                repaint();
            }

        }

        public void drawShape(Model.DrawObject d, Graphics2D g){
            if(d.selected){
                g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            }else {
                g.setStroke(new BasicStroke(d.thickness));
            }
            if(d.type == Model.Tool.CIRCLE){
                if(d.fill != null){
                    g.setColor(d.fill);
                    g.fillOval(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                }
                g.setColor(d.colour);
                g.drawOval(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
            }else if(d.type == Model.Tool.LINE){
                g.setColor(d.colour);
                g.drawLine(d.S.x, d.S.y, d.F.x, d.F.y);
            }else if(d.type == Model.Tool.RECT){
                if(d.fill != null){
                    g.setColor(d.fill);
                    g.fillRect(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                }
                g.setColor(d.colour);
                g.drawRect(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // cast to get 2D drawing methods
            Graphics2D g2 = (Graphics2D) g;

            ArrayList<Model.DrawObject> shapes = model.getShapes();

            for(int i = 0; i < shapes.size(); i++){
                drawShape(shapes.get(i), g2);
            }
        }

        public int hitTest(Point mPos){
            ArrayList<Model.DrawObject> shapes = model.getShapes();
            for(int i = shapes.size()-1; i >= 0; i--){
                Model.DrawObject d = shapes.get(i);
                if(d.type == Model.Tool.LINE){
                    //Ellipse2D.Double e = new Ellipse2D.Double(mPos.x,mPos.y,12,12);
                    //CALCULATE CLOSEST POINT
                    Line2D l = new Line2D.Double(d.S.x,d.S.y, d.F.x,d.F.y);
                    double distance = l.ptLineDist(mPos.x, mPos.y);
                    if(distance <= 6){
                        return i;
                    }
                }else if(d.type == Model.Tool.CIRCLE){
                    Ellipse2D.Double e = new Ellipse2D.Double(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                    if(e.contains(mPos.x,mPos.y)){
                        return i;
                    }
                }else if(d.type == Model.Tool.RECT){
                    Rectangle2D.Double e = new Rectangle2D.Double(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                    if(e.contains(mPos.x,mPos.y)){
                        return i;
                    }
                }
            }
            return -1;
        }

        public void update(Object observable) {
            this.currTool = model.getTool();
            this.currColour = model.getColour();
            this.mouseIn = model.getMouseIn();
            this.mouseClick = model.getMouseClick();
            this.thick = model.getThick();


            if(isSelected != -1 && drag && !startDrag){
                Model.DrawObject d = model.getShape(isSelected);
                Point mPos = model.getMousePos();
                if(d.type == Model.Tool.LINE){
                    //Ellipse2D.Double e = new Ellipse2D.Double(mPos.x,mPos.y,12,12);
                    //CALCULATE CLOSEST POINT
                    Line2D l = new Line2D.Double(d.S.x,d.S.y, d.F.x,d.F.y);
                    double distance = l.ptLineDist(mPos.x, mPos.y);
                    if(distance <= 6){
                        startDrag = true;
                        Point m = model.getMousePos();
                        dragPoint = new Point(m.x,m.y);
                        startPoint = new Point(d.S.x,d.S.y);
                        finishPoint = new Point(d.F.x,d.F.y);
                    }

                }else if(d.type == Model.Tool.CIRCLE){
                    Ellipse2D.Double e = new Ellipse2D.Double(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                    if(e.contains(mPos.x,mPos.y)){
                        startDrag = true;
                        Point m = model.getMousePos();
                        dragPoint = new Point(m.x,m.y);
                        startPoint = new Point(d.S.x,d.S.y);
                        finishPoint = new Point(d.F.x,d.F.y);
                    }
                }else if(d.type == Model.Tool.RECT){
                    Rectangle2D.Double e = new Rectangle2D.Double(Math.min(d.S.x,d.F.x), Math.min(d.S.y,d.F.y), Math.max(d.S.x,d.F.x)-Math.min(d.S.x,d.F.x), Math.max(d.S.y,d.F.y)-Math.min(d.S.y,d.F.y));
                    if(e.contains(mPos.x,mPos.y)){
                        startDrag = true;
                        Point m = model.getMousePos();
                        dragPoint = new Point(m.x,m.y);
                        startPoint = new Point(d.S.x,d.S.y);
                        finishPoint = new Point(d.F.x,d.F.y);
                    }
                }
            }

            if(isSelected != -1 && drag && startDrag){
                Model.DrawObject d = model.getShape(isSelected);
                Point m = model.getMousePos();
                d.S.x = startPoint.x + m.x - dragPoint.x;
                d.S.y = startPoint.y + m.y - dragPoint.y;
                d.F.x = finishPoint.x + m.x - dragPoint.x;
                d.F.y = finishPoint.y + m.y - dragPoint.y;
            }else if(this.mouseIn && this.mouseClick) {
                if (this.currTool == Model.Tool.MOUSE) {
                    int index = hitTest(model.getMousePos());
                    if(index != -1){
                        Model.DrawObject d = model.getShape(index);
                        model.quietClick();
                        this.mouseClick = false;
                        model.setColour(d.colour);
                        model.setThick(d.thickness);
                    }
                    model.selectObj(index);
                    isSelected = index;
                } else if (this.currTool == Model.Tool.LINE || this.currTool == Model.Tool.CIRCLE || this.currTool == Model.Tool.RECT) {
                    if(!during){
                        during = true;
                        start = model.getMousePos();
                        Point fixed = new Point(start.x,start.y);
                        currObj = model.makeObj(fixed, start, this.currTool, this.currColour, this.thick);
                        model.addToPaint(currObj);
                        model.setMouseClick(false);
                    }else{
                        during = false;
                        Point fixed = new Point(start.x,start.y);
                        currObj.F = fixed;
                    }
                }else if (this.currTool == Model.Tool.ERASER) {
                    int index = hitTest(model.getMousePos());
                    if(index != -1){
                        model.removeFromPaint(index);
                    }
                }else if (this.currTool == Model.Tool.BUCKET) {
                    int index = hitTest(model.getMousePos());
                    if(index != -1){
                        Model.DrawObject d = model.getShape(index);
                        d.fill = this.currColour;
                    }
                }

            }
            if(during){
                currObj.F = model.getMousePos();
            }
            repaint();
        }
    }


    public class TopBar extends JPanel implements Observer {

        private Model model;

        public TopBar(Model model) {
            SpringLayout layout = new SpringLayout();
            this.setPreferredSize(new Dimension(790,35));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
            this.setLayout(layout);

            //THIS CODE IS FROM STACK OVERFLOW
            JPopupMenu popup = new JPopupMenu();
            popup.add(new JMenuItem(new AbstractAction("New") {
                public void actionPerformed(ActionEvent e) {
                    model.clear();
                }
            }));
            popup.add(new JMenuItem(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent e) {
                    //FILECHOOSER CODE TAKEN FROM ONLINE EXAMPLE
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Specify a file to save");

                    int userSelection = fileChooser.showSaveDialog(mainWindow);
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        try {
                            PrintWriter writer = new PrintWriter(fileToSave.getAbsolutePath()+".txt", "UTF-8");
                            ArrayList<Model.DrawObject> shapes = model.getShapes();
                            for(int i = 0; i< shapes.size();i++){
                                Model.DrawObject d = shapes.get(i);
                                if(d.fill == null){
                                    writer.println(d.S.x+"|"+d.S.y+"|"+d.F.x+"|"+d.F.y+"|"+d.type+"|"+d.colour.getRGB()+"|"+"null"+"|"+d.thickness);
                                }else{
                                    writer.println(d.S.x+"|"+d.S.y+"|"+d.F.x+"|"+d.F.y+"|"+d.type+"|"+d.colour.getRGB()+"|"+d.fill.getRGB()+"|"+d.thickness);
                                }
                            }
                            writer.close();
                        }catch (IOException err){
                            System.out.println(err);
                        }
                    }
                }
            }));

/*

            public Point S;
            public Point F;
            public Tool type;
            public Color colour;
            public Color fill = null;
            public int thickness;
            public boolean selected = false;


        public DrawObject(Point S, Point F, Tool type, Color c, int t){

        makeObj(Point S, Point F, Tool type, Color c, int t)*/

            popup.add(new JMenuItem(new AbstractAction("Load") {
                public void actionPerformed(ActionEvent e) {

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Specify a file to load");

                    int userSelection = fileChooser.showOpenDialog(mainWindow);
                    //CODE BASED ON WEBSITE EXAMPLE
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToLoad = fileChooser.getSelectedFile();
                        model.clear();
                        try (BufferedReader br = new BufferedReader(new FileReader(fileToLoad))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                String[] s = line.split("\\|");
                                if(s.length <= 7){
                                    continue;
                                }
                                Model.DrawObject d = model.makeObj(new Point(Integer.parseInt(s[0]), Integer.parseInt(s[1])), new Point(Integer.parseInt(s[2]),Integer.parseInt(s[3])),
                                        Model.Tool.valueOf(s[4]), new Color(Integer.parseInt(s[5])),Integer.parseInt(s[7]));
                                try{
                                    d.fill = new Color(Integer.parseInt(s[6]));
                                }catch(Exception error){

                                }
                                model.addToPaint(d);
                            }
                            br.close();
                        }catch(IOException err){
                            System.out.println(err);
                        }
                        mainDraw.repaint();
                    }
                }
            }));


            JButton file = new JButton("File");
            file.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            });
            //END OF STACK OVERFLOW CODE


            this.add(file);
            JButton view = new JButton("View");
            this.add(view);

            layout.putConstraint(SpringLayout.WEST, file, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, file, 5, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.NORTH, view, 5, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.WEST, view, 5, SpringLayout.EAST, file);

            this.model = model;
            model.addObserver(this);

            setVisible(true);
        }

        public void update(Object observable) {
            // XXX Fill this in with the logic for updating the view when the model
            // changes.

        }
    }

    public class ToolBar extends JPanel implements Observer {

        private Model model;

        public ToolBar(Model model) {
            SpringLayout layout = new SpringLayout();
            this.setLayout(layout);
            this.setPreferredSize(new Dimension(102,590));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black));
            ToolBox tb = new ToolBox(model);
            this.add(tb);
            JPanel cp = new ColourPanel(model);
            this.add(cp);
            JButton picker = new JButton("Chooser");
            this.add(picker);
            JPanel tp = new Thickness(model);
            this.add(tp);
            layout.putConstraint(SpringLayout.NORTH, tb, 5, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.WEST, tb, 6, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, cp, 5, SpringLayout.SOUTH, tb);
            layout.putConstraint(SpringLayout.WEST, cp, 6, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, picker, 5, SpringLayout.SOUTH, cp);
            layout.putConstraint(SpringLayout.WEST, picker, 5, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, tp, 5, SpringLayout.SOUTH, picker);
            layout.putConstraint(SpringLayout.WEST, tp, 6, SpringLayout.WEST, this);

            picker.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Color c;
                    c = model.getColour();
                    c = JColorChooser.showDialog(null, "Choose a colour", c);
                    model.setColour(c);
                }
            } );


            this.model = model;
            model.addObserver(this);

            setVisible(true);
        }

        public void update(Object observable) {
            // XXX Fill this in with the logic for updating the view when the model
            // changes.

        }

        public class ColourPanel extends JPanel implements Observer {

            private Model model;
            private Color currColour;
            JButton red, blue, yellow, pink, green, orange;
            Color switchColour = Color.green;
            JPanel gSQR;

            public ColourPanel(Model model) {
                this.setLayout(new GridLayout(3, 2));
                this.setPreferredSize(new Dimension(90,135));
                JPanel rSQR = new JPanel();
                rSQR.setBackground(Color.red);
                rSQR.setPreferredSize(new Dimension(45,45));
                JPanel ySQR = new JPanel();
                ySQR.setBackground(Color.yellow);
                ySQR.setPreferredSize(new Dimension(45,45));
                JPanel bSQR = new JPanel();
                bSQR.setBackground(Color.blue);
                bSQR.setPreferredSize(new Dimension(45,45));
                JPanel oSQR = new JPanel();
                oSQR.setBackground(Color.orange);
                oSQR.setPreferredSize(new Dimension(45,45));
                JPanel pSQR = new JPanel();
                pSQR.setBackground(Color.pink);
                pSQR.setPreferredSize(new Dimension(45,45));
                gSQR = new JPanel();
                gSQR.setBackground(Color.green);
                gSQR.setPreferredSize(new Dimension(45,45));

                red = new JButton();
                red.setMargin(new Insets(1,1,1,1));
                red.add(rSQR);
                red.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = Color.red;
                        }
                        model.setColour(Color.red);
                    }
                } );

                yellow = new JButton();
                yellow.setMargin(new Insets(1,1,1,1));
                yellow.add(ySQR);
                yellow.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = Color.yellow;
                        }
                        model.setColour(Color.yellow);
                    }
                } );

                blue = new JButton();
                blue.setMargin(new Insets(1,1,1,1));
                blue.add(bSQR);
                blue.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = Color.blue;
                        }
                        model.setColour(Color.blue);
                    }
                } );

                orange = new JButton();
                orange.setMargin(new Insets(1,1,1,1));
                orange.add(oSQR);
                orange.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = Color.orange;
                        }
                        model.setColour(Color.orange);
                    }
                } );

                pink = new JButton();
                pink.setMargin(new Insets(1,1,1,1));
                pink.add(pSQR);
                pink.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = Color.pink;
                        }
                        model.setColour(Color.pink);
                    }
                } );

                green = new JButton();
                green.setMargin(new Insets(1,1,1,1));
                green.add(gSQR);
                green.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(isSelected != -1){
                            Model.DrawObject d = model.getShape(isSelected);
                            d.colour = switchColour;
                        }
                        model.setColour(switchColour);
                    }
                } );

                this.add(red);
                this.add(yellow);
                this.add(blue);
                this.add(orange);
                this.add(pink);
                this.add(green);


                this.model = model;
                model.addObserver(this);

                setVisible(true);
            }

            public void update(Object observable) {
                this.currColour = this.model.getColour();

                red.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                yellow.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                blue.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                green.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                orange.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                pink.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));


                if(this.currColour == Color.red) {
                    red.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                }else if(this.currColour == Color.yellow) {
                    yellow.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                }else if(this.currColour == Color.blue) {
                    blue.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                }else if(this.currColour == Color.pink) {
                    pink.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                }else if(this.currColour == Color.orange) {
                    orange.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                }else{
                    green.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                    gSQR.setBackground(this.currColour);
                    switchColour = this.currColour;
                }
            }
        }

        public class ToolBox extends JPanel implements Observer {

            private Model model;
            private Model.Tool currTool;
            JButton mouse, eraser, line, rectangle, circle, paintbucket;

            public ToolBox(Model model) {
                this.setLayout(new GridLayout(3, 2));
                this.setPreferredSize(new Dimension(90,135));
                mouse = new JButton();
                mouse.setMargin(new Insets(1,1,1,1));
                try {
                    Image img1 = ImageIO.read(getClass().getResource("src/Mouse.png"));
                    mouse.setIcon(new ImageIcon(img1));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                mouse.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.MOUSE);
                    }
                } );

                eraser = new JButton();
                eraser.setMargin(new Insets(1,1,1,1));
                try {
                    Image img2 = ImageIO.read(getClass().getResource("src/Eraser.png"));
                    eraser.setIcon(new ImageIcon(img2));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                eraser.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.ERASER);
                        model.selectObj(-1);
                        isSelected = -1;
                        mainDraw.repaint();
                    }
                } );

                line = new JButton();
                line.setMargin(new Insets(1,1,1,1));
                try {
                    Image img1 = ImageIO.read(getClass().getResource("src/Line.png"));
                    line.setIcon(new ImageIcon(img1));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                line.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.LINE);
                        model.selectObj(-1);
                        isSelected = -1;
                        mainDraw.repaint();
                    }
                } );

                circle = new JButton();
                circle.setMargin(new Insets(1,1,1,1));
                try {
                    Image img1 = ImageIO.read(getClass().getResource("src/Circle.png"));
                    circle.setIcon(new ImageIcon(img1));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                circle.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.CIRCLE);
                        model.selectObj(-1);
                        isSelected = -1;
                        mainDraw.repaint();
                    }
                } );

                rectangle = new JButton();
                rectangle.setMargin(new Insets(1,1,1,1));
                try {
                    Image img1 = ImageIO.read(getClass().getResource("src/Square.png"));
                    rectangle.setIcon(new ImageIcon(img1));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                rectangle.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.RECT);
                        model.selectObj(-1);
                        isSelected = -1;
                        mainDraw.repaint();
                    }
                } );

                paintbucket = new JButton();
                paintbucket.setMargin(new Insets(1,1,1,1));
                try {
                    Image img1 = ImageIO.read(getClass().getResource("src/Bucket.png"));
                    paintbucket.setIcon(new ImageIcon(img1));
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                paintbucket.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        model.setTool(Model.Tool.BUCKET);
                        model.selectObj(-1);
                        isSelected = -1;
                        mainDraw.repaint();
                    }
                } );

                this.add(mouse);
                this.add(eraser);
                this.add(line);
                this.add(circle);
                this.add(rectangle);
                this.add(paintbucket);

                this.model = model;
                model.addObserver(this);

                setVisible(true);
            }

            public void update(Object observable) {
                this.currTool = this.model.getTool();

                mouse.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                circle.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                line.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                rectangle.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                eraser.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
                paintbucket.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));


                switch (this.currTool){
                    case MOUSE:
                        mouse.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                    case CIRCLE:
                        circle.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                    case LINE:
                        line.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                    case RECT:
                        rectangle.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                    case ERASER:
                        eraser.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                    case BUCKET:
                        paintbucket.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));
                        break;
                }
            }
        }

        public class Thickness extends JPanel implements Observer {

            private Model model;
            boolean thick1in = false, thick2in = false, thick5in = false, thick10in = false;
            JPanel thick1, thick2, thick5, thick10;

            public Thickness(Model model) {
                this.model = model;
                model.addObserver(this);



                SpringLayout layout = new SpringLayout();
                this.setLayout(layout);

                this.setPreferredSize(new Dimension(90,85));
                this.setBackground(Color.white);

                thick1 = new JPanel();
                thick1.addMouseListener(new MouseAdapter() {
                    public void mouseClicked (MouseEvent mouseEvent) {
                        if(thick1in) {
                            if(isSelected != -1){
                                Model.DrawObject d = model.getShape(isSelected);
                                d.thickness = 1;
                            }
                            model.setThick(1);
                        }
                    }
                    public void mouseEntered (MouseEvent mouseEvent) {
                        thick1in = true;
                    }
                    public void mouseExited (MouseEvent mouseEvent) {
                        thick1in = false;
                    }
                });

                thick2 = new JPanel();
                thick2.addMouseListener(new MouseAdapter() {
                    public void mouseClicked (MouseEvent mouseEvent) {
                        if(thick2in) {
                            if(isSelected != -1){
                                Model.DrawObject d = model.getShape(isSelected);
                                d.thickness = 2;
                            }
                            model.setThick(2);
                        }
                    }
                    public void mouseEntered (MouseEvent mouseEvent) {
                        thick2in = true;
                    }
                    public void mouseExited (MouseEvent mouseEvent) {
                        thick2in = false;
                    }
                });


                thick5 = new JPanel();
                thick5.addMouseListener(new MouseAdapter() {
                    public void mouseClicked (MouseEvent mouseEvent) {
                        if(thick5in) {
                            if(isSelected != -1){
                                Model.DrawObject d = model.getShape(isSelected);
                                d.thickness = 5;
                            }
                            model.setThick(5);
                        }
                    }
                    public void mouseEntered (MouseEvent mouseEvent) {
                        thick5in = true;
                    }
                    public void mouseExited (MouseEvent mouseEvent) {
                        thick5in = false;
                    }
                });


                thick10 = new JPanel();
                thick10.addMouseListener(new MouseAdapter() {
                    public void mouseClicked (MouseEvent mouseEvent) {
                        if(thick10in) {
                            if(isSelected != -1){
                                Model.DrawObject d = model.getShape(isSelected);
                                d.thickness = 10;
                            }
                            model.setThick(10);
                        }
                    }
                    public void mouseEntered (MouseEvent mouseEvent) {
                        thick10in = true;
                    }
                    public void mouseExited (MouseEvent mouseEvent) {
                        thick10in = false;
                    }
                });

                thick1.setBackground(Color.red);
                thick2.setBackground(Color.black);
                thick5.setBackground(Color.black);
                thick10.setBackground(Color.black);
                thick1.setPreferredSize(new Dimension(70,1));
                thick2.setPreferredSize(new Dimension(70,2));
                thick5.setPreferredSize(new Dimension(70,5));
                thick10.setPreferredSize(new Dimension(70,10));
                this.add(thick1);
                this.add(thick2);
                this.add(thick5);
                this.add(thick10);

                layout.putConstraint(SpringLayout.WEST, thick1, 10, SpringLayout.WEST, this);
                layout.putConstraint(SpringLayout.WEST, thick2, 10, SpringLayout.WEST, this);
                layout.putConstraint(SpringLayout.WEST, thick5, 10, SpringLayout.WEST, this);
                layout.putConstraint(SpringLayout.WEST, thick10, 10, SpringLayout.WEST, this);

                layout.putConstraint(SpringLayout.NORTH, thick1, 15, SpringLayout.NORTH, this);
                layout.putConstraint(SpringLayout.NORTH, thick2, 14, SpringLayout.SOUTH, thick1);
                layout.putConstraint(SpringLayout.NORTH, thick5, 12, SpringLayout.SOUTH, thick2);
                layout.putConstraint(SpringLayout.NORTH, thick10, 10, SpringLayout.SOUTH, thick5);

                setVisible(true);
            }

            public void update(Object observable) {
                // XXX Fill this in with the logic for updating the view when the model
                // changes.
                int currThick = model.getThick();
                thick1.setBackground(Color.black);
                thick2.setBackground(Color.black);
                thick5.setBackground(Color.black);
                thick10.setBackground(Color.black);
                if(currThick == 1){
                    thick1.setBackground(Color.red);
                }else if(currThick == 2){
                    thick2.setBackground(Color.red);
                }else if(currThick == 5){
                    thick5.setBackground(Color.red);
                }else if(currThick == 10){
                    thick10.setBackground(Color.red);
                }

            }
        }
    }
}