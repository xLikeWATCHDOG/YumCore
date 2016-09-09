package pw.yumc.YumCore.tellraw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @since 2016年9月9日 下午3:47:17
 * @author 喵♂呜
 */
public abstract class ItemSerialize {
    static ItemSerialize itemSerialize;
    static {
        try {
            itemSerialize = new Automatic();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            itemSerialize = new Manual();
        }
    }

    public static String $(final ItemStack item) {
        return itemSerialize.parse(item);
    }

    public abstract String parse(final ItemStack item);

    static class Automatic extends ItemSerialize {
        Method asNMSCopyMethod;
        Method nmsSaveNBTMethod;
        Class<?> nmsNBTTagCompound;
        String ver = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

        public Automatic() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
            final Class<?> cis = getOBCClass("inventory.CraftItemStack");
            asNMSCopyMethod = cis.getMethod("asNMSCopy", ItemStack.class);
            final Class<?> nmsItemStack = getNMSClass("ItemStack");
            nmsNBTTagCompound = getNMSClass("NBTTagCompound");
            nmsSaveNBTMethod = nmsItemStack.getMethod("save", nmsNBTTagCompound);
        }

        public Class<?> getNMSClass(final String cname) throws ClassNotFoundException {
            return Class.forName("net.minecraft.server" + ver + "." + cname);
        }

        public Class<?> getOBCClass(final String cname) throws ClassNotFoundException {
            return Class.forName("org.bukkit.craftbukkit." + ver + "." + cname);
        }

        @Override
        public String parse(final ItemStack item) {
            try {
                return nmsSaveNBTMethod.invoke(asNMSCopyMethod.invoke(null, item), nmsNBTTagCompound.newInstance()).toString();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                itemSerialize = new Manual();
                return itemSerialize.parse(item);
            }
        }
    }

    static class JsonBuilder {
        public static final String[] REPLACEMENT_CHARS;
        static {
            REPLACEMENT_CHARS = new String[128];
            for (int i = 0; i <= 0x1f; i++) {
                REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
            }
            REPLACEMENT_CHARS['"'] = "\\\"";
            REPLACEMENT_CHARS['\\'] = "\\\\";
            REPLACEMENT_CHARS['\t'] = "\\t";
            REPLACEMENT_CHARS['\b'] = "\\b";
            REPLACEMENT_CHARS['\n'] = "\\n";
            REPLACEMENT_CHARS['\r'] = "\\r";
            REPLACEMENT_CHARS['\f'] = "\\f";
        }
        StringBuffer json;

        public JsonBuilder() {
            json = new StringBuffer();
        }

        public void append(final String value) {
            int last = 0;
            final int length = value.length();
            for (int i = 0; i < length; i++) {
                final char c = value.charAt(i);
                String replacement;
                if (c < 128) {
                    replacement = REPLACEMENT_CHARS[c];
                    if (replacement == null) {
                        continue;
                    }
                } else if (c == '\u2028') {
                    replacement = "\\u2028";
                } else if (c == '\u2029') {
                    replacement = "\\u2029";
                } else {
                    continue;
                }
                if (last < i) {
                    json.append(value, last, i - last);
                }
                json.append(replacement);
                last = i + 1;
            }
            if (last < length) {
                json.append(value, last, length - last);
            }
        }

        public void deleteCharAt(final int length) {
            json.deleteCharAt(length);
        }

        public int length() {
            return json.length();
        }
    }

    static class Manual extends ItemSerialize {

        @Override
        public String parse(final ItemStack item) {
            return serialize(item);
        }

        /**
         * 显示序列化
         *
         * @param im
         *            物品属性
         * @return 获取显示序列化
         */
        private String getDisplay(final ItemMeta im) {
            final JsonBuilder display = new JsonBuilder();
            display.append("{");
            if (im.hasDisplayName()) {
                display.append(String.format("Name:\"%s\",", im.getDisplayName()));
            }
            if (im.hasLore()) {
                display.append("Lore:[");
                for (final String line : im.getLore()) {
                    display.append(String.format("\"%s\",", line));
                }
                display.deleteCharAt(display.length());
                display.append("],");
            }
            display.deleteCharAt(display.length());
            display.append("}");
            return display.toString();
        }

        /**
         * 附魔序列化
         *
         * @param set
         *            附魔集合
         * @return 获得附魔序列化
         */
        private String getEnch(final Set<Entry<Enchantment, Integer>> set) {
            final StringBuffer enchs = new StringBuffer();
            for (final Map.Entry<Enchantment, Integer> ench : set) {
                enchs.append(String.format("{id:%s,lvl:%s},", ench.getKey().getId(), ench.getValue()));
            }
            enchs.deleteCharAt(enchs.length());
            return enchs.toString();
        }

        /**
         * 属性序列化
         *
         * @param im
         *            物品属性
         * @return 获得属性序列化
         */
        private String getTag(final ItemMeta im) {
            final StringBuffer meta = new StringBuffer();
            if (im.hasEnchants()) {
                meta.append(String.format("ench:[%s],", getEnch(im.getEnchants().entrySet())));
            }
            im.getItemFlags();
            if (im.hasDisplayName() || im.hasLore()) {
                meta.append(String.format("display:%s,", getDisplay(im)));
            }
            meta.deleteCharAt(meta.length());
            return meta.toString();
        }

        /**
         * 序列化物品
         *
         * @param item
         *            {@link ItemStack}
         * @return 物品字符串
         */
        private String serialize(final ItemStack item) {
            final StringBuffer json = new StringBuffer("{");
            json.append(String.format("id:\"%s\",Damage:\"%s\"", item.getTypeId(), item.getDurability()));
            if (item.getAmount() > 1) {
                json.append(String.format(",Count:\"%s\"", item.getAmount()));
            }
            if (item.hasItemMeta()) {
                json.append(String.format(",tag:%s", getTag(item.getItemMeta())));
            }
            json.append("}");
            return json.toString();
        }
    }
}
