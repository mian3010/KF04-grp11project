package dk.itu.grp11.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import dk.itu.grp11.data.Parser;
import dk.itu.grp11.data.Point;
import dk.itu.grp11.data.Road;
import dk.itu.grp11.enums.MapBound;
import dk.itu.grp11.enums.RoadType;
import dk.itu.grp11.enums.TrafficDirection;
import dk.itu.grp11.util.DimensionalTree;
import dk.itu.grp11.util.Interval;
import dk.itu.grp11.util.Interval2D;

public class ParserTest {
  //Testing to see whether (some) points get the right id and values assigned or not
  @Test
  public void test0() throws IOException {
    HashMap<Integer, Point> points = Parser.getParser().points();
    
    //The first point
    assertEquals(1, points.get(1).getID());
    assertEquals(595527.51786, points.get(1).getX(), 0);
    assertEquals(6402050.98297, points.get(1).getY(), 0);
    
    //The last point
    assertEquals(675902, points.get(points.size()).getID());
    assertEquals(692067.66450, points.get(points.size()).getX(), 0);
    assertEquals(6049914.43018, points.get(points.size()).getY(), 0);
  }
  /*
  //Testing if all roads, with zoom level 1, are included at zoom level 1
  @Test
  public void test1() throws IOException {
    dk.itu.grp11.data.Parser p = Parser.getTestParser(new File("test_points.txt"), new File("test_roads.txt"));
    
    // Road types to count
    HashSet<RoadType> roadTypes = new HashSet<>();
    roadTypes.add(RoadType.MOTORVEJ);
    roadTypes.add(RoadType.MOTORTRAFIKVEJ);
    roadTypes.add(RoadType.PROJ_MOTORVEJ);
    roadTypes.add(RoadType.PROJ_MOTORTRAFIKVEJ);
    roadTypes.add(RoadType.MOTORVEJSTUNNEL);
    roadTypes.add(RoadType.MOTORTRAFIKVEJSTUNNEL);
    roadTypes.add(RoadType.FAERGEFORBINDELSE);
    
    int roadCount = 0;
    
    // Count number of roads for every road type
    for(RoadType rt : roadTypes) {
      Interval<Double, RoadType> intervalX = new Interval<Double, RoadType>(Parser.getParser().mapBound(MapBound.MINX), Parser.getParser().mapBound(MapBound.MAXX), rt);
      Interval<Double, RoadType> intervalY = new Interval<Double, RoadType>(Parser.getParser().mapBound(MapBound.MINY), Parser.getParser().mapBound(MapBound.MAXY), rt);
      Interval2D<Double, RoadType> rect = new Interval2D<Double, RoadType>(intervalX, intervalY);
      HashSet<Road> roadsInViewbox = roads.query2D(rect);
      roadCount += roadsInViewbox.size();
    }
    
    // Expecting all roads with zoom level 1 to be included
    assertEquals(countRoads(
        RoadType.MOTORVEJ,
        RoadType.MOTORTRAFIKVEJ,
        RoadType.PROJ_MOTORVEJ,
        RoadType.PROJ_MOTORTRAFIKVEJ,
        RoadType.MOTORVEJSTUNNEL,
        RoadType.MOTORTRAFIKVEJSTUNNEL,
        RoadType.FAERGEFORBINDELSE), roadCount);
  }
  
  //Counts number of roads directly from file
  public static int countRoads(RoadType... roadTypes) throws IOException {
    //Road types to look for
    HashSet<RoadType> roadTypesToFind = new HashSet<>();
    for(RoadType rt : roadTypes) {
      roadTypesToFind.add(rt);
    }
    
    // Reading all points
    dk.itu.grp11.data.Parser p = new Parser(new File("kdv_node_unload.txt"), new File("kdv_unload.txt"));
    HashMap<Integer, Point> points = p.points();
    
    // Counting roads
    File f = new File("kdv_unload.txt");
    BufferedReader input = new BufferedReader(new FileReader(f));
    String line = input.readLine(); line = input.readLine();
    int count = 0;
    while(line != null) {
      String[] r = line.split(",");
      if(roadTypesToFind.contains(RoadType.getById(Integer.parseInt(r[5])))) count++;
      // Subtracting roads where x and y coordinates are equal
      if(roadTypesToFind.contains(RoadType.getById(Integer.parseInt(r[5]))) &&
         points.get(Integer.parseInt(r[0])).getX() == points.get(Integer.parseInt(r[1])).getX() &&
         points.get(Integer.parseInt(r[0])).getY() == points.get(Integer.parseInt(r[1])).getY()) count--;
      line = input.readLine();
    }
    return count;
  }*/
  
  @Test
  public void testRandom() {
    System.out.println("TEST");
    for(Entry<String, Road> entry : Parser.getParser().filterPrefix("Hånd").entrySet()) {
      System.out.println(entry.getValue().getName());
    }
  }
}
