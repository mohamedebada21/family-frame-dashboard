import com.familyframe.dashboard.NetworkPolicy;

public final class NetworkPolicyTest {
    public static void main(String[] args) {
        String[] accepted = {
            "https://api.open-meteo.com/v1/forecast?latitude=40.7&longitude=-73.8",
            "https://geocoding-api.open-meteo.com/v1/search?name=Queens",
            "https://api.aladhan.com/v1/calendar/2026/11?method=2"
        };
        String[] rejected = {
            "http://api.open-meteo.com/v1/forecast",
            "https://api.open-meteo.com.attacker.test/v1/forecast",
            "https://api.open-meteo.com@attacker.test/v1/forecast",
            "https://user:password@api.open-meteo.com/v1/forecast",
            "https://api.open-meteo.com:8768/v1/forecast",
            "https://api.open-meteo.com/v1/forecast#fragment",
            "https://api.aladhan.com/v1/other/2026/11",
            "https://api.aladhan.com/v1/calendar/2026/11/extra",
            "https://127.0.0.1/v1/forecast", "file:///sdcard/secret", "not a URI", null
        };
        for (String value : accepted) {
            if (!NetworkPolicy.permits(value)) throw new AssertionError("Blocked expected API " + value);
        }
        for (String value : rejected) {
            if (NetworkPolicy.permits(value)) throw new AssertionError("Allowed unexpected URL " + value);
        }
        System.out.println("Passed native API allowlist and URL escape checks");
    }
}
