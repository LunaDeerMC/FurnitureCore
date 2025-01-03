package cn.lunadeer.furnitureCore.functionality;

import cn.lunadeer.furnitureCoreApi.functionality.Illumination;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;

public class IlluminationFunction implements Illumination {

    private final Integer lightLevel;
    private final Boolean switchable;

    public IlluminationFunction(@NotNull JSONObject functionObject) {
        this.lightLevel = functionObject.containsKey("light_level") ? functionObject.getInteger("light_level") : 0;
        this.switchable = functionObject.containsKey("switchable") && functionObject.getBoolean("switchable");
    }


    @Override
    public Integer getLightLevel() {
        return this.lightLevel;
    }

    @Override
    public Boolean switchable() {
        return this.switchable;
    }
}
