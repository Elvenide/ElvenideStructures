package com.elvenide.structures;

import com.elvenide.core.Core;
import org.bukkit.plugin.java.JavaPlugin;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoreMock {

    public static MockedStatic<Core> mockCore() {
        MockedStatic<Core> mockedCore = Mockito.mockStatic(Core.class);
        
        // Try to initialize ElvenideCore via Core.plugin.set()
        if (Core.plugin != null) {
            JavaPlugin mockPlugin = mock(JavaPlugin.class);
            try {
                File tempDir = Files.createTempDirectory("elvenide-test").toFile();
                when(mockPlugin.getDataFolder()).thenReturn(tempDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Core.plugin.set(mockPlugin);
        }

        return mockedCore;
    }
}
