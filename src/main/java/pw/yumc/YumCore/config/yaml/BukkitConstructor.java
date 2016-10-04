package pw.yumc.YumCore.config.yaml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import pw.yumc.YumCore.bukkit.L;

public class BukkitConstructor extends YamlConstructor {
    Map<String, Method> constructor = new HashMap<>();

    public BukkitConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
        this.loadConstructor();
    }

    private void loadConstructor() {
        constructor.put(Location.class.getName(), L.deserialize);
    }

    private class ConstructCustomObject extends ConstructYamlMap {
        @Override
        public Object construct(final Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }

            final Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            if (raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                final Map<String, Object> typed = new LinkedHashMap<>(raw.size());
                for (final Map.Entry<?, ?> entry : raw.entrySet()) {
                    typed.put(entry.getKey().toString(), entry.getValue());
                }

                // 自定义解析部分
                final String key = raw.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY).toString();
                if (constructor.containsKey(key)) {
                    try {
                        return constructor.get(key).invoke(null, typed);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new YAMLException("Could not deserialize object", ex);
                    }
                }

                // Bukkit自动解析
                try {
                    return ConfigurationSerialization.deserializeObject(typed);
                } catch (final IllegalArgumentException ex) {
                    throw new YAMLException("Could not deserialize object", ex);
                }
            }

            return raw;
        }

        @Override
        public void construct2ndStep(final Node node, final Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }
    }
}
