package net.perkowitz.westworld;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Map;

public class Host {

    private static int IMAGE_HEIGHT = 4000;
    private static int IMAGE_WIDTH = (int)Math.ceil(IMAGE_HEIGHT * 1.4);
    private static int STROKE_WIDTH = (int)(IMAGE_HEIGHT * 0.0015);
    private static int CENTER_STEPS = 2;
    private static int CIRCLE_COUNT = 20 + CENTER_STEPS;
    private static int CIRCLE_STEP = IMAGE_HEIGHT / (CIRCLE_COUNT + 6);
    private static int MAX_RADIUS = CIRCLE_COUNT * CIRCLE_STEP;
    private static int POINT_SIZE = (int)(CIRCLE_STEP / 2.5);
    private static boolean CLEAR_BACKGROUND = false;

    // define color palettes for dark-on-light, light-on-dark
    private static Map<String, Color> darkPalette = Maps.newHashMap();
    static {
        darkPalette.put("back", new Color(0, 0, 0));
        darkPalette.put("base", new Color(100, 100, 120));
        darkPalette.put("highlight", new Color(230, 230, 255));
        darkPalette.put("poly", new Color(50, 70, 95));
        darkPalette.put("circle", new Color(30, 35, 50));
        darkPalette.put("text", new Color(200, 210, 255));
    }

    private static Map<String, Color> lightPalette = Maps.newHashMap();
    static {
        lightPalette.put("back", new Color(255, 255, 255));
        lightPalette.put("base", new Color(150, 150, 170));
        lightPalette.put("highlight", new Color(255, 255, 255));
        lightPalette.put("poly", new Color(100, 100, 150));
        lightPalette.put("circle", new Color(230, 230, 242));
        lightPalette.put("text", new Color(100, 100, 120));
    }

//    private static Map<String, Color> palette = darkPalette;
    private static Map<String, Color> palette = lightPalette;


    private static Map<String, Integer> attributes;
    private static List<String> attributeList;

    public static void main(String args[]) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage Host <data.csv>");
            System.exit(-1);
        }

        String dataFile = args[0];

        palette = darkPalette;
        String imageFile = dataFile + ".png";
        if (dataFile.endsWith(".csv")) {
            imageFile = dataFile.substring(0, dataFile.length()-4) + ".png";
        }
        generate(dataFile, imageFile);

        palette = lightPalette;
        imageFile = dataFile + "-light.png";
        if (dataFile.endsWith(".csv")) {
            imageFile = dataFile.substring(0, dataFile.length()-4) + "-light.png";
        }
        generate(dataFile, imageFile);



        
    }

    public static void generate(String dataFile, String imageFile) throws Exception {

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!CLEAR_BACKGROUND) {
            g.setColor(palette.get("back"));
            g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            g.setStroke(new BasicStroke(STROKE_WIDTH));
        }

        int centerX = (int) (IMAGE_WIDTH / 2);
        int centerY = (int) (IMAGE_HEIGHT / 2);
        g.setColor(palette.get("circle"));
        g.fillOval(centerX - CIRCLE_STEP * CIRCLE_COUNT / 2, centerY - CIRCLE_STEP * CIRCLE_COUNT / 2, CIRCLE_COUNT * CIRCLE_STEP, CIRCLE_COUNT * CIRCLE_STEP);

        System.out.printf("Reading personality data from %s..\n", dataFile);
        readAttributes(dataFile);
        drawAttributes(g);

        System.out.printf("Generating personality matrix in %s..\n", imageFile);
        ImageIO.write(image, "png", new File(imageFile));


    }

    public static Map<String, Integer> readAttributes(String filename) {

        BufferedReader br = null;
        String line;
        attributes = Maps.newHashMap();
        attributeList = Lists.newArrayList();

        try {

            int count = 0;
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] columns = line.split(",");
                    if (columns.length >= 2) {
                        String attribute = columns[0];
                        Integer value = new Integer(columns[1]);
                        if (count < 20) {
                            attributes.put(attribute, value);
                            attributeList.add(attribute);
                        }
                        count++;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return attributes;
    }

    public static void drawCircles(Graphics2D g) {

        int centerX = (int) (IMAGE_WIDTH / 2);
        int centerY = (int) (IMAGE_HEIGHT / 2);

        g.setColor(palette.get("base"));
        for (int i = 0; i < CIRCLE_COUNT; i++) {
            int step = (i+1) * CIRCLE_STEP;
            g.drawOval(centerX - step / 2, centerY - step / 2, step, step);
        }
    }

    public static void drawAttributes(Graphics2D g) {

        int centerX = (int) (IMAGE_WIDTH / 2);
        int centerY = (int) (IMAGE_HEIGHT / 2);

        int count = attributes.size();
        double stepAngle = 360 / count;

        Font font = new Font("Helvetica", Font.BOLD, CIRCLE_STEP / 2);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        Polygon polygon = new Polygon();

        // first compute all the points
        int i = 0;
        for (String attribute : attributeList) {

            // get value and write it back to the attributes
            Integer value = getValue(attributes.get(attribute));
            attributes.put(attribute, value);
            double radians = Math.toRadians(i * stepAngle - 90);

            // now the same for the actual attribute value
            int x = centerX + (int)(Math.cos(radians) * (value + CENTER_STEPS) * CIRCLE_STEP / 2);
            int y = centerY + (int)(Math.sin(radians) * (value + CENTER_STEPS) * CIRCLE_STEP / 2);
            polygon.addPoint(x, y);
            i++;
        }

        // draw the polygon and the circles on top
        g.setColor(palette.get("poly"));
        g.fillPolygon(polygon);
        drawCircles(g);

        // now draw the points and labels
        i = 0;
        for (String attribute : attributeList) {

            // compute point for outer circle
            double radians = Math.toRadians(i * stepAngle - 90);
            int x = centerX + (int)(Math.cos(radians) * MAX_RADIUS / 2);
            int y = centerY + (int)(Math.sin(radians) * MAX_RADIUS / 2);
            g.setColor(palette.get("base"));
            g.drawLine(centerX, centerY, x, y);
            g.setColor(palette.get("back"));
            g.fillOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
            g.setColor(palette.get("base"));
            g.drawOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);

            // add attribute label
            Integer value = getValue(attributes.get(attribute));
            String text = attribute.toUpperCase() + " [" + value + "]";
            if (i > count / 2) {
                text =  "[" + value + "] " + attribute.toUpperCase();
            }
            int textX = x;
            int textY = y;
            int textWidth = fontMetrics.stringWidth(text);
            if (i == 0) {
                textX = x - textWidth / 2;
                textY = y - (int)(1.5 * POINT_SIZE);
            } else if (i > 0 && i < count * 0.5) {
                textX = x + (int)(1.5 * POINT_SIZE);
                textY = y + POINT_SIZE / 2;
            } else if (i == count / 2) {
                textX = x - textWidth / 2;
                textY = y + (int)(2.5 * POINT_SIZE);
            } else {
                textX = x - textWidth - (int)(1.5 * POINT_SIZE);
                textY = y + POINT_SIZE / 2;
            }
            g.setColor(palette.get("text"));
            g.setFont(font);
            g.drawString(text, textX, textY);

            // now the same for the actual attribute value
            x = centerX + (int)(Math.cos(radians) * (value + CENTER_STEPS) * CIRCLE_STEP / 2);
            y = centerY + (int)(Math.sin(radians) * (value + CENTER_STEPS) * CIRCLE_STEP/ 2);
            polygon.addPoint(x, y);
            g.setColor(palette.get("highlight"));
            g.drawLine(centerX, centerY, x, y);
            g.setColor(palette.get("base"));
            g.fillOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
            g.setColor(palette.get("highlight"));
            g.drawOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);

            i++;
        }

        g.setColor(palette.get("circle"));
        g.fillOval(centerX - (CENTER_STEPS / 2 * CIRCLE_STEP), centerY - (CENTER_STEPS / 2 * CIRCLE_STEP), CIRCLE_STEP * CENTER_STEPS, CIRCLE_STEP * CENTER_STEPS);
        g.setColor(palette.get("highlight"));
        g.drawOval(centerX - (CENTER_STEPS / 2 * CIRCLE_STEP), centerY - (CENTER_STEPS / 2 * CIRCLE_STEP), CIRCLE_STEP * CENTER_STEPS, CIRCLE_STEP * CENTER_STEPS);
        
    }

    private static int getValue(int value) {
        if (value < 0) {
            return (int)(Math.random() * 20 + 1);
        }
        return Math.min(20, Math.max(1, value));
    }


}
