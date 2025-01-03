package cn.lunadeer.furnitureCore.functionality;

import cn.lunadeer.furnitureCoreApi.functionality.Storage;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;

public class StorageFunction implements Storage {

    private final Integer size;

    public StorageFunction(@NotNull JSONObject functionObject) {
        this.size = functionObject.containsKey("size") ? functionObject.getInteger("size") : 9;
    }
    
    @Override
    public Integer getSize() {
        return this.size;
    }
}
