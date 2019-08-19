package pw.brock.mmdn.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BrockWS
 */
public class MLVersion extends Version {

    public List<Library> libraries = new ArrayList<>();
    public Map<String, String> mainClass = new HashMap<>();
    public Map<String, List<String>> tweakers = new HashMap<>();

    @Override
    public String toString() {
        return super.toString() + "|" + this.libraries.toString();
    }
}
