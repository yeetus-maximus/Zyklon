package respectful.rapist.injector;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static net.bytebuddy.agent.ByteBuddyAgent.attach;

public class Controller {
    public void inject() {
        Alert alert;
        try {
            System.setProperty("java.library.path", System.getenv("JAVA_HOME") + "\\jre\\bin");
            Field sysPath = ClassLoader.class.getDeclaredField("sys_paths");
            sysPath.setAccessible(true);
            sysPath.set(null, null);
            for (VirtualMachineDescriptor VM : VirtualMachine.list()) {
                if (VM.displayName().contains("net.minecraft.launchwrapper.Launch")) {
                    writeFile(System.getenv("appdata") + "\\url", (Main.settings.getHost().isEmpty()) ? "http://localhost:" + Main.settings.getPort() : Main.settings.getHost());
                    attach(new File("loader.jar"), VM.id());
                    alert = new Alert(Alert.AlertType.CONFIRMATION, "Success", ButtonType.OK);
                    alert.showAndWait();
                    exit();
                }
            }
            alert = new Alert(Alert.AlertType.WARNING, "Make sure you have Minecraft Forge 1.7.10/1.8.9 running", ButtonType.OK);
            alert.showAndWait();
        } catch (Exception ex) {
            alert = new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void openSettings() {
        Main.settings.show();
    }

    public void applySettings() {
        Alert alert;
        if (Main.settings.getPort() == -1 && Main.settings.getHost().isEmpty()) {
            alert = new Alert(Alert.AlertType.WARNING, "Check your settings and try again", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            String URL;
            if (Main.settings.getHost().isEmpty()) {
                URL = "http://localhost:" + Main.settings.getPort() + "/client.jar";
                writeFile("cloud.bat", "@echo off\ncd cloud\nnode app.js " + Main.settings.getPort());
                Runtime.getRuntime().exec("cmd /c start cloud.bat");
            } else {
                URL = Main.settings.getHost() + "/client.jar";
            }
            Manifest manifest = new JarInputStream(new java.net.URL(URL).openStream()).getManifest();
            ((Label) Main.index.getNode("#info")).setText("Build Date: " + manifest.getMainAttributes().getValue("Build-Date") + "\n" + "Build Version: " + manifest.getMainAttributes().getValue("Build-Version"));
            Main.index.show();
            Main.settings.hide();
        } catch (Exception ex) {
            alert = new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void exit() {
        Platform.exit();
    }

    private void writeFile(String path, String text) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(text);
            fileWriter.flush();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.toString(), ButtonType.OK).showAndWait();
        }
    }
}
