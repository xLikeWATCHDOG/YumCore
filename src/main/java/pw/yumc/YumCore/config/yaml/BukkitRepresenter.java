package pw.yumc.YumCore.config.yaml;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.nodes.Node;

import pw.yumc.YumCore.bukkit.L;

public class BukkitRepresenter extends YamlRepresenter {
    public BukkitRepresenter() {
        this.multiRepresenters.put(Location.class, new RepresentLocation());
    }

    public class RepresentLocation extends RepresentMap {
        @Override
        public Node representData(final Object data) {
            return super.representData(L.serialize((Location) data));
        }
    }
}
