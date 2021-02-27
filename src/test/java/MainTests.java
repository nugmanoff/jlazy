import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTests {

    @BeforeEach
    void setup() {
        
    }

    @AfterEach
    void tearDown() {

    }


    @Test
    void addition() {
        Main.main(new String[]{"--dir", "test-temp/"});
        assertEquals(2, 2);
    }
}
