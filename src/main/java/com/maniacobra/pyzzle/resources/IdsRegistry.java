package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.properties.FilePaths;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IdsRegistry {

    private static IdsRegistry instance = new IdsRegistry();

    public static IdsRegistry getInstance() {
        return instance;
    }

    // CLASS

    private HashMap<String, Integer> registeredIds;

    public IdsRegistry() {
        registeredIds = new HashMap<>();
    }

    public void save() {

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> id : registeredIds.entrySet()) {
            builder.append(id.getKey()).append(":").append(id.getValue()).append("\n");
        }
        try {
            PyzzFileManager.getInstance().encode(FilePaths.getInstance().getIdsFile(), builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            registeredIds.clear();
            File f = FilePaths.getInstance().getIdsFile();
            if (f.exists()) {
                String data = PyzzFileManager.getInstance().decode(f);
                String[] lines = data.split("\n");
                for (String line : lines) {
                    String[] splitted = line.split(":");
                    if (splitted.length > 1) {
                        String key = splitted[0];
                        Integer val = Integer.parseInt(splitted[1]);
                        registeredIds.put(key, val);
                    }
                }
            }
            save();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void incrementId(String id) {
        if (!registeredIds.containsKey(id))
            registeredIds.put(id, 1);
        else
            registeredIds.put(id, registeredIds.get(id) + 1);
        save();
    }

    public int getCount(String id) {
        if (registeredIds.containsKey(id))
            return registeredIds.get(id);
        return 0;
    }
}
