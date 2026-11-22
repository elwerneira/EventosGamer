package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.junit.jupiter.api.Test;

class DependencySecurityTests {
    @Test void strictJsonRejectsAnEmbeddedEndOfInputBeforeTrailingGarbage() throws Exception {
        assertThat(JSONValue.parseStrict("{\"score\":5}")).isNotNull();
        String malformed = "1" + (char) 0x1A + "GARBAGE";
        assertThat(JSONValue.isValidJsonStrict(malformed)).isFalse();
        assertThatThrownBy(() -> JSONValue.parseStrict(malformed)).isInstanceOf(ParseException.class);
    }
}
