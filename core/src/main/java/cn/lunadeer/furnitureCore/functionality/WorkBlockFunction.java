package cn.lunadeer.furnitureCore.functionality;

import cn.lunadeer.furnitureCoreApi.functionality.WorkBlock;
import com.alibaba.fastjson.JSONObject;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class WorkBlockFunction implements WorkBlock {


    public WorkBlockFunction(@NotNull JSONObject functionObject) {

    }

    @Override
    public Inventory getBlock() {
        return null;
    }
}
