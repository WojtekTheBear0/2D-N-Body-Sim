package nbody.PhysicsEngine; 

import javafx.geometry.Point2D;

public class VerletObject {
    private Point2D Position;
    private Point2D OldPosition;
    private Point2D Accel;
    private float radius;
    private float mass;

    //Initalizers 
    public VerletObject(Point2D Pos, float rad, float Mass)
    {
        this.Position = Pos;
        this.radius = rad;
        this.mass = Mass;
    }

    //Default 
    public VerletObject()
    {
        this.Position = new Point2D(100.0, 100.0);
        radius = 10.0f;
        mass = 1.0f;
    }


    //Verlet Intergration Update 
    public void update()
    {

    };



    
    // functions you'll prob have to ask me about 
    public void addVelocity(Point2D force, float dt)
    {

    }

    public void SetVelocity(Point2D force, float dt)
    {

    }

    //Getters & Setters
    /* 
    public Point2D getPosition() { return }
    public float getRadius() { return  }
    public float getMass() { return  }
    public Point2D getAcceleration() { return }
    public Point2D GetForce() {return }
    public Point2D getVelo() { return }

    public void SetPosition() { return }
    public void SetRadius() { return  }
    public void SetMass() { return  }
    public Void SetAcceleration() { return }



*/
}
