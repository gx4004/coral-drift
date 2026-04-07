module com.coraldrift {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires java.prefs;
    requires java.desktop;
    
    exports com.coraldrift;
    exports com.coraldrift.core;
    exports com.coraldrift.entity;
    exports com.coraldrift.graphics;
    exports com.coraldrift.scene;
    exports com.coraldrift.spawner;
    exports com.coraldrift.ui;
    exports com.coraldrift.audio;
    exports com.coraldrift.util;
}
