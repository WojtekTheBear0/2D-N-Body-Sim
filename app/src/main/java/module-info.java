module nbody {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    exports nbody;
    exports nbody.PhysicsEngine;
    exports nbody.gui;
}