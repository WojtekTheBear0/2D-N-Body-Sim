package nbody.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
public class QuadTree<T> {
    private static final int MAX_OBJECTS = 4;
    private static final int MAX_LEVELS = 8;

    private int level;
    private List<T> objects;
    private Rectangle bounds;
    private QuadTree<T>[] nodes;
    private final QuadTreeObject<T> objectHelper;


        public interface QuadTreeObject<T> {
        Point2D getPosition(T object);
        float getRadius(T object);
        }
        public static class Rectangle {
            double x, y, width, height;
    
            public Rectangle(double x, double y, double width, double height) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }
    
            public boolean contains(Point2D point) {
                return point.getX() >= x && 
                       point.getX() <= x + width && 
                       point.getY() >= y && 
                       point.getY() <= y + height;
            }
    
            public boolean intersectsCircle(Point2D center, float radius) {
                double closestX = Math.max(x, Math.min(center.getX(), x + width));
                double closestY = Math.max(y, Math.min(center.getY(), y + height));
                
                double distanceX = center.getX() - closestX;
                double distanceY = center.getY() - closestY;
                
                return (distanceX * distanceX + distanceY * distanceY) <= (radius * radius);
            }
        }

        public QuadTree(int level, Rectangle bounds, QuadTreeObject<T> helper) {
            this.level = level;
            this.bounds = bounds;
            this.objects = new ArrayList<>();
            this.nodes = new QuadTree[4];
            this.objectHelper = helper;
        }
    
        public void clear() {
            objects.clear();
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    nodes[i].clear();
                    nodes[i] = null;
                }
            }
        }
    

        private void split() {
            double subWidth = bounds.width / 2;
            double subHeight = bounds.height / 2;
            double x = bounds.x;
            double y = bounds.y;
    
            nodes[0] = new QuadTree<>(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight), objectHelper);
            nodes[1] = new QuadTree<>(level + 1, new Rectangle(x, y, subWidth, subHeight), objectHelper);
            nodes[2] = new QuadTree<>(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight), objectHelper);
            nodes[3] = new QuadTree<>(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight), objectHelper);
        }


        private int getIndex(T object) {
            Point2D pos = objectHelper.getPosition(object);
            double verticalMidpoint = bounds.x + (bounds.width / 2);
            double horizontalMidpoint = bounds.y + (bounds.height / 2);
    
            boolean topQuadrant = pos.getY() < horizontalMidpoint;
            boolean bottomQuadrant = pos.getY() > horizontalMidpoint;
    
            if (pos.getX() < verticalMidpoint) {
                if (topQuadrant) return 1;
                else if (bottomQuadrant) return 2;
            } else if (pos.getX() > verticalMidpoint) {
                if (topQuadrant) return 0;
                else if (bottomQuadrant) return 3;
            }
    
            return -1;
        }


    public void insert(T object) {
        if (nodes[0] != null) {
            int index = getIndex(object);
            if (index != -1) {
                nodes[index].insert(object);
                return;
            }
        }

        objects.add(object);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index != -1) {
                    T obj = objects.remove(i);
                    nodes[index].insert(obj);
                } else {
                    i++;
                }
            }
        }
    }

    public void findPotentialCollisions(T object, List<T> potentialCollisions) {
        int index = getIndex(object);
        
        if (nodes[0] != null) {
            Point2D pos = objectHelper.getPosition(object);
            float radius = objectHelper.getRadius(object);
            
            for (int i = 0; i < nodes.length; i++) {
                if (i == index || nodes[i] == null) continue;
                
                if (nodes[i].bounds.intersectsCircle(pos, radius)) {
                    nodes[i].findPotentialCollisions(object, potentialCollisions);
                }
            }
            
            if (index != -1) {
                nodes[index].findPotentialCollisions(object, potentialCollisions);
            }
        }

        potentialCollisions.addAll(objects);
    }


}
