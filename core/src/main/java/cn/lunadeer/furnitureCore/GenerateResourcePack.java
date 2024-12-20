package cn.lunadeer.furnitureCore;

public class GenerateResourcePack {


    // 1. prepare resource_pack directory in cache directory (cache/resource_pack)

    // 2. copy pre-defined files to the resource_pack directory
    //      - pack.mcmeta (need to rename from pack.mcmeta.json)
    //      - pack.png
    //      - assets (dir)

    // 3. for each model (get from ModelManager)

    //      1. save the texture to the resource_pack/assets/furniture_core/textures/ with the name (<model_name>_<raw_texture_name>)
    //      2. change the texture reference in model json file to the new name (furniture_core:<model_name>_<raw_texture_name>)
    //      3. save the model json file to the resource_pack/assets/furniture_core/models/ with the name (<model_name>.json)
    //      4. add the item_frame model overrides[]:
    //                  predicate.custom_model_data: model.index
    //                  model: furniture_core:<model_name>

}
