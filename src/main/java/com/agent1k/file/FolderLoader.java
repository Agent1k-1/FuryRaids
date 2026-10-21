package com.agent1k.file;

import com.agent1k.FuryRaids;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FolderLoader {
    private final Map<String, FileLoader> files = new LinkedHashMap<>();

    public FolderLoader(FuryRaids plugin) {
        for (String path : List.of("region/config.yml", "region/block.yml", "region/messages.yml",
                "region/menu/menu.yml", "tnt/messages.yml", "tnt/tnt.yml")) {
            files.put(path, new FileLoader(plugin, path));
        }
    }

    public void loadAll() {
        for (FileLoader file : files.values()) file.load();
    }

    public FileLoader get(String path) {
        FileLoader loader = files.get(path);
        if (loader == null) throw new IllegalArgumentException("Неизвестный файл: " + path);
        return loader;
    }
}
