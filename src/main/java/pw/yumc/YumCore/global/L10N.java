package pw.yumc.YumCore.global;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.config.FileConfig;
import pw.yumc.YumCore.config.ext.YumConfig;

/**
 * 本地化工具类
 *
 * @since 2015年12月14日 下午1:33:52
 * @author 喵♂呜
 */
public class L10N {
    private static String CONFIG_NAME = "Item_zh_CN.yml";
    private static FileConfig custom;
    private static Map<String, String> content;

    static {
        content = new HashMap<>();
        Log.info("异步初始化本地化工具...");
        load();
    }

    private L10N() {
    }

    /**
     * 获取物品完整汉化名称(包括原版)
     *
     * @param i
     *            物品实体
     * @return 物品名称
     */
    public static String getFullName(ItemStack i) {
        return getItemName(getItemType(i)) + (i.hasItemMeta() && i.getItemMeta().hasDisplayName() ? "§r(" + i.getItemMeta().getDisplayName() + "§r)" : "");
    }

    /**
     * 获取物品汉化名称
     *
     * @param i
     *            物品实体
     * @return 物品名称
     */
    public static String getItemName(ItemStack i) {
        return getItemName(getItemType(i));
    }

    /**
     * 获取物品汉化名称(优先显示名称)
     *
     * @param i
     *            物品实体
     * @return 物品名称
     */
    public static String getName(ItemStack i) {
        return i.hasItemMeta() && i.getItemMeta().hasDisplayName() ? i.getItemMeta().getDisplayName() : getItemName(getItemType(i));
    }

    /**
     * 重载LocalUtil
     */
    public static void reload() {
        Log.info("异步重载本地化工具...");
        content.clear();
        load();
    }

    /**
     * 获取物品汉化名称
     *
     * @param iname
     *            物品类型名称
     * @return 物品名称
     */
    private static String getItemName(String iname) {
        String aname = content.get(iname);
        if (aname == null) {
            aname = iname;
            if (custom != null) {
                custom.set(iname, iname);
                custom.save();
            }
        }
        return aname;
    }

    /**
     * 获取物品类型名称
     *
     * @param i
     *            物品实体
     * @return 物品类型
     */
    private static String getItemType(ItemStack i) {
        String name = i.getType().name();
        String dura = "";
        if (i.getType() == Material.MONSTER_EGG) {
            name = ((SpawnEgg) i.getData()).getSpawnedType().name();
        } else {
            int dur = i.getDurability();
            dura = (i.getMaxStackSize() != 1 && dur != 0) ? Integer.toString(dur) : "";
        }
        return (name + (dura.isEmpty() ? "" : "-" + dura)).toUpperCase();
    }

    /**
     * 载入数据
     */
    private static void load() {
        new Thread(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                try {
                    Map<String, String> local = YumConfig.getLocal(CONFIG_NAME).getContentMap();
                    if (local != null) {
                        Log.info("本地汉化文件词条数量: " + local.size());
                        content.putAll(local);
                    }
                    Map<String, String> remote = YumConfig.getRemote(CONFIG_NAME).getContentMap();
                    if (remote != null) {
                        Log.info("远程汉化文件词条数量: " + remote.size());
                        content.putAll(remote);
                    }
                    Log.info("本地化工具初始化完毕...");
                } catch (Exception e) {
                    Log.warning(String.format("本地化工具初始化失败: %s %s", e.getClass().getName(), e.getMessage()));
                    Log.d(CONFIG_NAME, e);
                }
            }
        }).start();
    }
}
