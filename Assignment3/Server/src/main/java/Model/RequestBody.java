package Model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class RequestBody {
    private Integer time = null;
    private Integer liftID = null;
    private Integer waitTime = null;

    public RequestBody() {
    }

    public RequestBody(Integer time, Integer liftID, Integer waitTime) {
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
    }

    public Integer getTime() {
        return time;
    }

    public Integer getLiftID() {
        return liftID;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
            "time=" + time +
            ", liftID=" + liftID +
            ", waitTime=" + waitTime +
            '}';
    }
}
