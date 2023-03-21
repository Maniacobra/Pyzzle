package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.properties.AppProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PyzzFileManager {

    private static final PyzzFileManager instance = new PyzzFileManager();

    public static PyzzFileManager getInstance() {
        return instance;
    }

    // CLASS

    private PyzzFileManager() {
    }

    public String readNormal(File file) throws IOException {

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        String[] path = file.getAbsolutePath().split("\\.");
        if (path.length > 1 && path[path.length - 1].equals("json")) {
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < path.length; i++) {
                if (i == path.length - 1) {
                    strBuilder.append(".");
                    strBuilder.append(AppProperties.extension);
                }
                else
                    strBuilder.append(path[i]);
            }
            File newFile = new File(strBuilder.toString());
            encode(newFile, content);
        }
        return content;
    }

    public void encode(File file, String content) {
        try (FileOutputStream output = new FileOutputStream(file); Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String decode(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            InputStreamReader reader = new InputStreamReader(new GZIPInputStream(input), StandardCharsets.UTF_8);
            StringBuilder builder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                builder.append((char) c);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // OBSOLETE (encryption)

    /*

    private static final int[] key = {
            96, -27, 95, -3, -1, 30, -6, -28,
            1, -25, 67, 47, 25, 15, 88, 90
    };

    public void encrypt(File file, String content) {

        try (FileWriter writer = new FileWriter(file)) {
            int i = 0;
            for (byte val : content.getBytes(StandardCharsets.UTF_8)) {
                val += key[i];
                writer.write(val);
                i++;
                i %= key.length;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(File file) throws IOException {

        try (FileReader reader = new FileReader(file)) {
            ArrayList<Byte> bytes = new ArrayList<>();
            byte val;
            int i = 0;
            while ((val = (byte) reader.read()) != -1) {
                System.out.print(val);
                System.out.print(" ");
                val -= key[i];
                bytes.add(val);
                i++;
                i %= key.length;
            }
            byte[] arr = new byte[bytes.size()];
            i = 0;
            System.out.println("\n=============================\n");
            for (Byte b : bytes) {
                System.out.print(b);
                System.out.print(" ");
                arr[i] = b;
                i++;
            }
            System.out.println();
            return new String(arr, StandardCharsets.UTF_8);
        }
    }
    */
}
