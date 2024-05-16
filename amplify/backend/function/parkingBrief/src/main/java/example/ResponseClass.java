

package example;

import java.util.ArrayList;
import java.util.List;


import main.java.example.Maps;

public class ResponseClass {
    private ArrayList maps;
    
    public ArrayList getMaps() {
        return maps;
    }
    
    public void setMaps(ArrayList maps) {
        this.maps = maps;
    }

    public ResponseClass(ArrayList maps){
        this.maps = maps;
    }
    }