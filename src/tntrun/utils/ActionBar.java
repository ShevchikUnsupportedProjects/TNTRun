package tntrun.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tntrun.TNTRun;

public class ActionBar {
    private Class<?> nmsChatSerializer;
    private Class<?> nmsTitleAction;
    private Class<?> nmsPacketTitle;
    private Class<?> nmsPacketChat;
    private Class<?> nmsChatBaseComponent;
    private Object nmsIChatBaseComponent;
    private final String networkManager = "networkManager";
    private final String handle = "getHandle";
    private final String playerConnection = "playerConnection";
    private final String sendPacket = "sendPacket";
    
    private void loadClasses(){
        nmsIChatBaseComponent = getNMSClass("IChatBaseComponent");
        nmsPacketChat = getNMSClass("PacketPlayOutChat");

        nmsChatBaseComponent = getNMSClass("IChatBaseComponent");

        if (getVersion().contains("1_7")) {
            nmsChatSerializer = getNMSClass("ChatSerializer");
            return;
        }
        if (getVersion().contains("1_8") && getVersion().contains("R1")) {
            nmsChatSerializer = getNMSClass("ChatSerializer");
            nmsPacketTitle = getNMSClass("PacketPlayOutTitle");
            nmsTitleAction = getNMSClass("EnumTitleAction");
            return;
        }
        nmsChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        nmsPacketTitle = getNMSClass("PacketPlayOutTitle");
        nmsTitleAction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");

    }

    public Class<?> getNMSChatSerializer() {
        return nmsChatSerializer;
    }

    public Class<?> getNMSIChatBaseComponent() {
        return nmsIChatBaseComponent.getClass();
    }

    public Class<?> getNMSPacketTitle() {
        return nmsPacketTitle;
    }

    public void sendActionBar(Player player, String message) {
        loadClasses();
        try {
            Object handle = getMethod(player.getClass(), this.handle, new Class[0]).invoke(player, new Object[0]);
            Object playerConnection = getField(handle.getClass(), this.playerConnection).get(handle);
            Object serializedMessage = getMethod(this.nmsChatSerializer, "a", String.class).invoke(this.nmsChatSerializer, "{\"text\":\"" + message + "\"}");
            Object packet = null;
            if (getVersion().contains("1_7")) {
                packet = this.nmsPacketChat.getConstructor(this.nmsChatBaseComponent, Integer.class).newInstance(serializedMessage, (int) 2);
            } else if (getVersion().contains("1_8") || getVersion().contains("1_9")) {
                packet = this.nmsPacketChat.getConstructor(this.nmsChatBaseComponent, byte.class).newInstance(serializedMessage, (byte) 2);
            }

            getMethod(playerConnection.getClass(), this.sendPacket).invoke(playerConnection, packet);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        
    }
    
    private final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<Class<?>, Class<?>>();
    
    private Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES
                .get(clazz) : clazz;
    }

    private Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++)
            types[i] = getPrimitiveType(classes[i]);
        return types;
    }

    private boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
                return false;
        return true;
    }

    private Object getHandle(Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(String name, Class<?> clazz,
            Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t))
                return m;
        }
        return null;
    }

    public static String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        return version;
    }

    private Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getMethods())
            if (m.getName().equals(name)
                    && (args.length == 0 || ClassListEqual(args,
                            m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        return null;
    }

    private boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length)
            return false;
        for (int i = 0; i < l1.length; i++)
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        return equal;
    }
}
