package pw.brock.mmdn.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pw.brock.mmdn.util.Log;

import com.google.gson.annotations.SerializedName;

/**
 * @author BrockWS
 */
public class UpstreamPackage extends Package {

    @SerializedName("@detectors")
    public List<Detector> detectors = Collections.emptyList();

    @Override
    public boolean verify() {
        AtomicBoolean valid = new AtomicBoolean(true);
        for (Detector detector : this.detectors) {
            if (detector.type.isEmpty()) {
                Log.error("detector type string is empty!");
                valid.set(false);
            }
            detector.data.forEach((k, v) -> {
                if (k.isEmpty()) {
                    Log.error("detector key is empty!");
                    valid.set(false);
                }
                if (v.isEmpty()) {
                    Log.error("detector value is empty!");
                    valid.set(false);
                }
            });
        }
        return valid.get() && super.verify();
    }

    public List<Detector> detectors() {
        return this.detectors;
    }

    public static class Detector {
        public String type;
        public Map<String, String> data = new HashMap<>();

        public String type() {
            return this.type;
        }

        public Map<String, String> data() {
            return this.data;
        }
    }
}
