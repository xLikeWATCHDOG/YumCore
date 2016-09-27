package pw.yumc.YumCore.kit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.utility.MinecraftFields;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import pw.yumc.YumCore.utils.ReflectUtil;

public class EntityKit {

    private static Method _mGetNavigation;
    private static Method _mA3 = null;
    private static Method _mA2 = null;
    private static Method _mA1 = null;

    private static Field _fgoalSelector;
    private static Field _ftargetSelector;

    private final static Class<?> _cControllerMove = MinecraftReflection.getMinecraftClass("ControllerMove");
    private final static Class<?> _cPathEntity = MinecraftReflection.getMinecraftClass("PathEntity");
    private final static Class<?> _cEntityInsentient = MinecraftReflection.getMinecraftClass("EntityInsentient");
    private final static Class<?> _cPathfinderGoalSelector = MinecraftReflection.getMinecraftClass("PathfinderGoalSelector");

    private static Method _mPlayerList_MoveToWorld = null;
    private static Method _mPlayerConnection_Teleport = null;
    private static Field _fWorldServer_dimension = null;

    static {
        try {
            _mGetNavigation = _cEntityInsentient.getMethod("getNavigation");
            _fgoalSelector = _cEntityInsentient.getDeclaredField("goalSelector");
            _fgoalSelector.setAccessible(true);
            _ftargetSelector = _cEntityInsentient.getDeclaredField("targetSelector");
            _ftargetSelector.setAccessible(true);

            ReflectUtil.getMethodByNameAndParams(_cEntityInsentient, "getControllerMove");
            ReflectUtil.getDeclaredMethodByParams(_cControllerMove, double.class, double.class, double.class, double.class).get(0);

            final Class<?> cc = MinecraftReflection.getMinecraftClass("NavigationAbstract");
            for (final Method method : cc.getDeclaredMethods()) {
                final Class<?>[] parmeters = method.getParameterTypes();
                switch (parmeters.length) {
                case 1: {
                    if (parmeters[0] == double.class) {
                        _mA1 = method;
                    }
                    break;
                }
                case 2: {
                    if (parmeters[0] == _cPathEntity.getClass() && parmeters[1] == double.class) {
                        _mA2 = method;
                    }
                    break;
                }
                case 3: {
                    if (parmeters[0] == double.class && parmeters[1] == double.class && parmeters[2] == double.class) {
                        _mA3 = method;
                    }
                    break;
                }
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        try {
            _mPlayerList_MoveToWorld = ReflectUtil
                    .getDeclaredMethodByParamsAndType(MinecraftReflection.getPlayerListClass(),
                            MinecraftReflection.getEntityPlayerClass(),
                            MinecraftReflection.getEntityPlayerClass(),
                            Integer.TYPE,
                            Boolean.TYPE,
                            Location.class,
                            Boolean.TYPE)
                    .get(0);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 通过ID获取实体
     *
     * @param entityId
     *            实体ID
     * @return 实体
     */
    public static Entity getEntityById(final int entityId) {
        for (final World world : Bukkit.getWorlds()) {
            for (final Entity entity : world.getEntities()) {
                if (entity.getEntityId() == entityId) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * 判断实体是否在水里
     *
     * @param ent
     *            实体
     * @return 结果
     */
    public static boolean inWater(final Entity ent) {
        return ent.getLocation().getBlock().getTypeId() == 8 || ent.getLocation().getBlock().getTypeId() == 9;
    }

    /**
     * 判断实体是否隐藏
     *
     * @param entity
     *            实体
     * @return 是否隐藏
     */
    public static boolean isInvisible(final Entity entity) {
        final Object ent = ReflectUtil.getHandle(entity);
        try {
            return (boolean) ent.getClass().getMethod("isInvisible").invoke(ent);
        } catch (final Exception e) {
        }
        return false;
    }

    /**
     * 实体是否在地上
     *
     * @param ent
     *            实体
     * @return 结果
     */
    public static boolean isOnGround(final Entity ent) {
        return ent.isOnGround();
    }

    /**
     * 判断是否为禁摩状态
     *
     * @param entity
     *            实体
     * @return 结果
     */
    public static boolean isSilence(final Entity entity) {
        return WrappedDataWatcher.getEntityWatcher(entity).getByte(4) == 1;
    }

    /**
     * 判断实体是否在方块上
     *
     * @param entity
     *            实体
     * @return 结果
     */
    public static boolean onBlock(final Entity entity) {
        // Side Standing
        double xMod = entity.getLocation().getX() % 1;
        if (entity.getLocation().getX() < 0) {
            xMod += 1;
        }

        double zMod = entity.getLocation().getZ() % 1;
        if (entity.getLocation().getZ() < 0) {
            zMod += 1;
        }

        int xMin = 0;
        int xMax = 0;
        int zMin = 0;
        int zMax = 0;

        if (xMod < 0.3) {
            xMin = -1;
        }
        if (xMod > 0.7) {
            xMax = 1;
        }

        if (zMod < 0.3) {
            zMin = -1;
        }
        if (zMod > 0.7) {
            zMax = 1;
        }

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                // Standing on SOMETHING
                if (entity.getLocation().add(x, -0.5, z).getBlock().getType() != Material.AIR && !entity.getLocation().add(x, -0.5, z).getBlock().isLiquid()) {
                    return true;
                }

                // Inside a Lillypad
                if (entity.getLocation().add(x, 0, z).getBlock().getType() == Material.WATER_LILY) {
                    return true;
                }

                // Fences/Walls
                final Material beneath = entity.getLocation().add(x, -1.5, z).getBlock().getType();
                if (entity.getLocation().getY() % 0.5 == 0 && (beneath == Material.FENCE || beneath == Material.FENCE_GATE || beneath == Material.NETHER_FENCE || beneath == Material.COBBLE_WALL)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 解析实体类型
     *
     * @param str
     *            实体名称
     * @return {@link EntityType}
     */
    public static EntityType parseEntityType(final String str) {
        EntityType type = null;
        if (str != null) {
            type = EntityType.fromName(str);
            if (type == null) {
                try {
                    type = EntityType.valueOf(str.toUpperCase());
                } catch (final Exception e) {
                }
            }
        }
        return type;
    }

    /**
     * 移除全局选择器
     *
     * @param entity
     *            实体
     */
    public static void removeGoalSelectors(final Entity entity) {
        try {
            if (_fgoalSelector == null) {
                _fgoalSelector = _cEntityInsentient.getDeclaredField("goalSelector");
                _fgoalSelector.setAccessible(true);
            }
            final Object creature = ReflectUtil.getHandle(entity);
            if (_cEntityInsentient.isInstance(creature)) {
                final Object world = ReflectUtil.getHandle(entity.getWorld());
                final Object methodProfiler = world.getClass().getField("methodProfiler").get(world);
                final Object goalSelector = _cPathfinderGoalSelector.getConstructor(methodProfiler.getClass()).newInstance(methodProfiler);
                _fgoalSelector.set(creature, goalSelector);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏实体
     *
     * @param entity
     *            实体
     * @param invisible
     *            是否隐藏
     */
    public static void setInvisible(final Entity entity, final boolean invisible) {
        final Object ent = ReflectUtil.getHandle(entity);
        try {
            ent.getClass().getMethod("setInvisible", boolean.class).invoke(ent, invisible);
        } catch (final Exception e) {
        }
    }

    /**
     * 设置实体为静默状态
     *
     * @param entity
     *            实体
     * @param silence
     *            是否静默
     */
    public static void setSilence(final Entity entity, final boolean silence) {
        final byte value = (byte) (silence ? 1 : 0);
        final WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity);
        watcher.setObject(4, value);
    }

    /**
     * 静默传送, 不触发事件
     *
     * @param entity
     * @param to
     */
    public static void teleportQuietly(final Entity entity, final Location to) {
        if (!(entity instanceof Player)) {
            entity.teleport(to);
        } else {
            if (entity.getWorld().equals(to.getWorld())) {
                final Object playerConnection = MinecraftFields.getPlayerConnection((Player) entity);
                if (_mPlayerConnection_Teleport == null) {
                    _mPlayerConnection_Teleport = ReflectUtil.getMethodByNameAndParams(playerConnection.getClass(), "teleport", Location.class);
                }
                try {
                    _mPlayerConnection_Teleport.invoke(playerConnection, to);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                final Object toWorldServer = ReflectUtil.getHandle(to.getWorld());
                final Object server = ReflectUtil.getHandle(Bukkit.getServer());
                if (_fWorldServer_dimension == null) {
                    for (final Field field : ReflectUtil.getFieldByType(toWorldServer.getClass(), Integer.TYPE)) {
                        final int modifier = field.getModifiers();
                        if (Modifier.isFinal(modifier) && Modifier.isPublic(modifier)) {
                            _fWorldServer_dimension = field;
                        }
                    }
                }
                try {
                    _mPlayerList_MoveToWorld.invoke(server, ReflectUtil.getHandle(entity), (int) _fWorldServer_dimension.get(toWorldServer), true, to, true);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 让实体升高/降低
     *
     * @param ent
     * @param speed
     *            速度
     * @param yAdd
     *            Y轴加减
     * @param yMax
     *            Y轴最大值
     * @param groundBoost
     *            当实体在地上的时候，会稍微抬起一些
     */
    public static void velocity(final Entity ent, final double speed, final double yAdd, final double yMax, final boolean groundBoost) {
        velocity(ent, ent.getLocation().getDirection(), speed, false, 0.0D, yAdd, yMax, groundBoost);
    }

    /**
     * 让实体升高/降低
     *
     * @param ent
     * @param vec
     *            坐标
     * @param speed
     *            速度
     * @param ySet
     *            是否设置Y轴初始值
     * @param yBase
     *            Y轴初始值
     * @param yAdd
     *            Y轴加减
     * @param yMax
     *            Y轴最大值
     * @param groundBoost
     *            当实体在地上的时候，会稍微抬起一些
     */
    public static void velocity(final Entity ent, final Vector vec, final double speed, final boolean ySet, final double yBase, final double yAdd, final double yMax, final boolean groundBoost) {
        if ((Double.isNaN(vec.getX())) || (Double.isNaN(vec.getY())) || (Double.isNaN(vec.getZ())) || (vec.length() == 0.0D)) {
            return;
        }

        if (ySet) {
            vec.setY(yBase);
        }

        vec.normalize();
        vec.multiply(speed);

        vec.setY(vec.getY() + yAdd);

        if (vec.getY() > yMax) {
            vec.setY(yMax);
        }
        if ((groundBoost) && (ent.isOnGround())) {
            vec.setY(vec.getY() + 0.2D);
        }

        ent.setFallDistance(0.0F);
        ent.setVelocity(vec);
    }

    public static void walkTo(final Entity entity, final Location location) {
        if (entity == null || location == null) {
            return;
        }
        final Object nmsEntityEntity = ReflectUtil.getHandle(entity);
        if (!_cEntityInsentient.isInstance(nmsEntityEntity)) {
            entity.teleport(location);
            return;
        }
        try {
            final Object oFollowerNavigation = _mGetNavigation.invoke(nmsEntityEntity);

            final Object path = _mA3.invoke(oFollowerNavigation, location.getX(), location.getY(), location.getZ());
            if (path != null) {
                _mA2.invoke(oFollowerNavigation, path, 1D);
                _mA1.invoke(oFollowerNavigation, 2D);
            }
            if (location.distance(entity.getLocation()) > 20) {
                entity.teleport(location);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
