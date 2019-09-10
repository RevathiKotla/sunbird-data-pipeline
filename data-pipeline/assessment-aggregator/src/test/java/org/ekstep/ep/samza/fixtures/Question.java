package org.ekstep.ep.samza.fixtures;

import com.datastax.driver.mapping.annotations.UDT;

import java.util.List;
import java.util.Map;

@UDT(keyspace = "sunbird_courses", name ="question")
public class Question {
    private String id;
    private int maxscore;
    private List<Map<String,Object>> params;
    private String title;
    private String type;
    private String desc;

/*    public Question(String id, int maxscore, List<Map<String, Object>> params, String title, String type, String desc) {
        this.id = id;
        this.maxscore = maxscore;
        this.params = params;
        this.title = title;
        this.type = type;
        this.desc = desc;
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxscore() {
        return maxscore;
    }

    public void setMaxscore(int maxscore) {
        this.maxscore = maxscore;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
