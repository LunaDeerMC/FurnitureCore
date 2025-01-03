package cn.lunadeer.furnitureCore.functionality;

import cn.lunadeer.furnitureCoreApi.functionality.Chair;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;

public class ChairFunction implements Chair {

    private final Float height;

    public ChairFunction(@NotNull JSONObject functionObject) {
        this.height = functionObject.containsKey("height") ? functionObject.getFloat("height") : 0.0f;
    }

    @Override
    public Float getHeight() {
        return this.height;
    }
}
