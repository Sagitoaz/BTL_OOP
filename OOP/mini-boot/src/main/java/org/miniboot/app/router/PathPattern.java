package org.miniboot.app.router;
import org.miniboot.app.AppConfig;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Collections.min;

enum Type{STATIC, VARIABLE};
class Segment{
    Type type;
    String token;
    Segment(Type type, String token) {
        this.type = type;
        this.token = token;
    }
}
// Class Dung de kiem tra pattern va path
public class PathPattern {

    private String pattern;
    private List<Segment> segments;

    public PathPattern(String _pattern) {

        this.pattern = _pattern;
        this.pattern = pathNormalized(pattern);
        segments = parsePath();



    }
    // Chuan hoa pattern ve dang /.../
    private String pathNormalized(String path) {

        if(path == null || path.isEmpty()) return "/";
        path = path.replaceAll("/{2,}", "/");
        StringBuilder pathBuilder = new StringBuilder(path);

        if(pathBuilder.charAt(0) != '/') {
            pathBuilder.insert(0, '/');
        }
        if (pathBuilder.length() > 1  && pathBuilder.charAt(pathBuilder.length()-1) == '/') {
            pathBuilder.deleteCharAt(pathBuilder.length()-1);
        }


        return pathBuilder.toString();
    }
    // Bien pattern ve dang list gom cac bien khong co /
    private List<Segment> parsePath() {
        if(this.pattern.equals("/")){
            return new ArrayList<Segment>();
        }
        // Tach cac / ra ngoai
        String[] paths = this.pattern.split("/");
        List<Segment> _segments = new ArrayList<Segment>();
        for (String path : paths) {
            Segment segment = null;
            // Tao cac segment
            if (!path.isEmpty()) {
                if(path.charAt(0) == '{' && path.charAt(path.length()-1) == '}'){
                    segment = new Segment(Type.VARIABLE, path.substring(1, path.length()-1));
                }
                else{
                    segment = new Segment(Type.STATIC, path);
                }
                _segments.add(segment);
            }

        }
        return _segments;

    }
    private ArrayList<String> splitPath(String[] splitPaths){
        ArrayList<String> paths = new ArrayList<>();

        for(int i = 0; i < splitPaths.length; i++){
            if(!splitPaths[i].isEmpty()){
                int q = splitPaths[i].indexOf('?');
                int f = splitPaths[i].indexOf('#');
                if(q < 0){
                    q = AppConfig.MAX_INTEGER_VALUE;
                }
                if(f < 0){
                    f = AppConfig.MAX_INTEGER_VALUE;
                }
                int cut =Math.min(q, f);
                if(cut < AppConfig.MAX_INTEGER_VALUE){
                    splitPaths[i] = splitPaths[i].substring(0, cut);
                }

                paths.add(splitPaths[i]);
            }
        }
        return paths;
    }
    // Kiem tra xem pattern hien tai co match voi path truyen vao khong
    public boolean match(String path) {
        path = pathNormalized(path);

        ArrayList<String> paths = splitPath(path.trim().split("/"));
        // Khong match neu khong cung do dai
        if(paths.size() != segments.size()) return false;
        for(int i = 0; i < paths.size(); i++){
            String token = paths.get(i);
            if (!segments.get(i).token.equals(token) && segments.get(i).type == Type.STATIC) {
                return false;
            }

        }
        return true;
    }
    // Lay cac bien tu gia tri thuc te khi type = var
    public Map<String, String> extract(String path) {
        path = pathNormalized(path);

        ArrayList<String> paths = splitPath(path.trim().split("/"));


        Map<String, String> extractResult = new HashMap<>();
        if (paths.size() != segments.size()) return extractResult;

        for(int i = 0; i < paths.size(); i++){
            Segment segment = segments.get(i);
            if(segment.type == Type.VARIABLE){
                // Ten Bien trong pattern goc
                String name = segment.token;
                // Gia tri cua bien trong thuc te
                String value = URLDecoder.decode(paths.get(i), StandardCharsets.UTF_8);
                extractResult.put(name, value);
            }
        }
        return extractResult;

    }
    public int score(){
        int score = 0;
        for(int i = 0; i < segments.size(); i++){
            score += (segments.get(i).type == Type.VARIABLE) ? 10 : 100;
        }
        return score;
    }
}

