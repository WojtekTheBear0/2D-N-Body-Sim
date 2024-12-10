package nbody.PhysicsEngine; 

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class VerletObject {
    private Point2D Position;
    private Point2D OldPosition;
    private Point2D Accel;
    private float radius;
    private float mass;
    private Color color;


    //Initalizers 
    public VerletObject(Point2D Pos, float rad, float Mass)
    {
        this.Position = Pos;
        this.radius = rad;
        this.mass = Mass;
        this.color = Color.SKYBLUE; // Default color

    }


    //Default 
    public VerletObject()
    {
        this.Position = new Point2D(100.0, 100.0);
        radius = 10.0f;
        mass = 1.0f;
        this.color = Color.SKYBLUE; // Default color

    }


    //Verlet Intergration Update 
    public void update(float dt) {
        if (OldPosition == null) {
            OldPosition = Position;
        }
        if (Accel == null) {
            Accel = new Point2D(0, 0);
        }
        
        // Calculate last update movement
        Point2D lastUpdateMove = Position.subtract(OldPosition);
        
        // Define damping constant
        final float VELOCITY_DAMPING = 100.0f;
        
        // Calculate new position with damping
        Point2D newPosition = Position
            .add(lastUpdateMove)
            .add(Accel.subtract(lastUpdateMove.multiply(VELOCITY_DAMPING))
            .multiply(dt * dt));
        
        OldPosition = Position;
        Position = newPosition;
        resetAcceleration();
    }
    
    
    // functions you'll prob have to ask me about 
    public void addVelocity(Point2D force, float dt)
    {
        OldPosition = OldPosition.subtract(force.multiply(dt));
    }


    public void SetVelocity(Point2D force, float dt)
    {
        OldPosition = Position.subtract((force.multiply(dt)));
    }


    //Getters & Setters 
    public Point2D getPosition() { return Position; }
    public float getRadius() { return radius; }
    public float getMass() { return mass; }
    public Point2D getAcceleration() { return Accel; }
    public Point2D getForce() { return Accel.multiply(mass); }

    public Point2D getVelo(float dt) { return Position.subtract(OldPosition).multiply(1.0 / dt); }

    public void SetPosition(Point2D pos) { Position = pos; }
    public void SetRadius(float r) { radius = r; }
    public void SetMass(float m) { mass = m; }
    public void SetAcceleration(Point2D a) { Accel = a; }
    

    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color != null ? color : Color.SKYBLUE; // Default color if none set
    }


    public void SetPreviousPosition(Point2D prevPos) {
        OldPosition = prevPos;
    }
    
    public void AddAcceleration(Point2D a) {
        if (Accel == null) {
            Accel = a;
        } else {
            Accel = Accel.add(a); // Accumulate accelerations
        }
    }

    public void resetAcceleration() {
        Accel = new Point2D(0, 0);
    }



    public float getPositionX() {
        return (float) Position.getX();
    }

    public float getPositionY() {
        return (float) Position.getY();
    }

    
    public void setPositionX(float x) {
        Position = new Point2D(x, Position.getY());
    }

    public void setPositionY(float y) {
        Position = new Point2D(Position.getX(), y);
    }



}
