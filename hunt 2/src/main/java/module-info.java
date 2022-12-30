module oop.hunt {
    requires javafx.controls;
//    requires javafx.fxml;
    requires javafx.media;

    opens oop.hunt to javafx.fxml;
    exports oop.hunt;
}
