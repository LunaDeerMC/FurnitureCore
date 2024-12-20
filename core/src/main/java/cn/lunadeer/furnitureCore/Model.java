package cn.lunadeer.furnitureCore;

import com.alibaba.fastjson.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Model {

    public static Model loadModel(File modelFile) throws Exception{
        Model model = new Model();

        // 1. unzip the file to cache directory

        // 2. valid the json file exists then parse it to modelJson

        // 3. check if custom_name exists in json file then set it to model

        // 4. valid the texture referenced in json file exists then load it to textures
        //      - if texture under the directory, rename path to dir1_dir2_texture_name
        //      - change the texture reference in json file too if needed

        // 5. clean the cache directory in finally block

        return model;
    }

    private Integer index;
    private String customName;
    private String modelName;
    private JSONObject modelJson;
    private final Map<String, BufferedImage> textures = new HashMap<>();


}
