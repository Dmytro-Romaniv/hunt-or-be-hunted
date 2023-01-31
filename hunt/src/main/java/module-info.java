/**
 * why waste time create lot class when few class do trick
 * @author Dmytro Romaniv
 */
module oop.hunt.part2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens oop.hunt.part2 to javafx.fxml;
    exports oop.hunt.part2;
}